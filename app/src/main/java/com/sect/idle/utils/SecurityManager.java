package com.sect.idle.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * SecurityManager - Cryptographic Verification, Anti-Tamper Checksums & Constant-Time Integrity Checking.
 *
 * Capabilities:
 * - SHA-256 and CRC32 cryptographic payload hashing with device-unique salt.
 * - Constant-time string comparison preventing timing side-channel attacks.
 * - Anti-tampering verification for save records, promo codes, and market transactions.
 * - Safe obfuscation and deobfuscation algorithms.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class SecurityManager {
    private static final String PREFS_SEC = "IdleSectSec";
    private static final String KEY_HASH = "data_hash";
    private static final String KEY_SALT = "salt";
    private static final String KEY_DEVICE = "device_id";
    private static volatile SecurityManager instance;
    private static final Object LOCK = new Object();

    private final SharedPreferences prefs;
    private final String deviceId;
    private final CRC32 crc32;

    private SecurityManager(Context ctx) {
        Context appCtx = ctx != null ? ctx.getApplicationContext() : null;
        this.prefs = appCtx != null ? appCtx.getSharedPreferences(PREFS_SEC, Context.MODE_PRIVATE) : null;

        String devId = "";
        try {
            if (appCtx != null) {
                devId = Settings.Secure.getString(appCtx.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        } catch (Exception ignored) {}
        this.deviceId = (devId != null && !devId.isEmpty()) ? devId : "sect_dao_device";
        this.crc32 = new CRC32();

        if (prefs != null && !prefs.contains(KEY_SALT)) {
            prefs.edit().putString(KEY_SALT, generateSalt()).apply();
        }
    }

    public static SecurityManager get(Context ctx) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new SecurityManager(ctx);
                }
            }
        }
        return instance;
    }

    private String generateSalt() {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            sb.append((char)(65 + (int)(Math.random() * 26)));
        }
        return sb.toString();
    }

    /**
     * Computes a fast CRC32 checksum combined with device ID and salt.
     */
    public synchronized String computeHash(String data) {
        if (data == null) data = "";
        String salt = prefs != null ? prefs.getString(KEY_SALT, "DEFAULT_SALT_DAO") : "DEFAULT_SALT_DAO";
        String combined = data + "|" + salt + "|" + deviceId;
        crc32.reset();
        crc32.update(combined.getBytes());
        return Long.toHexString(crc32.getValue());
    }

    /**
     * Computes a cryptographically secure SHA-256 hash.
     */
    public String computeSha256(String data) {
        if (data == null) data = "";
        String salt = prefs != null ? prefs.getString(KEY_SALT, "DEFAULT_SALT_DAO") : "DEFAULT_SALT_DAO";
        String combined = data + ":" + salt + ":" + deviceId;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(combined.getBytes());
            StringBuilder sb = new StringBuilder(64);
            for (int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0xFF & digest[i]);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return computeHash(data);
        }
    }

    /**
     * Constant-time string equality check to mitigate timing side-channel attacks.
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return a == b;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    public void saveHash(String data) {
        if (prefs != null) {
            prefs.edit().putString(KEY_HASH, computeHash(data)).apply();
        }
    }

    public boolean verifyHash(String data) {
        if (prefs == null) return true;
        String saved = prefs.getString(KEY_HASH, "");
        return constantTimeEquals(saved, computeHash(data));
    }

    public boolean verifyIntegrity(String data, String expectedHash) {
        if (expectedHash == null || expectedHash.isEmpty()) return false;
        return constantTimeEquals(expectedHash, computeHash(data));
    }

    /**
     * Obfuscates text using a reversible XOR cipher with salt key.
     */
    public String obfuscate(String data) {
        if (data == null || data.isEmpty()) return "";
        String salt = prefs != null ? prefs.getString(KEY_SALT, "DEFAULT_SALT_DAO") : "DEFAULT_SALT_DAO";
        if (salt.isEmpty()) salt = "DEFAULT_SALT_DAO";
        StringBuilder sb = new StringBuilder(data.length());
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            char s = salt.charAt(i % salt.length());
            sb.append((char)(c ^ s));
        }
        return sb.toString();
    }

    public String deobfuscate(String data) {
        return obfuscate(data);
    }

    public boolean isDeviceChanged() {
        if (prefs == null) return false;
        String saved = prefs.getString(KEY_DEVICE, "");
        if (saved.isEmpty()) {
            prefs.edit().putString(KEY_DEVICE, deviceId).apply();
            return false;
        }
        return !constantTimeEquals(saved, deviceId);
    }

    public void reset() {
        if (prefs != null) {
            prefs.edit().clear().apply();
        }
        instance = null;
    }
}
