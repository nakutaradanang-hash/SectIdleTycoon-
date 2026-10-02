package com.sect.idle.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EncryptionManagerTest {

    private EncryptionManager encryptionManager;

    @Before
    public void setUp() {
        encryptionManager = EncryptionManager.getInstance(null);
    }

    @Test
    public void testGcmEncryptionAndDecryption() {
        String original = "{\"sectName\":\"Celestial Realm\",\"spiritStones\":999999,\"disciples\":[{\"name\":\"Li Xiao\",\"rank\":3}]}";
        String passphrase = "SuperSecretMasterKey123!";

        String ciphertext = encryptionManager.encryptGcm(original, passphrase);
        Assert.assertNotNull(ciphertext);
        Assert.assertFalse(ciphertext.isEmpty());
        Assert.assertNotEquals(original, ciphertext);

        String decrypted = encryptionManager.decryptGcm(ciphertext, passphrase);
        Assert.assertEquals(original, decrypted);
    }

    @Test
    public void testCbcHmacEncryptionAndDecryption() {
        String original = "Confidential Sect Scroll Dao Formula #42";
        String passphrase = "GrandmasterSecretPassword";

        String ciphertext = encryptionManager.encryptCbcHmac(original, passphrase);
        Assert.assertNotNull(ciphertext);
        Assert.assertFalse(ciphertext.isEmpty());

        String decrypted = encryptionManager.decryptCbcHmac(ciphertext, passphrase);
        Assert.assertEquals(original, decrypted);
    }

    @Test
    public void testSaveStateEncryptionIntegrity() {
        String saveData = "{\"version\":1,\"sectPower\":128000,\"dragonVeinLevel\":4}";

        String encrypted = encryptionManager.encryptSaveState(saveData);
        Assert.assertNotNull(encrypted);
        Assert.assertFalse(encrypted.isEmpty());

        String decrypted = encryptionManager.decryptSaveState(encrypted);
        Assert.assertEquals(saveData, decrypted);
    }

    @Test
    public void testTamperDetectionRejectsCorruptedCiphertext() {
        String original = "Valuable Spirit Pills Reserve: 500";
        String passphrase = "PassphraseAntiTamper";

        String ciphertext = encryptionManager.encryptCbcHmac(original, passphrase);
        Assert.assertNotNull(ciphertext);

        // Tamper with the ciphertext by flipping characters
        char[] chars = ciphertext.toCharArray();
        chars[chars.length - 5] = (char) (chars[chars.length - 5] ^ 0x05);
        String tamperedCiphertext = new String(chars);

        String decrypted = encryptionManager.decryptCbcHmac(tamperedCiphertext, passphrase);
        // Tampered ciphertext must be rejected (returns empty string)
        Assert.assertTrue(decrypted.isEmpty());
    }

    @Test
    public void testConstantTimeEquals() {
        byte[] a = "ImmortalKey2026".getBytes();
        byte[] b = "ImmortalKey2026".getBytes();
        byte[] c = "DifferentKeyDAO".getBytes();

        Assert.assertTrue(EncryptionManager.constantTimeEquals(a, b));
        Assert.assertFalse(EncryptionManager.constantTimeEquals(a, c));
        Assert.assertFalse(EncryptionManager.constantTimeEquals(a, null));
        Assert.assertTrue(EncryptionManager.constantTimeEquals((byte[]) null, (byte[]) null));
    }

    @Test
    public void testMemoryZeroization() {
        byte[] secret = new byte[]{1, 2, 3, 4, 5};
        EncryptionManager.zeroize(secret);
        for (byte b : secret) {
            Assert.assertEquals(0, b);
        }

        char[] chars = new char[]{'a', 'b', 'c'};
        EncryptionManager.zeroize(chars);
        for (char c : chars) {
            Assert.assertEquals('\0', c);
        }
    }
}
