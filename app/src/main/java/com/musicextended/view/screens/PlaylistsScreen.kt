package com.musicextended.view.screens

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.musicextended.MusicExtendedApplication
import com.musicextended.viewmodel.PlaylistsViewModel

@Composable
fun PlaylistsScreen(navController: NavController) {
    val application = LocalContext.current.applicationContext as MusicExtendedApplication
    val playlistViewModel: PlaylistsViewModel = viewModel(factory = PlaylistsViewModel.Factory(application))

    val playlists by playlistViewModel.playlists.collectAsState()
    val isLoading by playlistViewModel.isLoading.collectAsState()
    val error by playlistViewModel.error.collectAsState()

    // --- ANIMATED GRADIENT BACKGROUND LOGIC (Copy-pasted from HomeScreen) ---
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val background = MaterialTheme.colorScheme.background

    val gradientColors = remember(primaryColor, onSurfaceColor) {
        listOf(
            background.copy(alpha = 0.9f),
            surfaceColor.copy(alpha = 0.8f),
            primaryColor.copy(alpha = 0.6f),
            primaryColor.copy(alpha = 0.4f)
        )
    }

    val animatedColor1 = remember { Animatable(gradientColors[0]) }
    val animatedColor2 = remember { Animatable(gradientColors[1]) }
    val animatedColor3 = remember { Animatable(gradientColors[2]) }
    val animatedColor4 = remember { Animatable(gradientColors[3]) }

    LaunchedEffect(Unit) {
        var index = 0
        while (true) {
            animatedColor1.animateTo(gradientColors[index % gradientColors.size], animationSpec = tween(5000))
            animatedColor2.animateTo(gradientColors[(index + 1) % gradientColors.size], animationSpec = tween(5000))
            animatedColor3.animateTo(gradientColors[(index + 2) % gradientColors.size], animationSpec = tween(5000))
            animatedColor4.animateTo(gradientColors[(index + 3) % gradientColors.size], animationSpec = tween(5000))
            index++
            kotlinx.coroutines.delay(2000)
        }
    }
    // --- END ANIMATED GRADIENT BACKGROUND LOGIC ---

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
            )
    ) {
        // Your existing Column content goes inside this Box
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Your Playlists",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onBackground // Ensure text color is visible on background
            )

            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Loading playlists...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Error loading playlists: $error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                playlists.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No playlists found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                        items(playlists) { playlist ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    //containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f) // Adjust alpha for better contrast with background
                                    containerColor = Color.Black.copy(alpha = 0.3f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                androidx.compose.foundation.layout.Row( // Use fully qualified name
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    playlist.imageUrl?.let { imageUrl ->
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = "Playlist Cover for ${playlist.name}",
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } ?: run {
                                        Column(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.2f)),
                                                //.background(MaterialTheme.colorScheme.surface), // Placeholder background
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "No Image",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(Modifier.size(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = playlist.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        playlist.description?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "${playlist.totalTracks} tracks",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}