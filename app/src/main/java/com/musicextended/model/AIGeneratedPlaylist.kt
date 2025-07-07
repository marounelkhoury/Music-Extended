package com.musicextended.model

// This class will hold the structured AI-generated playlist data
data class AIGeneratedPlaylist(
    val name: String,
    val description: String,
    val songs: List<GeneratedSongSuggestion> // List of structured song suggestions
)

// This class will hold details for each suggested song,
// including Spotify-fetched info and AI's reasoning.
data class GeneratedSongSuggestion(
    val trackId: String?,           // Spotify Track ID (nullable, if not found)
    val trackName: String,          // Name of the song (from AI, then confirmed by Spotify)
    val artistNames: String,        // Artists of the song (from AI, then confirmed by Spotify)
    val albumImageUrl: String?,     // Album cover URL (from Spotify search)
    val aiDescription: String       // AI's specific reasoning/description for this song
)