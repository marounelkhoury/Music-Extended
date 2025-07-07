// app/src/main/java/com/musicextended/viewmodel/HomeViewModel.kt
package com.musicextended.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musicextended.MusicExtendedApplication
import com.musicextended.data.local.entities.UserEntity
import com.musicextended.data.repository.UserRepository
import com.musicextended.utils.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository, // Keep this for logout functionality
    private val userRepository: UserRepository // Inject UserRepository instead of SpotifyUserService
) : ViewModel() {

    private val tag = "HomeViewModel"

    // User Profile related states (keep your existing ones)
    private val _userProfile = MutableStateFlow<UserEntity?>(null)
    val userProfile: StateFlow<UserEntity?> = _userProfile.asStateFlow()

    private val _profileLoading = MutableStateFlow(false)
    val profileLoading: StateFlow<Boolean> = _profileLoading.asStateFlow()

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError.asStateFlow()

    // NEW: Authentication Status States
    private val _authStatus = MutableStateFlow(AuthStatus.LOADING)
    val authStatus: StateFlow<AuthStatus> = _authStatus.asStateFlow()

    init {
        Log.d(tag, "HomeViewModel initialized.")

        // First, set initial auth status based on AuthRepository
        viewModelScope.launch {
            if (authRepository.isAuthenticated()) {
                _authStatus.value = AuthStatus.AUTHENTICATED
                Log.d(tag, "Initial auth check: AUTHENTICATED. Starting profile collection.")
            } else {
                _authStatus.value = AuthStatus.UNAUTHENTICATED
                Log.d(tag, "Initial auth check: UNAUTHENTICATED.")
            }
        }

        // Then, collect user profile from the repository's Flow
        viewModelScope.launch {
            _profileLoading.value = true // Indicate loading starts when collecting
            userRepository.getCurrentUserProfile().collect { user ->
                _userProfile.value = user
                _profileLoading.value = false // Loading is done once data is collected

                if (user == null) {
                    _profileError.value = "Failed to load profile. Is user logged in?"
                    // If profile is null, and it's not due to an explicit logout,
                    // we might need to re-authenticate or indicate unauthenticated state.
                    if (authRepository.isAuthenticated()) {
                        Log.w(tag, "User is authenticated but profile is null. Trying to refresh.")
                        // Potentially trigger a refresh or explicit re-authentication flow if this happens unexpectedly.
                        _authStatus.value = AuthStatus.AUTHENTICATED // Still authenticated, just profile issue
                    } else {
                        _authStatus.value = AuthStatus.UNAUTHENTICATED
                        Log.d(tag, "Profile null, and not authenticated. Setting auth status to UNAUTHENTICATED.")
                    }
                } else {
                    _profileError.value = null
                    _authStatus.value = AuthStatus.AUTHENTICATED // If profile found, definitely authenticated
                    Log.d(tag, "User profile collected: ${user.display_name}")
                }
            }
        }
    }

    // This function will now trigger a refresh via the repository's logic
    fun refreshUserProfile() {
        Log.d(tag, "Refresh profile requested.")
        _profileLoading.value = true // Set loading true while the repository does its work
        // The collect block in init will automatically update when repository emits new data
        // No explicit call needed here, the repository's getCurrentUserProfile()
        // already handles network refresh logic and emits the latest data.
        // If your UserRepository has a separate `refreshProfile()` method that *forces* a network call,
        // you would call it here. For now, relying on the flow's internal refresh.
    }


    fun logout() {
        viewModelScope.launch {
            authRepository.clearAllTokens()
            // Clear local user data when logging out
            userRepository.clearUserData() // This method needs to be implemented in UserRepository
            _userProfile.value = null
            _profileError.value = null
            _authStatus.value = AuthStatus.UNAUTHENTICATED // Set auth status to unauthenticated
            Log.d(tag, "User logged out. Profile cleared and auth status set to UNAUTHENTICATED.")
        }
    }

    // NEW: Define the AuthStatus enum within the ViewModel
    enum class AuthStatus {
        LOADING, // Initial state, checking authentication
        AUTHENTICATED, // User is logged in and token is valid
        UNAUTHENTICATED // User is not logged in or token is invalid/expired
    }

    // ViewModel Factory (Important for injecting dependencies)
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                val app = application as MusicExtendedApplication
                // Pass the initialized authRepository and userRepository from the application class
                return HomeViewModel(app.authRepository, app.userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}