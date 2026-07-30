package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voiceovers")
data class VoiceoverEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val hindiText: String,
    val filePath: String,
    val fileName: String,
    val fileSizeFormatted: String,
    val durationMs: Long,
    val speechRate: Float,
    val speechPitch: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
