package com.example.collage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.collage.ui.UiState

@Composable
fun HomeProcessingContent(
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState) {
            is UiState.Processing -> {
                CircularProgressIndicator(progress = { uiState.progress })
                Spacer(modifier = Modifier.height(16.dp))
                Text(uiState.message)
                Text("${(uiState.progress * 100).toInt()}%")
            }
            is UiState.Error -> {
                Text("Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
            }
            is UiState.Success -> {
                Text("Processing Complete! Head to the Result tab.")
            }
            else -> {
                Text("No video is being processed. Go to Home to pick one.")
            }
        }
    }
}
