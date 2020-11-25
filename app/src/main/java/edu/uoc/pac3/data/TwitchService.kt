package edu.uoc.pac3.data

import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import edu.uoc.pac3.PEC3App
import edu.uoc.pac3.data.network.Endpoints
import edu.uoc.pac3.data.oauth.OAuthConstants.clientID
import edu.uoc.pac3.data.oauth.OAuthConstants.clientSecret
import edu.uoc.pac3.data.oauth.OAuthConstants.redirectUri
import edu.uoc.pac3.data.oauth.OAuthTokensResponse
import edu.uoc.pac3.data.oauth.UnauthorizedException
import edu.uoc.pac3.data.streams.Stream
import edu.uoc.pac3.data.streams.StreamsResponse
import edu.uoc.pac3.data.user.User
import edu.uoc.pac3.data.user.UserResponse
import edu.uoc.pac3.oauth.LoginActivity
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlin.coroutines.coroutineContext

/**
 * Created by alex on 24/10/2020.
 */

class TwitchApiService(private val httpClient: HttpClient) {

    companion object {
        const val TAG = "TwitchApiService"
    }

    /** Gets Access and Refresh Tokens on Twitch */
    @Throws(UnauthorizedException::class)
    suspend fun getTokens(authorizationCode: String): OAuthTokensResponse? {
        // Get Tokens from Twitch
        try {
            return httpClient.post<OAuthTokensResponse>(Endpoints.getOauthTokenUrl()) {
                parameter("client_id", clientID)
                parameter("client_secret", clientSecret)
                parameter("code", authorizationCode)
                parameter("grant_type", "authorization_code")
                parameter("redirect_uri", redirectUri)
            }
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
                    // Clear accessToken and set false to user login
                    SessionManager().clearAccessToken()
                    refreshAccessToken()
                    return getStreamsResponse(cursor)
                }
                else -> Log.d(TAG, "getStreams -> UnknownException")
            }
        }
        return null
    }

    @Throws(UnauthorizedException::class)
    private suspend fun getStreamsResponse (cursor: String? = null): StreamsResponse {
        return httpClient.get<StreamsResponse>(Endpoints.getTwitchStreamsUrl()) {
            header("Authorization", "Bearer ${SessionManager().getAccessToken()}")
            header("Client-Id", clientID)
            parameter("after", cursor)
        }
    }

    /// Gets Current Authorized User on Twitch
    @Throws(UnauthorizedException::class)
    suspend fun getUser(): UserResponse? {
        // Get User from Twitch
        try {
            return getUserResponse()
        } catch (e: ClientRequestException) {
            when (e.response?.status?.value) {
                401 -> {
                    Log.d(TAG, "getUser -> UnauthorizedException. So, trying renew tokens...")
                    // Clear accessToken and set false to user login
                    SessionManager().clearAccessToken()
                    refreshAccessToken()
                    return getUserResponse()
                }
                else -> Log.d(TAG, "getUser -> UnknownException")
            }
        }
        return null
    }

    @Throws(UnauthorizedException::class)
    suspend fun getUserResponse(): UserResponse {
        return httpClient.get<UserResponse>(Endpoints.getTwitchUserUrl()) {
            header("Authorization", "Bearer ${SessionManager().getAccessToken()}")
            header("Client-Id", clientID)
        }
    }

    /// Gets Current Authorized User on Twitch
    @Throws(UnauthorizedException::class)
    suspend fun updateUserDescription(description: String): User? {
        // Update User Description on Twitch
        try {
            return updateUserDescriptionResponse(description)
        } catch (t: Throwable) {
            return when (t) {
                is ClientRequestException -> {
                    return when (t.response?.status?.value) {
                        401 -> {
                            Log.d(TAG, "updateUserDescription -> UnauthorizedException. So, trying renew tokens...")
                            // Clear accessToken and set false to user login
                            SessionManager().clearAccessToken()
                            refreshAccessToken()
                            updateUserDescriptionResponse(description)
                        }
                        else -> {
                            Log.d(TAG, "updateUserDescription -> UnknownRequestException")
                            null
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "updateUserDescription -> UnknownException")
                    null
                }
            }
        }
    }



    /// Gets Current Authorized User on Twitch
    @Throws(UnauthorizedException::class)
    suspend fun updateUserDescriptionResponse(description: String): User {
        // Update User Description on Twitch
        return httpClient.put(Endpoints.getTwitchUserUrl()) {
            header("Authorization", "Bearer ${SessionManager().getAccessToken()}")
            header("Client-Id", clientID)
            parameter("description", description)
        }
    }


    @Throws(UnauthorizedException::class)
    private suspend fun refreshAccessToken() {
        try {
            val response = httpClient.post<OAuthTokensResponse>(Endpoints.getOauthTokenUrl()) {
                parameter("client_id", clientID)
                parameter("client_secret", clientSecret)
                parameter("refresh_token", SessionManager().getRefreshToken())
                parameter("grant_type", "refresh_token")
            }
            response.accessToken.let { accToken -> SessionManager().saveAccessToken(accToken) }
            response.refreshToken?.let { refToken -> SessionManager().saveRefreshToken(refToken) }
            if (SessionManager().isUserAvailable()) {
                Log.d(TAG, "refreshAccessToken -> accessToken and refreshToken successful renewed")
            } else {
                PEC3App.context.startActivity(Intent(PEC3App.context, LoginActivity::class.java))
            }
        } catch (e: ClientRequestException) {
            when (e.response?.status?.value) {
                401 -> {
                    Log.d(TAG, "refreshAccessToken -> UnauthorizedException")
                    PEC3App.context.startActivity(Intent(PEC3App.context, LoginActivity::class.java))
                    throw UnauthorizedException
                }
                else -> {
                    Log.d(TAG, "refreshAccessToken -> UnknownException")
                    PEC3App.context.startActivity(Intent(PEC3App.context, LoginActivity::class.java))
                }
            }
        }
    }

}