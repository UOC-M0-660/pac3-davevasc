package edu.uoc.pac3.data

import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.uoc.pac3.PEC3App
import edu.uoc.pac3.data.oauth.OAuthConstants
import edu.uoc.pac3.oauth.LoginActivity
import java.util.*
import kotlin.coroutines.coroutineContext

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
    }

    fun clearRefreshToken() {
        PEC3App.prefs.refresh = null
    }

    fun logoutSession() {
        CookieManager.getInstance().removeAllCookies(null)
        this.clearAccessToken()
        PEC3App.context.startActivity(Intent(PEC3App.context, LoginActivity::class.java))
        Toast.makeText(PEC3App.context, "Successfully logged out", Toast.LENGTH_SHORT).show()
    }

}