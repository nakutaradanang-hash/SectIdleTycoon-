package com.sect.idle.models;

public class Fairy {
    public String name;
    public String element;
    public int level;
    public int exp;
    public int bond;
    public int maxBond;
    public int efficiencyBonus;
    public int cultivationBonus;
    public int combatBonus;
    public int luckBonus;
    public String personality;
    public boolean isActive;
    public int spriteId;
    
    public Fairy(String name, String element) {
        this.name = name; this.element = element; this.level = 1;
        this.bond = 0; this.maxBond = 100; this.efficiencyBonus = 5;
        this.cultivationBonus = 3; this.combatBonus = 5; this.luckBonus = 2;
        this.isActive = true;
    }
    
    public void feed(int exp) {
        this.exp += exp;
        int need = level * 50;
        if (this.exp >= need) {
            this.exp -= need;
            level++;
            efficiencyBonus += 2;
            cultivationBonus += 1;
            combatBonus += 2;
            luckBonus += 1;
        }
    }
    
    public void interact() {
        bond = Math.min(maxBond, bond + 5);
        if (bond >= maxBond) {
            maxBond += 50;
            bond = 0;
            efficiencyBonus += 3;
        }
    }
}
