// app/src/main/java/com/musicextended/model/SpotifyPlaylistModels.kt
package com.musicextended.model

import com.google.gson.annotations.SerializedName
import com.musicextended.network.ExternalUrls // Correct import based on your confirmation
import com.musicextended.network.Image // Correct import based on your confirmation

// Top-level response for playlists
data class PlaylistResponse(
    val href: String,
    val items: List<SimplifiedPlaylist>, // List of simplified playlist objects
    val limit: Int,
    val next: String?, // URL to the next page of items (can be null)
    val offset: Int,
    val previous: String?, // URL to the previous page of items (can be null)
    val total: Int // Total number of playlists for the current user
)

// Simplified Playlist Object (used in list responses)
data class SimplifiedPlaylist(
    val id: String,
    val name: String,
    val description: String?,
    val collaborative: Boolean,
    val public: Boolean?,
    val images: List<Image>?,
    val owner: User, // Uses model.User
    val uri: String,
    val href: String?,
    @SerializedName("external_urls") val external_urls: ExternalUrls?, // Uses network.ExternalUrls
    val type: String,
    val tracks: TracksInfo, // <--- IMPORTANT: This MUST be TracksInfo, not TracksHref
    @SerializedName("snapshot_id") val snapshot_id: String? // Make nullable
)

// Public User Object (often used for owners, etc.)
data class PublicUser(
    val display_name: String?, // Can be null
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val type: String,
    val uri: String
)

// TracksHref (simplified track info within a playlist object)
data class TracksHref(
    val href: String, // A link to the Spotify Web API endpoint where the full track details can be retrieved.
    val total: Int // The total number of tracks in the playlist.
)