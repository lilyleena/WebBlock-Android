package com.kshrd.starter.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Hilt entry point. App-wide startup work (logging, crash reporting, schedulers) goes in `onCreate`. */
@HiltAndroidApp
class StarterApplication : Application()
