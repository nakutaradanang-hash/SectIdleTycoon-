package com.sect.idle.gameplay;

import com.sect.idle.utils.DataValidator;
import com.sect.idle.utils.ExceptionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * SecretManager - Manages secret unlockables, achievements, and secure promo code redemption.
 *
 * Implements strict input sanitization, whitelist validation, and one-time redemption protection.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class SecretManager {
    private static final String TAG = "SecretManager";
    private static final ArrayList<String> unlockedSecrets = new ArrayList<String>();
    private static final ArrayList<String> redeemedPromoCodes = new ArrayList<String>();
    private static final Map<String, PromoReward> PROMO_REGISTRY = new HashMap<String, PromoReward>();

    public static class PromoReward {
        public final String code;
        public final String description;
        public final long stones;
        public final long herbs;
        public final long pills;
        public final long ores;
        public final long jade;

        public PromoReward(String code, String desc, long stones, long herbs, long pills, long ores, long jade) {
            this.code = code;
            this.description = desc;
            this.stones = stones;
            this.herbs = herbs;
            this.pills = pills;
            this.ores = ores;
            this.jade = jade;
        }
    }

    static {
        // Register standard game promo codes
        registerPromo("DAO777", "Auspicious Dao Gift: 7,777 Spirit Stones & 77 Herbs", 7777, 77, 7, 70, 7);
        registerPromo("IMMORTAL2026", "Immortal Sovereign Starter Pack: 20,000 Stones & 50 Pills", 20000, 200, 50, 100, 26);
        registerPromo("SECTFOUNDER", "Founder's Blessing: 50,000 Spirit Stones & 88 Jade", 50000, 500, 100, 250, 88);
        registerPromo("SKYDRAGON", "Dragon Vein Treasury: 100,000 Spirit Stones & 500 Ores", 100000, 1000, 200, 500, 100);
    }

    private SecretManager() {}

    private static void registerPromo(String code, String desc, long stones, long herbs, long pills, long ores, long jade) {
        PROMO_REGISTRY.put(code.toUpperCase(), new PromoReward(code.toUpperCase(), desc, stones, herbs, pills, ores, jade));
    }

    public static boolean checkUnlock(String secretKey) {
        if (secretKey == null) return false;
        String clean = DataValidator.sanitizeString(secretKey, 64);
        if (clean.isEmpty()) return false;
        if (!unlockedSecrets.contains(clean)) {
            unlockedSecrets.add(clean);
            return true;
        }
        return false;
    }

    public static boolean isUnlocked(String secretKey) {
        if (secretKey == null) return false;
        return unlockedSecrets.contains(DataValidator.sanitizeString(secretKey, 64));
    }

    public static class RedeemResult {
        public final boolean success;
        public final String message;
        public final PromoReward reward;

        public RedeemResult(boolean s, String m, PromoReward r) {
            this.success = s;
            this.message = m;
            this.reward = r;
        }
    }

    /**
     * Securely redeems a promo code with strict input sanitization and duplicate checks.
     */
    public static synchronized RedeemResult redeemCode(String rawCode) {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            return new RedeemResult(false, "Please enter a valid Heavenly Dao code.", null);
        }

        // Strict sanitization: uppercase alphanumeric, max 20 chars
        String cleanCode = DataValidator.sanitizeRedeemCode(rawCode, 20);

        if (!DataValidator.isValidRedeemCode(cleanCode)) {
            ExceptionManager.get().logWarn(TAG, "Rejected invalid promo code format: " + rawCode);
            return new RedeemResult(false, "Invalid code format. Codes must contain only letters and numbers (4-20 chars).", null);
        }

        if (redeemedPromoCodes.contains(cleanCode)) {
            return new RedeemResult(false, "This Heavenly Dao code has already been redeemed by your sect.", null);
        }

        PromoReward reward = PROMO_REGISTRY.get(cleanCode);
        if (reward == null) {
            ExceptionManager.get().logWarn(TAG, "Unknown promo code attempt: " + cleanCode);
            return new RedeemResult(false, "Unknown or expired Heavenly Dao code.", null);
        }

        // Grant rewards securely via ResourceManager
        ResourceManager rm = ResourceManager.getInstance();
        if (reward.stones > 0) rm.addSpiritStones(reward.stones);
        if (reward.herbs > 0) rm.addSpiritHerbs(reward.herbs);
        if (reward.pills > 0) rm.addSpiritPills(reward.pills);
        if (reward.ores > 0) rm.earn(0, 0, reward.ores);

        redeemedPromoCodes.add(cleanCode);
        ExceptionManager.get().logOperationalEvent(TAG, "Promo Code Redeemed", "Code: " + cleanCode + " | Reward: " + reward.description);

        return new RedeemResult(true, "Successfully redeemed: " + reward.description, reward);
    }
}
