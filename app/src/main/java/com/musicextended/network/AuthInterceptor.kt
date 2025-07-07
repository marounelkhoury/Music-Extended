package com.musicextended.network

import android.util.Log
import com.musicextended.utils.AuthRepository // Corrected import path
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authRepository: AuthRepository) : Interceptor {

    private val tag = "AuthInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.url.encodedPath.contains("api/token") || originalRequest.header("Authorization") != null) {
            Log.d(tag, "Skipping AuthInterceptor for auth request or request with existing header.")
            return chain.proceed(originalRequest)
        }

        val accessToken = authRepository.getAccessToken()

        return if (accessToken != null) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
            Log.d(tag, "Adding access token to request: ${authenticatedRequest.url}")
            chain.proceed(authenticatedRequest)
        } else {
            Log.w(tag, "No access token available, proceeding without Authorization header for: ${originalRequest.url}")
            chain.proceed(originalRequest)
        }
    }
}