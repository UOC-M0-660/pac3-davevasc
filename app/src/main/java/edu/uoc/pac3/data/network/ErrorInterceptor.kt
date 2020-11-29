package edu.uoc.pac3.data.network

import android.content.Intent
import android.util.Log
import android.widget.Toast
import edu.uoc.pac3.PEC3App
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.oauth.OAuthConstants
import edu.uoc.pac3.data.oauth.OAuthTokensResponse
import edu.uoc.pac3.data.oauth.UnauthorizedException
import edu.uoc.pac3.oauth.LoginActivity
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection
import java.net.URLConnection


class ErrorInterceptor : Interceptor {

    /** Object with constants for use in this class */
    companion object {
        const val TAG = "TwitchApiService"
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        // Create Twitch Service
        val twitchService = TwitchApiService(Network.createHttpClient())

        val request = chain.request()
        val response = chain.proceed(request)
        //Log.d(TAG, "Request URL: $request")
        Log.d(TAG, "Response URL: $response")
        return when (response.code) {
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                synchronized(this) {
                     if (request.method != "POST") {
                         // Clear accessToken and set false to user login
                         SessionManager().clearAccessToken()
                         // Try to refresh Access Token
                         runBlocking { twitchService.refreshAccessToken() }
                         // Close current response
                         response.close()
                         val requestRefreshed = request.newBuilder()
                            .header("Authorization", "BeaXrer ${SessionManager().getAccessToken()}")
                            .build()
                         chain.proceed(requestRefreshed)
                     } else {
                         SessionManager().logoutSession()
                         throw UnauthorizedException
                     }

                 }
            }
            else -> {
                response
            }
        }

    }





}