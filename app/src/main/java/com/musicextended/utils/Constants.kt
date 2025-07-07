package com.musicextended.utils

import android.net.Uri
import com.musicextended.BuildConfig

// Extension to convert String to Uri
fun String.toUri(): Uri = Uri.parse(this)

object Constants {

    // Spotify API keys from BuildConfig (injected from build.gradle.kts)
    val SPOTIFY_CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
    val CLIENT_SECRET = BuildConfig.CLIENT_SECRET

    const val REDIRECT_URI = "com.musicextended.spotify://callback"
    const val SPOTIFY_AUTH_BASE_URL = "https://accounts.spotify.com/"

    const val TOKEN_REFRESH_BUFFER_SECONDS = 60L

    const val SPOTIFY_REDIRECT_URI_SCHEME = "com.musicextended"
    const val SPOTIFY_REDIRECT_URI_HOST = "callback"
    val SPOTIFY_REDIRECT_URI: Uri = "$SPOTIFY_REDIRECT_URI_SCHEME://$SPOTIFY_REDIRECT_URI_HOST".toUri()

    val SPOTIFY_AUTHORIZATION_ENDPOINT: Uri = "https://accounts.spotify.com/authorize".toUri()
    val SPOTIFY_TOKEN_ENDPOINT: Uri = "https://accounts.spotify.com/api/token".toUri()
    const val SPOTIFY_API_BASE_URL = "https://api.spotify.com"
    const val SPOTIFY_ACCOUNTS_BASE_URL = "https://accounts.spotify.com/"

    val SPOTIFY_END_SESSION_ENDPOINT: Uri? = null

    const val SPOTIFY_SCOPE =
        "user-read-private user-read-email user-library-read playlist-read-private playlist-read-collaborative user-top-read user-read-recently-played streaming app-remote-control"

    // Placeholder Apple Music & Anghami keys (replace when ready)
    const val APPLE_MUSIC_CLIENT_ID = "YOUR_APPLE_MUSIC_CLIENT_ID"
    const val APPLE_MUSIC_REDIRECT_URI = "com.musicextended://applemusic/callback"

    const val ANGHAMI_CLIENT_ID = "YOUR_ANGHAMI_CLIENT_ID"
    const val ANGHAMI_REDIRECT_URI = "com.musicextended://anghami/callback"

    val GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY

    const val AUTH_REQUEST_CODE = 1001
    const val SHARED_PREFS_NAME = "MusicExtendedPrefs"
    const val PREF_ACCESS_TOKEN = "access_token"
    const val PREF_REFRESH_TOKEN = "refresh_token"
    const val PREF_TOKEN_EXPIRY = "token_expiry"
    const val PREF_SERVICE_PROVIDER = "service_provider"

    enum class ServiceProvider {
        NONE, SPOTIFY, APPLE_MUSIC, ANGHAMI
    }

    const val ROUTE_AUTH = "auth_screen"
    const val ROUTE_HOME = "home_screen"
    const val ROUTE_PLAYLISTS = "playlists_screen"
    const val ROUTE_SAVED_TRACKS = "saved_tracks_screen"
    const val ROUTE_AI_GENERATION = "ai_generation_screen"
}
