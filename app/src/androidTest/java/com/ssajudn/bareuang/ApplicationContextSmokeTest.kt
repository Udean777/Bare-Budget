package com.ssajudn.bareuang

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Application smoke test for the installed package and runtime context.
 */
@RunWith(AndroidJUnit4::class)
class ApplicationContextSmokeTest {
    @Test
    fun appContext_hasBareuangPackage() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.ssajudn.bareuang", appContext.packageName)
    }
}
