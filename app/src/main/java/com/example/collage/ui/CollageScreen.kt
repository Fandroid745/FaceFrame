package com.example.collage.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.collage.domain.model.VideoResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollageScreen(
    viewModel: CollageViewModel,
    onShare: (Bitmap) -> Unit,
    onSave: (Bitmap) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.processVideo(it) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Collage Creator") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState) {
                is UiState.Idle -> {
                    Button(onClick = { launcher.launch("video/*") }) {
                        Text("Select Video")
                    }
                }
                is UiState.Processing -> {
                    CircularProgressIndicator(progress = { state.progress })
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(state.message)
                    Text("${(state.progress * 100).toInt()}%")
                }
                is UiState.Success -> {
                    CollageResult(
                        result = state.result,
                        collage = state.collage,
                        onShare = { onShare(state.collage) },
                        onSave = { onSave(state.collage) }
                    )
                }
                is UiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { launcher.launch("video/*") }) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}

@Composable
fun CollageResult(
    result: VideoResult,
    collage: Bitmap,
    onShare: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = collage.asImageBitmap(),
            contentDescription = "Generated Collage",
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onSave) { Text("Save to Gallery") }
            Button(onClick = onShare) { Text("Share") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Total People: ${result.people.size}", style = MaterialTheme.typography.headlineSmall)
        Text("Total Appearances: ${result.totalAppearances}")

        LazyColumn(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            items(result.people) { person ->
                ListItem(
                    headlineContent = { Text("Person ${person.id}") },
                    supportingContent = { Text("${person.appearanceCount} appearances") },
                    leadingContent = {
                        Image(
                            bitmap = person.representativeBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                )
            }
        }
    }
}
