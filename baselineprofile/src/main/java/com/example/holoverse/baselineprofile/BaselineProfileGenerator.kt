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
        includeInStartupProfile = true
    ) {
        // Start default activity
        pressHome()
        startActivityAndWait()

        // Journey 1: Home Screen Scrolling
        device.waitForIdle()
        val homeList = device.findObject(androidx.test.uiautomator.By.scrollable(true))
        if (homeList != null) {
            homeList.setGestureMargin(device.displayWidth / 5)
            homeList.fling(androidx.test.uiautomator.Direction.DOWN)
            device.waitForIdle()
        }

        // Journey 2: Navigate to 3D Gallery
        val galleryTab = device.findObject(androidx.test.uiautomator.By.desc("Gallery"))
        
        galleryTab?.click()
        device.waitForIdle()

        // Journey 3: Gallery Scrolling
        val galleryList = device.findObject(androidx.test.uiautomator.By.scrollable(true))
        if (galleryList != null) {
            galleryList.setGestureMargin(device.displayWidth / 5)
            galleryList.fling(androidx.test.uiautomator.Direction.DOWN)
            device.waitForIdle()
        }

        // Journey 4: Open 3D Viewer
        // Assuming the first item in the gallery is clickable
        val firstModel = device.findObject(androidx.test.uiautomator.By.desc("Model item"))
        firstModel?.click()
        device.waitForIdle()

        // Journey 5: AR Transition (if supported)
        val arButton = device.findObject(androidx.test.uiautomator.By.text("View in AR"))
        arButton?.click()
        device.waitForIdle()
    }
}
