package com.musicextended.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musicextended.MusicExtendedApplication
import com.musicextended.data.local.entities.PlaylistEntity
import com.musicextended.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch // Import catch operator
import kotlinx.coroutines.flow.onStart // Import onStart operator
import kotlinx.coroutines.launch

class PlaylistsViewModel(private val application: MusicExtendedApplication) : ViewModel() {

    private val playlistRepository: PlaylistRepository by lazy {
        application.playlistRepository
    }

    private val _playlists = MutableStateFlow<List<PlaylistEntity>>(emptyList())
    val playlists: StateFlow<List<PlaylistEntity>> = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchPlaylists()
    }

    fun fetchPlaylists() {
        viewModelScope.launch {
            playlistRepository.getCurrentUserPlaylists()
                .onStart { // Emit loading state before starting collection
                    _isLoading.value = true
                    _error.value = null // Clear any previous error
                    Log.d("PlaylistsViewModel", "Starting playlist fetch...")
                }
                .catch { e -> // Catch any exceptions in the flow
                    Log.e("PlaylistsViewModel", "Error fetching playlists: ${e.message}", e)
                    _error.value = e.message
                    _isLoading.value = false // Stop loading on error
                }
                .collect { fetchedPlaylists -> // Collect the emissions from the Flow
                    _playlists.value = fetchedPlaylists
                    Log.d("PlaylistsViewModel", "Collected ${fetchedPlaylists.size} playlists.")
                    _isLoading.value = false // Stop loading after successful collection
                }
        }
    }

    class Factory(private val application: MusicExtendedApplication) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlaylistsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlaylistsViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}