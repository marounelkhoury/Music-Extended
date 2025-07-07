// app/src/main/java/com/musicextended/data/repository/UserRepository.kt
package com.musicextended.data.repository

import android.util.Log
import com.musicextended.data.local.daos.UserDao
import com.musicextended.data.local.entities.UserEntity
import com.musicextended.network.SpotifyUserService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull // Keep this import
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn // Import flowOn
import kotlinx.coroutines.withContext // Import withContext

class UserRepository(
    private val userDao: UserDao,
    private val spotifyUserService: SpotifyUserService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val tag = "UserRepository"

    /**
     * Fetches and provides the current user's profile, prioritizing local cache.
     * Always attempts to refresh from network to keep data fresh.
     *
     * @return A Flow that emits UserEntity? (null if not found/error).
     */
    fun getCurrentUserProfile(): Flow<UserEntity?> = flow {
        // 1. Emit cached user immediately if available
        // Use .firstOrNull() on the Flow directly to get the current cached value once.
        // It's assumed getAllUsers() returns Flow<List<UserEntity>> and you typically have only one user.
        val cachedUser = userDao.getAllUsers().firstOrNull()?.firstOrNull()
        if (cachedUser != null) {
            emit(cachedUser)
            Log.d(tag, "Emitting cached current user: ${cachedUser.display_name}")
        } else {
            Log.d(tag, "No cached user found initially.")
        }

        // 2. Always attempt to refresh from network
        try {
            Log.d(tag, "Attempting to fetch user profile from network...")
            val networkUser = withContext(ioDispatcher) { // Ensure network call is on IO dispatcher
                spotifyUserService.fetchUserProfile()
            }

            if (networkUser != null) {
                val userEntity = UserEntity.fromSpotifyUserProfile(networkUser)
                userDao.insertUser(userEntity) // Insert or replace based on primary key (id)
                Log.d(tag, "User profile fetched from network and cached: ${userEntity.display_name}")

                // 3. Emit the newly updated data from the database
                // Get the updated user from the DB after insertion to ensure consistency
                val updatedUser = userDao.getUserById(userEntity.id).firstOrNull()
                emit(updatedUser)
                Log.d(tag, "Emitted updated user from database: ${updatedUser?.display_name}")
            } else {
                Log.e(tag, "Failed to fetch current user profile from network: networkUser was null.")
                // If network fetch failed and there was no cached user, emit null.
                if (cachedUser == null) {
                    emit(null)
                    Log.d(tag, "No cached user and network fetch failed. Emitting null.")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error refreshing current user profile from network: ${e.message}")
            // If network fetch failed with an exception and there was no cached user, emit null.
            if (cachedUser == null) {
                emit(null)
                Log.d(tag, "Network fetch failed with exception and no cached user. Emitting null.")
            }
        }
    }.flowOn(ioDispatcher) // Ensure the entire flow's operations run on the IO dispatcher

    suspend fun clearUserData() = withContext(ioDispatcher) {
        userDao.clearAllUsers()
        Log.d(tag, "Local user data cleared from database.")
    }
}