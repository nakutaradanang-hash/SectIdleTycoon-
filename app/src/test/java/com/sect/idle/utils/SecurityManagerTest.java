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
 * SecurityManagerTest - Comprehensive unit tests for cryptographic checksum generation,
 * SHA-256 payload signing, constant-time equality validation, and anti-tampering verification.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SecurityManagerTest {

    private SecurityManager securityManager;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        securityManager = SecurityManager.get(context);
        securityManager.reset();
        securityManager = SecurityManager.get(context);
    }

    @Test
    public void testConstantTimeEquals() {
        Assert.assertTrue(SecurityManager.constantTimeEquals("abcdef", "abcdef"));
        Assert.assertTrue(SecurityManager.constantTimeEquals("", ""));
        Assert.assertFalse(SecurityManager.constantTimeEquals("abcdef", "abcdeg"));
        Assert.assertFalse(SecurityManager.constantTimeEquals("short", "longer_string"));
        Assert.assertFalse(SecurityManager.constantTimeEquals(null, "some_str"));
        Assert.assertFalse(SecurityManager.constantTimeEquals("some_str", null));
        Assert.assertTrue(SecurityManager.constantTimeEquals(null, null));
    }

    @Test
    public void testComputeHashAndIntegrity() {
        String testPayload = "Mount Tai Sect|50000|1000|500|2|15000";
        String hash = securityManager.computeHash(testPayload);

        Assert.assertNotNull(hash);
        Assert.assertFalse(hash.isEmpty());

        // Verifying with matching data must succeed
        Assert.assertTrue(securityManager.verifyIntegrity(testPayload, hash));

        // Verifying with tampered data must fail
        String tamperedPayload = "Mount Tai Sect|999999999|1000|500|2|15000";
        Assert.assertFalse(securityManager.verifyIntegrity(tamperedPayload, hash));

        // Verifying with null/empty hash must fail
        Assert.assertFalse(securityManager.verifyIntegrity(testPayload, ""));
        Assert.assertFalse(securityManager.verifyIntegrity(testPayload, null));
    }

    @Test
    public void testSha256Hashing() {
        String data = "SecretDaoistScripture";
        String sha1 = securityManager.computeSha256(data);
        String sha2 = securityManager.computeSha256(data);

        Assert.assertNotNull(sha1);
        Assert.assertEquals(sha1, sha2);
        Assert.assertEquals(64, sha1.length());
    }

    @Test
    public void testSaveAndVerifyHash() {
        String data = "ActiveGameSessionData";
        securityManager.saveHash(data);
        Assert.assertTrue(securityManager.verifyHash(data));

        Assert.assertFalse(securityManager.verifyHash("ModifiedGameSessionData"));
    }

    @Test
    public void testObfuscationRoundtrip() {
        String original = "DaoistCultivationKey_12345";
        String obfuscated = securityManager.obfuscate(original);

        Assert.assertNotNull(obfuscated);
        Assert.assertNotEquals(original, obfuscated);

        String restored = securityManager.deobfuscate(obfuscated);
        Assert.assertEquals(original, restored);
    }
}
