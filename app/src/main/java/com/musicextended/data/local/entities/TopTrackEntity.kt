// app/src/main/java/com/musicextended/data/local/entities/TopTrackEntity.kt
package com.musicextended.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musicextended.model.Track // Import the network Track model
// No need for Image import here if we're just storing the URL as a String

@Entity(tableName = "top_tracks")
data class TopTrackEntity(
    @PrimaryKey val id: String,
    val name: String,
    val artistNames: String?, // Comma-separated string of artist names
    val albumName: String?,
    val albumImageUrl: String?, // URL of the album's largest image
    val durationMs: Long,
    val explicit: Boolean,
    val popularity: Int?,
    val previewUrl: String?,
    val spotifyUri: String?,
    val spotifyHref: String?,
    val lastUpdated: Long
) {
    companion object {
        fun fromTrack(track: Track): TopTrackEntity {
            return TopTrackEntity(
                id = track.id,
                name = track.name,
                artistNames = track.artists.joinToString(", ") { it.name },
                albumName = track.album.name,
                albumImageUrl = track.album.images?.firstOrNull()?.url,
                durationMs = track.duration_ms,
                explicit = track.explicit,
                popularity = track.popularity,
                previewUrl = track.preview_url,
                spotifyUri = track.uri,
                spotifyHref = track.href,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}