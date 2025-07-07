// app/src/main/java/com/musicextended/data/local/entities/TrackEntity.kt
package com.musicextended.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musicextended.model.Track // FIX: Corrected import to 'model'
import com.musicextended.network.ExternalUrls // FIX: Corrected import to 'model'
import com.musicextended.model.Artist // FIX: Corrected import to 'model'
import com.musicextended.model.Album // FIX: Corrected import to 'model'
import com.musicextended.network.Image // FIX: Corrected import to 'model'
// Assuming LinkedFrom and ExternalIds are also in 'model'
import com.musicextended.model.LinkedFrom
import com.musicextended.model.ExternalIds

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val name: String,
    val albumId: String?,
    val albumName: String?,
    val albumImageUrl: String?,
    val artistIds: String?, // Comma-separated string of artist IDs
    val artistNames: String?, // Comma-separated string of artist names
    val durationMs: Long?, // FIX: Keep as Long? as previously identified
    val explicit: Boolean?, // FIX: Keep as nullable Boolean
    val trackNumber: Int?,
    val discNumber: Int?, // FIX: Ensure this matches the entity field used below
    val previewUrl: String?,
    val spotifyUri: String?,
    val isLocal: Boolean?, // FIX: Keep as nullable Boolean
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
                discNumber = track.disc_number, // FIX: Use track.disc_number if it exists in the Track model
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
            id = this.albumId ?: "", // FIX: Provide default for non-nullable ID
            name = this.albumName ?: "Unknown Album", // FIX: Provide default for non-nullable name
            type = "album", // FIX: Provide default for non-nullable type
            uri = null, // Not stored in entity
            href = null, // Not stored in entity
            images = this.albumImageUrl?.let { listOf(Image(url = it, height = null, width = null)) } ?: emptyList(), // FIX: Provide emptyList if no image
            artists = emptyList(), // FIX: Provide emptyList if not stored fully
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
                    type = "artist", // FIX: Provide default for non-nullable type
                    uri = null, // Not stored
                    href = null, // Not stored
                    external_urls = null // Not stored
                )
            }
        } else {
            emptyList() // FIX: Provide emptyList if no artists
        }

        // Reconstruct Track
        return Track(
            id = this.id, // FIX: Ensure this is non-null for Track constructor
            name = this.name, // FIX: Ensure this is non-null for Track constructor
            artists = artists, // Use reconstructed list
            album = album, // Use reconstructed album
            duration_ms = this.durationMs ?: 0L, // FIX: Provide default for non-nullable Long in Track
            explicit = this.explicit ?: false, // FIX: Provide default for non-nullable Boolean in Track
            popularity = null, // Not stored
            preview_url = this.previewUrl,
            uri = this.spotifyUri ?: "", // FIX: Provide default for non-nullable String in Track
            href = null, // Not stored
            type = "track", // FIX: Provide default for non-nullable type in Track
            external_urls = this.previewUrl?.let { ExternalUrls(spotify = it) },
            is_local = this.isLocal ?: false, // FIX: Provide default for non-nullable Boolean in Track
            available_markets = null, // Not stored
            disc_number = this.discNumber, // FIX: Use the nullable discNumber from entity
            track_number = this.trackNumber,
            is_playable = this.isPlayable,
            linked_from = null, // Not stored
            external_ids = null // Not stored
        )
    }
}