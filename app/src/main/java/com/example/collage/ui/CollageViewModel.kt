package com.example.collage.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collage.data.VideoRepository
import com.example.collage.data.local.entity.VideoRecord
import com.example.collage.domain.processor.*
import com.example.collage.domain.model.VideoAnalysisResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    data class Processing(val progress: Float, val message: String) : UiState()
    data class Success(val result: VideoAnalysisResult) : UiState()
    data class SavedSuccess(
        val videoRecord: VideoRecord,
        val collageBitmap: Bitmap,
        val persons: List<com.example.collage.data.local.entity.PersonRecord>
    ) : UiState()
    data class Error(val message: String) : UiState()
}

class CollageViewModel(
    private val videoProcessor: VideoProcessor,
    private val repository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val history: StateFlow<List<VideoRecord>> = repository.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun processVideo(videoUri: Uri) {
        viewModelScope.launch {
            videoProcessor.processVideo(videoUri).collect { state ->
                when (state) {
                    is ProcessingState.ExtractingFrames -> {
                        _uiState.value = UiState.Processing(state.progress, "Analyzing video frames...")
                    }
                    is ProcessingState.GroupingPersons -> {
                        _uiState.value = UiState.Processing(state.progress, "Grouping identical faces...")
                    }
                    is ProcessingState.GeneratingCollage -> {
                        _uiState.value = UiState.Processing(0.95f, "Rendering final collage...")
                    }
                    is ProcessingState.Done -> {
                        repository.saveResult(state.result)
                        _uiState.value = UiState.Success(state.result)
                    }
                    is ProcessingState.Error -> {
                        _uiState.value = UiState.Error(state.message)
                    }
                }
            }
        }
    }

    fun reset() {
        _uiState.value = UiState.Idle
    }

    fun loadHistoryDetail(video: VideoRecord) {
        viewModelScope.launch {
            try {
                val bitmap = android.graphics.BitmapFactory.decodeFile(video.collagePath)
                val persons = repository.getPersonsForVideo(video.id)
                if (bitmap != null) {
                    _uiState.value = UiState.SavedSuccess(video, bitmap, persons)
                } else {
                    _uiState.value = UiState.Error("Could not load collage image file")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error loading history: ${e.message}")
            }
        }
    }

    fun deleteHistoryItem(video: VideoRecord) {
        viewModelScope.launch {
            repository.deleteResult(video)
        }
    }
}
