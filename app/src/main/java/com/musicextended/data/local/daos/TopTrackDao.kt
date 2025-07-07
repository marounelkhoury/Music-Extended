// app/src/main/java/com/musicextended/data/local/daos/TopTrackDao.kt
package com.musicextended.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.musicextended.data.local.entities.TopTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopTrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopTracks(tracks: List<TopTrackEntity>)

    @Query("SELECT * FROM top_tracks ORDER BY popularity DESC, name ASC")
    fun getAllTopTracks(): Flow<List<TopTrackEntity>>

    @Query("DELETE FROM top_tracks")
    suspend fun clearAllTopTracks()
}