package com.sect.idle.systems

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import java.lang.ref.WeakReference

/**
 * HapticManager - Centralized tactile and haptic feedback manager.
 * Supports smooth, tactile responses for game canvas taps, menu button clicks,
 * combat strikes, and breakthrough milestone events.
 *
 * Fully backward-compatible across Android 5.0+ (API 21) to Android 15 (API 35+).
 */
class HapticManager private constructor(context: Context) {

    private val appContext: Context = context.applicationContext
    private var vibrator: Vibrator? = null
    var isHapticsEnabled: Boolean = true

    init {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (ignored: Throwable) {
            vibrator = null
        }
    }

    companion object {
        @Volatile
        private var instance: HapticManager? = null

        @JvmStatic
        fun get(context: Context): HapticManager {
            return instance ?: synchronized(this) {
                instance ?: HapticManager(context).also { instance = it }
            }
        }

        @JvmStatic
        fun getInstance(): HapticManager? = instance
    }

    /**
     * Subtle light tap feedback for standard button clicks and menu item selections.
     */
    fun tap() {
        if (!isHapticsEnabled) return
        vibrate(durationMs = 12L, amplitude = 40, effectType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK else null)
    }

    /**
     * Standard UI click tactile feedback.
     */
    fun click() {
        if (!isHapticsEnabled) return
        vibrate(durationMs = 20L, amplitude = 80, effectType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_CLICK else null)
    }

    /**
     * Medium impact feedback for combat hits, weapon swings, and building upgrades.
     */
    fun impact() {
        if (!isHapticsEnabled) return
        vibrate(durationMs = 35L, amplitude = 160, effectType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_HEAVY_CLICK else null)
    }

    /**
     * Subtle haptic feedback for game canvas touch and drag events.
     */
    @JvmOverloads
    fun canvasTouch(x: Float = 0f, y: Float = 0f) {
        if (!isHapticsEnabled) return
        vibrate(durationMs = 8L, amplitude = 25, effectType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK else null)
    }

    /**
     * Tactile sword slash vibration pulse for combat attacks and mini-games.
     */
    fun swordSlash() {
        if (!isHapticsEnabled) return
        vibrate(durationMs = 15L, amplitude = 90, effectType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK else null)
    }

    /**
     * Double tactile impulse for critical combat strikes or rare item drops.
     */
    fun criticalHit() {
        if (!isHapticsEnabled) return
        vibratePattern(longArrayOf(0L, 25L, 40L, 35L), intArrayOf(0, 180, 0, 255))
    }

    /**
     * Celebratory celebratory rhythmic vibration for Realm Breakthroughs and milestone conquests.
     */
    fun breakthroughSuccess() {
        if (!isHapticsEnabled) return
        vibratePattern(
            longArrayOf(0L, 40L, 60L, 50L, 60L, 80L),
            intArrayOf(0, 120, 0, 180, 0, 255)
        )
    }

    /**
     * Warning or error haptic pattern for insufficient resources or rejected actions.
     */
    fun error() {
        if (!isHapticsEnabled) return
        vibratePattern(longArrayOf(0L, 30L, 70L, 30L), intArrayOf(0, 150, 0, 150))
    }

    /**
     * Performs direct view-based tactile feedback if available.
     */
    fun performViewHaptic(view: View?, feedbackConstant: Int = HapticFeedbackConstants.KEYBOARD_TAP) {
        if (!isHapticsEnabled || view == null) return
        try {
            view.performHapticFeedback(feedbackConstant, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
        } catch (ignored: Throwable) {
            tap()
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int, effectType: Int? = null) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && effectType != null) {
                v.vibrate(VibrationEffect.createPredefined(effectType))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmp = amplitude.coerceIn(1, 255)
                v.vibrate(VibrationEffect.createOneShot(durationMs, clampedAmp))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(durationMs)
            }
        } catch (ignored: Throwable) {}
    }

    private fun vibratePattern(timings: LongArray, amplitudes: IntArray) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && v.hasAmplitudeControl()) {
                v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(timings, -1)
            }
        } catch (ignored: Throwable) {}
    }
}
