// app/src/main/java/com/musicextended/network/SpotifyApi.kt
package com.musicextended.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.musicextended.utils.AuthRepository
import android.util.Log

object SpotifyApi {
    // CRITICAL FIX: The actual Spotify Web API base URL
    private const val BASE_URL = "https://api.spotify.com/v1/" // Correct Spotify Web API base URL

    lateinit var service: SpotifyApiService
    private lateinit var authRepositoryInstance: AuthRepository

    fun init(authRepository: AuthRepository) {
        authRepositoryInstance = authRepository

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authRepositoryInstance))
            .authenticator(AuthAuthenticator(authRepositoryInstance))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL) // Use the correct Web API base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(SpotifyApiService::class.java)
        Log.d("SpotifyApi", "SpotifyApi initialized with base URL: $BASE_URL")
    }
}