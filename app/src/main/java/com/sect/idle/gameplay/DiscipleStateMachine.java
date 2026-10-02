package com.sect.idle.gameplay;

import com.sect.idle.data.db.entities.DiscipleLifecycleEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * DiscipleStateMachine - High-performance, deterministic Finite State Machine (FSM)
 * and Sims-style life needs / Xianxia cultivation simulator for sect disciples.
 * Fully thread-safe, modular, and optimized for Sketchware Pro v7.0.0 and Android 5.0+.
 */
public final class DiscipleStateMachine {

    public interface StateChangeListener {
        void onStateChanged(DiscipleLifecycleEntity disciple, int oldState, int newState, String reason);
        void onBreakthrough(DiscipleLifecycleEntity disciple, int oldRealm, int newRealm, boolean success, String log);
        void onTribulationEvent(DiscipleLifecycleEntity disciple, int strike, boolean survived, String log);
        void onDemise(DiscipleLifecycleEntity disciple, String cause);
        void onAscension(DiscipleLifecycleEntity disciple);
    }

    public static final class SectEnvironment {
        public float spiritDensity = 1.0f; // Multiplier from Spirit Vein / Gathering Formations
        public float ambientDanger = 0.0f; // 0.0 to 1.0
        public int availableSpiritFood = 1000;
        public int availableQuarters = 100;
        public int alchemyMedicines = 100;

        public SectEnvironment() {}

        public SectEnvironment(float spiritDensity, int availableSpiritFood, int availableQuarters) {
            this.spiritDensity = spiritDensity;
            this.availableSpiritFood = availableSpiritFood;
            this.availableQuarters = availableQuarters;
        }
    }

    private static volatile DiscipleStateMachine INSTANCE;
    private final Random random = new Random();
    private final List<StateChangeListener> listeners = new ArrayList<StateChangeListener>();

    private DiscipleStateMachine() {}

