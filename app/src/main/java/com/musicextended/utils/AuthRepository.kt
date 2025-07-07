// app/src/main/java/com/musicextended/utils/AuthRepository.kt
package com.musicextended.utils

import android.content.Context
import android.util.Log
import com.musicextended.model.SpotifyAuthService
import com.musicextended.model.RefreshTokenResponse // Corrected import based on your project structure
import com.musicextended.model.AuthResponse // Your custom AuthResponse model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

// AppAuth specific imports
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import net.openid.appauth.AuthorizationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AuthRepository(
    private val context: Context,
    private val spotifyAuthService: SpotifyAuthService,
    private val tokenManager: TokenManager
) {
    private val tag = "AuthRepository"

    private val tokenRefreshMutex = Mutex()

    // In-memory cache for the current access token and its absolute expiry timestamp
    private var currentAccessToken: String? = null
    private var accessTokenExpiryTimeMillis: Long = 0L

    init {
        // Initialize from TokenManager on startup
        currentAccessToken = tokenManager.getAccessToken()
        // IMPORTANT: Ensure TokenManager.getAccessTokenExpiryTime() returns the ABSOLUTE MILLISECONDS timestamp
        // at which the token expires. This is crucial for correct expiry checks.
        accessTokenExpiryTimeMillis = tokenManager.getAccessTokenExpiryTime()
        Log.d(tag, "AuthRepository initialized. Current token: ${currentAccessToken?.take(5)}... Expires: ${java.util.Date(accessTokenExpiryTimeMillis)}")
    }

    /**
     * Provides the currently available access token. This token might be expired.
     * The AuthAuthenticator will decide if it needs refreshing.
     */
    fun getAccessToken(): String? {
        Log.d(tag, "Providing current access token (might be expired): ${currentAccessToken?.take(5)}...")
        return currentAccessToken
    }

    fun getRefreshToken(): String? {
        val token = tokenManager.getRefreshToken()
        Log.d(tag, "Providing refresh token: ${token?.take(5)}...")
        return token
    }

    /**
     * Checks if the currently held access token is expired or about to expire.
     * This relies on `accessTokenExpiryTimeMillis` being correctly set during `saveTokens` or `refreshAccessToken`.
     */
    fun isAccessTokenExpired(): Boolean {
        // Consider a small buffer (e.g., 60 seconds) to refresh proactively.
        // Constants.TOKEN_REFRESH_BUFFER_SECONDS must be defined in Constants.kt as a Long.
        val isExpired = currentAccessToken == null ||
                accessTokenExpiryTimeMillis <= System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Constants.TOKEN_REFRESH_BUFFER_SECONDS)
        Log.d(tag, "Is access token expired (or expiring soon)? $isExpired. Current expiry: ${java.util.Date(accessTokenExpiryTimeMillis)}")
        return isExpired
    }

    /**
     * Performs the actual network request to refresh the access token using the refresh token.
     * This method is intended to be called by the Authenticator.
     * It uses a Mutex to ensure only one refresh operation happens at a time.
     */
    suspend fun refreshAccessToken(): String? = tokenRefreshMutex.withLock {
        Log.d(tag, "Attempting to refresh access token...")
        val refreshToken = getRefreshToken()

        if (refreshToken == null) {
            Log.e(tag, "Refresh token not found. Cannot refresh access token.")
            clearAllTokens() // Force re-login if refresh token is missing
            return@withLock null
        }

        try {
            val response = spotifyAuthService.refreshAccessToken(
                refreshToken = refreshToken,
                clientId = Constants.SPOTIFY_CLIENT_ID,    // Must be defined in Constants.kt
                clientSecret = Constants.CLIENT_SECRET // Must be defined in Constants.kt
            )
            if (response.isSuccessful) {
                val tokenResponse = response.body()
                if (tokenResponse != null) {
                    currentAccessToken = tokenResponse.accessToken
                    // Calculate absolute expiry time based on duration from Spotify API (in seconds)
                    // ASSUMING RefreshTokenResponse.expiresIn is Int, convert to Long
                    accessTokenExpiryTimeMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(tokenResponse.expiresIn.toLong())

                    // Save the new tokens using TokenManager's saveTokens method
                    tokenManager.saveTokens(
                        accessToken = currentAccessToken,
                        refreshToken = tokenManager.getRefreshToken(), // Keep existing refresh token if not provided
                        // ASSUMING RefreshTokenResponse.expiresIn is Int, convert to Long
                        expiresIn = tokenResponse.expiresIn.toLong(),
                        tokenType = tokenResponse.tokenType
                    )
                    // Also save the calculated absolute expiry time in TokenManager
                    tokenManager.saveAccessTokenExpiryTime(accessTokenExpiryTimeMillis)

                    Log.d(tag, "Access token refreshed successfully. New token expires at: ${java.util.Date(accessTokenExpiryTimeMillis)}")
                    return@withLock currentAccessToken
                } else {
                    Log.e(tag, "Refresh token response body is null.")
                    return@withLock null
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(tag, "Failed to refresh access token. Code: ${response.code()}, Error: $errorBody")
                if (response.code() == 400 && errorBody?.contains("invalid_grant") == true) {
                    Log.e(tag, "Refresh token is invalid or revoked. User needs to re-login.")
                    clearAllTokens()
                }
                return@withLock null
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception during token refresh network call: ${e.message}", e)
            return@withLock null
        }
    }

    /**
     * Performs the initial token exchange after authorization.
     * This method uses AppAuth's AuthorizationService to make the token request.
     * It saves the tokens and returns your custom AuthResponse.
     */
    suspend fun exchangeToken(tokenRequest: TokenRequest): AuthResponse? {
        Log.d(tag, "AuthRepository: Performing initial token exchange.")
        val authService = AuthorizationService(context)

        return try {
            val tokenResponse = suspendCancellableCoroutine<TokenResponse?> { continuation ->
                authService.performTokenRequest(tokenRequest) { response, ex ->
                    if (ex != null) {
                        Log.e(tag, "Token exchange failed: ${ex.message}", ex)
                        continuation.resume(null)
                    } else if (response != null) {
                        continuation.resume(response)
                    } else {
                        Log.e(tag, "Token exchange failed: No response and no exception.")
                        continuation.resume(null)
                    }
                }
            }

            if (tokenResponse != null) {
                // Extract expires_in from additionalParameters
                val expiresInStr = tokenResponse.additionalParameters["expires_in"]
                val expiresInSeconds = expiresInStr?.toLongOrNull() ?: 0L
                val accessTokenExpiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresInSeconds)

                // Save tokens
                tokenManager.saveTokens(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken,
                    expiresIn = expiresInSeconds,
                    tokenType = tokenResponse.tokenType
                )
                tokenManager.saveAccessTokenExpiryTime(accessTokenExpiryTime)

                currentAccessToken = tokenResponse.accessToken
                accessTokenExpiryTimeMillis = accessTokenExpiryTime

                Log.d(tag, "Initial token exchange successful. Access token expires at: ${java.util.Date(accessTokenExpiryTimeMillis)}")

                return AuthResponse(
                    accessToken = tokenResponse.accessToken ?: "",
                    tokenType = tokenResponse.tokenType,
                    expiresIn = expiresInSeconds,
                    refreshToken = tokenResponse.refreshToken,
                    scope = tokenResponse.scope
                )
            } else {
                Log.e(tag, "Token exchange response was null.")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception during token exchange: ${e.message}", e)
            null
        } finally {
            authService.dispose()
        }
    }


    /**
     * Saves tokens obtained from a source other than the AppAuth exchange (e.g., direct API call).
     * This method is redundant if `exchangeToken` is the primary way tokens are obtained initially.
     * Consider removing this if `exchangeToken` is always used for initial token acquisition.
     */
    fun saveTokens(accessToken: String, refreshToken: String?, expiresIn: Long, tokenType: String?) {
        currentAccessToken = accessToken
        // Calculate and save absolute expiry time based on duration (in seconds)
        accessTokenExpiryTimeMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn)
        tokenManager.saveAccessTokenExpiryTime(accessTokenExpiryTimeMillis) // Save absolute expiry in TokenManager

        // Use TokenManager's saveTokens method for other token details
        tokenManager.saveTokens(accessToken, refreshToken, expiresIn, tokenType)
        Log.d(tag, "Tokens saved from login. Access token expires at: ${java.util.Date(accessTokenExpiryTimeMillis)}")
    }

    /**
     * Clears all stored tokens, forcing a re-login.
     * This method is called from HomeViewModel (logout) and AuthAuthenticator (refresh failure).
     */
    fun clearAllTokens() {
        tokenManager.clearAuthData() // Uses TokenManager's clearAuthData method
        currentAccessToken = null
        accessTokenExpiryTimeMillis = 0L
        Log.d(tag, "All Spotify tokens cleared.")
    }

    /**
     * Checks if the user is currently authenticated (has an access token).
     */
    fun isAuthenticated(): Boolean {
        return tokenManager.getAccessToken() != null
    }
}