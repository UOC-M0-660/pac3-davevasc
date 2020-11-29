package edu.uoc.pac3.sharedprefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Done by david on 27/11/2020.
 * Class for manage Shared Preferences */
class Prefs (context: Context) {
    private val prefersNAME = "edu.uoc.pac3"
    private val prefs: SharedPreferences = context.getSharedPreferences(prefersNAME, 0)
    private val argUSER = "is_user_available"
    private val argACC = "access_token"
    private val argREF = "refresh_token"

    // This variable is true when have a user logged and false when no
    var user: Boolean
        get() = prefs.getBoolean(argUSER, false)
        set(value) = prefs.edit().putBoolean(argUSER, value).apply()

    // This variable have access token
    var access: String?
        get() = prefs.getString(argACC, null)
        set(value) = prefs.edit().putString(argACC, value).apply()

    // This variable have refresh token
    var refresh: String?
        get() = prefs.getString(argREF, null)
        set(value) = prefs.edit().putString(argREF, value).apply()
}