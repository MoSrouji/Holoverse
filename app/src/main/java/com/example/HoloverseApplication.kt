package com.example

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class HoloverseApplication: Application()  {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        applicationScope.launch {
            val config = mapOf(
                "cloud_name" to "dmz782fcx",
                "secure" to true
            )
            MediaManager.init(this@HoloverseApplication, config)
        }
    }
}
