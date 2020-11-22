package edu.uoc.pac3.data

import android.util.Log
import edu.uoc.pac3.PEC3App
import edu.uoc.pac3.data.oauth.OAuthConstants.clientID
import edu.uoc.pac3.data.oauth.OAuthConstants.clientSecret
import edu.uoc.pac3.data.oauth.OAuthConstants.redirectUri
import edu.uoc.pac3.data.oauth.OAuthTokensResponse
import edu.uoc.pac3.data.oauth.UnauthorizedException
import edu.uoc.pac3.data.streams.Stream
import edu.uoc.pac3.data.streams.StreamsResponse
import edu.uoc.pac3.data.user.User
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*

/**
 * Created by alex on 24/10/2020.
 */

class TwitchApiService(private val httpClient: HttpClient) {
    private val TAG = "TwitchApiService"

    /** Gets Access and Refresh Tokens on Twitch */
    suspend fun getTokens(authorizationCode: String): OAuthTokensResponse? {
        // Get Tokens from Twitch
        try {
            val i =  httpClient.post<OAuthTokensResponse>("https://id.twitch.tv/oauth2/token") {
                parameter("client_id", clientID)
                parameter("client_secret", clientSecret)
                parameter("code", authorizationCode)
                parameter("grant_type", "authorization_code")
                parameter("redirect_uri", redirectUri)
            }
            val a = 1
            return i
        } catch (e: ClientRequestException) {
            when (e.response?.status?.value) {
                401 -> {
                    Log.d(TAG, "getTokens -> UnauthorizedException")
                    throw UnauthorizedException
                }
                else -> Log.d(TAG, "getTokens -> UnknownException")
            }
        }
        return null
    }

    /** Gets Streams on Twitch */
    @Throws(UnauthorizedException::class)
    suspend fun getStreams(cursor: String? = null): StreamsResponse? {
        // ("Get Streams from Twitch")
        // ("Support Pagination")
        try {
            return getStreamsResponse(cursor)
        } catch (e: ClientRequestException) {
            when (e.response?.status?.value) {
                401 -> {
                    Log.d(TAG, "getStreams -> UnauthorizedException. So, trying renew tokens...")
                    refreshAccessToken()
                    return getStreamsResponse(cursor)
                }
                else -> Log.d(TAG, "getStreams -> UnknownException")
            }
        }
        return null
    }

    /// Gets Current Authorized User on Twitch
    @Throws(UnauthorizedException::class)
    suspend fun getUser(): User? {
        TODO("Get User from Twitch")
    }

    /// Gets Current Authorized User on Twitch
    @Throws(UnauthorizedException::class)
    suspend fun updateUserDescription(description: String): User? {
        TODO("Update User Description on Twitch")
    }

    private suspend fun getStreamsResponse (cursor: String? = null): StreamsResponse? {
        return httpClient.get<StreamsResponse>("https://api.twitch.tv/helix/streams") {
            header("Authorization", "Bearer ${SessionManager().getAccessToken()}")
            header("Client-Id", clientID)
            parameter("after", cursor)
        }
    }

    private suspend fun refreshAccessToken() {
        try {
            val response = httpClient.post<OAuthTokensResponse>("https://id.twitch.tv/oauth2/token") {
                parameter("client_id", clientID)
                parameter("client_secret", clientSecret)
                parameter("refresh_token", SessionManager().getRefreshToken())
                parameter("grant_type", "refresh_token")
            }
            response.accessToken.let { accToken -> SessionManager().saveAccessToken(accToken) }
            response.refreshToken?.let { refToken -> SessionManager().saveRefreshToken(refToken) }
            Log.d(TAG, "refreshAccessToken -> accessToken and refreshToken successful renewed")
        } catch (e: ClientRequestException) {
            when (e.response?.status?.value) {
                401 -> {
                    Log.d(TAG, "refreshAccessToken -> UnauthorizedException")
                    throw UnauthorizedException
                }
                else -> Log.d(TAG, "refreshAccessToken -> UnknownException")
            }
        }
    }


}