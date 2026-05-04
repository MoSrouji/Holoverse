package com.example.holoverse.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test class generates a basic baseline profile for the target package.
 *
 * We recommend you collect a profile for the most common user journeys (ujs).
 * See [the guide on baseline profiles](https://d.android.com/baseline-profiles) for more.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.example.holoverse",
        // Check: Is there any specific condition to wait for or to setup?
        includeInStartupProfile = true
    ) {
        // This block defines the app's critical user journey. Here we are interested in
        // optimizing for app startup. But you can also navigate and scroll through your most important UI.

        // Start default activity
        pressHome()
        startActivityAndWait()

        // TODO: Add more journeys here to be optimized.
        // For example, scroll through a list or navigate to a screen.
    }
}
