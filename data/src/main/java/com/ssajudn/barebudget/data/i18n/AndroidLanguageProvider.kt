package com.ssajudn.barebudget.data.i18n

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.ssajudn.barebudget.domain.i18n.LanguageNormalizer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidLanguageProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getLanguage(): String = getLanguage(context)

    fun getLanguage(ctx: Context): String {
        return try {
            val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val lm = ctx.getSystemService(LocaleManager::class.java)
                val locales = lm?.applicationLocales
                if (locales != null && !locales.isEmpty) locales[0]?.language ?: Locale.getDefault().language
                else Locale.getDefault().language
            } else {
                val locales = AppCompatDelegate.getApplicationLocales()
                if (!locales.isEmpty) locales[0]?.language ?: Locale.getDefault().language
                else Locale.getDefault().language
            }
            LanguageNormalizer.normalize(raw)
        } catch (_: Exception) {
            LanguageNormalizer.normalize(Locale.getDefault().language)
        }
    }
}
