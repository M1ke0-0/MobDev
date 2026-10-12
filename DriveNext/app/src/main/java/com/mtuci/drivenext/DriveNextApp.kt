package com.mtuci.drivenext

import android.app.Application
import com.mtuci.drivenext.di.AppContainer

class DriveNextApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}
