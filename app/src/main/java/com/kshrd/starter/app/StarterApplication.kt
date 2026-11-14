package com.kshrd.starter.app

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import com.kshrd.core.api.ApiLog
import dagger.hilt.android.HiltAndroidApp

/** Hilt entry point. App-wide startup work (logging, crash reporting, schedulers) goes in `onCreate`. */
@HiltAndroidApp
class StarterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Failed v2 calls with their X-Request-Id, for QA (debug builds only).
        val debuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (debuggable) ApiLog.sink = { Log.w("WebBlockApi", it) }
    }
}
