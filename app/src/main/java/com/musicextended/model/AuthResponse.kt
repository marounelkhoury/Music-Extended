// app/src/main/java/com/musicextended/model/AuthResponse.kt
package com.musicextended.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_in") val expiresIn: Long, // Duration in seconds
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("scope") val scope: String? // Optional, if Spotify provides it
)