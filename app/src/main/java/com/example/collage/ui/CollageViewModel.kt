package com.example.collage.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collage.domain.*
import com.example.collage.domain.model.VideoAnalysisResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    data class Processing(val progress: Float, val message: String) : UiState()
    data class Success(val result: VideoAnalysisResult) : UiState()
    data class Error(val message: String) : UiState()
}

class CollageViewModel(
    private val videoProcessor: VideoProcessor
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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
}
