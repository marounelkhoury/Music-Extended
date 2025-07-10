// app/src/main/java/com/musicextended/data/local/entities/TrackEntity.kt
package com.musicextended.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musicextended.model.Album
import com.musicextended.model.Artist
import com.musicextended.model.Track
import com.musicextended.network.ExternalUrls
import com.musicextended.network.Image

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val name: String,
    val albumId: String?,
    val albumName: String?,
    val albumImageUrl: String?,
    val artistIds: String?,
    val artistNames: String?,
    val durationMs: Long?,
    val explicit: Boolean?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val previewUrl: String?,
    val spotifyUri: String?,
    val isLocal: Boolean?,
    val isPlayable: Boolean?,
    val lastUpdated: Long
) {
    companion object {
        fun fromTrack(track: Track): TrackEntity {
            return TrackEntity(
                id = track.id,
                name = track.name ?: "Unknown Track", // Provide default for non-nullable name if needed
                albumId = track.album?.id,
                albumName = track.album?.name,
                albumImageUrl = track.album?.images?.firstOrNull()?.url,
                artistIds = track.artists?.joinToString(",") { it.id },
                artistNames = track.artists?.joinToString(",") { it.name },
                durationMs = track.duration_ms,
                explicit = track.explicit,
                trackNumber = track.track_number,
                discNumber = track.disc_number,
                previewUrl = track.preview_url,
                spotifyUri = track.uri,
                isLocal = track.is_local,
                isPlayable = track.is_playable,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    fun toTrack(): Track {
        // Reconstruct Album
        val album = Album(
            id = this.albumId ?: "",
            name = this.albumName ?: "Unknown Album",
            type = "album",
            uri = null, // Not stored in entity
            href = null, // Not stored in entity
            images = this.albumImageUrl?.let { listOf(Image(url = it, height = null, width = null)) } ?: emptyList(),
            artists = emptyList(),
            album_type = null, // Not stored
            total_tracks = null, // Not stored
            available_markets = null, // Not stored
            release_date = null, // Not stored
            release_date_precision = null, // Not stored
            external_urls = null // Not stored
        )

        // Reconstruct Artists
        val artists = if (!artistIds.isNullOrEmpty() && !artistNames.isNullOrEmpty()) {
            artistIds.split(",").zip(artistNames.split(",")).map { (id, name) ->
                Artist(
                    id = id,
                    name = name,
                    type = "artist",
                    uri = null, // Not stored
                    href = null, // Not stored
                    external_urls = null // Not stored
                )
            }
        } else {
            emptyList()
        }

        // Reconstruct Track
        return Track(
            id = this.id,
            name = this.name,
            artists = artists, // Use reconstructed list
            album = album, // Use reconstructed album
            duration_ms = this.durationMs ?: 0L,
            explicit = this.explicit ?: false,
            popularity = null, // Not stored
            preview_url = this.previewUrl,
            uri = this.spotifyUri ?: "",
            href = null, // Not stored
            type = "track",
            external_urls = this.previewUrl?.let { ExternalUrls(spotify = it) },
            is_local = this.isLocal ?: false,
            available_markets = null, // Not stored
            disc_number = this.discNumber,
            track_number = this.trackNumber,
            is_playable = this.isPlayable,
            linked_from = null, // Not stored
            external_ids = null // Not stored
        )
    }
}