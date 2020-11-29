package edu.uoc.pac3.data.oauth

/**
 * Created by alex on 24/10/2020.
 * Done by david on 27/11/2020.
 * Define OAuth Exception class for catch unauthorized exceptions
 */
sealed class OAuthException : Throwable()

// Use this exception to indicate user is not authorized
// Can be throw in the network layer and caught in the Activities
object UnauthorizedException : OAuthException()