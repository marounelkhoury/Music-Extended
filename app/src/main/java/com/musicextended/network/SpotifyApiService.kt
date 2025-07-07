// app/src/main/java/com/musicextended/network/SpotifyApiService.kt
package com.musicextended.network

import com.musicextended.model.PlaylistResponse
import com.musicextended.model.SavedTracksResponse
import com.musicextended.network.SpotifyUserProfile
import com.musicextended.model.SpotifySearchResponse
import com.musicextended.model.TopArtistsResponse
import com.musicextended.model.TopTracksResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header // Keep this import if you have other methods using headers, but remove from these methods
import retrofit2.http.Query

interface SpotifyApiService {

    @GET("v1/me")
    suspend fun getCurrentUserProfile(): Response<SpotifyUserProfile> // Removed @Header

    @GET("v1/me/playlists")
    suspend fun getCurrentUserPlaylists(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = 20,    // Changed to Int
        @Query("offset") offset: Int = 0   // Changed to Int
    ): Response<PlaylistResponse> // Removed @Header

    @GET("v1/me/tracks")
    suspend fun getCurrentUserSavedTracks(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = 20,    // Changed to Int
        @Query("offset") offset: Int = 0   // Changed to Int
    ): Response<SavedTracksResponse> // Removed @Header

    @GET("v1/me/top/artists")
    suspend fun getUserTopArtists(
        @Header("Authorization") authHeader: String,
        @Query("time_range") timeRange: String = "medium_term",
        @Query("limit") limit: Int = 20,    // Changed to Int
        @Query("offset") offset: Int = 0   // Changed to Int
    ): Response<TopArtistsResponse> // Removed @Header

    @GET("v1/me/top/tracks")
    suspend fun getUserTopTracks(
        @Header("Authorization") authHeader: String,
        @Query("time_range") timeRange: String = "medium_term",
        @Query("limit") limit: Int = 20,    // Changed to Int
        @Query("offset") offset: Int = 0   // Changed to Int
    ): Response<TopTracksResponse> // Removed @Header

    @GET("v1/search")
    suspend fun search(
        @Header("Authorization") authHeader: String,
        @Query("q") query: String,
        @Query("type") type: String,
        @Query("limit") limit: Int = 1 // Changed to Int
    ): Response<SpotifySearchResponse> // Removed @Header
}