package edu.uoc.pac3.data.network

import android.util.Log
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.data.TwitchApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection

/** Done by david on 27/11/2020.
 * Class for Intercept Errors in the API request */
class ErrorInterceptor : Interceptor {

    /** Object with constants for use in this class */
    companion object {
        const val TAG = "PEC3_Interceptor"
    }
    /** Function which intercepts all API requests  */
    override fun intercept(chain: Interceptor.Chain): Response {
        // Create Twitch Service
        val twitchService = TwitchApiService(Network.createHttpClient())
        // Get request and response
        val request = chain.request()
        val response = chain.proceed(request)
        Log.d(TAG, "Response URL: $response")
        // Return different request responses depending on response code
        return when (response.code) {
            // Code 401
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                // Performing all 401 in sync blocks, to avoid multiply token updates
                synchronized(this) {
                    // If Unauthorized Request on Get Tokens or RefreshTokens -> logout
                    if (request.method == "POST") {
                        //Catch of TwitchService will manage logoutSession
                        response
                    }
                    // If Unauthorized Request on Get Streams, Get User or Put User Description -> refresh and retry
                    else {
                        // Clear accessToken and set false to user login
                        SessionManager().clearAccessToken()
                        // Try to refresh Access Token
                        Log.d(TAG, "Interceptor -> Trying refresh tokens...")
                        runBlocking { twitchService.refreshAccessToken() }
                        // Close current response for build new one
                        response.close()
                        // Build new request with refreshed access token
                        val requestRefreshed = request.newBuilder()
                                .header("Authorization", "Bearer ${SessionManager().getAccessToken()}")
                                .build()
                        // Proceed new request
                        chain.proceed(requestRefreshed)
                    }
                }
            }
            // Any other code different of 401, Catch of TwitchService will manage logoutSession
            else -> {
                response
            }
        }
    }
}