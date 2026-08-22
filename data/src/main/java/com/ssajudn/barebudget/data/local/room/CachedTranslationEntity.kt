package com.ssajudn.barebudget.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_translations")
data class CachedTranslationEntity(
    @PrimaryKey val cacheKey: String,
    val sourceLang: String,
    val targetLang: String,
    val originalText: String,
    val translatedText: String,
    val createdAt: Long = System.currentTimeMillis()
)
