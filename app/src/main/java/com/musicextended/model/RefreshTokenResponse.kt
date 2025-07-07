package com.musicextended.model

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("scope") val scope: String? = null
)