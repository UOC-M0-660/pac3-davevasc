package edu.uoc.pac3.data.streams

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by alex on 07/09/2020.
 * Done by david on 27/11/2020.
 * Define Streams Response data class serializable structure
 */
@Serializable
data class StreamsResponse(
        val data: List<Stream>? = null,
        val pagination: Cursor? = null
)
/** Defining Stream data class */
@Serializable
data class Stream(
        @SerialName("user_name") val userName: String? = null,
        @SerialName("title") val title: String? = null,
        @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
        @SerialName("viewer_count") val viewerCount: Int? = null,
        @SerialName("cursor") val cursor: String? = null
)
/** Defining Cursor data class */
@Serializable
data class Cursor(
        @SerialName("cursor") val cursor: String? = null
)

