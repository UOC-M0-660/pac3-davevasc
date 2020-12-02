package edu.uoc.pac3.data.network

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Created by alex on 07/09/2020.
 * Done by david on 27/11/2020.
 * Network definition creating using Ktor Http Client.
 */
object Network {

    // Log TAG
    private const val TAG = "Ktor"

    /** Creating and setup Ktor Http Client */
    fun createHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            // Json
            install(JsonFeature) {
                serializer = KotlinxSerializer(json)
            }
            // Logging
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.v(TAG, message)
                    }
                }
                level = LogLevel.ALL
            }
            // Timeout
            install(HttpTimeout) {
                requestTimeoutMillis = 15000L
                connectTimeoutMillis = 15000L
                socketTimeoutMillis = 15000L
            }
            // Interceptor
            engine {
                // Add Error Interceptor to network request
                addInterceptor(ErrorInterceptor())
            }
            // Apply to All Requests
            defaultRequest {
                // Content Type
                if (this.method != HttpMethod.Get) contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
    }
    /** Json kotlin serialization attributes */
    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }
}