// app/src/main/java/com/musicextended/data/repository/PlaylistRepository.kt
package com.musicextended.data.repository

import android.util.Log
import com.musicextended.data.local.daos.PlaylistDao
import com.musicextended.data.local.entities.PlaylistEntity
import com.musicextended.network.SpotifyUserService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class PlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val spotifyUserService: SpotifyUserService
) {
    private val tag = "PlaylistRepository"

    /**
     * Fetches and provides the current user's playlists, prioritizing local cache.
     * Always attempts to refresh from network to keep data fresh.
     *
     * @return A Flow that emits a List of PlaylistEntity.
     */
    fun getCurrentUserPlaylists(): Flow<List<PlaylistEntity>> = flow {
        // 1. Emit cached playlists immediately if available
        val cachedPlaylists = playlistDao.getAllPlaylists().firstOrNull()
        if (!cachedPlaylists.isNullOrEmpty()) {
            emit(cachedPlaylists)
            Log.d(tag, "Emitting ${cachedPlaylists.size} cached playlists.")
        }

        // 2. Always attempt to refresh from network
        try {
            // FIX: Explicitly pass limit and offset
            val networkResponse = spotifyUserService.fetchCurrentUserPlaylists(limit = 20, offset = 0)
            if (networkResponse != null && networkResponse.items.isNotEmpty()) {
                val playlistEntities = networkResponse.items.map { simplifiedPlaylist ->
                    PlaylistEntity.fromSimplifiedPlaylist(simplifiedPlaylist)
                }
                // Clear existing playlists before inserting new ones to ensure data consistency
                playlistDao.clearAllPlaylists()
                playlistDao.insertPlaylists(playlistEntities)
                Log.d(tag, "Fetched ${playlistEntities.size} playlists from network and cached them.")

                // 3. Emit the newly updated data from the database
                emit(playlistDao.getAllPlaylists().firstOrNull() ?: emptyList())
            } else {
                Log.e(tag, "Failed to fetch playlists from network or response was empty.")
                // If network fetch failed and there was no cache, ensure empty list is emitted
                if (cachedPlaylists.isNullOrEmpty()) {
                    emit(emptyList())
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error refreshing playlists from network: ${e.message}")
            // If network fetch failed due to exception and there was no cache, emit empty list
            if (cachedPlaylists.isNullOrEmpty()) {
                emit(emptyList())
            }
        }
    }
}