package edu.uoc.pac3

import android.app.Application
import android.content.Context
import android.webkit.WebStorage
import edu.uoc.pac3.sharedprefs.Prefs

class PEC3App : Application() {

    companion object {
        lateinit var prefs: Prefs
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(applicationContext)
        context = applicationContext
    }

}