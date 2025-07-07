// app/src/main/java/com/musicextended/data/local/entities/PlaylistEntity.kt
package com.musicextended.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musicextended.network.Image // Image is in 'network'
import com.musicextended.model.SimplifiedPlaylist // SimplifiedPlaylist is in 'model'
import com.musicextended.model.User // User is used for owner in SimplifiedPlaylist
import com.musicextended.network.ExternalUrls // ExternalUrls is in 'network'
import com.musicextended.model.TracksInfo // TracksInfo is in 'model'

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val collaborative: Boolean,
    val public: Boolean, // Stays Boolean here, we'll handle null from API with default
    val ownerId: String, // Storing owner's ID
    val ownerDisplayName: String?, // Storing owner's display name, now nullable as per User model
    val playlistUrl: String?, // external_urls.spotify
    val imageUrl: String?, // The URL of the playlist cover image
    val totalTracks: Int, // From tracks.total
    val lastUpdated: Long // Timestamp for caching logic
) {
    companion object {
        fun fromSimplifiedPlaylist(playlist: SimplifiedPlaylist): PlaylistEntity {
            return PlaylistEntity(
                id = playlist.id,
                name = playlist.name,
                description = playlist.description,
                collaborative = playlist.collaborative,
                public = playlist.public ?: false, // `public` is Boolean? in SimplifiedPlaylist
                ownerId = playlist.owner.id, // User.id is non-nullable
                ownerDisplayName = playlist.owner.display_name, // User.display_name is nullable
                playlistUrl = playlist.external_urls?.spotify, // external_urls is nullable in SimplifiedPlaylist
                imageUrl = playlist.images?.firstOrNull()?.url, // images is nullable in SimplifiedPlaylist
                totalTracks = playlist.tracks.total, // TracksInfo.total is non-nullable
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    // --- Conversion from PlaylistEntity back to SimplifiedPlaylist (if needed) ---
    fun toSimplifiedPlaylist(): SimplifiedPlaylist {
        // Reconstruct SimplifiedPlaylist from the entity.
        // Create a 'User' object based on the fields available in PlaylistEntity
        val ownerUser = User(
            id = this.ownerId, // Non-nullable in User model
            display_name = this.ownerDisplayName, // Nullable in User model
            href = null, // Not stored in PlaylistEntity, nullable in User model
            uri = null, // Not stored in PlaylistEntity, nullable in User model
            external_urls = null, // Not stored in PlaylistEntity, nullable in User model
            type = null // Not stored in PlaylistEntity, nullable in User model
        )

        return SimplifiedPlaylist(
            collaborative = this.collaborative,
            description = this.description,
            // external_urls is nullable in SimplifiedPlaylist model
            external_urls = this.playlistUrl?.let { ExternalUrls(spotify = it) },
            href = null, // Not stored directly, nullable in SimplifiedPlaylist model
            id = this.id, // Non-nullable in SimplifiedPlaylist model
            // images is nullable in SimplifiedPlaylist model
            images = this.imageUrl?.let { listOf(Image(url = it, height = null, width = null)) },
            name = this.name, // Non-nullable in SimplifiedPlaylist model
            owner = ownerUser, // Use the created User object
            public = this.public, // Passed directly, `public` is Boolean in entity and Boolean? in model
            snapshot_id = null, // `snapshot_id` is nullable in SimplifiedPlaylist model
            tracks = TracksInfo(href = "", total = this.totalTracks), // TracksInfo.href is non-nullable
            type = "playlist", // Non-nullable in SimplifiedPlaylist model
            uri = "" // Non-nullable in SimplifiedPlaylist model
        )
    }
}