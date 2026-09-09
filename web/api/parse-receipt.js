/** Vercel proxy for optional receipt OCR. Fails closed without persistent quota storage. */
import crypto from "node:crypto";

const GEMINI_MODEL = process.env.GEMINI_MODEL?.trim();
const GEMINI_API_URL = process.env.GEMINI_API_URL?.trim().replace(/\/+$/, "");
const MAX_BODY_BYTES = 3 * 1024 * 1024;
const MAX_IMAGE_BYTES = 2 * 1024 * 1024;
const MAX_TOTAL = 1_000_000_000_000;
const WINDOW_SECONDS = 24 * 60 * 60;
const kvRestUrl = process.env.bareuang_KV_REST_API_URL;
const kvRestToken = process.env.bareuang_KV_REST_API_TOKEN;
const INSTALLATION_LIMIT = Number(process.env.RATE_LIMIT_RPD || 20);
const IP_LIMIT = Number(process.env.RATE_LIMIT_IP_RPD || 60);
const GLOBAL_LIMIT = Number(process.env.GLOBAL_LIMIT_RPD || 5000);
const ALLOWED_MIMES = new Set(["image/jpeg", "image/png", "image/webp", "image/gif"]);
const CATEGORIES = new Set(["FOOD", "SHOPPING", "TRANSPORT", "BILLS", "ENTERTAINMENT", "HEALTH", "EDUCATION", "SOCIAL", "OTHER"]);
const ALLOWED_ORIGINS = new Set((process.env.ALLOWED_ORIGINS || "https://bareuang.vercel.app").split(",").map((x) => x.trim()).filter(Boolean));

function id() { return crypto.randomUUID(); }
function errorResponse(res, status, message, requestId) {
  res.setHeader("X-Request-Id", requestId);
  return res.status(status).json({ error: message, request_id: requestId });
}
function clientIp(req) {
  const forwarded = req.headers["x-forwarded-for"];
  const value = typeof forwarded === "string" && forwarded ? forwarded.split(",")[0].trim() : (req.headers["x-real-ip"] || req.socket?.remoteAddress || "unknown");
  return String(value).replace(/^::ffff:/i, "").slice(0, 64) || "unknown";
}
function validInstallation(value) { return typeof value === "string" && /^[a-f0-9-]{16,80}$/i.test(value); }
function quotaConfigured() { return Boolean(kvRestUrl && kvRestToken); }

async function pipeline(commands) {
  const response = await fetch(`${kvRestUrl}/pipeline`, {
    method: "POST",
    headers: { Authorization: `Bearer ${kvRestToken}`, "Content-Type": "application/json" },
    body: JSON.stringify(commands),
  });
  if (!response.ok) throw new Error("quota store unavailable");
  const result = await response.json();
  if (!Array.isArray(result)) throw new Error("invalid quota response");
  return result.map((entry) => entry?.result);
}
async function increment(key, limit) {
  const [value] = await pipeline([["INCR", key], ["EXPIRE", key, WINDOW_SECONDS]]);
  const count = Number(value);
  if (!Number.isSafeInteger(count)) throw new Error("invalid quota count");
  return count <= limit;
}
async function checkQuota(installation, ip) {
  if (!quotaConfigured()) return { configured: false };
  const day = new Date().toISOString().slice(0, 10);
  const [installOk, ipOk, globalOk] = await Promise.all([
    increment(`bareuang:ocr:installation:${day}:${installation}`, INSTALLATION_LIMIT),
    increment(`bareuang:ocr:ip:${day}:${ip}`, IP_LIMIT),
    increment(`bareuang:ocr:global:${day}`, GLOBAL_LIMIT),
  ]);
  return { configured: true, allowed: installOk && ipOk && globalOk };
}

function decode(value) {
  if (typeof value !== "string" || !value || value.length > MAX_BODY_BYTES || !/^[A-Za-z0-9+/]*={0,2}$/.test(value) || value.length % 4 === 1) return null;
  const buffer = Buffer.from(value, "base64");
  return buffer.length > 0 && buffer.length <= MAX_IMAGE_BYTES ? buffer : null;
}
function detectedMime(buffer) {
  if (buffer.length >= 3 && buffer[0] === 0xff && buffer[1] === 0xd8 && buffer[2] === 0xff) return "image/jpeg";
  if (buffer.length >= 8 && buffer.subarray(0, 8).equals(Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]))) return "image/png";
  if (buffer.length >= 6 && ["GIF87a", "GIF89a"].includes(buffer.subarray(0, 6).toString())) return "image/gif";
  if (buffer.length >= 12 && buffer.subarray(0, 4).toString() === "RIFF" && buffer.subarray(8, 12).toString() === "WEBP") return "image/webp";
  return null;
}
function parseImage(raw) {
  let mime = "image/jpeg"; let encoded = raw;
  if (typeof raw !== "string") return null;
  if (raw.startsWith("data:")) {
    const match = raw.match(/^data:([^;,]+);base64,(.*)$/s);
    if (!match) return null;
    mime = match[1].toLowerCase(); encoded = match[2];
  }
  if (!ALLOWED_MIMES.has(mime)) return null;
  const bytes = decode(encoded);
  return bytes && detectedMime(bytes) === mime ? { mime, encoded } : null;
}
function validDate(value) {
  if (value === "") return true;
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
  const date = new Date(`${value}T00:00:00Z`);
  return !Number.isNaN(date.valueOf()) && date.toISOString().slice(0, 10) === value;
}
function sanitize(parsed) {
  if (!parsed || typeof parsed !== "object" || Array.isArray(parsed)) return null;
  const merchant = typeof parsed.merchant === "string" ? parsed.merchant.trim().slice(0, 100) : "";
  const date = typeof parsed.date === "string" ? parsed.date.trim() : "";
  const total = parsed.total;
  if (!Number.isSafeInteger(total) || total < 0 || total > MAX_TOTAL || !validDate(date)) return null;
  const category = typeof parsed.category === "string" ? parsed.category.toUpperCase() : "OTHER";
  if (parsed.items != null && (!Array.isArray(parsed.items) || parsed.items.length > 30 || !parsed.items.every((x) => typeof x === "string" && x.length <= 100))) return null;
  const items = Array.isArray(parsed.items) ? parsed.items.map((x) => x.trim()) : [];
  const rawText = typeof parsed.raw_text === "string" ? parsed.raw_text.trim().slice(0, 4000) : "";
  return { merchant, date, total, category: CATEGORIES.has(category) ? category : "OTHER", items, raw_text: rawText };
}

