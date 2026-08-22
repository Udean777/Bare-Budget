package com.ssajudn.barebudget.data.translation

import android.util.LruCache
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitTranslatorManager @Inject constructor() {
    private val translationCache = LruCache<String, String>(200)
    private val translatorClients = mutableMapOf<String, Translator>()

    suspend fun translate(
        text: String,
        sourceLanguage: String = TranslateLanguage.ENGLISH,
        targetLanguage: String = TranslateLanguage.INDONESIAN
    ): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank() || sourceLanguage == targetLanguage) return@withContext Result.success(text)
        val cacheKey = "${sourceLanguage}_${targetLanguage}_${text}"
        translationCache.get(cacheKey)?.let { return@withContext Result.success(it) }
        try {
            val clientKey = "${sourceLanguage}_$targetLanguage"
            val translator = synchronized(translatorClients) {
                translatorClients.getOrPut(clientKey) {
                    Translation.getClient(
                        TranslatorOptions.Builder().setSourceLanguage(sourceLanguage).setTargetLanguage(targetLanguage).build()
                    )
                }
            }
            val conditions = DownloadConditions.Builder().requireWifi().build()
            translator.downloadModelIfNeeded(conditions).await()
            val translatedText = translator.translate(text).await()
            translationCache.put(cacheKey, translatedText)
            Result.success(translatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        synchronized(translatorClients) { translatorClients.values.forEach { it.close() }; translatorClients.clear() }
        translationCache.evictAll()
    }
}
