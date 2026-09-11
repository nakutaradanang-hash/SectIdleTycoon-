package com.sect.idle.models;

import com.sect.idle.core.GameConfig;

public class Equipment {
    public String id;
    public String name;
    public String desc;
    public int slot; // 0=weapon,1=armor,2=accessory,3=artifact
    public int rarity;
    public int level;
    public int element;
    public int atk, def, spd, hp, mp, crit;
    public int strBonus, agiBonus, intBonus, lckBonus, vitBonus, wisBonus, chaBonus;
    public int enhanceLevel;
    public int maxEnhance;
    public boolean isEquipped;
    public String setName;
    public int setPieces;
    
    public Equipment(String id, String name, int slot, int rarity) {
        this.id = id; this.name = name; this.slot = slot; this.rarity = rarity;
        this.level = 1; this.enhanceLevel = 0; this.maxEnhance = rarity * 3 + 3;
        this.element = GameConfig.ELEM_NONE;
    }
    
    public void calcStats() {
        int mult = 1 + rarity * 2 + level;
        if (slot == 0) { atk = 10 * mult; crit = rarity * 5; }
        else if (slot == 1) { def = 8 * mult; hp = 20 * mult; }
        else if (slot == 2) { spd = 5 * mult; mp = 10 * mult; }
        else if (slot == 3) { atk = 5 * mult; def = 5 * mult; hp = 30 * mult; crit = rarity * 10; }
        applyEnhance();
    }
    
    public void applyEnhance() {
        float e = 1 + enhanceLevel * 0.1f;
        atk = (int)(atk * e); def = (int)(def * e); spd = (int)(spd * e);
        hp = (int)(hp * e); mp = (int)(mp * e); crit = (int)(crit * e);
    }
    
    public boolean enhance() {
        if (enhanceLevel >= maxEnhance) return false;
        if (Math.random() < getEnhanceChance()) {
            enhanceLevel++;
            calcStats();
            return true;
        }
        return false;
    }
    
    public float getEnhanceChance() {
        float base = 0.9f - enhanceLevel * 0.08f;
        return Math.max(0.1f, base);
    }
    
    public int getColor() { return GameConfig.RARITY_COLORS[Math.min(rarity, GameConfig.RARITY_COLORS.length - 1)]; }
}