export default async function handler(req, res) {
  const requestId = id();
  const origin = req.headers.origin;
  if (origin && !ALLOWED_ORIGINS.has(origin)) return errorResponse(res, 403, "Origin not allowed", requestId);
  if (origin) res.setHeader("Access-Control-Allow-Origin", origin);
  res.setHeader("Vary", "Origin");
  res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Bareuang-Installation-Id");
  if (req.method === "OPTIONS") return res.status(204).end();
  if (req.method !== "POST") { res.setHeader("Allow", "POST"); return errorResponse(res, 405, "Method not allowed", requestId); }
  const installation = req.headers["x-bareuang-installation-id"];
  if (!validInstallation(installation)) return errorResponse(res, 401, "Client identity required", requestId);
  const length = Number(req.headers["content-length"]);
  if (!Number.isSafeInteger(length) || length <= 0 || length > MAX_BODY_BYTES) return errorResponse(res, 413, "Request body too large", requestId);
  if (!process.env.GEMINI_API_KEY || !GEMINI_MODEL || !GEMINI_API_URL) return errorResponse(res, 503, "OCR service unavailable", requestId);
  let body = req.body;
  if (typeof body === "string") { try { body = JSON.parse(body); } catch { return errorResponse(res, 400, "Invalid JSON", requestId); } }
  const image = parseImage(body?.image_base64);
  if (!image) return errorResponse(res, 400, "Invalid image data", requestId);
  let quota;
  try { quota = await checkQuota(installation, clientIp(req)); } catch { return errorResponse(res, 503, "OCR service temporarily unavailable", requestId); }
  if (!quota.configured) return errorResponse(res, 503, "OCR service temporarily unavailable", requestId);
  if (!quota.allowed) return errorResponse(res, 429, "Daily OCR limit reached", requestId);
  const prompt = "Kamu adalah parser struk Indonesia. Ekstrak JSON {merchant:string,date:YYYY-MM-DD,total:number,category:string,items:string[],raw_text:string}. Kembalikan HANYA JSON valid tanpa markdown. Category harus FOOD, SHOPPING, TRANSPORT, BILLS, ENTERTAINMENT, HEALTH, EDUCATION, SOCIAL, atau OTHER.";
  const started = Date.now();
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 25_000);
  try {
    const response = await fetch(`${GEMINI_API_URL}/models/${encodeURIComponent(GEMINI_MODEL)}:generateContent?key=${encodeURIComponent(process.env.GEMINI_API_KEY)}`, {
      method: "POST", headers: { "Content-Type": "application/json" }, signal: controller.signal,
      body: JSON.stringify({
        systemInstruction: { parts: [{ text: prompt }] },
        contents: [{ role: "user", parts: [{ text: "Parse struk ini ke JSON." }, { inlineData: { mimeType: image.mime, data: image.encoded } }] }],
        generationConfig: {
          responseMimeType: "application/json",
          responseSchema: {
            type: "OBJECT",
            properties: {
              merchant: { type: "STRING" }, date: { type: "STRING" }, total: { type: "INTEGER" },
              category: { type: "STRING" }, items: { type: "ARRAY", items: { type: "STRING" } }, raw_text: { type: "STRING" },
            },
            required: ["merchant", "date", "total", "category", "items", "raw_text"],
          },
          maxOutputTokens: 4096,
        },
      }),
    });
    if (!response.ok) return errorResponse(res, 502, "OCR provider unavailable", requestId);
    let parsed;
    try {
      const text = (await response.json())?.candidates?.[0]?.content?.parts?.[0]?.text?.trim() || "";
      parsed = JSON.parse(text.replace(/^```(?:json)?\s*/i, "").replace(/\s*```$/, ""));
    } catch { return errorResponse(res, 502, "OCR returned invalid data", requestId); }
    const result = sanitize(parsed);
    if (!result) return errorResponse(res, 502, "OCR returned invalid data", requestId);
    console.info(JSON.stringify({ event: "ocr_success", request_id: requestId, latency_ms: Date.now() - started }));
    return res.status(200).json({ ...result, request_id: requestId });
  } catch (error) {
    console.error(JSON.stringify({ event: "ocr_provider_failure", request_id: requestId, latency_ms: Date.now() - started, timeout: error?.name === "AbortError" }));
    return errorResponse(res, 502, "OCR provider unavailable", requestId);
  } finally { clearTimeout(timeout); }
}
