package com.example.collage.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collage.domain.*
import com.example.collage.domain.model.VideoResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    data class Processing(val progress: Float, val message: String) : UiState()
    data class Success(val result: VideoResult, val collage: Bitmap) : UiState()
    data class Error(val message: String) : UiState()
}

class CollageViewModel(
    private val videoProcessor: VideoProcessor,
    private val collageGenerator: CollageGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun processVideo(videoUri: Uri) {
        viewModelScope.launch {
            videoProcessor.processVideo(videoUri).collect { state ->
                when (state) {
                    is ProcessingState.Processing -> {
                        _uiState.value = UiState.Processing(state.progress, state.message)
                    }
                    is ProcessingState.Done -> {
                        val collage = collageGenerator.generate(state.result.people)
                        _uiState.value = UiState.Success(state.result, collage)
                    }
                    is ProcessingState.Error -> {
                        _uiState.value = UiState.Error(state.message)
                    }
                }
            }
        }
    }
}
