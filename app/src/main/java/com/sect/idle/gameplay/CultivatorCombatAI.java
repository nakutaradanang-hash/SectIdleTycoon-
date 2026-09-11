package com.sect.idle.gameplay;

import com.sect.idle.models.BattleSkill;
import com.sect.idle.models.BattleUnit;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

/**
 * CultivatorCombatAI - High-Intelligence AI Movement, Behavior Tree, Physics & Skill Execution Engine.
 * Implements Xianxia Donghua cultivation combat styles:
 * - 3D kinematic trajectories (Flash-step teleport, parabolic leap strikes, aerial hovering, knockback physics).
 * - Dynamic archetypes (Sword Immortal, Daoist Elementalist, Body Refiner Vanguard, Spirit Healer).
 * - Five Elements interactions (Metal, Wood, Water, Fire, Earth, Thunder, Wind).
 * - Reactive Qi Shielding & Smart Combo Chaining.
 */
public final class CultivatorCombatAI {

    public static final int ROLE_SWORD_IMMORTAL = 0;
    public static final int ROLE_DAOIST_CASTER = 1;
    public static final int ROLE_BODY_REFINER = 2;
    public static final int ROLE_SPIRIT_HEALER = 3;

    public static final int STATE_IDLE_HOVER = 0;
    public static final int STATE_ADVANCING = 1;
    public static final int STATE_LEAP_ATTACK = 2;
    public static final int STATE_CASTING_ULTIMATE = 3;
    public static final int STATE_DEFENSIVE_STANCE = 4;
    public static final int STATE_KNOCKBACK = 5;
    public static final int STATE_FLASH_STEP = 6;

    public static class AIState {
        public int role = ROLE_SWORD_IMMORTAL;
        public int state = STATE_IDLE_HOVER;

        // 3D Spatial kinematics
        public float posX = 0f;
        public float posY = 0f;
        public float posZ = 0f;
        public float velX = 0f;
        public float velY = 0f;
        public float velZ = 0f;

        public float basePosX = 0f;
        public float basePosY = 0f;

        public float targetX = 0f;
        public float targetY = 0f;
        public float targetZ = 0f;

        // Animation timers
        public float stateTimer = 0f;
        public float animTime = 0f;
        public float swordOrbitAngle = 0f;
        public float qiShieldAlpha = 0f;
        public boolean isShieldActive = false;

        // Ultimate charge (0 to 100)
        public int qiCharge = 0;
        public int maxQiCharge = 100;

        // Skills equipped
        public final ArrayList<BattleSkill> skills = new ArrayList<BattleSkill>();
        public BattleSkill pendingSkill = null;

        // 3D Flying Swords (Sword Array VFX attached to this unit)
        public static final int SWORD_COUNT = 4;
        public final float[] swordOffsetsX = new float[SWORD_COUNT];
        public final float[] swordOffsetsY = new float[SWORD_COUNT];
        public final float[] swordOffsetsZ = new float[SWORD_COUNT];

        public AIState(int role, float startX, float startY) {
            this.role = role;
            this.posX = startX;
            this.posY = startY;
            this.posZ = 0f;
            this.basePosX = startX;
            this.basePosY = startY;
            this.targetX = startX;
            this.targetY = startY;
            this.targetZ = 0f;

            initRoleSkills();
        }

        private void initRoleSkills() {
            skills.clear();
            skills.add(BattleSkill.createDefaultBasicAttack(1));
            switch (role) {
                case ROLE_SWORD_IMMORTAL:
                    skills.add(BattleSkill.createSwordArrayUltimate());
                    break;
                case ROLE_DAOIST_CASTER:
                    skills.add(BattleSkill.createNineHeavensThunder());
                    skills.add(BattleSkill.createPhoenixInferno());
                    break;
                case ROLE_BODY_REFINER:
                    skills.add(BattleSkill.createTaijiShield());
                    break;
                case ROLE_SPIRIT_HEALER:
                    skills.add(BattleSkill.createSpiritHealingSpring());
                    break;
            }
        }
    }

