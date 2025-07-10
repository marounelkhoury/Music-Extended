// app/src/main/java/com/musicextended/data/repository/TrackRepository.kt
package com.musicextended.data.repository

import android.util.Log
import com.musicextended.data.local.daos.TrackDao
import com.musicextended.data.local.entities.TrackEntity
import com.musicextended.network.SpotifyUserService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class TrackRepository(
    private val trackDao: TrackDao,
    private val spotifyUserService: SpotifyUserService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val tag = "TrackRepository"

    /**
     * Fetches and provides the current user's saved tracks, prioritizing local cache.
     * Always attempts to refresh from network to keep data fresh.
     *
     * @return A Flow that emits a List of TrackEntity.
     */
    fun getCurrentUserSavedTracks(
        limit: Int = 50, // Default to a reasonable limit
        offset: Int = 0
    ): Flow<List<TrackEntity>> = flow {
        // 1. Emit cached tracks immediately if available
        val cachedTracks = trackDao.getAllTracks().firstOrNull()
        if (!cachedTracks.isNullOrEmpty()) {
            emit(cachedTracks)
            Log.d(tag, "Emitting ${cachedTracks.size} cached saved tracks.")
        }

        // 2. Always attempt to refresh from network
        try {
            val networkTracks = withContext(ioDispatcher) {
                spotifyUserService.fetchCurrentUserSavedTracks(limit = limit, offset = offset)
            }

            if (!networkTracks.isNullOrEmpty()) { // Check if the list of tracks is not null or empty
                val trackEntities = networkTracks.map { track -> // Now 'track' is directly a Track object
                    TrackEntity.fromTrack(track) // Use fromTrack directly
                }
                // Clear existing tracks before inserting new ones to ensure data consistency
                trackDao.clearAllTracks()
                trackDao.insertTracks(trackEntities)
                Log.d(tag, "Fetched ${trackEntities.size} saved tracks from network and cached them.")

                // 3. Emit the newly updated data from the database
                emit(trackDao.getAllTracks().firstOrNull() ?: emptyList())
            } else {
                Log.e(tag, "Failed to fetch saved tracks from network or response was empty.")
                // If network fetch failed and there was no cache, ensure empty list is emitted
                if (cachedTracks.isNullOrEmpty()) {
                    emit(emptyList())
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error refreshing saved tracks from network: ${e.message}", e)
            // If network fetch failed due to exception and there was no cache, emit empty list
            if (cachedTracks.isNullOrEmpty()) {
                emit(emptyList())
            }
        }
    }.flowOn(ioDispatcher)

    /**
     * Clears all saved track data from the local database.
     */
    suspend fun clearSavedTrackData() = withContext(ioDispatcher) {
        trackDao.clearAllTracks()
        Log.d(tag, "Cleared all saved track data from local database.")
    }
}