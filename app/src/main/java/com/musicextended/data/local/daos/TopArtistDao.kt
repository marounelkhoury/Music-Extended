// app/src/main/java/com/musicextended/data/local/daos/TopArtistDao.kt
package com.musicextended.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.musicextended.data.local.entities.TopArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopArtists(artists: List<TopArtistEntity>)

    @Query("SELECT * FROM top_artists ORDER BY popularity DESC, name ASC")
    fun getAllTopArtists(): Flow<List<TopArtistEntity>>

    @Query("DELETE FROM top_artists")
    suspend fun clearAllTopArtists()
}