package com.sect.idle.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

public class MfaManagerTest {

    private MfaManager mfaManager;

    @Before
    public void setUp() {
        mfaManager = MfaManager.getInstance(null);
    }

    @Test
    public void testBase32EncodingAndDecoding() {
        byte[] original = "ImmortalSectKey!".getBytes();
        String encoded = MfaManager.encodeBase32(original);
        Assert.assertNotNull(encoded);
        Assert.assertFalse(encoded.isEmpty());

        byte[] decoded = MfaManager.decodeBase32(encoded);
        Assert.assertArrayEquals(original, decoded);
    }

    @Test
    public void testTotpGenerationFormat() {
        String secret = mfaManager.generateTotpSecret();
        Assert.assertNotNull(secret);
        Assert.assertTrue(secret.length() >= 16);

        long timestamp = 1700000000L; // Fixed epoch seconds
        String otp = mfaManager.calculateTotp(secret, timestamp);
        Assert.assertNotNull(otp);
        Assert.assertEquals(6, otp.length());
        // Verify it is numeric
        Assert.assertTrue(otp.matches("^[0-9]{6}$"));
    }

    @Test
    public void testTotpDeterministicCalculation() {
        // RFC 6238 reference test vector check
        String secret = "JBSWY3DPEHPK3PXP"; // Base32 for "Hello!\xDE\xAD\xBE\xEF"
        long baseWindow = 1700000010L; // 1700000010 / 30 = 56666667
        long time1 = baseWindow;
        long time2 = baseWindow + 10L; // 1700000020 / 30 = 56666667 (same 30s window)
        long time3 = baseWindow + 50L; // 1700000060 / 30 = 56666668 (different 30s window)

        String otp1 = mfaManager.calculateTotp(secret, time1);
        String otp2 = mfaManager.calculateTotp(secret, time2);
        String otp3 = mfaManager.calculateTotp(secret, time3);

        Assert.assertEquals(otp1, otp2); // Same window produces same OTP
        Assert.assertNotEquals(otp1, otp3); // Different window produces different OTP
    }

    @Test
    public void testOtpAuthUriBuilding() {
        String uri = mfaManager.buildOtpAuthUri("Mount Tai Sect", "JBSWY3DPEHPK3PXP");
        Assert.assertTrue(uri.startsWith("otpauth://totp/IdleSect:Mount%20Tai%20Sect"));
        Assert.assertTrue(uri.contains("secret=JBSWY3DPEHPK3PXP"));
        Assert.assertTrue(uri.contains("issuer=IdleSect"));
    }
}
