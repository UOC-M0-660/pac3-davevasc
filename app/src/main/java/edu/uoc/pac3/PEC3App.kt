package edu.uoc.pac3

import android.app.Application
import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import android.widget.Toast
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.oauth.LoginActivity
import edu.uoc.pac3.sharedprefs.Prefs

/** Done by david on 27/11/2020.
 * Declare Application Class */
class PEC3App : Application() {

    /** Object with lateinit variables for use in this application */
    companion object {
        // Shared preferences var
        lateinit var prefs: Prefs
        lateinit var context: Context
    }
    /** Application Activity on Create function */
    override fun onCreate() {
        super.onCreate()
        // Set prefs variable
        prefs = Prefs(applicationContext)
        context = applicationContext
    }
    /** Logout current session function */
    fun logoutSession() {
        // Remove all cookies
        CookieManager.getInstance().removeAllCookies(null)
        // Delete Access Token and set user available to false
        SessionManager().clearAccessToken()
        // Open Login Activity
        startActivity(Intent(applicationContext, LoginActivity::class.java))
        // Show info message to user: Successfully logged out
        Toast.makeText(applicationContext, "Successfully logged out", Toast.LENGTH_SHORT).show()
    }
}