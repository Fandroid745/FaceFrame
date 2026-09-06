package com.example.collage.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.collage.ui.UiState

/**
 * Enhanced ResultScreen ported with Snapshot's scrollable layout logic.
 * Maintains the current project's theme and button styles.
 */
@Composable
fun ResultScreen(
    uiState: UiState,
    onShare: (Bitmap) -> Unit,
    onSave: (Bitmap) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState is UiState.Success) {
        val result = uiState.result

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Full-Width Collage with fixed aspect ratio
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        bitmap = result.collageBitmap.asImageBitmap(),
                        contentDescription = "Generated Collage",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 2. Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onSave(result.collageBitmap) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                    Button(
                        onClick = { onShare(result.collageBitmap) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Share")
                    }
                }
            }

            // 3. Reset Button
            item {
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Process Another Video")
                }
            }

            // 4. Statistics Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Analysis Results",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${result.personCount} unique people identified • ${result.totalAppearances} appearances",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // 5. List of People (Integrated into main LazyColumn)
            items(result.persons) { person ->
                ListItem(
                    headlineContent = { Text("Person ${person.personId}", style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text("${person.appearanceCount} appearances", style = MaterialTheme.typography.bodySmall) },
                    leadingContent = {
                        Image(
                            bitmap = person.representativeShot.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No result yet. Process a video to see your collage.")
        }
    }
}
