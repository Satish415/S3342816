package uk.ac.tees.mad.servicescout

import android.app.Application
import android.content.Context

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: App? = null

        val context: Context
            get() = instance!!.applicationContext
    }
}