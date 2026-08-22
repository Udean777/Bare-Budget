package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.data.local.room.CachedTranslationDao
import com.ssajudn.barebudget.data.local.room.CachedTranslationEntity
import com.ssajudn.barebudget.data.translation.MlKitTranslatorManager
import com.ssajudn.barebudget.domain.repository.TranslationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class TranslationRepositoryImpl @Inject constructor(
    private val mlKit: MlKitTranslatorManager,
    private val dao: CachedTranslationDao
) : TranslationRepository {
    override suspend fun translate(text: String, sourceLang: String, targetLang: String): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank() || sourceLang == targetLang) return@withContext Result.success(text)
        val key = "${sourceLang}_${targetLang}_${text}"
        dao.getByKey(key)?.let { return@withContext Result.success(it.translatedText) }
        val result = mlKit.translate(text, sourceLang, targetLang)
        result.onSuccess { translated ->
            runCatching {
                dao.insert(CachedTranslationEntity(key, sourceLang, targetLang, text, translated))
            }
        }
        result
    }

    override suspend fun clearCache() = withContext(Dispatchers.IO) { dao.clearAll() }
}
