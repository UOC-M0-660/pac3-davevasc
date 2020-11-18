package edu.uoc.pac3.oauth

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import edu.uoc.pac3.R
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.network.Network.createHttpClient
import edu.uoc.pac3.data.oauth.OAuthConstants.authorizationUrl
import edu.uoc.pac3.data.oauth.OAuthConstants.clientID
import edu.uoc.pac3.data.oauth.OAuthConstants.clientSecret
import edu.uoc.pac3.data.oauth.OAuthConstants.redirectUri
import edu.uoc.pac3.data.oauth.OAuthConstants.scopes
import edu.uoc.pac3.data.oauth.OAuthConstants.uniqueState
import edu.uoc.pac3.data.oauth.OAuthTokensResponse
import edu.uoc.pac3.data.oauth.UnauthorizedException
import edu.uoc.pac3.twitch.streams.StreamsActivity
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders.Authorization
import kotlinx.android.synthetic.main.activity_oauth.*
import kotlinx.coroutines.launch

class OAuthActivity : AppCompatActivity() {

    private val TAG = "OAuthActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth)
        launchOAuthAuthorization()
    }

    fun buildOAuthUri(): Uri {
        // TODO: Create URI
        return Uri.parse(authorizationUrl)
                .buildUpon()
                .appendQueryParameter("client_id", clientID)
                .appendQueryParameter("redirect_uri", redirectUri)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("scope", scopes.joinToString(separator = " "))
                .appendQueryParameter("state", uniqueState)
                .build()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun launchOAuthAuthorization() {
        //  Create URI
        val uri = buildOAuthUri()

        // TODO: Set webView Redirect Listener
        webView.webViewClient = object : WebViewClient() {
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
                                Log.d(TAG, "Here is the authorization code! $code")
                                onAuthorizationCodeRetrieved(code)
                            } ?: run {
                                // User cancelled the login flow
                                // TODO: Handle error
                                Log.d(TAG, "User cancelled the login flow")
                                //UnauthorizedException
                                //return false ??
                            }
                        }
                    }
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        // Load OAuth Uri
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(uri.toString())
    }

    /** Call this method after obtaining the authorization code
     on the WebView to obtain the tokens */
    private fun onAuthorizationCodeRetrieved(authorizationCode: String) {
        // Show Loading Indicator
        progressBar.visibility = View.VISIBLE
        // Create Twitch Service
        val twitchService = TwitchApiService(createHttpClient(applicationContext))
        // Start Coroutine
        lifecycleScope.launch {
            // Get Tokens from Twitch
            val response = twitchService.getTokens(authorizationCode)
            // Save access token and refresh token using the SessionManager class
            response?.accessToken?.let { accToken -> SessionManager().saveAccessToken(accToken) }
            response?.refreshToken?.let { refToken -> SessionManager().saveRefreshToken(refToken) }
            // If we are correctly identified, load directly Streams Activity, else, load login again
            if (SessionManager().isUserAvailable()) {
                // Launch Streams Activity
                Toast.makeText(applicationContext, "Login correctly identified!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(applicationContext, StreamsActivity::class.java))
            } else {
                // Return to Login Activity
                Toast.makeText(applicationContext, "Error when login, please try again", Toast.LENGTH_SHORT).show()
                startActivity(Intent(applicationContext, LoginActivity::class.java))
            }
        }
    }
}