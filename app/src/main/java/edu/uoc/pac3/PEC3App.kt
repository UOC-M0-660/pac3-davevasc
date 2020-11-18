package edu.uoc.pac3

import android.app.Application
import edu.uoc.pac3.sharedprefs.Prefs

class PEC3App : Application() {
    companion object {
        lateinit var prefs: Prefs
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(applicationContext)
    }
}