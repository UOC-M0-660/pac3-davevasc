package edu.uoc.pac3.data.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by alex on 07/09/2020.
 * Done by david on 27/11/2020.
 * Define User Response data class serializable structure
 */
@Serializable
data class UserResponse(
        val data: List<User>? = null
)
/** Defining User data class */
@Serializable
data class User(
        @SerialName("profile_image_url") val profileImage: String? = null,
        @SerialName("view_count") val viewCount: Int? = null,
        @SerialName("display_name") val userName: String? = null,
        @SerialName("email") val userEmail: String? = null,
        @SerialName("description") val description: String? = null,
        @SerialName("created_at") val createdDate: String? = null
)