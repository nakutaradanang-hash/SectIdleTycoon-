package com.sect.idle.utils;

import android.content.Context;
import android.os.Build;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

/**
 * HapticManager - Centralized utility for tactile haptic feedback across mobile devices.
 * Adheres strictly to Android HapticFeedbackConstants and pure Java 7 standards.
 *
 * Provides distinct feedback signatures for:
 * 1. Standard button clicks (VIRTUAL_KEY / KEYBOARD_TAP)
 * 2. Successful breakthrough events (CONFIRM / LONG_PRESS / Sacred Qi Pulse)
 * 3. Critical strikes & battlefield clashes (FLAG_IGNORE_GLOBAL_SETTING)
 * 4. Alchemy cauldron completions & legendary item discoveries
 */
public final class HapticManager {

    private static volatile HapticManager instance;
    private static final Object LOCK = new Object();

    private boolean hapticsEnabled = true;

    private HapticManager() {}

    public static HapticManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new HapticManager();
                }
            }
        }
        return instance;
    }

    public boolean isHapticsEnabled() {
        return hapticsEnabled;
    }

    public void setHapticsEnabled(boolean enabled) {
        this.hapticsEnabled = enabled;
    }

    /**
     * Tactile feedback for standard UI button taps and menu selections.
     */
    public void performClick(View view) {
        if (!hapticsEnabled || view == null) return;
        try {
            int feedback = HapticFeedbackConstants.VIRTUAL_KEY;
            int flags = HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
            view.performHapticFeedback(feedback, flags);
        } catch (Exception ignored) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Exception fallbackIgnored) {}
        }
    }

    /**
     * Tactile feedback for lightweight UI touches and slider changes.
     */
    public void performLightTap(View view) {
        if (!hapticsEnabled || view == null) return;
        try {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        } catch (Exception ignored) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Exception fallbackIgnored) {}
        }
    }

    /**
     * Strong, rewarding tactile resonance for successful disciple realm breakthroughs.
     */
    public void performBreakthroughSuccess(View view) {
        if (!hapticsEnabled || view == null) return;
        try {
            int feedback = HapticFeedbackConstants.LONG_PRESS;
            int flags = HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
            view.performHapticFeedback(feedback, flags);
        } catch (Exception ignored) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            } catch (Exception fallbackIgnored) {}
        }

        // Secondary pulse for immersive Qi ascension feel if Vibrator service is accessible
        try {
            Context ctx = view.getContext();
            if (ctx != null) {
                Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null && v.hasVibrator()) {
                    long[] pattern = {0, 40, 60, 90}; // Quick double-tap sacred pulse
                    v.vibrate(pattern, -1);
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Heavy tactile impact for Critical Strikes and War Campaign victories.
     */
    public void performHeavyImpact(View view) {
        if (!hapticsEnabled || view == null) return;
        try {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        } catch (Exception ignored) {}
    }

    /**
     * Tactile feedback for failed breakthroughs or failed alchemy attempts.
     */
    public void performFailure(View view) {
        if (!hapticsEnabled || view == null) return;
        try {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        } catch (Exception ignored) {}
    }
}
