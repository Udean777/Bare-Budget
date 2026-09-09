package com.ssajudn.bareuang.ui.ocr

import androidx.compose.ui.graphics.Path

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.common.labelRes
import com.ssajudn.bareuang.ui.components.AmountTextField
import com.ssajudn.bareuang.ui.components.AppDatePickerDialog
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.ui.common.DateFormatter
import java.io.File

@Composable
fun ReceiptPaperCard(rawText: String, merchant: String, total: Long) {
    val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
    // Heuristic: header = first 3 lines, footer = last 3 lines, body = middle
    val headerLines = lines.take(4)
    val footerLines = if (lines.size > 7) lines.takeLast(3) else emptyList()
    val bodyLines = when {
        lines.size > 7 -> lines.drop(4).dropLast(3)
        lines.size > 4 -> lines.drop(4)
        else -> emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFFEF8))
            .padding(0.dp)
    ) {
        // Top perforation
        Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
            val r = 6.dp.toPx()
            var x = r
            while (x < size.width) {
                drawCircle(Color(0xFFE0E0E0), radius = r, center = Offset(x, r / 2))
                x += r * 2
            }
        }
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // Header centered bold
            headerLines.forEachIndexed { idx, line ->
                Text(
                    text = line,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    fontSize = if (idx == 0) 16.sp else 11.sp,
                    fontWeight = if (idx == 0) FontWeight.ExtraBold else FontWeight.Normal,
                    color = Color(0xFF1A1A1A),
                    lineHeight = 14.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            DashedDivider()
            // Body mono
            bodyLines.forEach { line ->
                val isTotal = line.uppercase().contains("TOTAL") || line.uppercase().contains("JUMLAH") || line.uppercase().contains("TAGIHAN")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Split price to right if contains numbers
                    val hasPrice = Regex("""\d[\d.,]*""").containsMatchIn(line)
                    if (hasPrice && line.length > 24) {
                        // try to align price right: split at last number
                        val m = Regex("""(.+?)(\s+Rp?\.?\s*\d[\d.,]*)${'$'}""").find(line)
                        if (m != null) {
                            Text(m.groupValues[1].trim(), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = if (isTotal) Color(0xFF111111) else Color(0xFF2B2B2B), fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                            Text(m.groupValues[2].trim(), fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal, color = if (isTotal) Color(0xFF111111) else Color(0xFF2B2B2B))
                        } else {
                            Text(line, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF2B2B2B))
                        }
                    } else {
                        Text(line, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = if (isTotal) Color(0xFF111111) else Color(0xFF2B2B2B), fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            if (bodyLines.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                DashedDivider()
            }
            // Total highlight
            if (total > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFF3CD))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.ocr_total), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5D4037))
                    Text(CurrencyFormatter.formatRupiah(total), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5D4037))
                }
                Spacer(Modifier.height(6.dp))
                DashedDivider()
            }
            // Footer centered
            footerLines.forEach { line ->
                Text(
                    text = line,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF6D6D6D)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.ocr_receipt_original, merchant),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = Color(0xFF9E9E9E)
            )
        }
        // Bottom zigzag
        Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
            val zig = 10.dp.toPx()
            val path = Path().apply {
                moveTo(0f, 0f)
                var x = 0f
                var up = true
                while (x < size.width) {
                    lineTo(x + zig / 2, if (up) zig else 0f)
                    x += zig / 2
                    up = !up
                }
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path, Color(0xFFFFFEF8))
        }
        // Caption outside paper
        Text(
            text = stringResource(R.string.ocr_preview_caption),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun DashedDivider() {
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp).padding(vertical = 4.dp)) {
        val dash = 6.dp.toPx()
        val gap = 4.dp.toPx()
        drawLine(
            color = Color(0xFFBDBDBD),
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, gap), 0f)
        )
    }
}
