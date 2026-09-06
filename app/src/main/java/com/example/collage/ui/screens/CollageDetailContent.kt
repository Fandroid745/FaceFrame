package com.example.collage.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import coil.compose.rememberAsyncImagePainter
import com.example.collage.ui.UiState
import java.io.File

@Composable
fun CollageDetailContent(
    uiState: UiState,
    onShare: (Bitmap) -> Unit,
    onSave: (Bitmap) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stateData = when (uiState) {
        is UiState.Success -> {
            val res = uiState.result
            DetailData(
                collage = res.collageBitmap,
                personCount = res.personCount,
                totalAppearances = res.totalAppearances,
                personsList = {
                    items(res.persons) { person ->
                        PersonListItem(
                            label = "Person ${person.personId}",
                            appearanceCount = person.appearanceCount,
                            thumbnail = {
                                Image(
                                    bitmap = person.representativeShot.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        )
                    }
                }
            )
        }
        is UiState.SavedSuccess -> {
            val rec = uiState.videoRecord
            DetailData(
                collage = uiState.collageBitmap,
                personCount = rec.personCount,
                totalAppearances = rec.totalAppearances,
                personsList = {
                    items(uiState.persons) { person ->
                        PersonListItem(
                            label = "Person ${person.personId}",
                            appearanceCount = person.appearanceCount,
                            thumbnail = {
                                Image(
                                    painter = rememberAsyncImagePainter(File(person.thumbnailPath)),
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        )
                    }
                }
            )
        }
        else -> null
    }

    if (stateData == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No result available.")
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().aspectRatio(9f / 16f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    bitmap = stateData.collage.asImageBitmap(),
                    contentDescription = "Generated Collage",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(onClick = { onSave(stateData.collage) }, modifier = Modifier.weight(1f)) {
                    Text("Save")
                }
                Button(onClick = { onShare(stateData.collage) }, modifier = Modifier.weight(1f)) {
                    Text("Share")
                }
            }
        }

        item {
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Process Another Video")
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(text = "Analysis Results", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${stateData.personCount} unique people identified • ${stateData.totalAppearances} appearances",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        stateData.personsList(this)
    }
}

@Composable
private fun PersonListItem(
    label: String,
    appearanceCount: Int,
    thumbnail: @Composable () -> Unit
) {
    Column {
        ListItem(
            headlineContent = { Text(label, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text("$appearanceCount appearances", style = MaterialTheme.typography.bodySmall) },
            leadingContent = thumbnail
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
    }
}

private data class DetailData(
    val collage: Bitmap,
    val personCount: Int,
    val totalAppearances: Int,
    val personsList: LazyListScope.() -> Unit
)
