package edu.uoc.pac3.oauth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.network.Network.createHttpClient
import edu.uoc.pac3.data.oauth.OAuthConstants.authorizationUrl
import edu.uoc.pac3.data.oauth.OAuthConstants.clientID
import edu.uoc.pac3.data.oauth.OAuthConstants.redirectUri
import edu.uoc.pac3.data.oauth.OAuthConstants.scopes
import edu.uoc.pac3.data.oauth.OAuthConstants.uniqueState
import edu.uoc.pac3.databinding.ActivityOauthBinding
import edu.uoc.pac3.twitch.streams.StreamsActivity
import kotlinx.coroutines.launch

/**
 * Created by alex on 24/10/2020.
 * Done by david on 27/11/2020.
 * Class for manage login on Twitch by his API */
class OAuthActivity : AppCompatActivity() {

    /** Object with constants for use in this activity */
    companion object {
        const val TAG = "PEC3_OAuthActivity"
    }

    // Declare binding variable for this activity
    private lateinit var binding: ActivityOauthBinding

    /** onCreate activity function */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set binding variable for current activity
        binding = ActivityOauthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Launch OAuth authorization function
        launchOAuthAuthorization()
    }
    /** Create URI from OAuthConstants data class constants and values */
    private fun buildOAuthUri(): Uri {
        return Uri.parse(authorizationUrl)
                .buildUpon()
                .appendQueryParameter("client_id", clientID)
                .appendQueryParameter("redirect_uri", redirectUri)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("scope", scopes.joinToString(separator = " "))
                .appendQueryParameter("state", uniqueState)
                .build()
    }
    /** Load authorization url into webView and set webView redirect listener */
    @SuppressLint("SetJavaScriptEnabled")
    private fun launchOAuthAuthorization() {
        //  Create URI for load into webView
        val uri = buildOAuthUri()
        // Set webView Redirect Listener
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                request?.let {
                    // Check if this url is our OAuth redirect, otherwise ignore it
                    if (request.url.toString().startsWith(redirectUri)) {
                        // To prevent CSRF attacks, check that we got the same state value we sent, otherwise ignore it
                        val responseState = request.url.getQueryParameter("state")
                        if (responseState == uniqueState) {
                            // This is our request, obtain the code!
                            request.url.getQueryParameter("code")?.let { code ->
                                // Got it!
                                Log.d(TAG, "shouldOverrideUrlLoading -> Here is the authorization code! $code")
                                // Send authorization code to Twitch by his API, for obtain Tokens
                                onAuthorizationCodeRetrieved(code)
                            } ?: run {
                                // User cancelled the login flow
                                Log.d(TAG, "shouldOverrideUrlLoading -> User cancelled the login flow")
                                Toast.makeText(applicationContext, "Couldn't log in with Twitch please try again later", Toast.LENGTH_SHORT).show()
                                // Remove all Cookies, clear Access Token and open LoginActivity for try again, and finish current Activity
                                SessionManager().logoutSession()
                                finish()
                            }
                        }
                    }
                }
                // Load url on webView only when is asking user and password, to avoid ugly localhost error page
                return if (request?.url.toString().startsWith(redirectUri)) {
                    true
                } else {
                    super.shouldOverrideUrlLoading(view, request)
                }
            }
        }
        // Load OAuth Uri
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl(uri.toString())
    }
    /** Call this method after obtaining the authorization code
     * on the WebView to obtain the tokens */
    private fun onAuthorizationCodeRetrieved(authorizationCode: String) {
        // Create Twitch Service
        val twitchService = TwitchApiService(createHttpClient())
        // Start Coroutine
        lifecycleScope.launch {
            // Show Loading Indicator
            binding.pbLoading.visibility = View.VISIBLE
            // Get Tokens from Twitch
            val response = twitchService.getTokens(authorizationCode)
            // If exists, save Access Token and Refresh Token using the SessionManager class
            response?.accessToken?.let { accToken -> SessionManager().saveAccessToken(accToken) }
            response?.refreshToken?.let { refToken -> SessionManager().saveRefreshToken(refToken) }
            // Open Streams Activity and finish this Activity
            if (SessionManager().isUserAvailable()) {
                Toast.makeText(applicationContext, "Login correctly identified!!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onAuthorizationCodeRetrieved -> Login correctly identified!!")
                startActivity(Intent(applicationContext, StreamsActivity::class.java))
                finish()
            }
            // Hide Loading Indicator
            binding.pbLoading.visibility = View.GONE
        }
    }
}