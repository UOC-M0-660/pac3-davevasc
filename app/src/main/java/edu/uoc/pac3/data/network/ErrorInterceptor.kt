package edu.uoc.pac3.data.network

import android.util.Log
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.oauth.UnauthorizedException
import io.ktor.client.features.*
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.lang.Exception
import java.net.HttpURLConnection

class ErrorInterceptor : Interceptor {

    /** Object with constants for use in this class */
    companion object {
        const val TAG = "TwitchApiService"
    }
    override fun intercept(chain: Interceptor.Chain): Response {

        // Create Twitch Service
        val twitchService = TwitchApiService(Network.createHttpClient())
        // Get request and response
        val request = chain.request()
        val response = chain.proceed(request)
        Log.d(TAG, "Response URL: $response")
        return when (response.code) {
            // Code 200
            HttpURLConnection.HTTP_OK -> {
                response
            }
            // Code 401
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                synchronized(this) {
                    // If Unauthorized Request on Get Tokens or RefreshTokens
                     if (request.method == "POST") {
                         SessionManager().logoutSession()
                         throw UnauthorizedException
                     }
                     // If Unauthorized Request on Get Streams, Get User or Put User Description
                     else {
                         // Clear accessToken and set false to user login
                         SessionManager().clearAccessToken()
                         // Try to refresh Access Token
                         runBlocking { twitchService.refreshAccessToken() }
                         // Close current response
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
            // Any other code other than 401 and 200
            else -> {
                SessionManager().logoutSession()
                throw Exception()
            }
        }

    }





}