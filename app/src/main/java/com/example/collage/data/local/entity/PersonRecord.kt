package com.example.collage.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "person_records",
    foreignKeys = [
        ForeignKey(
            entity = VideoRecord::class,
            parentColumns = ["id"],
            childColumns = ["videoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["videoId"])]
)
data class PersonRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: Long,
    val personId: Int, // The ID from clustering
    val appearanceCount: Int,
    val thumbnailPath: String
)
