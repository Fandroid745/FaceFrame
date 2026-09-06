package com.example.collage.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_records")
data class VideoRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoUri: String,
    val durationMs: Long,
    val personCount: Int,
    val totalAppearances: Int,
    val processedTimestamp: Long,
    val collagePath: String
)