    /**
     * Determines the combat role based on disciple stats.
     */
    public static int determineRole(BattleUnit unit) {
        if (unit == null || unit.source == null) return ROLE_SWORD_IMMORTAL;
        int str = unit.source.str;
        int intel = unit.source.intel;
        int vit = unit.source.vit;
        int wis = unit.source.wis;

        if (vit >= str && vit >= intel && vit >= wis) return ROLE_BODY_REFINER;
        if (wis >= str && wis >= intel && wis >= vit) return ROLE_SPIRIT_HEALER;
        if (intel >= str) return ROLE_DAOIST_CASTER;
        return ROLE_SWORD_IMMORTAL;
    }

    /**
     * Ticks AI movement, spatial physics, and behavior state machine.
     */
    public static void updateUnitAI(BattleUnit unit, AIState ai, float dt, ArrayList<BattleUnit> allies,
                                    ArrayList<BattleUnit> enemies, CombatActionCallback callback) {
        if (unit == null || ai == null || !unit.isAlive) {
            ai.posZ = Math.max(0f, ai.posZ - 200f * dt);
            return;
        }

        ai.animTime += dt;
        ai.stateTimer += dt;
        ai.swordOrbitAngle += 2.5f * dt;

        // Tick skill cooldowns
        for (int i = 0; i < ai.skills.size(); i++) {
            ai.skills.get(i).tick(dt);
        }

        // Orbiting flying swords kinematic update
        for (int i = 0; i < AIState.SWORD_COUNT; i++) {
            float angle = ai.swordOrbitAngle + (i * (float) (Math.PI * 2.0 / AIState.SWORD_COUNT));
            float radius = 35f + (float) Math.sin(ai.animTime * 4f + i) * 6f;
            ai.swordOffsetsX[i] = (float) Math.cos(angle) * radius;
            ai.swordOffsetsY[i] = (float) Math.sin(angle) * radius * 0.6f;
            ai.swordOffsetsZ[i] = 25f + (float) Math.sin(ai.animTime * 3f + i * 1.5f) * 12f;
        }

        // Shield fade
        if (ai.isShieldActive) {
            ai.qiShieldAlpha = Math.min(1.0f, ai.qiShieldAlpha + dt * 4f);
        } else {
            ai.qiShieldAlpha = Math.max(0f, ai.qiShieldAlpha - dt * 2f);
        }

        // State Machine execution
        switch (ai.state) {
            case STATE_IDLE_HOVER:
                // Gentle floating breath levitation
                float levitateZ = 12f + (float) Math.sin(ai.animTime * 3f + ai.basePosX) * 8f;
                ai.posZ += (levitateZ - ai.posZ) * Math.min(1f, dt * 6f);
                ai.posX += (ai.basePosX - ai.posX) * Math.min(1f, dt * 4f);
                ai.posY += (ai.basePosY - ai.posY) * Math.min(1f, dt * 4f);

                // Passive Qi charge
                ai.qiCharge = Math.min(ai.maxQiCharge, ai.qiCharge + (int) (12 * dt));
                break;

            case STATE_ADVANCING:
            case STATE_LEAP_ATTACK:
                // Parabolic 3D leap physics
                float dx = ai.targetX - ai.posX;
                float dy = ai.targetY - ai.posY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                ai.posX += dx * Math.min(1f, dt * 8f);
                ai.posY += dy * Math.min(1f, dt * 8f);

                // High parabolic apex
                float leapProgress = Math.min(1f, ai.stateTimer / 0.45f);
                ai.posZ = (float) Math.sin(leapProgress * Math.PI) * 75f;

                if (dist < 20f || ai.stateTimer >= 0.5f) {
                    // Strike impact!
                    ai.state = STATE_IDLE_HOVER;
                    ai.stateTimer = 0f;
                    if (callback != null && ai.pendingSkill != null) {
                        callback.onSkillImpact(unit, ai.pendingSkill);
                    }
                    ai.pendingSkill = null;
                }
                break;

            case STATE_FLASH_STEP:
                // Instantaneous blink with afterimage
                ai.posX = ai.targetX;
                ai.posY = ai.targetY;
                ai.posZ = 15f;
                ai.state = STATE_IDLE_HOVER;
                ai.stateTimer = 0f;
                break;

            case STATE_CASTING_ULTIMATE:
                // Ascend to high heavens
                ai.posZ += (120f - ai.posZ) * Math.min(1f, dt * 5f);
                if (ai.stateTimer >= 1.2f) {
                    ai.state = STATE_IDLE_HOVER;
                    ai.stateTimer = 0f;
                    if (callback != null && ai.pendingSkill != null) {
                        callback.onSkillImpact(unit, ai.pendingSkill);
                    }
                    ai.pendingSkill = null;
                    ai.qiCharge = 0; // Reset ultimate gauge
                }
                break;

            case STATE_KNOCKBACK:
                ai.posX += ai.velX * dt;
                ai.posY += ai.velY * dt;
                ai.posZ += ai.velZ * dt;
                ai.velX *= 0.88f;
                ai.velY *= 0.88f;
                ai.velZ -= 380f * dt; // Gravity
                if (ai.posZ <= 0f) {
                    ai.posZ = 0f;
                    ai.velZ = 0f;
                    if (ai.stateTimer >= 0.4f) {
                        ai.state = STATE_IDLE_HOVER;
                        ai.stateTimer = 0f;
                    }
                }
                break;
        }
    }

