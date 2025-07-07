// app/src/main/java/com/musicextended/viewmodel/AIGenerationViewModel.kt
package com.musicextended.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musicextended.MusicExtendedApplication
import com.musicextended.data.data.repository.TopTrackRepository
import com.musicextended.data.local.entities.TopArtistEntity
import com.musicextended.data.local.entities.TopTrackEntity
import com.musicextended.data.repository.TopArtistRepository
import com.musicextended.model.AIGeneratedPlaylist
import com.musicextended.model.GeneratedSongSuggestion
import com.musicextended.network.SpotifyUserService
import com.musicextended.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class AIGenerationViewModel(
    private val topArtistRepository: TopArtistRepository,
    private val topTrackRepository: TopTrackRepository,
    private val spotifyUserService: SpotifyUserService
) : ViewModel() {

    private val tag = "AIGenerationViewModel"

    private val _topArtists = MutableStateFlow<List<TopArtistEntity>>(emptyList())
    val topArtists: StateFlow<List<TopArtistEntity>> = _topArtists.asStateFlow()

    private val _topTracks = MutableStateFlow<List<TopTrackEntity>>(emptyList())
    val topTracks: StateFlow<List<TopTrackEntity>> = _topTracks.asStateFlow()

    private val _generatedContent = MutableStateFlow<AIGeneratedPlaylist?>(null)
    val generatedContent: StateFlow<AIGeneratedPlaylist?> = _generatedContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        Log.d(tag, "AIGenerationViewModel initialized.")
        viewModelScope.launch {
            topArtistRepository.getUserTopArtists().collect { artists ->
                _topArtists.value = artists
                Log.d(tag, "AIGVM: Collected ${artists.size} top artists from repository.")
            }
        }
        viewModelScope.launch {
            topTrackRepository.getUserTopTracks().collect { tracks ->
                _topTracks.value = tracks
                Log.d(tag, "AIGVM: Collected ${tracks.size} top tracks from repository.")
            }
        }
    }

    fun generatePlaylistIdea(promptSuffix: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _generatedContent.value = null

            try {
                val artists = _topArtists.value
                val tracks = _topTracks.value

                Log.d(tag, "Artists for prompt: ${artists.size}, Tracks for prompt: ${tracks.size}")

                if (artists.isEmpty() && tracks.isEmpty()) {
                    _error.value = "No top artists or tracks available to generate ideas. Please listen to more music on Spotify."
                    _isLoading.value = false
                    return@launch
                }

                val artistNames = artists.take(10).joinToString(", ") { it.name }
                val trackNames = tracks.take(10).joinToString(", ") { "${it.name} by ${it.artistNames}" }

                var prompt = """
                    Based on the following user's music taste, suggest a unique and creative playlist idea.
                    Provide a concise **Playlist Name** and a **Description**.
                    Then, suggest 5-10 songs that would fit this playlist.
                    For each song, provide its exact title, primary artist(s), and a brief **Reasoning** why it fits.
                    
                    **Format your response exactly like this:**
                    
                    ## Playlist Name: [Your Catchy Playlist Name Here]
                    Description: [A brief, engaging description of the playlist's vibe or theme.]
                    
                    ### Songs:
                    - **Song:** [Song Title 1] by [Artist Name(s) 1]
                      **Reasoning:** [Why this song fits the playlist.]
                    - **Song:** [Song Title 2] by [Artist Name(s) 2]
                      **Reasoning:** [Why this song fits the playlist.]
                    ... (up to 10 songs)
                    
                    Prioritize suggesting songs the user might not have heard before, but that align with their established taste. Focus on a cohesive theme or mood.
                    
                    User's Top Artists: ${artistNames.ifEmpty { "No top artists available." }}
                    User's Top Tracks: ${trackNames.ifEmpty { "No top tracks available." }}
                    Additional request: ${promptSuffix.ifBlank { "No additional request." }}
                    """.trimIndent()

                Log.d(tag, "Sending prompt to Gemini: $prompt")

                val rawGeneratedResult = withContext(Dispatchers.IO) {
                    try {
                        val apiKey = Constants.GEMINI_API_KEY
                        val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
                        val url = URL(apiUrl)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "POST"
                        connection.setRequestProperty("Content-Type", "application/json")
                        connection.doOutput = true

                        val chatHistory = JSONObject().apply {
                            put("role", "user")
                            put("parts", org.json.JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            })
                        }
                        val payload = JSONObject().apply {
                            put("contents", org.json.JSONArray().apply {
                                put(chatHistory)
                            })
                        }

                        connection.outputStream.use { os ->
                            val input = payload.toString().toByteArray(Charsets.UTF_8)
                            os.write(input, 0, input.size)
                        }

                        val responseCode = connection.responseCode
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { br ->
                                val responseString = br.readText()
                                Log.d(tag, "Gemini API Raw Response: $responseString")

                                val jsonResponse = JSONObject(responseString)
                                val candidates = jsonResponse.optJSONArray("candidates")
                                if (candidates != null && candidates.length() > 0) {
                                    val content = candidates.optJSONObject(0)?.optJSONObject("content")
                                    content?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                                } else {
                                    null
                                }
                            }
                        } else {
                            val errorStream = BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream, Charsets.UTF_8))
                            val errorResponse = errorStream.readText()
                            Log.e(tag, "Gemini API Error: $responseCode - $errorResponse")
                            "Error: Gemini API error: $responseCode - $errorResponse"
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Exception during AI playlist generation (Network Op in withContext): ${e.message}", e)
                        "Error: Exception during network call: ${e.message}"
                    }
                }

                if (!rawGeneratedResult.isNullOrBlank() && !rawGeneratedResult.startsWith("Error:")) {
                    val parsedPlaylist = parseAndSearchSongs(rawGeneratedResult)
                    _generatedContent.value = parsedPlaylist
                    Log.d(tag, "Gemini generated content successfully and parsed.")
                } else if (rawGeneratedResult != null && rawGeneratedResult.startsWith("Error:")) {
                    _error.value = rawGeneratedResult
                } else {
                    _error.value = "Gemini API returned empty or malformed content."
                    Log.e(tag, "Gemini API returned empty/malformed content or was null: $rawGeneratedResult")
                }

            } catch (e: Exception) {
                _error.value = "Error generating playlist: ${e.message}"
                Log.e(tag, "General exception during AI playlist generation process", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun parseAndSearchSongs(geminiResponse: String): AIGeneratedPlaylist? {
        var playlistName = "Generated Playlist"
        var playlistDescription = "A playlist generated by AI based on your taste."
        val songSuggestions = mutableListOf<GeneratedSongSuggestion>()

        try {
            // Regex to extract Playlist Name
            val namePattern = Pattern.compile("## Playlist Name: (.+)")
            val nameMatcher = namePattern.matcher(geminiResponse)
            if (nameMatcher.find()) {
                playlistName = nameMatcher.group(1).trim()
                Log.d(tag, "Parsed Playlist Name: $playlistName")
            }

            // Regex to extract Description
            val descriptionPattern = Pattern.compile("Description: (.+)")
            val descriptionMatcher = descriptionPattern.matcher(geminiResponse)
            if (descriptionMatcher.find()) {
                playlistDescription = descriptionMatcher.group(1).trim()
                Log.d(tag, "Parsed Description: $playlistDescription")
            }

            // Regex to extract each song suggestion (Song, Artist, Reasoning)
            // This pattern handles cases where Reasoning might be the last thing in the string
            val songPattern = Pattern.compile("- \\*\\*Song:\\*\\* (.+?) by (.+?)\\s+\\*\\*Reasoning:\\*\\* (.+?)(?=\\n- \\*\\*Song:|\\Z)", Pattern.DOTALL)
            val songMatcher = songPattern.matcher(geminiResponse)

            while (songMatcher.find()) {
                val songTitle = songMatcher.group(1).trim()
                val artistNames = songMatcher.group(2).trim()
                val aiReasoning = songMatcher.group(3).trim()

                Log.d(tag, "Extracted Song: '$songTitle' by '$artistNames'. Reasoning: '$aiReasoning'")

                var trackId: String? = null
                var albumImageUrl: String? = null

                // IMPROVED SEARCH QUERY: Prioritize exact matches for song and artist
                val searchQuery = "track:\"$songTitle\" artist:\"$artistNames\""
                val searchResult = spotifyUserService.searchTracks(searchQuery, limit = 1) // Only fetch 1 exact match

                if (searchResult != null && searchResult.tracks?.items?.isNotEmpty() == true) {
                    val spotifyTrack = searchResult.tracks.items.first()
                    trackId = spotifyTrack.id
                    // Ensure you get the smallest image for better performance if multiple sizes exist
                    albumImageUrl = spotifyTrack.album.images?.minByOrNull { it.width ?: Int.MAX_VALUE }?.url
                    Log.d(tag, "Found Spotify Track: '${spotifyTrack.name}' by '${spotifyTrack.artists.firstOrNull()?.name}'. Image URL: $albumImageUrl")
                } else {
                    Log.w(tag, "No exact Spotify track found for '$songTitle' by '$artistNames'. Trying broader search...")
                    // Fallback to a broader search if exact match fails
                    val broadSearchQuery = "$songTitle $artistNames"
                    val broadSearchResult = spotifyUserService.searchTracks(broadSearchQuery, limit = 1)
                    if (broadSearchResult != null && broadSearchResult.tracks?.items?.isNotEmpty() == true) {
                        val spotifyTrack = broadSearchResult.tracks.items.first()
                        trackId = spotifyTrack.id
                        albumImageUrl = spotifyTrack.album.images?.minByOrNull { it.width ?: Int.MAX_VALUE }?.url
                        Log.d(tag, "Found Spotify Track with broader search: '${spotifyTrack.name}' by '${spotifyTrack.artists.firstOrNull()?.name}'. Image URL: $albumImageUrl")
                    } else {
                        Log.w(tag, "Still no Spotify track found for '$songTitle' by '$artistNames' after broader search.")
                    }
                }

                songSuggestions.add(
                    GeneratedSongSuggestion(
                        trackId = trackId,
                        trackName = songTitle,
                        artistNames = artistNames,
                        albumImageUrl = albumImageUrl,
                        aiDescription = aiReasoning
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Error parsing Gemini response or searching Spotify: ${e.message}", e)
            _error.value = "Failed to parse AI response or find some songs on Spotify. Check logs for details."
            return null // Return null or partially filled data on error
        }

        return AIGeneratedPlaylist(
            name = playlistName,
            description = playlistDescription,
            songs = songSuggestions
        )
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AIGenerationViewModel::class.java)) {
                val app = application as MusicExtendedApplication
                return AIGenerationViewModel(
                    app.topArtistRepository,
                    app.topTrackRepository,
                    app.spotifyUserService
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}