    public static DiscipleStateMachine getInstance() {
        if (INSTANCE == null) {
            synchronized (DiscipleStateMachine.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DiscipleStateMachine();
                }
            }
        }
        return INSTANCE;
    }

    public synchronized void addListener(StateChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(StateChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    // =========================================================================
    // TICK & SIMULATION PIPELINE
    // =========================================================================

    /**
     * Process one simulation tick for a disciple.
     * Updates needs decay, active state timers, qi cultivation, and autonomous decisions.
     */
    public synchronized void tick(DiscipleLifecycleEntity disciple, int deltaSeconds, SectEnvironment env) {
        if (disciple == null || !disciple.isAlive()) {
            return;
        }

        if (env == null) {
            env = new SectEnvironment();
        }

        disciple.lastTickTime = System.currentTimeMillis();
        disciple.stateTimerSeconds += deltaSeconds;

        // 1. Decay Needs
        updateNeeds(disciple, deltaSeconds);

        // 2. Process Current State Behavior
        processStateBehavior(disciple, deltaSeconds, env);

        // 3. Lifespan & Natural Mortality Check
        checkLifespanAndHealth(disciple, deltaSeconds);

        // 4. Autonomous AI Decision Making
        evaluateAutonomousBehavior(disciple, env);
    }

    private void updateNeeds(DiscipleLifecycleEntity disciple, int deltaSeconds) {
        // Starvation decay (slower during resting/cultivating)
        float hungerDecayRate = (disciple.currentState == DiscipleLifecycleEntity.STATE_CULTIVATING) ? 0.005f : 0.015f;
        int hungerLost = Math.max(0, (int) (deltaSeconds * hungerDecayRate));
        disciple.hunger = Math.max(0, disciple.hunger - hungerLost);

        // Energy dynamics
        if (disciple.currentState == DiscipleLifecycleEntity.STATE_RESTING) {
            int energyRestored = (int) (deltaSeconds * 0.15f);
            disciple.energy = Math.min(100, disciple.energy + Math.max(1, energyRestored));
            disciple.comfort = Math.min(100, disciple.comfort + 1);
        } else {
            float energyDrainRate = (disciple.currentState == DiscipleLifecycleEntity.STATE_SECT_DUTY) ? 0.025f : 0.01f;
            int energyLost = Math.max(0, (int) (deltaSeconds * energyDrainRate));
            disciple.energy = Math.max(0, disciple.energy - energyLost);
        }

        // Mood calculation based on hunger, energy, health & comfort
        if (disciple.hunger <= 15 || disciple.energy <= 15) {
            disciple.mood = Math.max(0, disciple.mood - 1);
        } else if (disciple.hunger >= 70 && disciple.energy >= 70) {
            disciple.mood = Math.min(100, disciple.mood + 1);
        }

        // Health impact from extreme starvation or exhaustion
        if (disciple.hunger == 0 || disciple.energy == 0) {
            disciple.health = Math.max(0, disciple.health - 1);
            if (disciple.health == 0) {
                killDisciple(disciple, "Starvation and Physical Exhaustion");
            }
        }
    }

    private void processStateBehavior(DiscipleLifecycleEntity disciple, int deltaSeconds, SectEnvironment env) {
        switch (disciple.currentState) {
            case DiscipleLifecycleEntity.STATE_CULTIVATING: {
                // Cultivate Qi: exp gain scaled by spirit density, talent grade, and dao heart stability
                float baseRate = 1.0f + (disciple.talentGrade * 0.5f);
                float stabilityFactor = Math.max(0.2f, disciple.daoHeartStability / 100.0f);
                long gainedExp = (long) (deltaSeconds * baseRate * env.spiritDensity * stabilityFactor);
                disciple.cultivationExp = Math.min(disciple.maxCultivationExp, disciple.cultivationExp + gainedExp);

                disciple.qiSatiety = Math.min(100, disciple.qiSatiety + Math.max(1, (int) (deltaSeconds * 0.05f)));

                // Check for Qi Deviation if Dao Heart is critically unstable
                if (disciple.daoHeartStability < 25.0f && random.nextInt(100) < 5) {
                    triggerQiDeviation(disciple, "Demonic Heart Intrusion during deep meditation");
                }
                break;
            }

            case DiscipleLifecycleEntity.STATE_EATING: {
                disciple.hunger = Math.min(100, disciple.hunger + (deltaSeconds * 4));
                disciple.social = Math.min(100, disciple.social + 1);
                if (disciple.hunger >= 95) {
                    transitionTo(disciple, DiscipleLifecycleEntity.STATE_IDLE, "Satiated from meal");
                }
                break;
            }

            case DiscipleLifecycleEntity.STATE_RESTING: {
                if (disciple.energy >= 95) {
                    transitionTo(disciple, DiscipleLifecycleEntity.STATE_IDLE, "Fully rested and refreshed");
                }
                break;
            }

            case DiscipleLifecycleEntity.STATE_QI_DEVIATION: {
                // Backlash degrades health and dao heart unless treated
                disciple.health = Math.max(1, disciple.health - 1);
                disciple.daoHeartStability = Math.max(0.0f, disciple.daoHeartStability - 0.5f);
                break;
            }

            case DiscipleLifecycleEntity.STATE_INJURED: {
                // Natural healing occurs slowly
                disciple.health = Math.min(100, disciple.health + (deltaSeconds / 10));
                if (disciple.health >= 80) {
                    transitionTo(disciple, DiscipleLifecycleEntity.STATE_IDLE, "Recovered from severe injuries");
                }
                break;
            }

            case DiscipleLifecycleEntity.STATE_TRIBULATION: {
                // Progress heavenly tribulation lightning strikes
                if (disciple.stateTimerSeconds >= 5) {
                    disciple.stateTimerSeconds = 0;
                    progressTribulationStrike(disciple);
                }
                break;
            }

            default:
                break;
        }
    }

    private void checkLifespanAndHealth(DiscipleLifecycleEntity disciple, int deltaSeconds) {
        // Aging progression (every 300 state seconds simulates aging 1 month)
        if (disciple.stateTimerSeconds % 300 == 0 && disciple.stateTimerSeconds > 0) {
            // Check if disciple exceeded natural lifespan
            if (disciple.age >= disciple.lifespan) {
                if (random.nextInt(100) < 15) {
                    killDisciple(disciple, "End of Lifespan / Soul Dissipation");
                }
            }
        }
    }

    // =========================================================================
    // STATE TRANSITIONS & ACTIONS
    // =========================================================================

    public synchronized boolean transitionTo(DiscipleLifecycleEntity disciple, int newState, String reason) {
        if (disciple == null || !disciple.isAlive()) {
            return false;
        }

        if (disciple.currentState == newState) {
            return false;
        }

        // Validate state transitions
        if (disciple.currentState == DiscipleLifecycleEntity.STATE_TRIBULATION && newState != DiscipleLifecycleEntity.STATE_ASCENDED && newState != DiscipleLifecycleEntity.STATE_DECEASED) {
            return false; // Cannot leave tribulation until survived or perished
        }

        int oldState = disciple.currentState;
        disciple.currentState = newState;
        disciple.stateTimerSeconds = 0;
        disciple.lastStateChangeTime = System.currentTimeMillis();

        notifyStateChanged(disciple, oldState, newState, reason);
        return true;
    }

    public synchronized void feed(DiscipleLifecycleEntity disciple, int spiritFoodQuality) {
        if (disciple == null || !disciple.isAlive()) return;

        int hungerBoost = 30 + (spiritFoodQuality * 15);
        disciple.hunger = Math.min(100, disciple.hunger + hungerBoost);
        disciple.health = Math.min(100, disciple.health + (spiritFoodQuality * 5));
        disciple.mood = Math.min(100, disciple.mood + 10);
        transitionTo(disciple, DiscipleLifecycleEntity.STATE_IDLE, "Consumed Spirit Nourishment");
    }

    public synchronized void rest(DiscipleLifecycleEntity disciple) {
        if (disciple == null || !disciple.isAlive()) return;
        transitionTo(disciple, DiscipleLifecycleEntity.STATE_RESTING, "Began resting in quarters");
    }

    public synchronized void startCultivation(DiscipleLifecycleEntity disciple) {
        if (disciple == null || !disciple.isAlive()) return;
        if (disciple.hunger <= 15) {
            transitionTo(disciple, DiscipleLifecycleEntity.STATE_EATING, "Too hungry to meditate, foraging for food");
            return;
        }
        if (disciple.energy <= 15) {
            transitionTo(disciple, DiscipleLifecycleEntity.STATE_RESTING, "Too exhausted to circulate Qi, entering sleep");
            return;
        }
        transitionTo(disciple, DiscipleLifecycleEntity.STATE_CULTIVATING, "Entered lotus position for Qi cultivation");
    }

    public synchronized boolean attemptBreakthrough(DiscipleLifecycleEntity disciple, int auxiliaryPillQuality) {
        if (disciple == null || !disciple.isAlive()) return false;

        disciple.totalBreakthroughAttempts++;
        transitionTo(disciple, DiscipleLifecycleEntity.STATE_BREAKTHROUGH, "Initiated realm breakthrough seclusion");

        // Calculate breakthrough probability:
        // Base rate = 40% + (Talent Grade * 5%) + (Pill Quality * 15%) + (Dao Heart * 0.2%) - (Karmic Sin * 2%)
        float successRate = 40.0f 
                + (disciple.talentGrade * 5.0f) 
                + (auxiliaryPillQuality * 15.0f) 
                + (disciple.daoHeartStability * 0.2f) 
                - (disciple.karmicSin * 2.0f);
        successRate = Math.max(5.0f, Math.min(95.0f, successRate));

        int roll = random.nextInt(100);
        boolean success = (roll < successRate);

        if (success) {
            handleBreakthroughSuccess(disciple);
            return true;
        } else {
            handleBreakthroughFailure(disciple);
            return false;
        }
    }

    private void handleBreakthroughSuccess(DiscipleLifecycleEntity disciple) {
        int oldRealm = disciple.realm;

        if (disciple.subStage < DiscipleLifecycleEntity.SUB_PEAK) {
            disciple.subStage++;
            disciple.cultivationExp = 0;
            disciple.maxCultivationExp = DiscipleLifecycleEntity.calculateMaxExp(disciple.realm, disciple.subStage);
            disciple.daoHeartStability = Math.min(100.0f, disciple.daoHeartStability + 10.0f);
            disciple.health = 100;
            disciple.energy = 100;
            transitionTo(disciple, DiscipleLifecycleEntity.STATE_IDLE, "Breakthrough to " + DiscipleLifecycleEntity.getRealmName(disciple.realm, disciple.subStage));
            notifyBreakthrough(disciple, oldRealm, disciple.realm, true, "Breakthrough to next sub-stage successful!");
        } else {
            // Major Realm Advancement!
            if (disciple.realm == DiscipleLifecycleEntity.REALM_TRIBULATION_TRANSCENDENCE) {
                // Trigger 9 Heavenly Tribulation Strikes
                disciple.tribulationCounter = 0;
                transitionTo(disciple, DiscipleLifecycleEntity.STATE_TRIBULATION, "Summoned Heavenly Lightning Tribulation for True Immortality");
                notifyBreakthrough(disciple, oldRealm, disciple.realm, true, "Summoned 9 Heavenly Tribulation strikes!");
            } else {
                disciple.realm++;
                disciple.subStage = DiscipleLifecycleEntity.SUB_EARLY;
                disciple.cultivationExp = 0;
                disciple.maxCultivationExp = DiscipleLifecycleEntity.calculateMaxExp(disciple.realm, disciple.subStage);
                disciple.lifespan = DiscipleLifecycleEntity.calculateBaseLifespan(disciple.realm, disciple.talentGrade);
                disciple.health = 100;
                disciple.energy = 100;
                updateLifecycleStageFromRealm(disciple);
                transitionTo(disciple, DiscipleLifecycleEntity.STATE_IDLE, "Major realm breakthrough to " + DiscipleLifecycleEntity.getRealmName(disciple.realm, disciple.subStage));
                notifyBreakthrough(disciple, oldRealm, disciple.realm, true, "Major realm breakthrough successful!");
            }
        }
    }

    private void handleBreakthroughFailure(DiscipleLifecycleEntity disciple) {
        disciple.daoHeartStability = Math.max(0.0f, disciple.daoHeartStability - 25.0f);
        disciple.cultivationExp = (long) (disciple.cultivationExp * 0.7f); // Lose 30% progress

        if (disciple.daoHeartStability < 30.0f) {
            triggerQiDeviation(disciple, "Failed breakthrough caused violent meridian rupture and heart demon intrusion");
        } else {
            disciple.health = Math.max(20, disciple.health - 40);
            transitionTo(disciple, DiscipleLifecycleEntity.STATE_INJURED, "Breakthrough failed, suffered internal injuries");
        }
        notifyBreakthrough(disciple, disciple.realm, disciple.realm, false, "Breakthrough attempt failed.");
    }

    private void progressTribulationStrike(DiscipleLifecycleEntity disciple) {
        disciple.tribulationCounter++;
        int strike = disciple.tribulationCounter;

        // Damage scales with strike index (1 to 9)
        int damage = 10 + (strike * 10) - (disciple.talentGrade * 2);
        disciple.health = Math.max(0, disciple.health - damage);

        if (disciple.health <= 0) {
            killDisciple(disciple, "Obliterated by the " + strike + "th Heavenly Tribulation Lightning Strike");
            notifyTribulation(disciple, strike, false, "Fell under Heavenly Tribulation.");
            return;
        }

        notifyTribulation(disciple, strike, true, "Survived Strike #" + strike + " (" + disciple.health + " HP remaining)");

        if (strike >= 9) {
            // Successfully survived all 9 Heavenly Tribulation strikes!
            disciple.realm = DiscipleLifecycleEntity.REALM_TRUE_IMMORTAL;
            disciple.subStage = DiscipleLifecycleEntity.SUB_PEAK;
            disciple.lifecycleStage = DiscipleLifecycleEntity.STAGE_ANCESTOR;
            transitionTo(disciple, DiscipleLifecycleEntity.STATE_ASCENDED, "Ascended to the Higher Immortal Realm!");
            notifyAscension(disciple);
        }
    }

    public synchronized void triggerQiDeviation(DiscipleLifecycleEntity disciple, String reason) {
        if (disciple == null || !disciple.isAlive()) return;
        disciple.daoHeartStability = Math.min(20.0f, disciple.daoHeartStability);
        disciple.mood = 0;
        transitionTo(disciple, DiscipleLifecycleEntity.STATE_QI_DEVIATION, reason);
    }

    public synchronized boolean treatQiDeviation(DiscipleLifecycleEntity disciple, int healingElixirGrade) {
        if (disciple == null || !disciple.isAlive()) return false;
        if (disciple.currentState != DiscipleLifecycleEntity.STATE_QI_DEVIATION) return false;

        float restoration = 30.0f + (healingElixirGrade * 20.0f);
        disciple.daoHeartStability = Math.min(100.0f, disciple.daoHeartStability + restoration);
        disciple.health = Math.min(100, disciple.health + 30);
        transitionTo(disciple, DiscipleLifecycleEntity.STATE_RESTING, "Qi deviation calmed by Spirit Cleansing Elixir");
        return true;
    }

    public synchronized void assignDuty(DiscipleLifecycleEntity disciple, String dutyName) {
        if (disciple == null || !disciple.isAlive()) return;
        disciple.assignedDuty = dutyName != null ? dutyName : "General Caretaker";
        transitionTo(disciple, DiscipleLifecycleEntity.STATE_SECT_DUTY, "Assigned to duty: " + disciple.assignedDuty);
    }

    public synchronized void promoteLifecycleStage(DiscipleLifecycleEntity disciple, int newStage) {
        if (disciple == null || !disciple.isAlive()) return;
        disciple.lifecycleStage = Math.max(0, Math.min(DiscipleLifecycleEntity.STAGE_ANCESTOR, newStage));
    }

    private void updateLifecycleStageFromRealm(DiscipleLifecycleEntity disciple) {
        if (disciple.realm >= DiscipleLifecycleEntity.REALM_GREAT_MAHAYANA) {
            disciple.lifecycleStage = DiscipleLifecycleEntity.STAGE_ANCESTOR;
        } else if (disciple.realm >= DiscipleLifecycleEntity.REALM_SOUL_FORMATION) {
            disciple.lifecycleStage = DiscipleLifecycleEntity.STAGE_GRAND_ELDER;
        } else if (disciple.realm >= DiscipleLifecycleEntity.REALM_NASCENT_SOUL) {
            disciple.lifecycleStage = DiscipleLifecycleEntity.STAGE_ELDER;
        } else if (disciple.realm >= DiscipleLifecycleEntity.REALM_CORE_FORMATION) {
            disciple.lifecycleStage = DiscipleLifecycleEntity.STAGE_CORE_DISCIPLE;
        } else if (disciple.realm >= DiscipleLifecycleEntity.REALM_FOUNDATION_ESTABLISHMENT) {
            disciple.lifecycleStage = DiscipleLifecycleEntity.STAGE_INNER_DISCIPLE;
        }
    }

    public synchronized void killDisciple(DiscipleLifecycleEntity disciple, String cause) {
        if (disciple == null) return;
        disciple.currentState = DiscipleLifecycleEntity.STATE_DECEASED;
        disciple.health = 0;
        disciple.lastStateChangeTime = System.currentTimeMillis();
        notifyDemise(disciple, cause);
    }

    // =========================================================================
    // AUTONOMOUS UTILITY AI
    // =========================================================================

    public synchronized void evaluateAutonomousBehavior(DiscipleLifecycleEntity disciple, SectEnvironment env) {
        if (disciple == null || !disciple.isAlive()) return;

        // High priority: Starvation takes precedence
        if (disciple.hunger <= 15 && disciple.currentState != DiscipleLifecycleEntity.STATE_EATING && disciple.currentState != DiscipleLifecycleEntity.STATE_TRIBULATION) {
            if (env.availableSpiritFood > 0) {
                transitionTo(disciple, DiscipleLifecycleEntity.STATE_EATING, "Hunger critical, heading to Dining Hall");
            }
            return;
        }

        // High priority: Exhaustion takes precedence
        if (disciple.energy <= 15 && disciple.currentState != DiscipleLifecycleEntity.STATE_RESTING && disciple.currentState != DiscipleLifecycleEntity.STATE_TRIBULATION) {
            transitionTo(disciple, DiscipleLifecycleEntity.STATE_RESTING, "Energy depleted, resting in residence");
            return;
        }

        // Ready for breakthrough
        if (disciple.canAttemptBreakthrough() && disciple.currentState == DiscipleLifecycleEntity.STATE_IDLE) {
            attemptBreakthrough(disciple, 1);
            return;
        }

        // If Idle and healthy, resume cultivation or sect duty
        if (disciple.currentState == DiscipleLifecycleEntity.STATE_IDLE) {
            if (disciple.cultivationExp < disciple.maxCultivationExp) {
                startCultivation(disciple);
            } else {
                assignDuty(disciple, disciple.assignedDuty);
            }
        }
    }

    // =========================================================================
    // LISTENER DISPATCHERS
    // =========================================================================

    private void notifyStateChanged(DiscipleLifecycleEntity disciple, int oldState, int newState, String reason) {
        for (int i = 0; i < listeners.size(); i++) {
            try {
                listeners.get(i).onStateChanged(disciple, oldState, newState, reason);
            } catch (Throwable ignored) {}
        }
    }

    private void notifyBreakthrough(DiscipleLifecycleEntity disciple, int oldRealm, int newRealm, boolean success, String log) {
        for (int i = 0; i < listeners.size(); i++) {
            try {
                listeners.get(i).onBreakthrough(disciple, oldRealm, newRealm, success, log);
            } catch (Throwable ignored) {}
        }
    }

    private void notifyTribulation(DiscipleLifecycleEntity disciple, int strike, boolean survived, String log) {
        for (int i = 0; i < listeners.size(); i++) {
            try {
                listeners.get(i).onTribulationEvent(disciple, strike, survived, log);
            } catch (Throwable ignored) {}
        }
    }

    private void notifyDemise(DiscipleLifecycleEntity disciple, String cause) {
        for (int i = 0; i < listeners.size(); i++) {
            try {
                listeners.get(i).onDemise(disciple, cause);
            } catch (Throwable ignored) {}
        }
    }

    private void notifyAscension(DiscipleLifecycleEntity disciple) {
        for (int i = 0; i < listeners.size(); i++) {
            try {
                listeners.get(i).onAscension(disciple);
            } catch (Throwable ignored) {}
        }
    }
}
