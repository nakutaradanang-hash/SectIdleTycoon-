package com.sect.idle.gameplay;

import com.sect.idle.data.db.entities.DiscipleLifecycleEntity;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/**
 * DiscipleLifecycleStateMachineTest - Unit Tests for DiscipleLifecycleEntity Room Model,
 * Sims-style survival needs, FSM state transitions, and Xianxia cultivation breakthroughs.
 */
public class DiscipleLifecycleStateMachineTest {

    private DiscipleStateMachine stateMachine;
    private DiscipleLifecycleEntity disciple;
    private DiscipleStateMachine.SectEnvironment environment;

    @Before
    public void setUp() {
        stateMachine = DiscipleStateMachine.getInstance();
        disciple = new DiscipleLifecycleEntity(
                "disciple_test_001",
                "Lin Dong",
                1,
                18,
                DiscipleLifecycleEntity.ELEMENT_FIRE,
                7
        );
        environment = new DiscipleStateMachine.SectEnvironment(2.0f, 500, 50);
    }

    @Test
    public void testInitialDiscipleLifecycleEntityCreation() {
        assertNotNull(disciple.discipleId);
        assertEquals("Lin Dong", disciple.name);
        assertEquals(18, disciple.age);
        assertEquals(DiscipleLifecycleEntity.STAGE_OUTER_DISCIPLE, disciple.lifecycleStage);
        assertEquals(DiscipleLifecycleEntity.STATE_IDLE, disciple.currentState);
        assertEquals(100, disciple.hunger);
        assertEquals(100, disciple.energy);
        assertEquals(100, disciple.health);
        assertEquals(DiscipleLifecycleEntity.REALM_QI_CONDENSATION, disciple.realm);
        assertEquals(DiscipleLifecycleEntity.SUB_EARLY, disciple.subStage);
        assertTrue(disciple.isAlive());
        assertFalse(disciple.isStarving());
        assertFalse(disciple.isExhausted());
    }

    @Test
    public void testNeedsDecayAndStarvation() {
        disciple.currentState = DiscipleLifecycleEntity.STATE_SECT_DUTY;
        disciple.hunger = 20;
        disciple.energy = 20;

        // Simulate 500 seconds of duty
        stateMachine.tick(disciple, 500, environment);

        assertTrue("Hunger should have decayed", disciple.hunger < 20);
        assertTrue("Energy should have decayed", disciple.energy < 20);
    }

    @Test
    public void testFeedingAndRestingRestoration() {
        disciple.hunger = 20;
        disciple.energy = 20;
        disciple.health = 50;

        stateMachine.feed(disciple, 3);
        assertTrue("Hunger should be restored", disciple.hunger > 60);
        assertTrue("Health should be improved", disciple.health > 50);

        stateMachine.rest(disciple);
        assertEquals(DiscipleLifecycleEntity.STATE_RESTING, disciple.currentState);

        stateMachine.tick(disciple, 600, environment);
        assertTrue("Energy should be restored after rest", disciple.energy >= 90);
    }

    @Test
    public void testCultivationExpAccumulation() {
        stateMachine.startCultivation(disciple);
        assertEquals(DiscipleLifecycleEntity.STATE_CULTIVATING, disciple.currentState);

        long initialExp = disciple.cultivationExp;
        stateMachine.tick(disciple, 100, environment);

        assertTrue("Cultivation EXP should increase", disciple.cultivationExp > initialExp);
        assertTrue("Qi Satiety should increase", disciple.qiSatiety > 50);
    }

    @Test
    public void testBreakthroughSuccess() {
        disciple.realm = DiscipleLifecycleEntity.REALM_QI_CONDENSATION;
        disciple.subStage = DiscipleLifecycleEntity.SUB_EARLY;
        disciple.cultivationExp = disciple.maxCultivationExp;
        disciple.daoHeartStability = 100.0f;
        disciple.talentGrade = 9; // High talent ensures high breakthrough chance

        final AtomicBoolean breakthroughFired = new AtomicBoolean(false);
        DiscipleStateMachine.StateChangeListener listener = new DiscipleStateMachine.StateChangeListener() {
            @Override
            public void onStateChanged(DiscipleLifecycleEntity d, int oldState, int newState, String reason) {}

            @Override
            public void onBreakthrough(DiscipleLifecycleEntity d, int oldRealm, int newRealm, boolean success, String log) {
                breakthroughFired.set(true);
            }

            @Override
            public void onTribulationEvent(DiscipleLifecycleEntity d, int strike, boolean survived, String log) {}

            @Override
            public void onDemise(DiscipleLifecycleEntity d, String cause) {}

            @Override
            public void onAscension(DiscipleLifecycleEntity d) {}
        };

        stateMachine.addListener(listener);
        boolean success = stateMachine.attemptBreakthrough(disciple, 4);
        stateMachine.removeListener(listener);

        assertTrue("Breakthrough callback should fire", breakthroughFired.get());
        if (success) {
            assertEquals("Sub-stage should advance on breakthrough", DiscipleLifecycleEntity.SUB_MID, disciple.subStage);
            assertEquals(0L, disciple.cultivationExp);
        }
    }

