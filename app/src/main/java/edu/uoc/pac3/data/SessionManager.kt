package edu.uoc.pac3.data

import android.content.Intent
import android.webkit.CookieManager
import android.widget.Toast
import edu.uoc.pac3.PEC3App
import edu.uoc.pac3.oauth.LoginActivity

/**
 * Created by alex on 06/09/2020.
 * Done by david on 27/11/2020.
 * Define Session Manager for manage session login and logout
 */
class SessionManager {
    /** Obtain if user is available from shared preferences */
    fun isUserAvailable(): Boolean {
        return PEC3App.prefs.user
    }
    /** Obtain Access Token from shared preferences */
    fun getAccessToken(): String? {
        return PEC3App.prefs.access
    }
    /** Save Access Token into shared preferences */
    fun saveAccessToken(accessToken: String) {
        PEC3App.prefs.access = accessToken
        PEC3App.prefs.user = true
    }
    /** Delete Access Token and set user available to false into shared preferences */
    fun clearAccessToken() {
        PEC3App.prefs.access = null
        PEC3App.prefs.user = false
    }
    /** Obtain Refresh Token from shared preferences */
    fun getRefreshToken(): String? {
        return PEC3App.prefs.refresh
    }
    /** Save Refresh Token into shared preferences */
    fun saveRefreshToken(refreshToken: String) {
        PEC3App.prefs.refresh = refreshToken
    }
    /** Delete Refresh Token into shared preferences */
    fun clearRefreshToken() {
        PEC3App.prefs.refresh = null
    }
    /** Logout current session function */
    fun logoutSession() {
        // Remove all cookies
        CookieManager.getInstance().removeAllCookies(null)
        // Delete Access Token and set user available to false
        this.clearAccessToken()
        // Open Login Activity
        PEC3App.context.startActivity(Intent(PEC3App.context, LoginActivity::class.java))
        // Show info message to user: Successfully logged out
        Toast.makeText(PEC3App.context, "Successfully logged out", Toast.LENGTH_SHORT).show()
    }
}