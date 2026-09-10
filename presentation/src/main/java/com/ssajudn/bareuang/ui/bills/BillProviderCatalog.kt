package com.ssajudn.bareuang.ui.bills

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.ssajudn.bareuang.presentation.R

/**
 * Single source of truth for bill provider icons: the built-in provider list,
 * name→drawable matching, `res://` URL encoding/decoding, and local-file icon
 * rendering. Previously duplicated three times inside DueBillsScreen.kt.
 */
object BillProviderCatalog {

    data class Provider(val name: String, val iconRes: Int? = null, val isCustom: Boolean = false)

    val builtin: List<Provider> = listOf(
        Provider("Shopee PayLater", R.drawable.logo_shopee),
        Provider("Kredivo", R.drawable.logo_kredivo),
        Provider("GoPay Later", R.drawable.logo_gopay),
        Provider("Lainnya (Custom)", null, isCustom = true)
    )

    fun customLabel(context: Context): String = context.getString(R.string.bills_provider_custom)

    /** Encodes a drawable resource as a stable `res://entryName` URL. */
    fun toResUrl(context: Context, iconRes: Int): String {
        val entryName = runCatching { context.resources.getResourceEntryName(iconRes) }.getOrNull()
            ?: "logo_shopee"
        return "res://$entryName"
    }

    /**
     * Resolves a bill's icon to something renderable by [LocalProviderIcon]:
     * a drawable resource ID, a local image File, or null.
     */
    fun resolve(context: Context, providerIconUrl: String?, providerName: String): Any? {
        if (providerIconUrl != null && providerIconUrl.startsWith("res://")) {
            val payload = providerIconUrl.removePrefix("res://")
            val byName = when (payload) {
                "logo_shopee", "ic_provider_shopee" -> R.drawable.logo_shopee
                "logo_kredivo", "ic_provider_kredivo" -> R.drawable.logo_kredivo
                "logo_gopay", "ic_provider_gopay" -> R.drawable.logo_gopay
                else -> null
            }
            if (byName != null) return byName
            val legacyInt = payload.toIntOrNull()
            if (legacyInt != null) {
                return runCatching {
                    if (context.resources.getResourceTypeName(legacyInt) == "drawable") legacyInt else null
                }.getOrNull()
            }
            return null
        }
        if (providerIconUrl != null && providerIconUrl.startsWith("/")) {
            // Path file lokal yang didukung; OCR online adalah fitur terpisah.
            return java.io.File(providerIconUrl)
        }
        return when {
            providerName.contains("Shopee", ignoreCase = true) -> R.drawable.logo_shopee
            providerName.contains("Kredivo", ignoreCase = true) -> R.drawable.logo_kredivo
            providerName.contains("GoPay", ignoreCase = true) -> R.drawable.logo_gopay
            else -> null
        }
    }
    /**
     * Copies a selected `content://` URI to app-private internal storage (`filesDir`).
     *
     * Transient content permissions granted by the system file picker do not persist
     * across app process restarts, so copying the file ensures persistent access.
     */
    fun persistPickedImage(context: Context, uri: android.net.Uri): String? {
        return try {
            val mime = context.contentResolver.getType(uri) ?: ""
            if (mime.isNotBlank() && !mime.startsWith("image/")) return null
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                if (pfd.statSize > 5 * 1024 * 1024) return null
            }
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val dir = java.io.File(context.filesDir, "duebill_icons").apply { mkdirs() }
            val ext = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)?.takeIf { it.isNotBlank() } ?: "jpg"
            if (ext !in setOf("jpg","jpeg","png","webp","gif")) return null
            val file = java.io.File(dir, "duebill_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.$ext")
            file.outputStream().use { input.copyTo(it) }
            input.close()
            file.absolutePath
        } catch (_: Exception) { null }
    }
}

/**
 * Renders a local-only provider icon: drawable resource id or a local image
 * file (from the photo picker). Core bill data stays local; optional OCR uses
 * the separately disclosed online endpoint.
 */
@Composable
internal fun LocalProviderIcon(model: Any, size: androidx.compose.ui.unit.Dp) {
    when (model) {
        is Int -> Image(
            painter = painterResource(id = model),
            contentDescription = null,
            modifier = Modifier.size(size).clip(MaterialTheme.shapes.small)
        )
        is java.io.File -> {
            val bitmap = remember(model.absolutePath) {
                runCatching {
                    android.graphics.BitmapFactory.decodeFile(model.absolutePath)?.asImageBitmap()
                }.getOrNull()
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.size(size).clip(MaterialTheme.shapes.small)
                )
            }
        }
    }
}
