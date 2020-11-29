package edu.uoc.pac3.data

import android.content.Intent
import android.util.Log
import android.widget.Toast
import edu.uoc.pac3.PEC3App
import edu.uoc.pac3.data.network.Endpoints
import edu.uoc.pac3.data.oauth.*
import edu.uoc.pac3.data.oauth.OAuthConstants.clientID
import edu.uoc.pac3.data.oauth.OAuthConstants.clientSecret
import edu.uoc.pac3.data.oauth.OAuthConstants.redirectUri
import edu.uoc.pac3.data.streams.StreamsResponse
import edu.uoc.pac3.data.user.User
import edu.uoc.pac3.data.user.UserResponse
import edu.uoc.pac3.oauth.LoginActivity
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*

/**
 * Created by alex on 24/10/2020.
 * Done by david on 27/11/2020.
 * Definition of Twitch API Service methods
 */
class TwitchApiService(private val httpClient: HttpClient) {

    /** Object with constants for use in this class */
    companion object {
        const val TAG = "TwitchApiService"
    }
    /** Gets Access and Refresh Tokens on Twitch */
    @Throws(UnauthorizedException::class)
    suspend fun getTokens(authorizationCode: String): OAuthTokensResponse? {
        // Try to get Tokens from Twitch
        try {
            return httpClient.post<OAuthTokensResponse>(Endpoints.getOauthTokenUrl()) {
                parameter("client_id", clientID)
                parameter("client_secret", clientSecret)
                parameter("code", authorizationCode)
                parameter("grant_type", "authorization_code")
                parameter("redirect_uri", redirectUri)
            }
        } catch (t: Throwable) {
            return when (t) {
                is ClientRequestException -> {
                    return when (t.response?.status?.value) {
                        // When 401 code request exception (unauthorized exception)
                        401 -> {
                            Log.d(TAG, "getTokens -> UnauthorizedException")
                            // Remove all Cookies, clear Access Token and open LoginActivity for try login again
                            SessionManager().logoutSession()
                            throw UnauthorizedException
                        }
                        // When another request exception
                        else -> {
                            Log.d(TAG, "getTokens -> UnknownRequestException")
                            null
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "getTokens -> UnknownException: ${t.message}")
                    null
                }
            }
        }
    }
    /** Gets Streams on Twitch with Pagination */
    @Throws(UnauthorizedException::class)
    suspend fun getStreams(cursor: String? = null): StreamsResponse? {
        // Try to get Streams from Twitch and Support Pagination
        try {
            return getStreamsResponse(cursor)
        } catch (t: Throwable) {
            return when (t) {
                is ClientRequestException -> {
                    return when (t.response?.status?.value) {
                        // When 401 code request exception (unauthorized exception)
                        401 -> {
                            Log.d(TAG, "getStreams -> UnauthorizedException. So, trying renew tokens...")
                            // Clear accessToken and set false to user login
                            SessionManager().clearAccessToken()
                            // Try to refresh Access Token
                            refreshAccessToken()
                            try {
                                // Try to get Streams again
                                getStreamsResponse(cursor)
                            } catch (e: ClientRequestException) {
                                return when (t.response?.status?.value) {
                                    // When 401 code request exception again (unauthorized exception)
                                    401 -> {
                                        Log.d(TAG, "getStreams -> UnauthorizedException")
                                        // Remove all Cookies, clear Access Token and open LoginActivity for try login again
                                        SessionManager().logoutSession()
                                        throw UnauthorizedException
                                    }
                                    // When another request exception
                                    else -> {
                                        Log.d(TAG, "getStreams -> UnknownRequestException")
                                        null
                                    }
                                }
                            }
                        }
                        // When another request exception
                        else -> {
                            Log.d(TAG, "getStreams -> UnknownRequestException")
                            null
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "getStreams -> UnknownException: ${t.message}")
                    null
                }
            }
        }
    }
    /** Obtain Streams Response from Twitch by API */
    @Throws(UnauthorizedException::class)
    suspend fun getStreamsResponse (cursor: String? = null): StreamsResponse {
        return httpClient.get(Endpoints.getTwitchStreamsUrl()) {
            header("Authorization", "Bearer ${SessionManager().getAccessToken()}")
            header("Client-Id", clientID)
            parameter("after", cursor)
        }
    }
    /** Gets Current Authorized User on Twitch */
    @Throws(UnauthorizedException::class)
    suspend fun getUser(): UserResponse? {
        // Try to get User from Twitch
        try {
            return getUserResponse()
        } catch (t: Throwable) {
            return when (t) {
                is ClientRequestException -> {
                    return when (t.response?.status?.value) {
                        // When 401 code request exception (unauthorized exception)
                        401 -> {
                            Log.d(TAG, "getUser -> UnauthorizedException. So, trying renew tokens...")
                            // Clear accessToken and set false to user login
                            SessionManager().clearAccessToken()
                            // Try to refresh Access Token
                            refreshAccessToken()
                            try {
                                // Try to get User again
                                getUserResponse()
                            } catch (e: ClientRequestException) {
                                return when (t.response?.status?.value) {
                                    // When 401 code request exception again (unauthorized exception)
                                    401 -> {
                                        Log.d(TAG, "getUser -> UnauthorizedException")
                                        // Remove all Cookies, clear Access Token and open LoginActivity for try login again
                                        SessionManager().logoutSession()
                                        throw UnauthorizedException
                                    }
                                    // When another request exception
                                    else -> {
                                        Log.d(TAG, "getUser -> UnknownRequestException")
                                        null
                                    }
                                }
                            }
                        }
                        // When another request exception
                        else -> {
                            Log.d(TAG, "getUser -> UnknownRequestException")
                            null
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "getUser -> UnknownException")
                    null
                }
            }
        }
    }
    /** Obtain User Response from Twitch by API */
    @Throws(UnauthorizedException::class)
    suspend fun getUserResponse(): UserResponse {
        return httpClient.get(Endpoints.getTwitchUserUrl()) {
            header("Authorization", "Bearer ${SessionManager().getAccessToken()}")
            header("Client-Id", clientID)
        }
    }
    /** Update user description on Twitch */
    @Throws(UnauthorizedException::class)
    suspend fun updateUserDescription(description: String): User? {
        // try to update User Description on Twitch
        try {
            return updateUserDescriptionResponse(description)
        } catch (t: Throwable) {
            return when (t) {
                is ClientRequestException -> {
                    return when (t.response?.status?.value) {
                        // When 401 code request exception (unauthorized exception)
                        401 -> {
                            Log.d(TAG, "updateUserDescription -> UnauthorizedException. So, trying renew tokens...")
                            // Clear accessToken and set false to user login
                            SessionManager().clearAccessToken()
                            // Try to refresh Access Token
                            refreshAccessToken()
                            try {
                                // Try to update user description again
                                updateUserDescriptionResponse(description)
                            } catch (e: ClientRequestException) {
                                return when (t.response?.status?.value) {
                                    // When 401 code request exception again (unauthorized exception)
                                    401 -> {
                                        Log.d(TAG, "updateUserDescription -> UnauthorizedException")
                                        // Remove all Cookies, clear Access Token and open LoginActivity for try login again
                                        SessionManager().logoutSession()
                                        throw UnauthorizedException
                                    }
                                    // When another request exception
                                    else -> {
                                        Log.d(TAG, "updateUserDescription -> UnknownRequestException")
                                        null
                                    }
                                }
                            }
                        }
                        // When another request exception
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
    /** Update user description on Twitch by API */
    @Throws(UnauthorizedException::class)
    suspend fun updateUserDescriptionResponse(description: String): User {
        return httpClient.put(Endpoints.getTwitchUserUrl()) {
            header("Authorization", "Bearer ${SessionManager().getAccessToken()}")
            header("Client-Id", clientID)
            parameter("description", description)
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
            // If access token was saved correctly, then inform to user by toast, else, load Login Activity
            if (SessionManager().isUserAvailable()) {
                Log.d(TAG, "refreshAccessToken -> accessToken and refreshToken successful renewed")
            } else {
                PEC3App.context.startActivity(Intent(PEC3App.context, LoginActivity::class.java))
            }
        } catch (t: Throwable) {
            when (t) {
                is ClientRequestException -> {
                    when (t.response?.status?.value) {
                        // When 401 code request exception (unauthorized exception)
                        401 -> {
                            Log.d(TAG, "refreshAccessToken -> UnauthorizedException: ${t.message}")
                            // Remove all Cookies, clear Access Token and open LoginActivity for try login again
                            SessionManager().logoutSession()
                            throw UnauthorizedException
                        }
                        // When another request exception
                        else -> {
                            Log.d(TAG, "refreshAccessToken -> UnknownRequestException: ${t.message}")
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "refreshAccessToken -> UnknownException: ${t.message}")
                }
            }
        }
    }
}