// app/src/main/java/com/musicextended/data/data/repository/TopTrackRepository.kt
package com.musicextended.data.data.repository // Note: This package path seems to have 'data.data'. Confirm if intended.

import android.util.Log
import com.musicextended.data.local.daos.TopTrackDao
import com.musicextended.data.local.entities.TopTrackEntity
import com.musicextended.network.SpotifyUserService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class TopTrackRepository(
    private val topTrackDao: TopTrackDao,
    private val spotifyUserService: SpotifyUserService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val tag = "TopTrackRepository"

    fun getUserTopTracks(
        timeRange: String = "medium_term",
        limit: Int = 50, // Get a good number for AI processing
        offset: Int = 0
    ): Flow<List<TopTrackEntity>> = flow {
        // Emit cached data immediately if available
        val cachedTracks = topTrackDao.getAllTopTracks().firstOrNull()
        if (!cachedTracks.isNullOrEmpty()) {
            Log.d(tag, "Emitting cached top tracks.")
            emit(cachedTracks)
        } else {
            Log.d(tag, "No cached top tracks. Fetching from network.")
        }

        // Always attempt to fetch fresh data from the network
        withContext(ioDispatcher) {
            try {
                val response = spotifyUserService.fetchUserTopTracks(
                    timeRange = timeRange,
                    limit = limit,
                    offset = offset
                )
                if (response != null && !response.items.isNullOrEmpty()) {
                    val entities = response.items.map { track ->
                        TopTrackEntity.fromTrack(track)
                    }
                    topTrackDao.clearAllTopTracks() // Clear old data
                    topTrackDao.insertTopTracks(entities) // Insert new data
                    Log.d(tag, "Fetched and saved ${entities.size} top tracks from network.")
                    emit(entities) // Emit the newly fetched and saved data
                } else {
                    Log.e(tag, "Failed to fetch top tracks from network. Response was null or empty.")
                    // If network fetch fails, and there was no cache, emit empty list or handle error appropriately
                    if (cachedTracks.isNullOrEmpty()) emit(emptyList())
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception fetching top tracks from network: ${e.message}", e)
                // If an exception occurs, and there was no cache, emit empty list or handle error
                if (cachedTracks.isNullOrEmpty()) emit(emptyList())
            }
        }
    }.flowOn(ioDispatcher)

    suspend fun clearTopTrackData() = withContext(ioDispatcher) {
        topTrackDao.clearAllTopTracks()
        Log.d(tag, "Cleared all top track data from local database.")
    }
}