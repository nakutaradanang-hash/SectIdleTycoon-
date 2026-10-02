package com.sect.idle.security;

import android.content.Context;
import android.content.SharedPreferences;
import com.sect.idle.utils.ExceptionManager;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * EncryptionManager - Enterprise-grade Multi-Layer Data Encryption Engine.
 *
 * Capabilities:
 * - Authenticated AES-256-GCM (Galois/Counter Mode with 128-bit authentication tag)
 * - Authenticated AES-256-CBC with HMAC-SHA256 fallback for compatibility
 * - PBKDF2WithHmacSHA256 key derivation with 10,000 iterations & cryptographic salt
 * - Anti-tampering verification: detect any unauthorized modification of save records
 * - Sensitive memory zeroization (RAM scrubbing)
 * - Pure Java 7 & Sketchware Pro v7.0.0 Compatible (Embedded Pure Base64).
 */
public final class EncryptionManager {

    private static final String PREFS_NAME = "IdleSectEncryptedVault";
    private static final String KEY_VAULT_SALT = "vault_master_salt";
    private static final String KEY_DEVICE_PEPPER = "vault_device_pepper";
    private static final int ITERATION_COUNT = 10000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12; // Standard 96-bit nonce
    private static final int CBC_IV_LENGTH_BYTES = 16;
    private static final int SALT_LENGTH_BYTES = 16;

    private static volatile EncryptionManager instance;
    private static final Object LOCK = new Object();

    private final Context appContext;
    private final SharedPreferences prefs;
    private final SecureRandom secureRandom;
    private final byte[] masterSalt;
    private final byte[] devicePepper;

    private EncryptionManager(Context context) {
        this.appContext = context != null ? context.getApplicationContext() : null;
        this.prefs = this.appContext != null ? this.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) : null;
        this.secureRandom = new SecureRandom();

        byte[] loadedSalt = null;
        byte[] loadedPepper = null;

        if (prefs != null) {
            String savedSaltBase64 = prefs.getString(KEY_VAULT_SALT, null);
            if (savedSaltBase64 == null) {
                loadedSalt = new byte[SALT_LENGTH_BYTES];
                secureRandom.nextBytes(loadedSalt);
                prefs.edit().putString(KEY_VAULT_SALT, toBase64(loadedSalt)).apply();
            } else {
                try {
                    loadedSalt = fromBase64(savedSaltBase64);
                } catch (Exception e) {
                    loadedSalt = new byte[SALT_LENGTH_BYTES];
                    secureRandom.nextBytes(loadedSalt);
                }
            }

            String savedPepperBase64 = prefs.getString(KEY_DEVICE_PEPPER, null);
            if (savedPepperBase64 == null) {
                loadedPepper = new byte[SALT_LENGTH_BYTES];
                secureRandom.nextBytes(loadedPepper);
                prefs.edit().putString(KEY_DEVICE_PEPPER, toBase64(loadedPepper)).apply();
            } else {
                try {
                    loadedPepper = fromBase64(savedPepperBase64);
                } catch (Exception e) {
                    loadedPepper = new byte[SALT_LENGTH_BYTES];
                    secureRandom.nextBytes(loadedPepper);
                }
            }
        }

        if (loadedSalt == null || loadedSalt.length == 0) {
            loadedSalt = new byte[SALT_LENGTH_BYTES];
            secureRandom.nextBytes(loadedSalt);
        }
        if (loadedPepper == null || loadedPepper.length == 0) {
            loadedPepper = new byte[SALT_LENGTH_BYTES];
            secureRandom.nextBytes(loadedPepper);
        }

