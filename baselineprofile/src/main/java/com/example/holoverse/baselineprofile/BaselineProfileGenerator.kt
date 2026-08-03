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
            homeList.fling(androidx.test.uiautomator.Direction.UP)
            device.waitForIdle()
        }

        // Journey 2: Search Journey
        val searchBar = device.findObject(androidx.test.uiautomator.By.textContains("Search for"))
        searchBar?.click()
        device.waitForIdle()
        val searchInput = device.findObject(androidx.test.uiautomator.By.clazz("android.widget.EditText"))
        searchInput?.text = "Android"
        device.waitForIdle()
        device.pressBack() // Close search
        device.waitForIdle()

        // Journey 3: Tab Switching
        val yourCoursesTab = device.findObject(androidx.test.uiautomator.By.text("Your Courses"))
        yourCoursesTab?.click()
        device.waitForIdle()
        val exploreTab = device.findObject(androidx.test.uiautomator.By.text("Explore"))
        exploreTab?.click()
        device.waitForIdle()

        // Journey 4: Navigate to 3D Gallery
        val galleryTab = device.findObject(androidx.test.uiautomator.By.desc("Gallery"))
        galleryTab?.click()
        device.waitForIdle()

        // Journey 5: Gallery Scrolling
        val galleryList = device.findObject(androidx.test.uiautomator.By.scrollable(true))
        if (galleryList != null) {
            galleryList.setGestureMargin(device.displayWidth / 5)
            galleryList.fling(androidx.test.uiautomator.Direction.DOWN)
            device.waitForIdle()
        }

        // Journey 6: Open 3D Viewer
        val firstModel = device.findObject(androidx.test.uiautomator.By.desc("Model item"))
        firstModel?.click()
        device.waitForIdle()

        // Journey 7: Navigate to Profile
        device.pressBack() // Back to gallery
        device.waitForIdle()
        val profileTab = device.findObject(androidx.test.uiautomator.By.desc("Profile"))
        profileTab?.click()
        device.waitForIdle()
    }
}
