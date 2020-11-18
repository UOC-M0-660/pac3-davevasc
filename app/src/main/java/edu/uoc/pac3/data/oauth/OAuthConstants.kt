package edu.uoc.pac3.data.oauth

import android.net.Uri
import java.util.*

/**
 * Created by alex on 07/09/2020.
 */
object OAuthConstants {

    // TODO: Set OAuth2 Variables
    const val clientID = "4swfvnoaa9r26ev28rpk7s61i8u237"
    const val clientSecret = "lgw3fffrrq8085ywupl16u7361sjom"
    const val redirectUri = "http://localhost"
    const val authorizationUrl = "https://id.twitch.tv/oauth2/authorize"
    val scopes: List<String> = listOf("user:read:email", "user:edit")
    val uniqueState: String = UUID.randomUUID().toString()

}