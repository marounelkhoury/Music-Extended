// app/src/main/java/com/musicextended/model/TopArtistsResponse.kt
package com.musicextended.model

import com.google.gson.annotations.SerializedName
// Artist is already defined in SpotifyTrackModels.kt or similar.
// Make sure you have 'Artist' data class available in this package or a connected one.

data class TopArtistsResponse(
    val href: String,
    val items: List<Artist>, // List of full Artist objects
    val limit: Int,
    val next: String?,
    val offset: Int,
    val previous: String?,
    val total: Int
)