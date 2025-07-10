// app/src/main/java/com/musicextended/data/local/entities/TopArtistEntity.kt
package com.musicextended.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musicextended.model.Artist

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
                imageUrl = null,
                spotifyUri = artist.uri,
                spotifyHref = artist.href,
                genres = null,
                popularity = null,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

}