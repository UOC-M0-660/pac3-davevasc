package edu.uoc.pac3.data.network

/**
 * Created by alex on 07/09/2020.
 */
object Endpoints {

    // OAuth2 API Endpoints
    private const val oauthBaseUrl = "https://id.twitch.tv/oauth2"
    private const val oauthTokenUrl = "https://id.twitch.tv/oauth2/token"

    // Twitch API Endpoints
    private const val twitchBaseUrl = "https://api.twitch.tv/helix"
    private const val twitchStreamsUrl = "https://api.twitch.tv/helix/streams"
    private const val twitchUserUrl = "https://api.twitch.tv/helix/users"

    fun getOauthBaseUrl(): String {
        return oauthBaseUrl
    }

    fun getOauthTokenUrl(): String {
        return oauthTokenUrl
    }

    fun getTwitchStreamsUrl(): String {
        return twitchStreamsUrl
    }

    fun getTwitchUserUrl(): String {
        return twitchUserUrl
    }

}