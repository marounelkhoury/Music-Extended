// app/src/main/java/com/musicextended/data/local/MusicExtendedDatabase.kt
package com.musicextended.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.musicextended.data.local.daos.PlaylistDao
import com.musicextended.data.local.daos.TopArtistDao
import com.musicextended.data.local.daos.TopTrackDao
import com.musicextended.data.local.daos.TrackDao
import com.musicextended.data.local.daos.UserDao
import com.musicextended.data.local.entities.PlaylistEntity
import com.musicextended.data.local.entities.TopArtistEntity
import com.musicextended.data.local.entities.TopTrackEntity
import com.musicextended.data.local.entities.TrackEntity
import com.musicextended.data.local.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PlaylistEntity::class,
        TrackEntity::class,
        TopArtistEntity::class,
        TopTrackEntity::class

    ],
    version = 2,
    exportSchema = true // Set to true to export schema for migrations (recommended for production)
)
abstract class MusicExtendedDatabase : RoomDatabase() {

    // Abstract methods to get your DAOs
    abstract fun userDao(): UserDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun trackDao(): TrackDao
    abstract fun topArtistDao(): TopArtistDao
    abstract fun topTrackDao(): TopTrackDao


    companion object {
        @Volatile // Make sure the INSTANCE is always up-to-date across threads
        private var INSTANCE: MusicExtendedDatabase? = null

        fun getDatabase(context: Context): MusicExtendedDatabase {

            return INSTANCE ?: synchronized(this) { // Use synchronized to ensure thread safety
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Use application context to prevent memory leaks
                    MusicExtendedDatabase::class.java,
                    "music_extended_database" // The name of your database file
                )

                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}