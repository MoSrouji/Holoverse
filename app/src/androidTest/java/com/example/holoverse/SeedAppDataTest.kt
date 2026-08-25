package com.example.holoverse

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.holoverse.core.utils.DummyDataPopulator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeedAppDataTest {

    @Test
    fun seedDummyData() = runBlocking {
        val firestore = FirebaseFirestore.getInstance()
        val populator = DummyDataPopulator(firestore)
        
        println("Starting Seeding Process...")
        populator.populateData()
        println("Seeding Process Finished.")
    }
}
