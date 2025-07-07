// app/src/main/java/com/musicextended/network/SpotifyUserProfile.kt
package com.musicextended.network

import com.google.gson.annotations.SerializedName

// This is a sample structure, adjust based on your actual SpotifyUserProfile JSON response
data class SpotifyUserProfile(
    val id: String,
    val display_name: String?,
    val email: String?,
    val href: String?,
    val uri: String?,
    val product: String?, // Make sure this is present and nullable in your model
    val country: String?, // Make sure this is present and nullable in your model
    val followers: Followers?, // Make sure this is present and nullable
    val images: List<Image>?, // Make sure this is present and nullable
    @SerializedName("explicit_content") val explicit_content: ExplicitContent?, // Make sure this is present and nullable
    @SerializedName("external_urls") val external_urls: Map<String, String>?, // Or your specific ExternalUrls class if you have one
    val type: String? // "user"
    // Add any other fields from the Spotify API response you need
)

// Example data class for nested objects, adjust based on your needs
data class Followers(
    val href: String?,
    val total: Int?
)

data class Image(
    val url: String?,
    val height: Int?,
    val width: Int?
)

data class ExplicitContent(
    @SerializedName("filter_enabled") val filter_enabled: Boolean?,
    @SerializedName("filter_locked") val filter_locked: Boolean?
)

// If you have a specific ExternalUrls class:
data class ExternalUrls(@SerializedName("spotify") val spotify: String?)