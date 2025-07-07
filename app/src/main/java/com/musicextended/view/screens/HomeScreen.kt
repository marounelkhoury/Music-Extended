package com.musicextended.view.screens

import android.util.Log
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage // Import AsyncImage
import com.musicextended.MusicExtendedApplication
import com.musicextended.utils.Constants
import com.musicextended.view.activities.AuthActivity
import com.musicextended.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    application: MusicExtendedApplication = androidx.compose.ui.platform.LocalContext.current.applicationContext as MusicExtendedApplication
) {
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(application)
    )

    val userProfile by homeViewModel.userProfile.collectAsState()
    val profileLoading by homeViewModel.profileLoading.collectAsState()
    val profileError by homeViewModel.profileError.collectAsState()
    val authStatus by homeViewModel.authStatus.collectAsState()

    Log.d("HomeScreen", "AuthStatus: $authStatus, ProfileLoading: $profileLoading, ProfileError: $profileError")

    // --- MODIFIED GRADIENT COLORS START ---
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val background = MaterialTheme.colorScheme.background

    val gradientColors = remember(primaryColor, onSurfaceColor) { // Re-remember if theme colors change
        listOf(
            // Start with a darker version of your primary or background for depth
            // Use your theme's colors or derived colors for consistency
            background.copy(alpha = 0.9f), // A slightly faded version of your background
            surfaceColor.copy(alpha = 0.8f), // A slightly lighter surface color
            primaryColor.copy(alpha = 0.6f), // Your primary color, slightly transparent
            primaryColor.copy(alpha = 0.4f)  // A more faded version of your primary color
            // You can experiment with more colors or darker/lighter variations
            // For example, if primary is green:
            // Color(0xFF1DB954).darken(0.3f), // Spotify green, darkened
            // Color(0xFF1DB954).darken(0.1f),
            // Color(0xFF1DB954), // Actual Spotify green
            // Color(0xFF1DB954).lighten(0.1f)
        )
    }

    // You could also pick from MaterialTheme.colorScheme directly:
    // val gradientColors = remember {
    //     listOf(
    //         MaterialTheme.colorScheme.background,
    //         MaterialTheme.colorScheme.surface,
    //         MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
    //         MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    //     )
    // }
    // --- MODIFIED GRADIENT COLORS END ---


    // Animatable colors for the gradient
    val animatedColor1 = remember { Animatable(gradientColors[0]) }
    val animatedColor2 = remember { Animatable(gradientColors[1]) }
    val animatedColor3 = remember { Animatable(gradientColors[2]) }
    val animatedColor4 = remember { Animatable(gradientColors[3]) }


    // Animate the gradient colors
    LaunchedEffect(Unit) {
        var index = 0
        while (true) {
            animatedColor1.animateTo(gradientColors[index % gradientColors.size], animationSpec = tween(5000))
            animatedColor2.animateTo(gradientColors[(index + 1) % gradientColors.size], animationSpec = tween(5000))
            animatedColor3.animateTo(gradientColors[(index + 2) % gradientColors.size], animationSpec = tween(5000))
            animatedColor4.animateTo(gradientColors[(index + 3) % gradientColors.size], animationSpec = tween(5000))
            index++
            kotlinx.coroutines.delay(2000) // Delay before starting next animation phase
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedColor1.value,
                        animatedColor2.value,
                        animatedColor3.value,
                        animatedColor4.value
                    )
                )
            ),
        contentAlignment = Alignment.Center // Center content in the Box
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (authStatus == HomeViewModel.AuthStatus.LOADING) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Checking authentication status...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else if (authStatus == HomeViewModel.AuthStatus.UNAUTHENTICATED) {
                Text(
                    "You are not logged in.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val intent = android.content.Intent(application, AuthActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    application.startActivity(intent)
                }) {
                    Text("Go to Login")
                }
            } else { // AUTHENTICATED state
                if (profileLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Loading profile...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else if (profileError != null) {
                    Text(
                        "Error loading profile: ${profileError}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { homeViewModel.refreshUserProfile() }) {
                        Text("Retry Load Profile")
                    }
                } else if (userProfile == null) {
                    Text(
                        "No user profile found.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { homeViewModel.refreshUserProfile() }) {
                        Text("Retry Load Profile")
                    }
                } else {
                    // Display Profile Picture
                    userProfile?.imageUrl?.let { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "User Profile Picture",
                            modifier = Modifier
                                .size(120.dp) // Large enough for profile pic
                                .clip(CircleShape), // Circular image
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(24.dp)) // More space after image
                    }

                    Text(
                        text = "Welcome, ${userProfile?.display_name ?: "User"}!",
                        style = MaterialTheme.typography.headlineLarge, // Prominent welcome message
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(48.dp)) // More space before buttons

                    Button(
                        onClick = { navController.navigate(Constants.ROUTE_PLAYLISTS) },
                        modifier = Modifier.height(56.dp) // Taller button
                    ) {
                        Text("View Playlists", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // More space between buttons
                    Button(
                        onClick = { navController.navigate(Constants.ROUTE_SAVED_TRACKS) },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("View Saved Tracks", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate(Constants.ROUTE_AI_GENERATION) },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Generate AI Playlist Idea", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.height(48.dp)) // More space before logout

                    Button(
                        onClick = { homeViewModel.logout() },
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors( // Using outlined button for logout
                            contentColor = MaterialTheme.colorScheme.error // Error color for logout text
                        )
                    ) {
                        Text("Logout", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}