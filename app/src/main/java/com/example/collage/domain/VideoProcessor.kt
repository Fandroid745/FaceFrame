package com.example.collage.domain

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.collage.domain.model.VideoAnalysisResult
import com.example.collage.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.math.max

private const val FRAME_INTERVAL_MS = 200L

class VideoProcessor(
    private val context: Context,
    private val faceAnalyzer: FaceAnalyzer,
    private val tracker: AppearanceTracker,
    private val grouper: PersonGrouper,
    private val collageGenerator: CollageGenerator
) {

    fun processVideo(videoUri: Uri): Flow<ProcessingState> = flow {
        emit(ProcessingState.ExtractingFrames(0f, 0, 100))
        tracker.reset()

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            if (durationMs <= 0L) {
                emit(ProcessingState.Error("Invalid video duration"))
                return@flow
            }

            val totalFrames = ((durationMs / FRAME_INTERVAL_MS) + 1).toInt()
            var frameIndex = 0

            for (timeMs in 0L..durationMs step FRAME_INTERVAL_MS) {
                val frameBitmap = retriever.getFrameAtTime(timeMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST) ?: continue

                val progress = (frameIndex.toFloat() / totalFrames) * 0.70f
                emit(ProcessingState.ExtractingFrames(progress, frameIndex + 1, totalFrames))

                val detections = faceAnalyzer.analyze(frameBitmap, timeMs)
                tracker.processFrameDetections(timeMs, detections)
                frameIndex++
            }

            emit(ProcessingState.GroupingPersons(0.85f, frameIndex))
            val appearances = tracker.finishTracking()
            val people = grouper.groupAppearances(appearances)

            if (people.isEmpty()) {
                emit(ProcessingState.Error("No faces detected"))
                return@flow
            }

            emit(ProcessingState.GeneratingCollage)
            val collage = collageGenerator.generate(people)

            val totalAppearances = people.sumOf { it.appearanceCount }
            val result = VideoAnalysisResult(
                videoUri = videoUri.toString(),
                durationMs = durationMs,
                totalAppearances = totalAppearances,
                personCount = people.size,
                persons = people,
                collageBitmap = collage
            )

            emit(ProcessingState.Done(result))
        } catch (e: Exception) {
            emit(ProcessingState.Error(e.message ?: "Unknown error"))
        } finally {
            retriever.release()
        }
    }.flowOn(Dispatchers.Default)
}

sealed class ProcessingState {
    data class ExtractingFrames(val progress: Float, val current: Int, val total: Int) : ProcessingState()
    data class GroupingPersons(val progress: Float, val segmentCount: Int) : ProcessingState()
    object GeneratingCollage : ProcessingState()
    data class Done(val result: VideoAnalysisResult) : ProcessingState()
    data class Error(val message: String) : ProcessingState()
}