    @Test
    public void testQiDeviationAndTreatment() {
        stateMachine.triggerQiDeviation(disciple, "Heart Demon Intrusion");
        assertEquals(DiscipleLifecycleEntity.STATE_QI_DEVIATION, disciple.currentState);
        assertTrue(disciple.daoHeartStability <= 20.0f);

        boolean treated = stateMachine.treatQiDeviation(disciple, 3);
        assertTrue("Qi deviation treatment should succeed", treated);
        assertTrue("Dao heart should recover", disciple.daoHeartStability > 50.0f);
    }

    @Test
    public void testTribulationAndImmortalAscension() {
        disciple.realm = DiscipleLifecycleEntity.REALM_TRIBULATION_TRANSCENDENCE;
        disciple.subStage = DiscipleLifecycleEntity.SUB_PEAK;
        disciple.health = 100;
        disciple.talentGrade = 9;

        final AtomicInteger tribulationStrikes = new AtomicInteger(0);
        final AtomicBoolean ascended = new AtomicBoolean(false);

        DiscipleStateMachine.StateChangeListener listener = new DiscipleStateMachine.StateChangeListener() {
            @Override
            public void onStateChanged(DiscipleLifecycleEntity d, int oldState, int newState, String reason) {}

            @Override
            public void onBreakthrough(DiscipleLifecycleEntity d, int oldRealm, int newRealm, boolean success, String log) {}

            @Override
            public void onTribulationEvent(DiscipleLifecycleEntity d, int strike, boolean survived, String log) {
                tribulationStrikes.incrementAndGet();
            }

            @Override
            public void onDemise(DiscipleLifecycleEntity d, String cause) {}

            @Override
            public void onAscension(DiscipleLifecycleEntity d) {
                ascended.set(true);
            }
        };

        stateMachine.addListener(listener);
        stateMachine.transitionTo(disciple, DiscipleLifecycleEntity.STATE_TRIBULATION, "Initiated 9 Heavenly Strikes");

        // Simulate surviving 9 tribulation ticks
        for (int i = 1; i <= 9; i++) {
            disciple.health = 100; // Keep alive for test
            disciple.stateTimerSeconds = 5;
            stateMachine.tick(disciple, 5, environment);
        }

        stateMachine.removeListener(listener);

        assertTrue("Tribulation strikes should have triggered", tribulationStrikes.get() >= 9);
        assertEquals(DiscipleLifecycleEntity.REALM_TRUE_IMMORTAL, disciple.realm);
        assertEquals(DiscipleLifecycleEntity.STATE_ASCENDED, disciple.currentState);
        assertTrue("Disciple should be marked ascended", ascended.get());
    }

    @Test
    public void testAutonomousAiDecisions() {
        // Starving disciple should autonomously seek dining hall
        disciple.hunger = 10;
        disciple.currentState = DiscipleLifecycleEntity.STATE_IDLE;
        stateMachine.evaluateAutonomousBehavior(disciple, environment);
        assertEquals(DiscipleLifecycleEntity.STATE_EATING, disciple.currentState);

        // Exhausted disciple should autonomously rest
        disciple.hunger = 80;
        disciple.energy = 5;
        disciple.currentState = DiscipleLifecycleEntity.STATE_IDLE;
        stateMachine.evaluateAutonomousBehavior(disciple, environment);
        assertEquals(DiscipleLifecycleEntity.STATE_RESTING, disciple.currentState);
    }

    @Test
    public void testDutyAssignmentAndLifecycleStagePromotion() {
        stateMachine.assignDuty(disciple, "Alchemy Cauldron Refiner");
        assertEquals(DiscipleLifecycleEntity.STATE_SECT_DUTY, disciple.currentState);
        assertEquals("Alchemy Cauldron Refiner", disciple.assignedDuty);

        stateMachine.promoteLifecycleStage(disciple, DiscipleLifecycleEntity.STAGE_ELDER);
        assertEquals(DiscipleLifecycleEntity.STAGE_ELDER, disciple.lifecycleStage);
        assertEquals("Hall Elder", DiscipleLifecycleEntity.getStageName(disciple.lifecycleStage));
    }
}
