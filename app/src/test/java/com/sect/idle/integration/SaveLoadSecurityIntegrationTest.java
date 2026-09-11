package com.sect.idle.integration;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.sect.idle.gameplay.SaveManager;
import com.sect.idle.gameplay.SectData;
import com.sect.idle.utils.DataValidator;
import com.sect.idle.utils.SecurityManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * SaveLoadSecurityIntegrationTest - Validates end-to-end save/load persistence,
 * cryptographic signature generation and verification, anti-tampering defenses,
 * XSS/SQLi payload sanitization, and fallback recovery.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SaveLoadSecurityIntegrationTest {

    private Context context;
    private SaveManager saveManager;
    private SectData sectData;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        saveManager = new SaveManager(context);
        sectData = SectData.getInstance();
        sectData.reset();
        sectData.sectName = "Immortal Sword Mountain";
        sectData.spiritStones = 25000;
        sectData.spiritHerbs = 1500;
        sectData.spiritOres = 800;
        sectData.sectRealm = 2;
        sectData.sectExp = 5000;
    }

    @Test
    public void testSaveAndLoadFullWorkflow() {
        // Save state
        saveManager.save();

        // Mutate in-memory state
        sectData.sectName = "Corrupted State";
        sectData.spiritStones = 0;
        sectData.sectRealm = 0;

        // Restore from persistent storage
        boolean loaded = saveManager.load();
        Assert.assertTrue("Save load should succeed", loaded);
        Assert.assertEquals("Immortal Sword Mountain", sectData.sectName);
        Assert.assertEquals(25000, sectData.spiritStones);
        Assert.assertEquals(2, sectData.sectRealm);
    }

    @Test
    public void testExportAndImportSaveStringSecurity() {
        String exportedString = saveManager.exportSaveString();
        Assert.assertNotNull(exportedString);
        Assert.assertTrue(exportedString.contains("::SIG::"));

        // Reset state
        sectData.reset();

        // Import valid signed string
        boolean importSuccess = saveManager.importSaveString(exportedString);
        Assert.assertTrue("Import of unaltered signed save string must succeed", importSuccess);
        Assert.assertEquals("Immortal Sword Mountain", sectData.sectName);
        Assert.assertEquals(25000, sectData.spiritStones);
    }

    @Test
    public void testTamperedSaveDataRejection() {
        String exportedString = saveManager.exportSaveString();
        Assert.assertNotNull(exportedString);

        // Tamper with values in the payload (e.g. inject 999999999999 stones)
        String tampered = exportedString.replace("25000", "999999999999");

        boolean tamperedImportSuccess = saveManager.importSaveString(tampered);
        Assert.assertFalse("Tampered save data with invalid signature MUST be rejected", tamperedImportSuccess);
    }

    @Test
    public void testMaliciousPayloadInjectionResistance() {
        // Test XSS injection in save data
        String xssPayload = "<script>alert('pwned')</script>|1000|100|50|0|0::SIG::invalidsig";
        boolean xssResult = saveManager.importSaveString(xssPayload);
        Assert.assertFalse("Malicious XSS script injection must be rejected", xssResult);

        // Test SQL injection in save data
        String sqliPayload = "Tai Sect'; DROP TABLE sect;--|1000|100|50|0|0::SIG::invalidsig";
        boolean sqliResult = saveManager.importSaveString(sqliPayload);
        Assert.assertFalse("SQL injection patterns must be rejected", sqliResult);

        // Test oversize payload resistance
        StringBuilder largePayload = new StringBuilder(600000);
        for (int i = 0; i < 600000; i++) {
            largePayload.append('A');
        }
        boolean largeResult = saveManager.importSaveString(largePayload.toString());
        Assert.assertFalse("Oversized payload exceeding buffer limit must be rejected", largeResult);
    }

    @Test
    public void testCryptographicFunctionsAndConstantTimeComparisons() {
        SecurityManager sec = SecurityManager.get(context);
        String testData = "SecurePayload_12345";

        String hash1 = sec.computeHash(testData);
        String hash2 = sec.computeHash(testData);
        Assert.assertEquals("Deterministic hashing for identical payload", hash1, hash2);

        String sha256 = sec.computeSha256(testData);
        Assert.assertNotNull(sha256);
        Assert.assertTrue(sha256.length() > 0);

        // Constant time comparison checks
        Assert.assertTrue(SecurityManager.constantTimeEquals("SAME_STRING", "SAME_STRING"));
        Assert.assertFalse(SecurityManager.constantTimeEquals("STRING_A", "STRING_B"));
        Assert.assertFalse(SecurityManager.constantTimeEquals("SHORT", "LONG_STRING"));
    }
}
