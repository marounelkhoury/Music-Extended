// app/src/main/java/com/musicextended/model/SpotifyAuthService.kt
package com.musicextended.model

import com.musicextended.model.AuthResponse
import com.musicextended.model.RefreshTokenResponse
import retrofit2.Response // Correct import for Response
import retrofit2.http.Field // Correct import for Field
import retrofit2.http.FormUrlEncoded // Correct import for FormUrlEncoded
import retrofit2.http.Headers // Correct import for Headers
import retrofit2.http.POST // Correct import for POST

// Assuming your SpotifyAuthService interface looks something like this:
interface SpotifyAuthService {

    @POST("api/token")
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun requestAccessToken(
        @Field("grant_type") grantType: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Response<AuthResponse>

    @POST("api/token")
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun refreshAccessToken(
        @Field("grant_type") grantType: String = "refresh_token", // Default value
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String, // May or may not be required depending on Spotify's current API
        @Field("client_secret") clientSecret: String // May or may not be required depending on Spotify's current API
    ): Response<RefreshTokenResponse>
}