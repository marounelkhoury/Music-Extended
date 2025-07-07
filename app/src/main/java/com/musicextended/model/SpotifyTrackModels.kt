// app/src/main/java/com/musicextended/model/SpotifyTrackModels.kt
package com.musicextended.model

import com.google.gson.annotations.SerializedName
import com.musicextended.network.ExternalUrls // Correct import based on your confirmation
import com.musicextended.network.Image // Correct import based on your confirmation

// Top-level response for saved tracks
data class SavedTracksResponse(
    val href: String,
    val items: List<SavedTrackItem>, // List of saved track items
    val limit: Int,
    val next: String?,
    val offset: Int,
    val previous: String?,
    val total: Int
)

// Represents an item in the saved tracks list
data class SavedTrackItem(
    val added_at: String, // Timestamp when the track was added to the library
    val track: Track // The full track object
)

// Full Track Object
data class Track(
    val id: String,
    val name: String,
    val artists: List<Artist>, // List of Artist objects
    val album: Album, // Album info for the track
    val duration_ms: Long,
    val explicit: Boolean,
    val popularity: Int?,
    val preview_url: String?, // A URL to a 30-second preview (nullable)
    val uri: String,
    val href: String?,
    val type: String, // "track"
    @SerializedName("external_urls") val external_urls: ExternalUrls?,
    @SerializedName("is_local") val is_local: Boolean,
    @SerializedName("available_markets") val available_markets: List<String>?, // <-- Make nullable
    @SerializedName("disc_number") val disc_number: Int?, // <-- Make nullable
    @SerializedName("track_number") val track_number: Int?, // <-- Make nullable
    @SerializedName("is_playable") val is_playable: Boolean?, // <-- Make nullable
    @SerializedName("linked_from") val linked_from: LinkedFrom?, // <-- Make nullable
    @SerializedName("external_ids") val external_ids: ExternalIds? // <-- Make nullable
    // Add any other relevant track fields (e.g., episode, track_href, video_thumbnail)
)

// Album Object (simplified, as it's part of a track)
data class Album(
    val id: String,
    val name: String,
    val type: String, // "album"
    val uri: String?,
    val href: String?,
    val images: List<Image>?, // List of Image objects
    val artists: List<Artist>?, // List of Artist objects
    @SerializedName("album_type") val album_type: String?, // <-- Make nullable
    @SerializedName("total_tracks") val total_tracks: Int?, // <-- Make nullable
    @SerializedName("available_markets") val available_markets: List<String>?, // <-- Make nullable
    @SerializedName("release_date") val release_date: String?, // <-- Make nullable
    @SerializedName("release_date_precision") val release_date_precision: String?, // <-- Make nullable
    @SerializedName("external_urls") val external_urls: ExternalUrls?,
    // Add any other relevant album fields if you need them later
)
// Artist Object (simplified, as it's part of a track or album)
data class Artist(
    val id: String,
    val name: String,
    val href: String?,
    val uri: String?,
    @SerializedName("external_urls") val external_urls: ExternalUrls?, // Uses your network.ExternalUrls
    val type: String? // "artist"
)

// External IDs for a track (e.g., ISRC, EAN, UPC)
data class ExternalIds(
    val isrc: String?,
    val ean: String?,
    val upc: String?
)

// Linked From (for track relinking)
data class LinkedFrom(
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val type: String,
    val uri: String
)