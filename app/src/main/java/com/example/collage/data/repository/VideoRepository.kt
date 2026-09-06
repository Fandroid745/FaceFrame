package com.example.collage.data.repository

import android.content.Context
import com.example.collage.data.local.daos.VideoDao
import com.example.collage.data.local.entity.PersonRecord
import com.example.collage.data.local.entity.VideoRecord
import com.example.collage.domain.model.VideoAnalysisResult
import com.example.collage.util.FileStorageHelper
import kotlinx.coroutines.flow.Flow

class VideoRepository(
    private val context: Context,
    private val videoDao: VideoDao
) {

    fun getAllHistory(): Flow<List<VideoRecord>> = videoDao.getAllVideos()

    suspend fun getPersonsForVideo(videoId: Long): List<PersonRecord> = videoDao.getPersonsForVideo(videoId)

    suspend fun saveResult(result: VideoAnalysisResult) {
        val collagePath = FileStorageHelper.saveBitmap(
            context,
            result.collageBitmap,
            "collages",
            "collage"
        ) ?: ""

        val videoRecord = VideoRecord(
            videoUri = result.videoUri,
            durationMs = result.durationMs,
            personCount = result.personCount,
            totalAppearances = result.totalAppearances,
            processedTimestamp = System.currentTimeMillis(),
            collagePath = collagePath
        )

        val videoId = videoDao.insertVideo(videoRecord)

        val personRecords = result.persons.map { person ->
            val thumbnailPath = FileStorageHelper.saveBitmap(
                context,
                person.representativeShot,
                "thumbnails",
                "person_${person.personId}"
            ) ?: ""

            PersonRecord(
                videoId = videoId,
                personId = person.personId,
                appearanceCount = person.appearanceCount,
                thumbnailPath = thumbnailPath
            )
        }

        videoDao.insertPersons(personRecords)
    }

    suspend fun deleteResult(video: VideoRecord) {
        // 1. Delete image files
        FileStorageHelper.deleteFile(video.collagePath)
        val persons = videoDao.getPersonsForVideo(video.id)
        persons.forEach { FileStorageHelper.deleteFile(it.thumbnailPath) }

        // 2. Delete from DB
        videoDao.deleteVideo(video.id)
    }
}
