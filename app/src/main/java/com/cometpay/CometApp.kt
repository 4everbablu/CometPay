package com.cometpay

import android.app.Application
import com.cometpay.ussd.UssdEngine

// process-scoped engine, activity ka nahi
class CometApp : Application() {

    lateinit var ussdEngine: UssdEngine
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        ussdEngine = UssdEngine(this)
    }

    companion object {
        lateinit var instance: CometApp
            private set
    }
}
