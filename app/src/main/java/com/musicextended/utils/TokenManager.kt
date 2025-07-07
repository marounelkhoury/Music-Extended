// app/src/main/java/com/musicextended/utils/TokenManager.kt
package com.musicextended.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.openid.appauth.TokenResponse
import org.json.JSONException
import java.util.concurrent.TimeUnit

class TokenManager(context: Context) {

    private val tag = "TokenManager"

    // MasterKey for encrypting preferences
    private val masterKeyAlias = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // EncryptedSharedPreferences for secure storage
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "spotify_auth_prefs",
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val ACCESS_TOKEN_KEY = "access_token"
    private val REFRESH_TOKEN_KEY = "refresh_token"
    private val EXPIRES_IN_KEY = "expires_in" // Duration in seconds (from token response)
    private val TOKEN_TYPE_KEY = "token_type"
    private val LAST_TOKEN_RESPONSE_JSON_KEY = "last_token_response_json"
    private val ACCESS_TOKEN_EXPIRY_ABS_TIME_KEY = "access_token_expiry_abs_time" // <--- NEW KEY

    init {
        Log.d(tag, "TokenManager initialized.")
    }

    /**
     * Saves all token-related data, including calculating and storing the absolute expiry time.
     * @param accessToken The access token.
     * @param refreshToken The refresh token.
     * @param expiresIn The duration (in seconds) until the access token expires.
     * @param tokenType The type of token (e.g., "Bearer").
     */
    fun saveTokens(
        accessToken: String?,
        refreshToken: String?,
        expiresIn: Long?, // This is the duration in seconds
        tokenType: String?
    ) {
        sharedPreferences.edit().apply {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
            expiresIn?.let { putLong(EXPIRES_IN_KEY, it) } ?: remove(EXPIRES_IN_KEY)
            putString(TOKEN_TYPE_KEY, tokenType)

            // Calculate and save the absolute expiry timestamp
            if (accessToken != null && expiresIn != null) {
                val absoluteExpiryMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn)
                putLong(ACCESS_TOKEN_EXPIRY_ABS_TIME_KEY, absoluteExpiryMillis)
                Log.d(tag, "Calculated and saved absolute expiry: ${java.util.Date(absoluteExpiryMillis)}")
            } else {
                // If no access token, clear the expiry time
                remove(ACCESS_TOKEN_EXPIRY_ABS_TIME_KEY)
                Log.d(tag, "No access token, cleared absolute expiry.")
            }
            apply()
        }
        Log.d(tag, "Tokens saved: AccessToken: ${accessToken?.take(5)}... RefreshToken: ${refreshToken?.take(5)}...")
    }

    /**
     * Explicitly saves the absolute access token expiry time. Used when AppAuth provides it directly.
     * @param expiryTimeMillis The absolute timestamp (in milliseconds) when the token expires.
     */
    fun saveAccessTokenExpiryTime(expiryTimeMillis: Long) {
        sharedPreferences.edit().putLong(ACCESS_TOKEN_EXPIRY_ABS_TIME_KEY, expiryTimeMillis).apply()
        Log.d(tag, "Explicitly saved absolute expiry: ${java.util.Date(expiryTimeMillis)}")
    }


    fun saveLastTokenResponse(tokenResponse: TokenResponse?) {
        val json = tokenResponse?.jsonSerializeString()
        sharedPreferences.edit().putString(LAST_TOKEN_RESPONSE_JSON_KEY, json).apply()
        Log.d(tag, "Last token response JSON saved.")
    }

    fun getAccessToken(): String? = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)

    fun getRefreshToken(): String? = sharedPreferences.getString(REFRESH_TOKEN_KEY, null)

    fun getExpiresIn(): Long? = if (sharedPreferences.contains(EXPIRES_IN_KEY)) {
        sharedPreferences.getLong(EXPIRES_IN_KEY, 0L)
    } else null

    fun getTokenType(): String? = sharedPreferences.getString(TOKEN_TYPE_KEY, null)

    /**
     * Retrieves the absolute access token expiry timestamp (in milliseconds).
     * Returns 0L if not found or expired.
     */
    fun getAccessTokenExpiryTime(): Long {
        return sharedPreferences.getLong(ACCESS_TOKEN_EXPIRY_ABS_TIME_KEY, 0L)
    }

    fun getLastTokenResponse(): TokenResponse? {
        val json = sharedPreferences.getString(LAST_TOKEN_RESPONSE_JSON_KEY, null)
        return try {
            json?.let { TokenResponse.jsonDeserialize(it) }
        } catch (e: JSONException) {
            Log.e(tag, "Error deserializing TokenResponse from JSON", e)
            null
        }
    }

    fun clearAuthData() {
        sharedPreferences.edit().clear().apply()
        Log.d(tag, "Authentication data cleared from SharedPreferences.")
    }
}