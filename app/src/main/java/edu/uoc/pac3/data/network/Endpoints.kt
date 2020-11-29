package edu.uoc.pac3.data.network

/**
 * Created by alex on 07/09/2020.
 * Done by david on 27/11/2020.
 * Endpoints for connect with Twitch API
 */
object Endpoints {
    // OAuth2 API Endpoints
    private const val oauthBaseUrl = "https://id.twitch.tv/oauth2"
    // Twitch API Endpoints
    private const val twitchBaseUrl = "https://api.twitch.tv/helix"

    /** Get Oauth Token URL for Twitch API */
    fun getOauthTokenUrl(): String {
        return "$oauthBaseUrl/token"
    }
    /** Get Streams URL for Twitch API */
    fun getTwitchStreamsUrl(): String {
        return "$twitchBaseUrl/streams"
    }
    /** Get User URL for Twitch API */
    fun getTwitchUserUrl(): String {
        return "$twitchBaseUrl/users"
    }
}