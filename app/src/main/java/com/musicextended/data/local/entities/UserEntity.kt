// app/src/main/java/com/musicextended/data/local/entities/UserEntity.kt
package com.musicextended.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musicextended.network.Image
import com.musicextended.network.SpotifyUserProfile
import com.musicextended.network.Followers // Still needed for Followers object
import com.musicextended.network.ExplicitContent // Assuming ExplicitContent is in your network package

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val display_name: String?,
    val email: String?,
    val country: String?,
    val spotifyUri: String?,
    val spotifyHref: String?,
    val profileUrl: String?, // This stores the actual "spotify" URL string
    val followersTotal: Int?,
    val imageUrl: String?,
    val lastUpdated: Long
    // If you ever need to store explicit content settings, add them here:
    // val explicitContentEnabled: Boolean?,
    // val explicitContentFilterLocked: Boolean?
) {
    companion object {
        fun fromSpotifyUserProfile(userProfile: SpotifyUserProfile): UserEntity {
            return UserEntity(
                id = userProfile.id,
                display_name = userProfile.display_name,
                email = userProfile.email,
                country = userProfile.country,
                spotifyUri = userProfile.uri,
                spotifyHref = userProfile.href,
                // Safely get the Spotify URL from the external_urls map
                profileUrl = userProfile.external_urls?.get("spotify"),
                followersTotal = userProfile.followers?.total,
                imageUrl = userProfile.images?.firstOrNull()?.url,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    fun toSpotifyUserProfile(): SpotifyUserProfile {
        return SpotifyUserProfile(
            country = this.country,
            display_name = this.display_name,
            email = this.email,
            external_urls = this.profileUrl?.let { mapOf("spotify" to it) },
            followers = this.followersTotal?.let { Followers(href = null, total = it) },
            href = this.spotifyHref,
            id = this.id,
            images = this.imageUrl?.let {
                listOf(Image(url = it, height = null, width = null))
            },
            product = null, // Assuming product is not stored in UserEntity for now
            type = "user", // Assuming type is always "user" for this conversion
            uri = this.spotifyUri,
            // FIX: Add explicit_content, providing a default if not stored in UserEntity
            explicit_content = ExplicitContent(
                filter_enabled = false, // Default to false if not tracked
                filter_locked = false   // Default to false if not tracked
            ) // You might need to adjust this default based on how you want to handle it.
            // If SpotifyUserProfile's explicit_content is nullable, you could pass null.
        )
    }
}