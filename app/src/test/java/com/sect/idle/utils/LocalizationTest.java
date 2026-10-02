package com.sect.idle.utils;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.sect.idle.data.db.entities.DiscipleLifecycleEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Locale;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class LocalizationTest {

    private Context context;
    private LocalizationManager locManager;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        locManager = LocalizationManager.getInstance(context);
    }

    @Test
    public void testSupportedLanguagesList() {
        String[] supported = LocalizationManager.SUPPORTED_LANGUAGES;
        assertNotNull(supported);
        assertEquals(8, supported.length);

        assertTrue(locManager.isSupported(LocalizationManager.LANG_EN));
        assertTrue(locManager.isSupported(LocalizationManager.LANG_JA));
        assertTrue(locManager.isSupported(LocalizationManager.LANG_KO));
        assertTrue(locManager.isSupported(LocalizationManager.LANG_ZH));
        assertTrue(locManager.isSupported(LocalizationManager.LANG_ID));
        assertTrue(locManager.isSupported("id"));
        assertTrue(locManager.isSupported(LocalizationManager.LANG_AR));
        assertTrue(locManager.isSupported(LocalizationManager.LANG_ES));
        assertTrue(locManager.isSupported(LocalizationManager.LANG_PT));
        assertFalse(locManager.isSupported("unsupported_lang"));
    }

    @Test
    public void testLanguageSwitchingAndPersistence() {
        locManager.setLanguage(LocalizationManager.LANG_JA);
        assertEquals(LocalizationManager.LANG_JA, locManager.getCurrentLanguage());
        assertFalse(locManager.isRtl());

        locManager.setLanguage(LocalizationManager.LANG_AR);
        assertEquals(LocalizationManager.LANG_AR, locManager.getCurrentLanguage());
        assertTrue(locManager.isRtl());

        locManager.setLanguage(LocalizationManager.LANG_ID);
        assertEquals(LocalizationManager.LANG_ID, locManager.getCurrentLanguage());
        assertFalse(locManager.isRtl());

        locManager.setLanguage(LocalizationManager.LANG_ZH);
        assertEquals(LocalizationManager.LANG_ZH, locManager.getCurrentLanguage());

        locManager.setLanguage(LocalizationManager.LANG_KO);
        assertEquals(LocalizationManager.LANG_KO, locManager.getCurrentLanguage());

        locManager.setLanguage(LocalizationManager.LANG_ES);
        assertEquals(LocalizationManager.LANG_ES, locManager.getCurrentLanguage());

        locManager.setLanguage(LocalizationManager.LANG_PT);
        assertEquals(LocalizationManager.LANG_PT, locManager.getCurrentLanguage());

        // Fallback on invalid code
        locManager.setLanguage("xyz");
        assertEquals(LocalizationManager.LANG_EN, locManager.getCurrentLanguage());
    }

    @Test
    public void testNativeDisplayNames() {
        assertEquals("English", LocalizationManager.getLanguageNativeName(LocalizationManager.LANG_EN));
        assertEquals("日本語 (Japanese)", LocalizationManager.getLanguageNativeName(LocalizationManager.LANG_JA));
        assertEquals("한국어 (Korean)", LocalizationManager.getLanguageNativeName(LocalizationManager.LANG_KO));
        assertEquals("简体中文 (Chinese)", LocalizationManager.getLanguageNativeName(LocalizationManager.LANG_ZH));
        assertEquals("Bahasa Indonesia", LocalizationManager.getLanguageNativeName(LocalizationManager.LANG_ID));
        assertEquals("العربية (Arabic)", LocalizationManager.getLanguageNativeName(LocalizationManager.LANG_AR));
        assertEquals("Español (Spanish)", LocalizationManager.getLanguageNativeName(LocalizationManager.LANG_ES));
        assertEquals("Português (Portuguese)", LocalizationManager.getLanguageNativeName(LocalizationManager.LANG_PT));
    }

    @Test
    public void testLocalizedRealmNames() {
        for (int r = 0; r <= 9; r++) {
            String realmName = locManager.getLocalizedRealmName(r);
            assertNotNull(realmName);
            assertFalse(realmName.isEmpty());
        }
    }

    @Test
    public void testLocalizedStateNames() {
        int[] states = new int[] {
                DiscipleLifecycleEntity.STATE_IDLE,
                DiscipleLifecycleEntity.STATE_RESTING,
                DiscipleLifecycleEntity.STATE_EATING,
                DiscipleLifecycleEntity.STATE_CULTIVATING,
                DiscipleLifecycleEntity.STATE_SECT_DUTY,
                DiscipleLifecycleEntity.STATE_BREAKTHROUGH,
                DiscipleLifecycleEntity.STATE_TRIBULATION,
                DiscipleLifecycleEntity.STATE_QI_DEVIATION,
                DiscipleLifecycleEntity.STATE_INJURED,
                DiscipleLifecycleEntity.STATE_ASCENDED,
                DiscipleLifecycleEntity.STATE_DECEASED
        };

        for (int s : states) {
            String stateName = locManager.getLocalizedStateName(s);
            assertNotNull(stateName);
            assertFalse(stateName.isEmpty());
        }
    }

    @Test
    public void testLocalizedRankAndElementNames() {
        for (int rank = 0; rank <= 7; rank++) {
            String rankName = locManager.getLocalizedRankName(rank);
            assertNotNull(rankName);
            assertFalse(rankName.isEmpty());
        }

        for (int elem = 0; elem <= 6; elem++) {
            String elemName = locManager.getLocalizedElementalName(elem);
            assertNotNull(elemName);
            assertFalse(elemName.isEmpty());
        }
    }
}
