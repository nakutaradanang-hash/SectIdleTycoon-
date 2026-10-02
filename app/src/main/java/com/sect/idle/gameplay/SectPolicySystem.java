package com.sect.idle.gameplay;

import com.sect.idle.utils.ErrorCode;
import com.sect.idle.utils.ExceptionManager;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * SectPolicySystem - Civilization Style Policy Cards & Governance Doctrines.
 * Allows adopting grand doctrines with powerful passive modifiers and strategic tradeoffs.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class SectPolicySystem {

    private static volatile SectPolicySystem instance;
    private static final Object LOCK = new Object();

    public static class SectPolicy {
        public String id;
        public String title;
        public String description;
        public String positiveEffect;
        public String negativeTradeoff;
        public boolean active;
        public int category; // 1 = Military, 2 = Economic, 3 = Daoist/Spiritual

        public SectPolicy(String id, String title, String description, String positive, String negative, int category) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.positiveEffect = positive;
            this.negativeTradeoff = negative;
            this.active = false;
            this.category = category;
        }
    }

    public final ArrayList<SectPolicy> policies;
    private final HashMap<String, SectPolicy> policyMap;

    public static SectPolicySystem getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new SectPolicySystem();
                }
            }
        }
        return instance;
    }

    private SectPolicySystem() {
        this.policies = new ArrayList<SectPolicy>(6);
        this.policyMap = new HashMap<String, SectPolicy>(8);
        initPolicies();
    }

    private void initPolicies() {
        policies.clear();
        policyMap.clear();

        registerPolicy(new SectPolicy("POL_MARTIAL_EXPANSION", "Iron Martial Expansion", "Focus all sect resources into martial strength and weapon refinement.", "+30% Battle ATK, +50% War Conquest Loot", "-20% Farming & Herb Yield", 1));
        registerPolicy(new SectPolicy("POL_SILK_ROAD", "Grand Silk Road Monopoly", "Turn the sect into the supreme mercantile trading capital of the realm.", "+50% Spirit Stone Daily Revenue", "-15% Disciple Combat DEF", 2));
        registerPolicy(new SectPolicy("POL_HERMIT_DAO", "Hermit Mountain Solitude", "Abandon mortal entanglements to concentrate purely on Dao meditation.", "+40% Cultivation EXP, +100% Breakthrough Chance", "-30% Market & Quest Revenue", 3));
        registerPolicy(new SectPolicy("POL_HEAVENLY_HARMONY", "Heavenly Dao Benevolence", "Provide generous stipends, herbal baths, and tea pavilions to disciples.", "+20 Disciple Loyalty, +30 Max Mood, 0 Defection Risk", "+25% Daily Disciple Wages", 3));
        registerPolicy(new SectPolicy("POL_DEMONIC_BLOOD", "Forbidden Blood Cultivation", "Permit disciples to absorb blood essences for rapid realm breakthroughs.", "+100% Realm Breakthrough Speed", "-25 Loyalty & +15 Demonic Deviation Risk", 1));
    }

    private void registerPolicy(SectPolicy p) {
        policies.add(p);
        policyMap.put(p.id, p);
    }

    public SectPolicy getPolicy(String id) {
        return policyMap.get(id);
    }

    public boolean togglePolicy(String policyId) {
        SectPolicy p = getPolicy(policyId);
        if (p == null) return false;
        p.active = !p.active;
        ExceptionManager.get().logUserAction("POLICY_TOGGLE", p.title, p.active ? "Enacted" : "Repealed");
        return true;
    }

    public boolean isPolicyActive(String id) {
        SectPolicy p = getPolicy(id);
        return p != null && p.active;
    }

    public int getActivePolicyCount() {
        int count = 0;
        for (int i = 0; i < policies.size(); i++) {
            if (policies.get(i).active) count++;
        }
        return count;
    }
}
