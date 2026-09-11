package com.sect.idle.systems

import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * HapticManagerTest - Unit tests for tactile feedback, vibration patterns,
 * error state resilience, and disabled toggle.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HapticManagerTest {

    private lateinit var context: Context
    private lateinit var hapticManager: HapticManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        hapticManager = HapticManager.get(context)
        hapticManager.isHapticsEnabled = true
    }

    @Test
    fun testSingletonInstanceRetrieval() {
        val instance1 = HapticManager.get(context)
        val instance2 = HapticManager.getInstance()
        assertNotNull(instance1)
        assertNotNull(instance2)
        assertTrue(instance1 === instance2)
    }

    @Test
    fun testAllHapticFeedbackTriggersWithoutCrash() {
        // Execute every tactile response to ensure no NPE or unchecked exceptions
        hapticManager.tap()
        hapticManager.click()
        hapticManager.impact()
        hapticManager.canvasTouch(100f, 200f)
        hapticManager.canvasTouch()
        hapticManager.swordSlash()
        hapticManager.criticalHit()
        hapticManager.breakthroughSuccess()
        hapticManager.error()
    }

    @Test
    fun testDisabledHapticsDoesNotTriggerVibrations() {
        hapticManager.isHapticsEnabled = false
        assertFalse(hapticManager.isHapticsEnabled)

        // These should safely no-op
        hapticManager.tap()
        hapticManager.click()
        hapticManager.impact()
        hapticManager.canvasTouch(50f, 50f)
        hapticManager.swordSlash()
        hapticManager.criticalHit()
        hapticManager.breakthroughSuccess()
        hapticManager.error()
    }

    @Test
    fun testPerformViewHapticWithNullOrNonNullView() {
        // Should not crash on null view
        hapticManager.performViewHaptic(null)

        val view = View(context)
        hapticManager.performViewHaptic(view)
    }
}
