package com.example.collage.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.collage.R
import com.example.collage.data.local.entity.VideoRecord
import com.example.collage.ui.UiState
import com.example.collage.ui.components.HistoryItem
import com.example.collage.ui.components.ProcessingHistoryItem

@Composable
fun HomePickerContent(
    uiState: UiState,
    onVideoSelected: (Uri) -> Unit,
    onRecentClick: (VideoRecord) -> Unit,
    onCancelProcessing: () -> Unit,
    recentCollage: VideoRecord? = null,
    modifier: Modifier = Modifier
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onVideoSelected(it) }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.dialog_cancel_title)) },
            text = { Text(stringResource(R.string.dialog_cancel_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    onCancelProcessing()
                    launcher.launch("video/*")
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Selection Area (Always Visible)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (uiState is UiState.Processing) {
                                showCancelDialog = true
                            } else {
                                launcher.launch("video/*")
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.home_pick_video_btn))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.home_recent_collage),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Integrated Recent/Processing section
        when (uiState) {
            is UiState.Processing -> {
                ProcessingHistoryItem(
                    progress = uiState.progress,
                    statusMessage = uiState.message
                )
            }
            else -> {
                if (recentCollage != null) {
                    HistoryItem(
                        video = recentCollage,
                        onClick = { onRecentClick(recentCollage) }
                    )
                }
            }
        }
    }
}
