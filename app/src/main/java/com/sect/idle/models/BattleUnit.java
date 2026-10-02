package com.sect.idle.models;

import com.sect.idle.core.Vector2;
import com.sect.idle.gameplay.TalentSystem;
import java.util.ArrayList;

/**
 * BattleUnit - Represents an active combatant in turn-based and ATB battle scenarios.
 * Fully integrates Talent statistical modifiers (Elemental damage bonuses, Damage reduction,
 * Life steal, and HP regeneration).
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public class BattleUnit {
    public String name;
    public int hp, maxHp;
    public int mp, maxMp;
    public int atk, def, spd;
    public int critRate, critDmg;
    public int dodge, accuracy;
    public int element;
    public int team; // 0=player, 1=enemy
    public Vector2 pos;
    public boolean isAlive;
    public int actionBar;
    public int maxActionBar;
    public Disciple source;
    
    public int kills = 0;
    private final ArrayList<Integer> skillCooldowns;
    
    public BattleUnit(Disciple d, int team) {
        this.source = d;
        this.team = team;
        this.name = d != null && d.name != null ? d.name : "Cultivator";
        this.hp = d != null ? d.hp : 100;
        this.maxHp = d != null ? d.maxHp : 100;
        this.mp = d != null ? d.mp : 50;
        this.maxMp = d != null ? d.maxMp : 50;
        this.atk = d != null ? d.atk : 10;
        this.def = d != null ? d.def : 10;
        this.spd = d != null ? d.spd : 10;
        this.critRate = d != null ? d.critRate : 5;
        this.critDmg = d != null ? d.critDmg : 150;
        this.dodge = d != null ? d.dodge : 5;
        this.accuracy = d != null ? d.accuracy : 80;
        this.element = d != null ? d.element : 0;
        this.pos = new Vector2();
        this.isAlive = this.hp > 0;
        this.actionBar = 0;
        this.maxActionBar = Math.max(80, 200 - this.spd);

        this.skillCooldowns = new ArrayList<Integer>();
        if (d != null && d.skills != null) {
            for (int i = 0; i < d.skills.size(); i++) {
                skillCooldowns.add(0);
            }
        }
    }

    public BattleUnit(Disciple d, boolean isPlayer, int team) {
        this(d, team);
    }
    
    public void tickAction() {
        actionBar += Math.max(1, spd / 5);
        if (actionBar >= maxActionBar) actionBar = maxActionBar;
    }
    
    public boolean canAct() { return actionBar >= maxActionBar && isAlive; }
    public void resetAction() { actionBar = 0; }
    
    public int calcDamage(BattleUnit target, float skillPower) {
        if (target == null) return 0;
        int realmVal = source != null ? source.realm : 0;
        float dmg = atk * skillPower * (1.0f + (float)realmVal / 10.0f);
        dmg = dmg * (100f / (100f + Math.max(0, target.def)));

        // Elemental Affinity damage amplification
        if (source != null) {
            float elemBonus = TalentSystem.getElementalDamageBonus(source, element);
            if (elemBonus > 0f) {
                dmg *= (1.0f + elemBonus);
            }
        }

        // Critical strike resolution
        if (Math.random() * 100 < critRate) {
            dmg *= critDmg / 100f;
        }

        // Accuracy vs Dodge calculation
        float acc = Math.min(100f, accuracy - target.dodge + 80f);
        if (Math.random() * 100 > acc) {
            return 0; // Miss
        }

        int finalDmg = Math.max(1, (int)dmg);

        // Life steal talent resolution
        if (source != null && finalDmg > 0) {
            float lifeSteal = TalentSystem.getBattleLifeStealPct(source);
            if (lifeSteal > 0f) {
                heal((int)(finalDmg * lifeSteal));
            }
        }

        return finalDmg;
    }
    
    public void takeDamage(int rawDmg) {
        if (rawDmg <= 0) return;
        // Flat talent damage reduction mitigation
        float reduction = source != null ? TalentSystem.getBattleDamageReductionPct(source) : 0f;
        int mitigatedDmg = Math.max(1, (int)(rawDmg * (1.0f - reduction)));

        hp -= mitigatedDmg;
        if (hp <= 0) {
            hp = 0;
            isAlive = false;
        }
    }
    
    public void heal(int amt) {
        if (amt <= 0 || !isAlive) return;
        hp = Math.min(maxHp, hp + amt);
    }

    public void tickRegen() {
        if (!isAlive || source == null) return;
        float regenPct = TalentSystem.getBattleHpRegenPct(source);
        if (regenPct > 0f) {
            heal((int)(maxHp * regenPct));
        }
    }

    public void tickCooldowns() {
        for (int i = 0; i < skillCooldowns.size(); i++) {
            int cd = skillCooldowns.get(i);
            if (cd > 0) skillCooldowns.set(i, cd - 1);
        }
    }

    public boolean isSkillReady(int skillIndex) {
        if (skillIndex < 0 || skillIndex >= skillCooldowns.size()) return false;
        return skillCooldowns.get(skillIndex) <= 0;
    }

    public void setSkillCooldown(int skillIndex, int cd) {
        if (skillIndex >= 0 && skillIndex < skillCooldowns.size()) {
            skillCooldowns.set(skillIndex, cd);
        }
    }
}
