// app/src/main/java/com/musicextended/data/repository/TopArtistRepository.kt
package com.musicextended.data.repository

import android.util.Log
import com.musicextended.data.local.daos.TopArtistDao
import com.musicextended.data.local.entities.TopArtistEntity
import com.musicextended.network.SpotifyUserService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class TopArtistRepository(
    private val topArtistDao: TopArtistDao,
    private val spotifyUserService: SpotifyUserService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val tag = "TopArtistRepository"

    fun getUserTopArtists(
        timeRange: String = "medium_term",
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<TopArtistEntity>> = flow {
        // 1. Emit cached data if available
        val cachedArtists = topArtistDao.getAllTopArtists().firstOrNull()
        if (!cachedArtists.isNullOrEmpty()) {
            Log.d(tag, "Emitting cached top artists.")
            emit(cachedArtists)
        } else {
            Log.d(tag, "No cached top artists found. Attempting network fetch.")
        }

        // 2. Always try to fetch from network
        try {
            val response = withContext(ioDispatcher) {
                // FIX: Explicitly pass parameters to fetchUserTopArtists
                spotifyUserService.fetchUserTopArtists(
                    timeRange = timeRange,
                    limit = limit,
                    offset = offset
                )
            }

            // FIX: Add safe call for 'items' in case response.items is null
            if (response != null && !response.items.isNullOrEmpty()) {
                val entities = response.items.map { artist -> TopArtistEntity.fromArtist(artist) }

                withContext(ioDispatcher) {
                    topArtistDao.clearAllTopArtists()
                    topArtistDao.insertTopArtists(entities)
                }

                Log.d(tag, "Fetched and saved ${entities.size} top artists from network.")
                emit(entities)
            } else {
                Log.e(tag, "Network fetch failed or returned empty list.")
                if (cachedArtists.isNullOrEmpty()) emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception during network fetch: ${e.message}", e)
            if (cachedArtists.isNullOrEmpty()) emit(emptyList())
        }
    }.flowOn(ioDispatcher)

    suspend fun clearTopArtistData() = withContext(ioDispatcher) {
        topArtistDao.clearAllTopArtists()
        Log.d(tag, "Cleared all top artist data from local database.")
    }
}