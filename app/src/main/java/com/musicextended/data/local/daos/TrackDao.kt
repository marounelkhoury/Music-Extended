// app/src/main/java/com/musicextended/data/local/daos/TrackDao.kt
package com.musicextended.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.musicextended.data.local.entities.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTracks(tracks: List<TrackEntity>)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    fun getTrackById(trackId: String): Flow<TrackEntity?>

    @Query("SELECT * FROM tracks ORDER BY name ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrack(trackId: String)

    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks")
    suspend fun clearAllTracks()
}