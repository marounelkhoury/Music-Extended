package com.musicextended.network

import android.util.Log
import com.musicextended.utils.AuthRepository // Corrected import path
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AuthAuthenticator(
    private val authRepository: AuthRepository
) : Authenticator {

    private val tag = "AuthAuthenticator"

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(tag, "Authentication challenge received (401). Trying to refresh token for: ${response.request.url}")

        if (response.request.url.encodedPath.contains("api/token")) {
            Log.e(tag, "Refresh token request itself failed. Giving up on authentication.")
            authRepository.clearAllTokens()
            return null
        }

        val newAccessToken = runBlocking {
            authRepository.refreshAccessToken()
        }

        return if (newAccessToken != null) {
            Log.d(tag, "Token refreshed successfully. Retrying request with new token.")
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            Log.e(tag, "Failed to refresh token. User needs to re-authenticate.")
            null
        }
    }
}