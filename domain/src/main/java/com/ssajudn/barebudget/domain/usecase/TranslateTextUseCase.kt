package com.ssajudn.barebudget.domain.usecase

import com.ssajudn.barebudget.domain.repository.TranslationRepository
import javax.inject.Inject

class TranslateTextUseCase @Inject constructor(
    private val repository: TranslationRepository
) {
    suspend operator fun invoke(text: String, sourceLang: String, targetLang: String): Result<String> =
        repository.translate(text, sourceLang, targetLang)
}
