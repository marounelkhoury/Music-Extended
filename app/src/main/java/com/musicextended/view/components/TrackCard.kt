// app/src/main/java/com/musicextended/view/components/TrackCard.kt
package com.musicextended.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicextended.model.SavedTrackItem // Ensure this import is correct

@Composable
fun TrackCard(trackItem: SavedTrackItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            trackItem.track.album.images?.firstOrNull()?.url?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Album Art",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Column {
                Text(text = trackItem.track.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Artist: ${trackItem.track.artists.firstOrNull()?.name ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Album: ${trackItem.track.album.name}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}