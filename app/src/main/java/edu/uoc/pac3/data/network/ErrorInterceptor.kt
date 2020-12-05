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
        // Return different request responses depending on response code
        return when (response.code) {
            // Code 401
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                // Get current request url (Endpoint)
                val requestURL = request.url.toString()
                // IF: If Unauthorized Request on getStreams, getUser or updateUserDescription -> refresh and retry
                if (requestURL == Endpoints.getTwitchStreamsUrl() || requestURL == Endpoints.getTwitchUserUrl()) {
                    // IF: If exists refresh token
                    if (SessionManager().getRefreshToken() != null) {
                        // Clear accessToken and set false to user login
                        SessionManager().clearAccessToken()
                        // Try to refresh Access Token
                        Log.d(TAG, "Try to refresh Access Token...")
                        runBlocking { twitchService.refreshAccessToken() }
                        // Close current response for build new one
                        response.close()
                        // Build new request with refreshed access token
                        val requestRefreshed = request.newBuilder()
                                .header("Authorization", "Bearer ${SessionManager().getAccessToken()}")
                                .build()
                        // Proceed new request
                        chain.proceed(requestRefreshed)
                    // ELSE: If not exists refresh token
                    } else {
                        // Return response for manage actions in the service and activity
                        response
                    }
                // ELSE: If Unauthorized Request on getTokens or refreshTokens -> logout
                } else {
                    // Return response for manage actions in the service and activity
                    response
                }
            }
            // Any other code different of 401, Return response for manage actions in the service and activity
            else -> {
                response
            }
        }
    }
}