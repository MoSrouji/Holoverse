package com.example

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HoloverseApplication: Application()  {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val config = mapOf(
            "cloud_name" to "dmz782fcx",
            "secure" to true
        )
        MediaManager.init(this, config)

    }

}