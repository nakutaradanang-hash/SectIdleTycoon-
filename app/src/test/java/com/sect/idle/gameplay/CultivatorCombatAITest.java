package com.sect.idle.gameplay;

import com.sect.idle.models.BattleUnit;
import com.sect.idle.models.Disciple;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.*;

/**
 * Unit Tests for CultivatorCombatAI.
 * Validates role determination heuristics, 3D kinematic trajectories, flying sword arrays,
 * defensive shield fading, and fault-tolerant AI state transitions.
 */
public class CultivatorCombatAITest {

    private Disciple swordDisciple;
    private Disciple casterDisciple;
    private Disciple tankDisciple;
    private Disciple healerDisciple;

    @Before
    public void setUp() {
        swordDisciple = new Disciple("Li Bai", 1);
        swordDisciple.str = 80;
        swordDisciple.intel = 20;
        swordDisciple.vit = 30;
        swordDisciple.wis = 20;

        casterDisciple = new Disciple("Zhuge Liang", 1);
        casterDisciple.str = 20;
        casterDisciple.intel = 90;
        casterDisciple.vit = 20;
        casterDisciple.wis = 40;

        tankDisciple = new Disciple("Dian Wei", 1);
        tankDisciple.str = 40;
        tankDisciple.intel = 10;
        tankDisciple.vit = 95;
        tankDisciple.wis = 15;

        healerDisciple = new Disciple("Hua Tuo", 1);
        healerDisciple.str = 10;
        healerDisciple.intel = 30;
        healerDisciple.vit = 25;
        healerDisciple.wis = 90;
    }

    @Test
    public void testDetermineRole() {
        BattleUnit uSword = new BattleUnit(swordDisciple, true, 0);
        assertEquals(CultivatorCombatAI.ROLE_SWORD_IMMORTAL, CultivatorCombatAI.determineRole(uSword));

        BattleUnit uCaster = new BattleUnit(casterDisciple, true, 1);
        assertEquals(CultivatorCombatAI.ROLE_DAOIST_CASTER, CultivatorCombatAI.determineRole(uCaster));

        BattleUnit uTank = new BattleUnit(tankDisciple, true, 2);
        assertEquals(CultivatorCombatAI.ROLE_BODY_REFINER, CultivatorCombatAI.determineRole(uTank));

        BattleUnit uHealer = new BattleUnit(healerDisciple, true, 3);
        assertEquals(CultivatorCombatAI.ROLE_SPIRIT_HEALER, CultivatorCombatAI.determineRole(uHealer));

        // Null unit fallback
        assertEquals(CultivatorCombatAI.ROLE_SWORD_IMMORTAL, CultivatorCombatAI.determineRole(null));
    }

    @Test
    public void testAIStateInitialization() {
        CultivatorCombatAI.AIState aiState = new CultivatorCombatAI.AIState(CultivatorCombatAI.ROLE_SWORD_IMMORTAL, 100f, 200f);
        assertEquals(CultivatorCombatAI.ROLE_SWORD_IMMORTAL, aiState.role);
        assertEquals(100f, aiState.posX, 0.001f);
        assertEquals(200f, aiState.posY, 0.001f);
        assertEquals(0f, aiState.posZ, 0.001f);
        assertFalse(aiState.skills.isEmpty());
        assertEquals(CultivatorCombatAI.AIState.SWORD_COUNT, aiState.swordOffsetsX.length);
    }

    @Test
    public void testUpdateUnitAI_IdleHoverAndSwordOrbit() {
        BattleUnit unit = new BattleUnit(swordDisciple, true, 0);
        CultivatorCombatAI.AIState ai = new CultivatorCombatAI.AIState(CultivatorCombatAI.ROLE_SWORD_IMMORTAL, 50f, 50f);
        ArrayList<BattleUnit> allies = new ArrayList<BattleUnit>();
        allies.add(unit);
        ArrayList<BattleUnit> enemies = new ArrayList<BattleUnit>();

        // Tick 0.1s
        CultivatorCombatAI.updateUnitAI(unit, ai, 0.1f, allies, enemies, null);

        assertTrue("Anim time must advance", ai.animTime > 0f);
        assertTrue("Sword orbit angle must advance", ai.swordOrbitAngle > 0f);
        // Verify flying sword offset arrays are calculated without NaN
        for (int i = 0; i < CultivatorCombatAI.AIState.SWORD_COUNT; i++) {
            assertFalse(Float.isNaN(ai.swordOffsetsX[i]));
            assertFalse(Float.isNaN(ai.swordOffsetsY[i]));
            assertFalse(Float.isNaN(ai.swordOffsetsZ[i]));
        }
    }

    @Test
    public void testDeadUnitGravityDrop() {
        BattleUnit unit = new BattleUnit(swordDisciple, true, 0);
        unit.isAlive = false;
        CultivatorCombatAI.AIState ai = new CultivatorCombatAI.AIState(CultivatorCombatAI.ROLE_SWORD_IMMORTAL, 50f, 50f);
        ai.posZ = 100f;

        CultivatorCombatAI.updateUnitAI(unit, ai, 0.1f, new ArrayList<BattleUnit>(), new ArrayList<BattleUnit>(), null);
        assertTrue("Dead unit must fall towards ground", ai.posZ < 100f);
    }
}