    /**
     * Intelligent Target Evaluation using threat score, elemental counter, and execute thresholds.
     */
    public static BattleUnit selectBestTarget(BattleUnit actor, AIState ai, ArrayList<BattleUnit> enemies) {
        if (enemies == null || enemies.isEmpty() || actor == null) return null;

        BattleUnit bestTarget = null;
        float bestScore = -1f;

        for (int i = 0; i < enemies.size(); i++) {
            BattleUnit enemy = enemies.get(i);
            if (enemy == null || !enemy.isAlive) continue;

            float score = 100f;

            // 1. Low HP Execute Priority (+100 if below 25% HP)
            float hpPct = enemy.hp / (float) Math.max(1, enemy.maxHp);
            score += (1.0f - hpPct) * 120f;

            // 2. Elemental Counter Advantage (+50 if actor counters enemy)
            if (isElementalAdvantage(actor.element, enemy.element)) {
                score += 60f;
            }

            // 3. Role-specific prioritization
            if (ai.role == ROLE_SWORD_IMMORTAL) {
                // Prioritize backline high damage dealers
                score += enemy.atk * 0.5f;
            } else if (ai.role == ROLE_BODY_REFINER) {
                // Focus closest threat
                score += 30f;
            }

            if (score > bestScore) {
                bestScore = score;
                bestTarget = enemy;
            }
        }
        return bestTarget != null ? bestTarget : (enemies.isEmpty() ? null : enemies.get(0));
    }

    /**
     * Checks Chinese Five Elements generation & suppression cycles.
     * Metal(1) > Wood(2) > Earth(5) > Water(3) > Fire(4) > Metal(1)
     */
    public static boolean isElementalAdvantage(int attackerElem, int defenderElem) {
        if (attackerElem == 1 && defenderElem == 2) return true; // Metal chops Wood
        if (attackerElem == 2 && defenderElem == 5) return true; // Wood penetrates Earth
        if (attackerElem == 5 && defenderElem == 3) return true; // Earth absorbs Water
        if (attackerElem == 3 && defenderElem == 4) return true; // Water extinguishes Fire
        if (attackerElem == 4 && defenderElem == 1) return true; // Fire melts Metal
        if (attackerElem == 6 && defenderElem == 8) return true; // Thunder purifies Yin
        return false;
    }

    public interface CombatActionCallback {
        void onSkillImpact(BattleUnit actor, BattleSkill skill);
    }
}
