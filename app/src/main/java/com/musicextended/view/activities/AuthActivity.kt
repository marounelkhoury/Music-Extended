// app/src/main/java/com/musicextended/view/activities/AuthActivity.kt
package com.musicextended.view.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.musicextended.MusicExtendedApplication
import com.musicextended.utils.AuthRepository
import com.musicextended.utils.Constants
import com.musicextended.view.theme.MusicExtendedTheme
import kotlinx.coroutines.launch
import net.openid.appauth.*
import com.musicextended.model.AuthResponse // <--- ADDED THIS IMPORT: Your custom AuthResponse model

class AuthActivity : ComponentActivity() {

    private val authRepository: AuthRepository
        get() = (application as MusicExtendedApplication).authRepository

    private val authService: AuthorizationService by lazy { AuthorizationService(this) }

    private val tag = "AuthActivity"

    private val authResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                handleAuthorizationResponse(data)
            } else {
                Log.e(tag, "Authorization result data is null.")
                Toast.makeText(this, "Authentication failed: No data received.", Toast.LENGTH_LONG).show()
            }
        } else {
            val ex = AuthorizationException.fromIntent(result.data ?: Intent())
            Log.e(tag, "Authorization failed with result code: ${result.resultCode}, Exception: ${ex?.message}", ex)
            Toast.makeText(this, "Authentication cancelled or failed: ${ex?.message}", Toast.LENGTH_LONG).show()
            if (ex?.code == AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW.code) {
                Log.i(tag, "User cancelled authentication flow.")
            } else {
                // Corrected: Use clearAllTokens() from AuthRepository
                authRepository.clearAllTokens()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MusicExtendedTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val coroutineScope = rememberCoroutineScope()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Please log in to Spotify", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = {
                            coroutineScope.launch {
                                initiateSpotifyAuth()
                            }
                        }) {
                            Text("Log in with Spotify")
                        }
                    }
                }
            }
        }
    }

    private suspend fun initiateSpotifyAuth() {
        Log.d(tag, "Initiating Spotify authentication.")

        val serviceConfiguration = AuthorizationServiceConfiguration(
            Constants.SPOTIFY_AUTHORIZATION_ENDPOINT,
            Constants.SPOTIFY_TOKEN_ENDPOINT,
            null,
            Constants.SPOTIFY_END_SESSION_ENDPOINT
        )

        val authRequest = AuthorizationRequest.Builder(
            serviceConfiguration,
            Constants.SPOTIFY_CLIENT_ID, // Ensure this matches Constants.kt
            ResponseTypeValues.CODE,
            Constants.SPOTIFY_REDIRECT_URI
        )
            .setScope(Constants.SPOTIFY_SCOPE)
            .build()

        val authIntent = authService.getAuthorizationRequestIntent(authRequest)

        try {
            authResultLauncher.launch(authIntent)
        } catch (e: Exception) {
            Log.e(tag, "Failed to launch authorization intent: ${e.message}", e)
            Toast.makeText(this, "Failed to open browser for login.", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleAuthorizationResponse(intent: Intent) {
        val response = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)

        if (response != null) {
            Log.d(tag, "Authorization successful. Exchanging code for tokens.")
            lifecycleScope.launch {
                // Pass the AppAuth TokenRequest to AuthRepository's exchangeToken
                performTokenRequest(response.createTokenExchangeRequest())
            }
        } else {
            Log.e(tag, "Authorization failed: ${ex?.message}", ex)
            Toast.makeText(this, "Authentication failed: ${ex?.message}", Toast.LENGTH_LONG).show()
            // It's good practice to clear tokens on auth failure too
            authRepository.clearAllTokens()
        }
    }

    private suspend fun performTokenRequest(tokenRequest: TokenRequest) {
        Log.d(tag, "Performing token exchange request.")

        // Corrected: Call exchangeToken method on authRepository
        val authResult: AuthResponse? = authRepository.exchangeToken(tokenRequest) // Expecting your AuthResponse model

        if (authResult != null) {
            Log.d(tag, "Token exchange successful!")
            // Corrected: Access properties from your AuthResponse model
            Log.i(tag, "Access Token: ${authResult.accessToken?.take(5)}...")
            Log.i(tag, "Refresh Token: ${authResult.refreshToken?.take(5)}...")
            // Corrected: Use 'expiresIn' from your AuthResponse (duration in seconds)
            Log.i(tag, "Expires In (duration): ${authResult.expiresIn} seconds")
            Log.i(tag, "Token Type: ${authResult.tokenType ?: "N/A"}")

            startActivity(Intent(this@AuthActivity, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(this@AuthActivity, "Failed to get tokens. Please try again.", Toast.LENGTH_LONG).show()
            Log.e(tag, "Token exchange failed. Check logs for details.")
            // Corrected: Clear all tokens on failure
            authRepository.clearAllTokens()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        authService.dispose()
    }
}