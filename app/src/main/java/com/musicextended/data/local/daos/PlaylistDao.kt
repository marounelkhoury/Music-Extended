// app/src/main/java/com/musicextended/data/local/daos/PlaylistDao.kt
package com.musicextended.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.musicextended.data.local.entities.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPlaylists(playlists: List<PlaylistEntity>)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistById(playlistId: String): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> // Observe all playlists

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Query("DELETE FROM playlists")
    suspend fun deleteAllPlaylists()

    @Query("DELETE FROM playlists")
    suspend fun clearAllPlaylists()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<PlaylistEntity>)

}