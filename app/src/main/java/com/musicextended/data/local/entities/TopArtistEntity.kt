// app/src/main/java/com/musicextended/data/local/entities/TopArtistEntity.kt
package com.musicextended.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musicextended.model.Artist // Import the network Artist model
import com.musicextended.network.Image // Import the network Image model (if you use it separately, otherwise just map URL directly)

@Entity(tableName = "top_artists")
data class TopArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String?, // URL of the largest image for the artist
    val spotifyUri: String?,
    val spotifyHref: String?,
    val genres: String?, // Comma-separated string of genres
    val popularity: Int?,
    val lastUpdated: Long // Timestamp of when this entity was last updated
) {
    companion object {
        fun fromArtist(artist: Artist): TopArtistEntity {
            return TopArtistEntity(
                id = artist.id,
                name = artist.name,
                // Assuming Artist model might eventually include images or you fetch them separately for artists.
                // For now, if Artist only has basic info, you might need another step to get full artist details.
                // If the 'Artist' model in 'SpotifyTrackModels.kt' does not have 'images' field directly,
                // you might need to adjust or fetch full Artist object (not simplified) to get image.
                // For simplicity, let's assume 'images' could be derived or added to the network Artist model later,
                // or leave imageUrl as null if not available from the current Artist model.
                // For now, we'll keep it nullable and assume it might come from a full artist object.
                imageUrl = null, // Placeholder: Artist object typically doesn't have images directly. Full Artist obj does.
                // You'd need a separate API call for full artist details to get images and genres reliably.
                // For now, setting to null, we can refine this later.
                spotifyUri = artist.uri,
                spotifyHref = artist.href,
                genres = null, // Placeholder: Artist model does not have genres directly. Full Artist obj does.
                popularity = null, // Placeholder: Artist model does not have popularity directly. Full Artist obj does.
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
    // Note: To get image URLs, popularity, and full genre list for Artists, you often need the "Full Artist Object"
    // from an endpoint like /artists/{id}, not just the simplified Artist object found in tracks/playlists.
    // For now, we'll keep `imageUrl`, `genres`, `popularity` as nullable and address fetching full artist details later if needed.
}