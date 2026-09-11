package com.sect.idle.gameplay;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SecretManagerTest - Tests promo code redemption, duplicate prevention, and input sanitization.
 */
public class SecretManagerTest {

    @Test
    public void testRedeemPromoCode_Success() {
        SecretManager.RedeemResult res = SecretManager.redeemCode("DAO777");
        assertTrue(res.success || res.message.contains("already been redeemed"));
    }

    @Test
    public void testRedeemPromoCode_InvalidFormat() {
        SecretManager.RedeemResult res = SecretManager.redeemCode("<script>alert(1)</script>");
        assertFalse(res.success);
        assertTrue(res.message.contains("Invalid") || res.message.contains("Unknown"));
    }

    @Test
    public void testRedeemPromoCode_UnknownCode() {
        SecretManager.RedeemResult res = SecretManager.redeemCode("NONEXISTENTCODE999");
        assertFalse(res.success);
        assertTrue(res.message.contains("Unknown"));
    }

    @Test
    public void testRedeemPromoCode_NullOrEmpty() {
        SecretManager.RedeemResult res1 = SecretManager.redeemCode(null);
        assertFalse(res1.success);

        SecretManager.RedeemResult res2 = SecretManager.redeemCode("   ");
        assertFalse(res2.success);
    }
}
