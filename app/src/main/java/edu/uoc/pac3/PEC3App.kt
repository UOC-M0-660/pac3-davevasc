package edu.uoc.pac3

import android.app.Application
import android.content.Context
import edu.uoc.pac3.sharedprefs.Prefs

/** Done by david on 27/11/2020.
 * Declare Application Class */
class PEC3App : Application() {

    /** Object with lateinit variables for use in this application */
    companion object {
        // Shared preferences var
        lateinit var prefs: Prefs
        // Context var
        lateinit var context: Context
    }
    /** Application Activity on Create function */
    override fun onCreate() {
        super.onCreate()
        // Set prefs variable
        prefs = Prefs(applicationContext)
        // Set context variable with application context
        context = applicationContext
    }
}