// app/src/main/java/com/musicextended/model/SpotifySearchResponse.kt (New File)
package com.musicextended.model

import com.google.gson.annotations.SerializedName

// Top-level response for Spotify Search API
data class SpotifySearchResponse(
    val tracks: TracksSearchResult? // Contains the search results for tracks
    // You could add albums, artists, playlists here if you search for multiple types
)

// Represents the tracks object within the search response
data class TracksSearchResult(
    val href: String,
    val items: List<Track>, // List of full Track objects (reusing your existing Track data class)
    val limit: Int,
    val next: String?,
    val offset: Int,
    val previous: String?,
    val total: Int
)