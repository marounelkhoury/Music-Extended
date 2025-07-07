// app/src/main/java/com/musicextended/model/User.kt
package com.musicextended.model

import com.google.gson.annotations.SerializedName
import com.musicextended.network.ExternalUrls // Uses network.ExternalUrls

data class User(
    val id: String,
    @SerializedName("display_name") val display_name: String?,
    val href: String?,
    val uri: String?,
    @SerializedName("external_urls") val external_urls: ExternalUrls?, // Uses network.ExternalUrls
    val type: String? // e.g., "user", "artist"
)