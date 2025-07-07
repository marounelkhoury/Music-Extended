// app/src/main/java/com/musicextended/model/TracksInfo.kt
package com.musicextended.model

import com.google.gson.annotations.SerializedName

data class TracksInfo(
    val href: String,
    val total: Int // Total number of tracks in the playlist
)