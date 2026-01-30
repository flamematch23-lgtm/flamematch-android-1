package com.flamematch.app

import android.app.Application
import com.google.firebase.FirebaseApp

class FlameMatchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
