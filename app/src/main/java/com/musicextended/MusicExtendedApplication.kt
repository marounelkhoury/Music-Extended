// app/src/main/java/com/musicextended/MusicExtendedApplication.kt
package com.musicextended

import android.app.Application
import com.musicextended.data.data.repository.TopTrackRepository
import com.musicextended.data.local.MusicExtendedDatabase
import com.musicextended.data.repository.PlaylistRepository
import com.musicextended.data.repository.TopArtistRepository
import com.musicextended.data.repository.TrackRepository
import com.musicextended.data.repository.UserRepository
import com.musicextended.network.AuthAuthenticator // Add this import
import com.musicextended.network.AuthInterceptor // Add this import
import com.musicextended.network.SpotifyApiService
import com.musicextended.network.SpotifyUserService
import com.musicextended.model.SpotifyAuthService // This import is correct, but ensure the class name matches

import com.musicextended.utils.AuthRepository
import com.musicextended.utils.Constants
import com.musicextended.utils.TokenManager
import net.openid.appauth.AuthorizationService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MusicExtendedApplication : Application() {

    lateinit var authRepository: AuthRepository
        private set

    lateinit var spotifyUserService: SpotifyUserService
        private set

    lateinit var database: MusicExtendedDatabase
        private set
    lateinit var userDao: com.musicextended.data.local.daos.UserDao
        private set
    lateinit var playlistDao: com.musicextended.data.local.daos.PlaylistDao
        private set
    lateinit var trackDao: com.musicextended.data.local.daos.TrackDao
        private set
    lateinit var topArtistDao: com.musicextended.data.local.daos.TopArtistDao
        private set
    lateinit var topTrackDao: com.musicextended.data.local.daos.TopTrackDao
        private set

    lateinit var userRepository: UserRepository
        private set
    lateinit var playlistRepository: PlaylistRepository
        private set
    lateinit var trackRepository: TrackRepository
        private set

    lateinit var topArtistRepository: TopArtistRepository
        private set
    lateinit var topTrackRepository: TopTrackRepository
        private set

    // Declare the Retrofit SpotifyAuthService here
    private lateinit var spotifyAuthServiceRetrofit: SpotifyAuthService
    // And if you want the AppAuth AuthorizationService accessible, declare it here too
    private lateinit var appAuthAuthorizationService: AuthorizationService


    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Http Logging Interceptor (can be shared)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Set to BODY for full logs
        }

        // 2. Build OkHttpClient for SpotifyAuthService (token refresh)
        // This client should NOT include AuthInterceptor or AuthAuthenticator
        val authOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // 3. Initialize SpotifyAuthService Retrofit instance
        // This uses the authOkHttpClient and points to the Spotify Accounts API (for /api/token)
        spotifyAuthServiceRetrofit = Retrofit.Builder()
            .baseUrl(Constants.SPOTIFY_ACCOUNTS_BASE_URL) // Make sure this constant is defined
            .client(authOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyAuthService::class.java)

        // 4. Initialize TokenManager (needed for AuthRepository)
        val tokenManager = TokenManager(applicationContext)

        // 5. Initialize AuthRepository, passing the *correct* spotifyAuthService
        authRepository = AuthRepository(applicationContext, spotifyAuthServiceRetrofit, tokenManager)

        // 6. Initialize AuthInterceptor and AuthAuthenticator (depend on AuthRepository)
        val authInterceptor = AuthInterceptor(authRepository)
        val authAuthenticator = AuthAuthenticator(authRepository)

        // 7. Build OkHttpClient for main Spotify Web API calls (with auth interceptor/authenticator)
        val authenticatedOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(authAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // 8. Initialize Retrofit for main Spotify Web API
        val mainApiRetrofit = Retrofit.Builder()
            .baseUrl(Constants.SPOTIFY_API_BASE_URL) // Main Spotify Web API base URL
            .client(authenticatedOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // 9. Initialize SpotifyApiService and SpotifyUserService using the main API Retrofit
        val spotifyApiService = mainApiRetrofit.create(SpotifyApiService::class.java)
        spotifyUserService = SpotifyUserService(authRepository, spotifyApiService)
        //spotifyUserService = mainApiRetrofit.create(SpotifyUserService::class.java)


        // 10. Initialize AppAuth's AuthorizationService (if you use it for the login flow)
        appAuthAuthorizationService = AuthorizationService(this)


        // 11. Initialize Room Database and DAOs
        database = MusicExtendedDatabase.getDatabase(this)
        userDao = database.userDao()
        playlistDao = database.playlistDao()
        trackDao = database.trackDao()
        topArtistDao = database.topArtistDao()
        topTrackDao = database.topTrackDao()

        // 12. Initialize Repositories using DAOs and SpotifyUserService/SpotifyApiService
        userRepository = UserRepository(userDao, spotifyUserService)
        playlistRepository = PlaylistRepository(playlistDao, spotifyUserService) // Make sure PlaylistRepository accepts SpotifyUserService
        trackRepository = TrackRepository(trackDao, spotifyUserService) // Make sure TrackRepository accepts SpotifyUserService
        topArtistRepository = TopArtistRepository(topArtistDao, spotifyUserService) // Use spotifyApiService if TopArtist calls are through it
        topTrackRepository = TopTrackRepository(topTrackDao, spotifyUserService)   // Use spotifyApiService if TopTrack calls are through it
    }

    // Don't forget to dispose of AppAuth's AuthorizationService when the application terminates
    override fun onTerminate() {
        super.onTerminate()
        if (::appAuthAuthorizationService.isInitialized) {
            appAuthAuthorizationService.dispose()
        }
    }
}