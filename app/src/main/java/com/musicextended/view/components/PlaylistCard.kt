// app/src/main/java/com/musicextended/view/components/PlaylistCard.kt
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
import com.musicextended.model.SimplifiedPlaylist // Ensure this import is correct

@Composable
fun PlaylistCard(playlist: SimplifiedPlaylist) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            playlist.images?.firstOrNull()?.url?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Playlist Image",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Column {
                Text(text = playlist.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "by ${playlist.owner.display_name ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
                Text(text = "${playlist.tracks.total} tracks", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}