package edu.uoc.pac3.data

import android.util.Log
import edu.uoc.pac3.data.network.Endpoints
import edu.uoc.pac3.data.oauth.*
import edu.uoc.pac3.data.oauth.OAuthConstants.clientID
import edu.uoc.pac3.data.oauth.OAuthConstants.clientSecret
import edu.uoc.pac3.data.oauth.OAuthConstants.redirectUri
import edu.uoc.pac3.data.streams.StreamsResponse
import edu.uoc.pac3.data.user.User
import edu.uoc.pac3.data.user.UserResponse
import io.ktor.client.*
import io.ktor.client.request.*

/**
 * Created by alex on 24/10/2020.
 * Done by david on 27/11/2020.
 * Definition of Twitch API Service methods
 */
class TwitchApiService(private val httpClient: HttpClient) {

    /** Object with constants for use in this class */
    companion object {
        const val TAG = "PEC3_TwitchApiService"
    }
    /** Gets Access and Refresh Tokens on Twitch */
    @Throws(UnauthorizedException::class)
    suspend fun getTokens(authorizationCode: String): OAuthTokensResponse? {
        // Try to get Tokens from Twitch
        return try {
            httpClient.post<OAuthTokensResponse>(Endpoints.getOauthTokenUrl()) {
                parameter("client_id", clientID)
                parameter("client_secret", clientSecret)
                parameter("code", authorizationCode)
                parameter("grant_type", "authorization_code")
                parameter("redirect_uri", redirectUri)
            }
        } catch (t: Throwable) {
            // When error -> logout session
            Log.d(TAG, "getTokens -> Error: ${t.message}")
            // Remove all Cookies, clear Access Token and open LoginActivity for try login again
            SessionManager().logoutSession()
            null
        }
    }
    /** Gets Streams on Twitch with Pagination */
    @Throws(UnauthorizedException::class)
    suspend fun getStreams(cursor: String? = null): StreamsResponse? {
        // Try to get Streams from Twitch and Support Pagination
        return try {
            httpClient.get(Endpoints.getTwitchStreamsUrl()) {
                header("Authorization", "Bearer ${SessionManager().getAccessToken()}")
                header("Client-Id", clientID)
                parameter("after", cursor)
            }
        } catch (t: Throwable) {
            // First time (error != 401) or second time (error = 401), so, in both cases -> logout
            Log.d(TAG, "getStreams -> Error: ${t.message}")
            // Remove all Cookies, clear Access Token and open LoginActivity for try login again
            SessionManager().logoutSession()
            null
        }
    }
    /** Gets Current Authorized User on Twitch */
    @Throws(UnauthorizedException::class)
    suspend fun getUser(): UserResponse? {
        // Try to get User from Twitch
        return try {
            httpClient.get(Endpoints.getTwitchUserUrl()) {
                header("Authorization", "Bearer ${SessionManager().getAccessToken()}")
                header("Client-Id", clientID)
            }
        } catch (t: Throwable) {
            // First time (error != 401) or second time (error = 401), so, in both cases -> logout
            Log.d(TAG, "getStreams -> Error: ${t.message}")
            // Remove all Cookies, clear Access Token and open LoginActivity for try login again
            SessionManager().logoutSession()
            null
        }
    }
    /** Update user description on Twitch */
    @Throws(UnauthorizedException::class)
    suspend fun updateUserDescription(description: String): User? {
        // try to update User Description on Twitch
        return try {
            httpClient.put(Endpoints.getTwitchUserUrl()) {
                header("Authorization", "Bearer ${SessionManager().getAccessToken()}")
                header("Client-Id", clientID)
                parameter("description", description)
            }
        } catch (t: Throwable) {
            // First time (error != 401) or second time (error = 401), so, in both cases -> logout
            Log.d(TAG, "getStreams -> Error: ${t.message}")
            // Remove all Cookies, clear Access Token and open LoginActivity for try login again
            SessionManager().logoutSession()
            null
        }
    }
    /** Refresh Access and Refresh Tokens by using current Refresh Token */
    @Throws(UnauthorizedException::class)
    suspend fun refreshAccessToken() {
        try {
            // Try to get access and refresh token from Twitch
            val response = httpClient.post<OAuthTokensResponse>(Endpoints.getOauthTokenUrl()) {
                parameter("client_id", clientID)
                parameter("client_secret", clientSecret)
                parameter("refresh_token", SessionManager().getRefreshToken())
                parameter("grant_type", "refresh_token")
            }
            // Save Access and Refresh token into shared preferences
            response.accessToken.let { accToken -> SessionManager().saveAccessToken(accToken) }
            response.refreshToken?.let { refToken -> SessionManager().saveRefreshToken(refToken) }
            // If access token was saved correctly, write in log
            if (SessionManager().isUserAvailable()) {
                Log.d(TAG, "refreshAccessToken -> accessToken and refreshToken successful renewed")
            }
        } catch (t: Throwable) {
            // When error -> logout session from getStreams, getUser or updateUser, not form here
            Log.d(TAG, "refreshAccessToken -> Error: ${t.message}")
            // Here not needs logout session because logout is launched by catch of GET/PUT functions
        }
    }
}