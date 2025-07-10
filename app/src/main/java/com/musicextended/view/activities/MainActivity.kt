// app/src/main/java/com/musicextended/view/activities/MainActivity.kt
package com.musicextended.view.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.musicextended.MusicExtendedApplication
import com.musicextended.network.SpotifyUserService
import com.musicextended.utils.AuthRepository
import com.musicextended.utils.Constants
import com.musicextended.view.screens.AIGenerationScreen
import com.musicextended.view.screens.HomeScreen
import com.musicextended.view.screens.PlaylistsScreen
import com.musicextended.view.screens.SavedTracksScreen
import com.musicextended.view.theme.MusicExtendedTheme

class MainActivity : ComponentActivity() {

    // Access application-level singletons (these are still used by the ViewModel factories)
    private val authRepository: AuthRepository
        get() = (application as MusicExtendedApplication).authRepository

    private val spotifyUserService: SpotifyUserService
        get() = (application as MusicExtendedApplication).spotifyUserService

    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MusicExtendedTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Initial authentication check:
                    LaunchedEffect(Unit) {
                        Log.d(tag, "Initial LaunchedEffect: Checking authentication status...")
                        if (!authRepository.isAuthenticated()) {
                            Log.i(tag, "Not authenticated. Navigating to AuthActivity.")
                            startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                            finish()
                        } else {
                            Log.d(tag, "Authenticated. Navigating to Home Screen.")
                        }
                    }

                    // Define your navigation graph
                    NavHost(
                        navController = navController,
                        startDestination = Constants.ROUTE_HOME,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Home Screen Composable
                        composable(Constants.ROUTE_HOME) {
                            HomeScreen(
                                navController = navController
                            )
                        }

                        // Playlists Screen Composable
                        composable(Constants.ROUTE_PLAYLISTS) {
                            PlaylistsScreen(
                                navController = navController
                            )
                        }

                        // Saved Tracks Screen Composable
                        composable(Constants.ROUTE_SAVED_TRACKS) {
                            SavedTracksScreen(
                                navController = navController
                            )
                        }

                        composable(Constants.ROUTE_AI_GENERATION) {
                            AIGenerationScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultMainPreview() {
    MusicExtendedTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Main Activity Preview (NavHost setup)")
        }
    }
}