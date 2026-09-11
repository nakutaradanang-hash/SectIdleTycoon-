package com.sect.idle.integration;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.sect.idle.systems.AudioManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * AudioVisualSyncIntegrationTest - Validates audio manager lifecycle,
 * sound effect dispatching across all game actions, volume controls, audio ducking,
 * and background music theme switching.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AudioVisualSyncIntegrationTest {

    private AudioManager audioManager;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        audioManager = AudioManager.getInstance(context);
    }

    @Test
    public void testAudioVolumeAndDuckingControls() {
        audioManager.setMasterVolume(0.8f);
        audioManager.setBgmVolume(0.7f);
        audioManager.setSfxVolume(0.9f);

        Assert.assertEquals(0.8f, audioManager.getMasterVolume(), 0.01f);
        Assert.assertEquals(0.7f, audioManager.getBgmVolume(), 0.01f);
        Assert.assertEquals(0.9f, audioManager.getSfxVolume(), 0.01f);

        // Test normal effective volume
        float normalEffective = audioManager.getEffectiveBgmVolume();
        Assert.assertTrue(normalEffective > 0f);

        // Test ducking attenuation
        audioManager.setDucking(true);
        float duckedEffective = audioManager.getEffectiveBgmVolume();
        Assert.assertTrue(duckedEffective < normalEffective);

        audioManager.setDucking(false);
    }

    @Test
    public void testThemeSwitchingAndBgmPlayback() {
        audioManager.playBgm(AudioManager.THEME_SECT_PEACE);
        audioManager.playBgm(AudioManager.THEME_COMBAT_INTENSE);
        audioManager.playBgm(AudioManager.THEME_MEDITATION_ZEN);

        Assert.assertTrue(audioManager.isBgmEnabled());

        audioManager.pauseBgm();
        Assert.assertFalse(audioManager.isBgmEnabled());

        audioManager.resumeBgm();
        Assert.assertTrue(audioManager.isBgmEnabled());
    }

    @Test
    public void testSfxDispatchingAcrossGameActions() {
        // Trigger diverse SFX events to verify no crashes or audio buffer overflows
        audioManager.playSfx(AudioManager.SFX_CLICK);
        audioManager.playSfx(AudioManager.SFX_BREAKTHROUGH);
        audioManager.playSfx(AudioManager.SFX_CRITICAL_STRIKE, 1.2f, 1.0f);
        audioManager.playSfx(AudioManager.SFX_BESTOW_PILL);
        audioManager.playSfx(AudioManager.SFX_VICTORY);
        audioManager.playSfx(AudioManager.SFX_DEFEAT);
        audioManager.playSfx(AudioManager.SFX_QI_BURST);
        audioManager.playSfx(AudioManager.SFX_IMMORTAL_BELL);

        Assert.assertTrue(audioManager.isEnabled());
    }
}
