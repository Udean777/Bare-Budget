package com.ssajudn.barebudget.domain.i18n

object LanguageNormalizer {
    fun normalize(code: String): String = if (code == "in") "id" else code
}
