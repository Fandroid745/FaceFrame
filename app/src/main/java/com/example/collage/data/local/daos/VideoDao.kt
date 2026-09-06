package com.example.collage.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.collage.data.local.entity.PersonRecord
import com.example.collage.data.local.entity.VideoRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersons(persons: List<PersonRecord>)

    @Query("SELECT * FROM video_records ORDER BY processedTimestamp DESC")
    fun getAllVideos(): Flow<List<VideoRecord>>

    @Query("SELECT * FROM video_records WHERE id = :videoId")
    suspend fun getVideoById(videoId: Long): VideoRecord?

    @Query("SELECT * FROM person_records WHERE videoId = :videoId ORDER BY personId ASC")
    suspend fun getPersonsForVideo(videoId: Long): List<PersonRecord>

    @Query("DELETE FROM video_records WHERE id = :videoId")
    suspend fun deleteVideo(videoId: Long)
}
