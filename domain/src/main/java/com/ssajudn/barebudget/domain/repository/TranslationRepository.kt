package com.ssajudn.barebudget.domain.repository

interface TranslationRepository {
    suspend fun translate(text: String, sourceLang: String, targetLang: String): Result<String>
    suspend fun clearCache()
}
