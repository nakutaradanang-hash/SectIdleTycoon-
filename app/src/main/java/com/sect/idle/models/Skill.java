package com.sect.idle.models;

public class Skill {
    public String id;
    public String name;
    public String desc;
    public int type; // 0=passive, 1=active, 2=ultimate
    public int element;
    public int level;
    public int maxLevel;
    public int cooldown;
    public int currentCooldown;
    public int mpCost;
    public float power; // damage/heal multiplier
    public int effectType; // 0=dmg, 1=heal, 2=buff, 3=debuff, 4=shield
    public int targetType; // 0=self, 1=single, 2=area, 3=all
    public int rarity;
    public boolean unlocked;
    
    public Skill(String id, String name, String desc, int type, int element, int maxLevel, int cd, int mp, float power, int effect, int target, int rarity) {
        this.id = id; this.name = name; this.desc = desc; this.type = type;
        this.element = element; this.maxLevel = maxLevel; this.cooldown = cd;
        this.mpCost = mp; this.power = power; this.effectType = effect;
        this.targetType = target; this.rarity = rarity;
        this.level = 0; this.currentCooldown = 0; this.unlocked = false;
    }
    
    public void use() { if (currentCooldown > 0) currentCooldown--; }
    public void resetCd() { currentCooldown = cooldown; }
    public boolean isReady() { return currentCooldown <= 0; }
    
    public float getPower() { return power * (1 + level * 0.2f); }
    public String getRarityName() {
        String[] r = {"Common","Uncommon","Rare","Epic","Legend","Mythic"};
        return r[Math.min(rarity, r.length - 1)];
    }
}
