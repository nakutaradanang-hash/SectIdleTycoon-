package com.sect.idle.utils;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * DataValidatorSecurityEdgeCasesTest - Comprehensive unit and edge-case testing
 * for input sanitization, XSS and SQL injection defenses, path traversal immunity,
 * integer range protection, and cryptographic checksum validation.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class DataValidatorSecurityEdgeCasesTest {

    private Context context;
    private SecurityManager securityManager;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        securityManager = SecurityManager.get(context);
    }

    @Test
    public void testSanitizeStringEdgeCases() {
        // Null and empty strings
        Assert.assertEquals("", DataValidator.sanitizeString(null, 32));
        Assert.assertEquals("", DataValidator.sanitizeString("", 32));
        Assert.assertEquals("", DataValidator.sanitizeString("   ", 32));

        // Normal clean string
        Assert.assertEquals("Cloud Peak Sect", DataValidator.sanitizeString("Cloud Peak Sect", 32));

        // Pipe character stripping (used in internal delimiter serialization)
        Assert.assertEquals("CleanData", DataValidator.sanitizeString("Clean|Data", 32));

        // Truncation check
        Assert.assertEquals("12345", DataValidator.sanitizeString("1234567890", 5));

        // Control characters & non-printable ASCII
        String rawControl = "Sect\u0000\u0007\u001FName";
        String stripped = DataValidator.stripControlCharacters(rawControl);
        Assert.assertEquals("SectName", stripped);
    }

    @Test
    public void testXssAndHtmlInjectionVectors() {
        String rawScript = "<script>alert('XSS')</script>";
        Assert.assertTrue(DataValidator.containsXss(rawScript));

        String stripped = DataValidator.stripHtmlTags(rawScript);
        Assert.assertFalse(stripped.contains("<script>"));

        String encoded = DataValidator.encodeHtml("<script>");
        Assert.assertEquals("&lt;script&gt;", encoded);

        // Nested and Obfuscated Tags
        String rawNested = "<img src='x' onerror='alert(1)'>";
        Assert.assertTrue(DataValidator.containsXss(rawNested));
    }

    @Test
    public void testContainsSqlInjectionVectors() {
        // Safe strings
        Assert.assertFalse(DataValidator.containsSqlInjection("My Heroic Cultivator"));
        Assert.assertFalse(DataValidator.containsSqlInjection("Spirit Herb Garden"));

        // SQL injection vectors
        Assert.assertTrue(DataValidator.containsSqlInjection("admin' OR '1'='1"));
        Assert.assertTrue(DataValidator.containsSqlInjection("1; DROP TABLE sect_state;--"));
        Assert.assertTrue(DataValidator.containsSqlInjection("UNION SELECT * FROM sqlite_master"));
        Assert.assertTrue(DataValidator.containsSqlInjection("/* SQL comment */ SELECT"));
        Assert.assertTrue(DataValidator.containsSqlInjection("EXEC sp_executesql"));

        // Escape SQL
        String escaped = DataValidator.escapeSql("O'Reilly");
        Assert.assertEquals("O''Reilly", escaped);
    }

    @Test
    public void testPathTraversalAndFilenameSanitization() {
        // Safe filenames
        Assert.assertTrue(DataValidator.isSafeFilePath("savegame_01.dat"));
        Assert.assertTrue(DataValidator.isSafeFilePath("profile_backup.json"));

        // Traversal attempts
        Assert.assertFalse(DataValidator.isSafeFilePath("../../../etc/passwd"));
        Assert.assertFalse(DataValidator.isSafeFilePath("..\\..\\Windows\\System32"));
        Assert.assertFalse(DataValidator.isSafeFilePath("save.dat\u0000.txt"));

        // Filename sanitization
        String sanitized = DataValidator.sanitizeFileName("../../malicious.dat", 32);
        Assert.assertEquals("malicious.dat", sanitized);
    }

    @Test
    public void testDataValidationRangesAndTypes() {
        Assert.assertTrue(DataValidator.isValidName("Immortal Sect"));
        Assert.assertFalse(DataValidator.isValidName(""));
        Assert.assertFalse(DataValidator.isValidName("<script>"));

        // Resource validation
        Assert.assertTrue(DataValidator.validateResourceAmount(5000L, 1000000L));
        Assert.assertFalse(DataValidator.validateResourceAmount(-1L, 1000000L));
        Assert.assertFalse(DataValidator.validateResourceAmount(2000000L, 1000000L));

        // Disciple validation
        DataValidator.ValidationResult result = DataValidator.validateDiscipleData("Han Li", 2, 100, 100, 1);
        Assert.assertTrue(result.valid);

        DataValidator.ValidationResult invalidRealm = DataValidator.validateDiscipleData("Han Li", 99, 100, 100, 1);
        Assert.assertFalse(invalidRealm.valid);
    }

    @Test
    public void testSecurityManagerIntegrityVerification() {
        String payload = "{\"sectName\":\"Jade Pavilion\",\"spiritStones\":50000}";

        // Compute valid signature
        String signature = securityManager.computeHash(payload);
        Assert.assertNotNull(signature);
        Assert.assertTrue(signature.length() > 0);

        // Verify valid payload
        boolean valid = securityManager.verifyIntegrity(payload, signature);
        Assert.assertTrue("Valid data and signature must pass verification", valid);

        // Tampered payload
        String tamperedPayload = "{\"sectName\":\"Jade Pavilion\",\"spiritStones\":999999999}";
        boolean tamperedValid = securityManager.verifyIntegrity(tamperedPayload, signature);
        Assert.assertFalse("Tampered payload must fail verification", tamperedValid);

        // Corrupted signature
        boolean corruptSigValid = securityManager.verifyIntegrity(payload, "invalid_signature_checksum");
        Assert.assertFalse("Invalid signature must fail verification", corruptSigValid);

        // Obfuscation and Deobfuscation
        String secret = "ImmortalSecretKey123";
        String obfuscated = securityManager.obfuscate(secret);
        Assert.assertNotEquals(secret, obfuscated);
        String restored = securityManager.deobfuscate(obfuscated);
        Assert.assertEquals(secret, restored);

        // Constant time equals
        Assert.assertTrue(SecurityManager.constantTimeEquals("secret123", "secret123"));
        Assert.assertFalse(SecurityManager.constantTimeEquals("secret123", "secret456"));
        Assert.assertFalse(SecurityManager.constantTimeEquals("secret123", "short"));
    }
}
