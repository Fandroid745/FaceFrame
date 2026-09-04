package com.example.collage.domain

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private const val FRAME_INTERVAL_MS = 250L

class VideoProcessor(
    private val context: Context,
    private val faceAnalyzer: FaceAnalyzer,
    private val faceClusteringService: FaceClusteringService
) {

    fun processVideo(videoUri: Uri): Flow<ProcessingState> = flow {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, videoUri)

            val durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?: error("Could not read video duration")

            val allDetectedFaces = mutableListOf<com.example.collage.domain.model.DetectedFace>()
            var currentTimeMs = 0L

            while (currentTimeMs < durationMs) {
                val bitmap = retriever.getFrameAtTime(
                    currentTimeMs * 1000L,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                )

                if (bitmap != null) {
                    val analysisResults = faceAnalyzer.analyze(bitmap)
                    analysisResults.forEach { result ->
                        allDetectedFaces.add(
                            com.example.collage.domain.model.DetectedFace(
                                frameTimeMs = currentTimeMs,
                                boundingBox = android.graphics.RectF(result.boundingBox),
                                embedding = result.embedding,
                                frameBitmap = result.frameBitmap,
                                smilingProbability = result.smilingProbability,
                                leftEyeOpenProbability = result.leftEyeOpenProbability,
                                rightEyeOpenProbability = result.rightEyeOpenProbability,
                                headEulerAngleX = result.headEulerAngleX,
                                headEulerAngleY = result.headEulerAngleY,
                                headEulerAngleZ = result.headEulerAngleZ,
                                sharpnessScore = result.sharpnessScore
                            )
                        )
                    }
                }

                currentTimeMs += FRAME_INTERVAL_MS

                val progress = (currentTimeMs.toFloat() / durationMs).coerceIn(0f, 1f)
                emit(ProcessingState.Processing(progress, "Analyzing frames..."))
            }

            emit(ProcessingState.Processing(1f, "Clustering appearances..."))
            val result = faceClusteringService.clusterFaces(allDetectedFaces)
            emit(ProcessingState.Done(result))
        } catch (e: Exception) {
            emit(ProcessingState.Error(e.message ?: "Unknown error"))
        } finally {
            retriever.release()
        }
    }.flowOn(Dispatchers.Default)
}

sealed class ProcessingState {
    data class Processing(val progress: Float, val message: String) : ProcessingState()
    data class Done(val result: com.example.collage.domain.model.VideoResult) : ProcessingState()
    data class Error(val message: String) : ProcessingState()
}