        this.masterSalt = loadedSalt;
        this.devicePepper = loadedPepper;
    }

    public static EncryptionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new EncryptionManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Derives a 256-bit AES key from a passphrase and salt using PBKDF2WithHmacSHA256.
     */
    public SecretKey deriveKey(String passphrase, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (passphrase == null) passphrase = "DefaultImmortalMasterPassphrase";
        if (salt == null || salt.length == 0) salt = masterSalt;

        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        }

        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BITS);
        SecretKey secretKey = factory.generateSecret(spec);
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    /**
     * Encrypts plaintext string using AES-256-GCM.
     * Format: [IV: 12 bytes] + [GCM Ciphertext + Tag] (Base64 encoded)
     */
    public String encryptGcm(String plaintext, String passphrase) {
        if (plaintext == null || plaintext.isEmpty()) return "";
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            SecretKey key = deriveKey(passphrase, masterSalt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] plaintextBytes = plaintext.getBytes(Charset.forName("UTF-8"));
            byte[] cipherText = cipher.doFinal(plaintextBytes);

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            zeroize(plaintextBytes);
            return toBase64(combined);
        } catch (Exception e) {
            ExceptionManager.logE("EncryptionManager", "GCM Encryption failed, falling back to CBC: " + e.getMessage(), e);
            return encryptCbcHmac(plaintext, passphrase);
        }
    }

    /**
     * Decrypts AES-256-GCM ciphertext.
     */
    public String decryptGcm(String base64Ciphertext, String passphrase) {
        if (base64Ciphertext == null || base64Ciphertext.isEmpty()) return "";
        try {
            byte[] combined = fromBase64(base64Ciphertext);
            if (combined == null || combined.length < GCM_IV_LENGTH_BYTES + 16) {
                return decryptCbcHmac(base64Ciphertext, passphrase);
            }

            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH_BYTES);

            int cipherLength = combined.length - GCM_IV_LENGTH_BYTES;
            byte[] cipherText = new byte[cipherLength];
            System.arraycopy(combined, GCM_IV_LENGTH_BYTES, cipherText, 0, cipherLength);

            SecretKey key = deriveKey(passphrase, masterSalt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] decrypted = cipher.doFinal(cipherText);
            String result = new String(decrypted, Charset.forName("UTF-8"));
            zeroize(decrypted);
            return result;
        } catch (Exception e) {
            ExceptionManager.logW("EncryptionManager", "GCM Decryption failed, trying CBC fallback: " + e.getMessage());
            return decryptCbcHmac(base64Ciphertext, passphrase);
        }
    }

    /**
     * Encrypts plaintext string using AES-256-CBC with HMAC-SHA256 authentication (Encrypt-then-MAC).
     * Format: [IV: 16 bytes] + [HMAC: 32 bytes] + [Ciphertext] (Base64 encoded)
     */
    public String encryptCbcHmac(String plaintext, String passphrase) {
        if (plaintext == null || plaintext.isEmpty()) return "";
        try {
            byte[] iv = new byte[CBC_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            SecretKey encKey = deriveKey(passphrase, masterSalt);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, encKey, new IvParameterSpec(iv));

            byte[] plaintextBytes = plaintext.getBytes(Charset.forName("UTF-8"));
            byte[] cipherBytes = cipher.doFinal(plaintextBytes);
            zeroize(plaintextBytes);

            // Compute HMAC over IV + CipherText
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKey macKey = new SecretKeySpec(devicePepper, "HmacSHA256");
            hmac.init(macKey);
            hmac.update(iv);
            byte[] hmacBytes = hmac.doFinal(cipherBytes);

            byte[] combined = new byte[iv.length + hmacBytes.length + cipherBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(hmacBytes, 0, combined, iv.length, hmacBytes.length);
            System.arraycopy(cipherBytes, 0, combined, iv.length + hmacBytes.length, cipherBytes.length);

            return toBase64(combined);
        } catch (Exception e) {
            ExceptionManager.logE("EncryptionManager", "CBC Encryption failed: " + e.getMessage(), e);
            return "";
        }
    }

    /**
     * Decrypts AES-256-CBC with HMAC-SHA256 authentication.
     */
    public String decryptCbcHmac(String base64Ciphertext, String passphrase) {
        if (base64Ciphertext == null || base64Ciphertext.isEmpty()) return "";
        try {
            byte[] combined = fromBase64(base64Ciphertext);
            int headerLen = CBC_IV_LENGTH_BYTES + 32; // IV (16) + HMAC-SHA256 (32)
            if (combined == null || combined.length <= headerLen) return "";

            byte[] iv = new byte[CBC_IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, CBC_IV_LENGTH_BYTES);

            byte[] expectedHmac = new byte[32];
            System.arraycopy(combined, CBC_IV_LENGTH_BYTES, expectedHmac, 0, 32);

            int cipherLen = combined.length - headerLen;
            byte[] cipherBytes = new byte[cipherLen];
            System.arraycopy(combined, headerLen, cipherBytes, 0, cipherLen);

            // Verify HMAC-SHA256 (Constant Time)
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKey macKey = new SecretKeySpec(devicePepper, "HmacSHA256");
            hmac.init(macKey);
            hmac.update(iv);
            byte[] actualHmac = hmac.doFinal(cipherBytes);

            if (!constantTimeEquals(expectedHmac, actualHmac)) {
                ExceptionManager.logE("EncryptionManager", "Tampering detected! HMAC mismatch in decryptCbcHmac.");
                return "";
            }

            SecretKey encKey = deriveKey(passphrase, masterSalt);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, encKey, new IvParameterSpec(iv));

            byte[] decrypted = cipher.doFinal(cipherBytes);
            String result = new String(decrypted, Charset.forName("UTF-8"));
            zeroize(decrypted);
            return result;
        } catch (Exception e) {
            ExceptionManager.logE("EncryptionManager", "CBC Decryption failed: " + e.getMessage(), e);
            return "";
        }
    }

    /**
     * High-level helper to encrypt user game save data with default vault key.
     */
    public String encryptSaveState(String jsonSaveData) {
        return encryptGcm(jsonSaveData, "Vault_Sect_Immortal_Key_2026");
    }

    /**
     * High-level helper to decrypt user game save data with default vault key.
     */
    public String decryptSaveState(String encryptedSaveData) {
        return decryptGcm(encryptedSaveData, "Vault_Sect_Immortal_Key_2026");
    }

    /**
     * Constant-time byte array equality check to prevent timing side-channel attacks.
     */
    public static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) return a == b;
        if (a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    /**
     * Securely zeroizes byte array in memory.
     */
    public static void zeroize(byte[] array) {
        if (array != null) {
            Arrays.fill(array, (byte) 0);
        }
    }

    /**
     * Securely zeroizes char array in memory.
     */
    public static void zeroize(char[] array) {
        if (array != null) {
            Arrays.fill(array, '\0');
        }
    }

    // =========================================================================
    // Pure Java Base64 Encoding / Decoding (Zero Android Stub Dependencies)
    // =========================================================================

    private static final char[] B64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final int[] B64_INV = new int[128];

    static {
        Arrays.fill(B64_INV, -1);
        for (int i = 0; i < B64_CHARS.length; i++) {
            B64_INV[B64_CHARS[i]] = i;
        }
        B64_INV['='] = 0;
    }

    public static String toBase64(byte[] data) {
        if (data == null || data.length == 0) return "";
        int len = data.length;
        StringBuilder sb = new StringBuilder(((len + 2) / 3) * 4);

        for (int i = 0; i < len; i += 3) {
            int b0 = data[i] & 0xFF;
            int b1 = (i + 1 < len) ? (data[i + 1] & 0xFF) : 0;
            int b2 = (i + 2 < len) ? (data[i + 2] & 0xFF) : 0;

            sb.append(B64_CHARS[b0 >>> 2]);
            sb.append(B64_CHARS[((b0 & 0x03) << 4) | (b1 >>> 4)]);
            if (i + 1 < len) {
                sb.append(B64_CHARS[((b1 & 0x0F) << 2) | (b2 >>> 6)]);
            } else {
                sb.append('=');
            }
            if (i + 2 < len) {
                sb.append(B64_CHARS[b2 & 0x3F]);
            } else {
                sb.append('=');
            }
        }
        return sb.toString();
    }

    public static byte[] fromBase64(String str) {
        if (str == null || str.isEmpty()) return new byte[0];
        String s = str.trim();
        int pad = 0;
        if (s.endsWith("==")) pad = 2;
        else if (s.endsWith("=")) pad = 1;

        int len = (s.length() * 3 / 4) - pad;
        if (len < 0) return new byte[0];
        byte[] out = new byte[len];
        int outIdx = 0;

        for (int i = 0; i < s.length(); i += 4) {
            if (i + 3 >= s.length()) break;
            int c0 = s.charAt(i) < 128 ? B64_INV[s.charAt(i)] : -1;
            int c1 = s.charAt(i + 1) < 128 ? B64_INV[s.charAt(i + 1)] : -1;
            int c2 = s.charAt(i + 2) < 128 ? B64_INV[s.charAt(i + 2)] : -1;
            int c3 = s.charAt(i + 3) < 128 ? B64_INV[s.charAt(i + 3)] : -1;

            if (c0 < 0 || c1 < 0 || c2 < 0 || c3 < 0) break;

            int triple = (c0 << 18) | (c1 << 12) | (c2 << 6) | c3;

            if (outIdx < len) out[outIdx++] = (byte) ((triple >>> 16) & 0xFF);
            if (outIdx < len && s.charAt(i + 2) != '=') out[outIdx++] = (byte) ((triple >>> 8) & 0xFF);
            if (outIdx < len && s.charAt(i + 3) != '=') out[outIdx++] = (byte) (triple & 0xFF);
        }
        return out;
    }
}
