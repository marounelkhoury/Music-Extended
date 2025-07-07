// app/src/main/java/com/musicextended/model/TopTracksResponse.kt
package com.musicextended.model

import com.google.gson.annotations.SerializedName
// Track is already defined in SpotifyTrackModels.kt or similar.
// Make sure you have 'Track' data class available in this package or a connected one.

data class TopTracksResponse(
    val href: String,
    val items: List<Track>, // List of full Track objects
    val limit: Int,
    val next: String?,
    val offset: Int,
    val previous: String?,
    val total: Int
)