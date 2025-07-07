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

    // Companion object to provide a singleton instance of the database
    // This prevents multiple instances of the database from being opened simultaneously,
    // which can be expensive and lead to errors.
    companion object {
        @Volatile // Make sure the INSTANCE is always up-to-date across threads
        private var INSTANCE: MusicExtendedDatabase? = null

        fun getDatabase(context: Context): MusicExtendedDatabase {
            // If the INSTANCE is not null, then return it,
            // otherwise create a new instance of the database.
            return INSTANCE ?: synchronized(this) { // Use synchronized to ensure thread safety
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Use application context to prevent memory leaks
                    MusicExtendedDatabase::class.java,
                    "music_extended_database" // The name of your database file
                )
                    // .fallbackToDestructiveMigration() // Use this only for development if you frequently change schema and don't care about data loss
                    // Add your migration strategies here if you change the schema version later
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}