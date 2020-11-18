package edu.uoc.pac3.data

import android.content.Context
import edu.uoc.pac3.PEC3App

/**
 * Created by alex on 06/09/2020.
 */

class SessionManager {

    fun isUserAvailable(): Boolean {
        return PEC3App.prefs.user
    }

    fun getAccessToken(): String? {
        return PEC3App.prefs.access
    }

    fun saveAccessToken(accessToken: String) {
        PEC3App.prefs.access = accessToken
        PEC3App.prefs.user = true
    }

    fun clearAccessToken() {
        PEC3App.prefs.access = null
        PEC3App.prefs.user = false
    }

    fun getRefreshToken(): String? {
        return PEC3App.prefs.refresh
    }

    fun saveRefreshToken(refreshToken: String) {
        PEC3App.prefs.refresh = refreshToken
        PEC3App.prefs.user = true
    }

    fun clearRefreshToken() {
        PEC3App.prefs.refresh = null
        PEC3App.prefs.user = false
    }

}