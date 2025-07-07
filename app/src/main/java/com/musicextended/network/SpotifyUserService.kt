// app/src/main/java/com/musicextended/network/SpotifyUserService.kt
package com.musicextended.network

import android.util.Log
import com.musicextended.model.PlaylistResponse
import com.musicextended.model.SavedTracksResponse
import com.musicextended.network.SpotifyUserProfile
import com.musicextended.model.SpotifySearchResponse
import com.musicextended.model.TopArtistsResponse
import com.musicextended.model.TopTracksResponse
import com.musicextended.model.Track // Import Track model
import com.musicextended.utils.AuthRepository

class SpotifyUserService(
    private val authRepository: AuthRepository,
    private val spotifyApiService: SpotifyApiService
) {
    private val tag = "SpotifyUserService"

    suspend fun fetchUserProfile(): SpotifyUserProfile? {
        Log.d(tag, "Fetching user profile...")
        return try {
            val response = spotifyApiService.getCurrentUserProfile()
            if (response.isSuccessful) {
                Log.d(tag, "Successfully fetched user profile.")
                response.body()
            } else {
                Log.e(tag, "Failed to fetch user profile: ${response.code()} ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception fetching user profile", e)
            null
        }
    }

    suspend fun fetchCurrentUserPlaylists(limit: Int = 20, offset: Int = 0): PlaylistResponse? {
        Log.d(tag, "Fetching current user playlists from Spotify API...")
        return try {
            val accessToken = authRepository.getAccessToken()
            if (accessToken == null) {
                Log.e(tag, "Access token is null. Cannot fetch playlists.")
                return null
            }

            val response = spotifyApiService.getCurrentUserPlaylists(
                authHeader = "Bearer $accessToken",
                limit = limit,
                offset = offset
            )

            if (response.isSuccessful) {
                Log.d(tag, "Successfully fetched playlists.")
                response.body()
            } else {
                Log.e(tag, "Failed to fetch playlists. Code: ${response.code()}, message: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception while fetching playlists: ${e.message}", e)
            null
        }
    }

    // FIX: Change return type to List<Track>? and unwrap SavedTrackItem
//    suspend fun fetchCurrentUserSavedTracks(limit: Int, offset: Int): List<Track>? {
//        Log.d(tag, "Fetching user's saved tracks...")
//        return try {
//            val accessToken = authRepository.getAccessToken()
//            if (accessToken == null) {
//                Log.e(tag, "Access token is null. Cannot fetch saved tracks.")
//                return null
//            }
//
//            val response = spotifyApiService.getCurrentUserSavedTracks(
//                authHeader = "Bearer $accessToken",
//                limit = limit,
//                offset = offset
//            )
//
//            if (response.isSuccessful) {
//                Log.d(tag, "Fetched saved tracks successfully.")
//                // Extract the nested 'Track' objects from 'SavedTrackItem'
//                response.body()?.items?.map { it.track } // This line unwraps SavedTrackItem to Track
//            } else {
//                Log.e(tag, "Failed to fetch saved tracks. Code: ${response.code()}, message: ${response.message()}")
//                null
//            }
//        } catch (e: Exception) {
//            Log.e(tag, "Exception while fetching saved tracks: ${e.message}", e)
//            null
//        }
//    }

    suspend fun fetchUserTopArtists(timeRange: String, limit: Int, offset: Int): TopArtistsResponse? {
        Log.d(tag, "Fetching user's top artists...")
        return try {
            val accessToken = authRepository.getAccessToken()
            if (accessToken == null) {
                Log.e(tag, "Access token is null. Cannot fetch top artists.")
                return null
            }

            val response = spotifyApiService.getUserTopArtists(
                authHeader = "Bearer $accessToken",
                timeRange = timeRange,
                limit = limit,
                offset = offset
            )

            if (response.isSuccessful) {
                Log.d(tag, "Fetched top artists successfully.")
                response.body()
            } else {
                Log.e(tag, "Failed to fetch top artists. Code: ${response.code()}, message: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception while fetching top artists: ${e.message}", e)
            null
        }
    }

    suspend fun fetchUserTopTracks(timeRange: String, limit: Int, offset: Int): TopTracksResponse? {
        Log.d(tag, "Fetching user's top tracks...")
        return try {
            val accessToken = authRepository.getAccessToken()
            if (accessToken == null) {
                Log.e(tag, "Access token is null. Cannot fetch top tracks.")
                return null
            }

            val response = spotifyApiService.getUserTopTracks(
                authHeader = "Bearer $accessToken",
                timeRange = timeRange,
                limit = limit,
                offset = offset
            )

            if (response.isSuccessful) {
                Log.d(tag, "Fetched top tracks successfully.")
                response.body()
            } else {
                Log.e(tag, "Failed to fetch top tracks. Code: ${response.code()}, message: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception while fetching top tracks: ${e.message}", e)
            null
        }
    }

    suspend fun searchTracks(query: String, limit: Int = 1): SpotifySearchResponse? {
        Log.d(tag, "Searching for tracks with query: '$query'...")
        return try {
            val accessToken = authRepository.getAccessToken() // Assuming search also needs auth
            if (accessToken == null) {
                Log.e(tag, "Access token is null. Cannot search tracks.")
                return null
            }
            val response = spotifyApiService.search(
                authHeader = "Bearer $accessToken", // Assuming search needs authHeader
                query = query,
                type = "track",
                limit = limit
            )
            if (response.isSuccessful) {
                Log.d(tag, "Successfully searched for tracks.")
                response.body()
            } else {
                Log.e(tag, "Failed to search tracks: ${response.code()} ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception searching for tracks", e)
            null
        }
    }

    suspend fun fetchCurrentUserSavedTracks(limit: Int, offset: Int): List<Track>? {
        Log.d(tag, "Fetching user's saved tracks...")
        return try {
            val accessToken = authRepository.getAccessToken()
            if (accessToken == null) {
                Log.e(tag, "Access token is null. Cannot fetch saved tracks.")
                return null
            }

            val response = spotifyApiService.getCurrentUserSavedTracks(
                authHeader = "Bearer $accessToken",
                limit = limit,
                offset = offset
            )

            if (response.isSuccessful) {
                Log.d(tag, "Fetched saved tracks successfully.")
                response.body()?.items?.map { it.track } // This line unwraps SavedTrackItem to Track
            } else {
                // Add logging for saved tracks as well, for consistency
                Log.e(tag, "Failed to fetch saved tracks. HTTP Code: ${response.code()}, Message: ${response.message()}, Error Body: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception while fetching saved tracks: ${e.message}", e)
            null
        }
    }
}