package com.ssajudn.barebudget.utils

import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.ssajudn.barebudget.domain.i18n.LanguageNormalizer

object LanguageManager {

    val SUPPORTED_LANGUAGES = listOf(
        "en" to "English",
        "id" to "Bahasa Indonesia"
    )

    /** Normalized language tag for Accept-Language (e.g. "en" or "id"), not "en-US". */
    fun getActiveLanguageTag(context: Context): String = getCurrentLanguageCode(context)

    fun getCurrentLanguageCode(context: Context): String {
        // Reads active application locale across API levels (Tiramisu per-app language or AppCompat fallback)
        val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
            val locales = localeManager?.applicationLocales
            if (locales != null && !locales.isEmpty) {
                locales[0]?.language ?: "en"
            } else {
                java.util.Locale.getDefault().language
            }
        } else {
            val locales = AppCompatDelegate.getApplicationLocales()
            if (!locales.isEmpty) {
                locales[0]?.language ?: "en"
            } else {
                java.util.Locale.getDefault().language
            }
        }
        return LanguageNormalizer.normalize(raw)
    }

    fun setLanguage(context: Context, languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
            localeManager?.applicationLocales = LocaleList.forLanguageTags(languageCode)
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(languageCode)
            )
        }
    }
}
