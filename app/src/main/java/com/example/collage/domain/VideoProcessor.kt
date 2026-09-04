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

class VideoProcessor(private val context: Context) {

    fun extractFrames(videoUri: Uri): Flow<ProcessingState> = flow {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, videoUri)

            val durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?: error("Could not read video duration")

            val frames = mutableListOf<Pair<Long, Bitmap>>()
            var currentTimeMs = 0L

            while (currentTimeMs < durationMs) {
                val bitmap = retriever.getFrameAtTime(
                    currentTimeMs * 1000L,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                )

                if (bitmap != null) {
                    frames.add(currentTimeMs to bitmap)
                }

                currentTimeMs += FRAME_INTERVAL_MS

                val progress = (currentTimeMs.toFloat() / durationMs).coerceIn(0f, 1f)
                emit(ProcessingState.ExtractingFrames(progress))
            }

            emit(ProcessingState.Done(frames))
        } finally {
            retriever.release()
        }
    }.flowOn(Dispatchers.Default)
}

sealed class ProcessingState {
    data class ExtractingFrames(val progress: Float) : ProcessingState()
    data class Done(val frames: List<Pair<Long, Bitmap>>) : ProcessingState()
    data class Error(val message: String) : ProcessingState()
}
