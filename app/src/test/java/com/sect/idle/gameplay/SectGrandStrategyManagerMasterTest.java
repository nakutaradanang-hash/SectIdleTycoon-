package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Master Integration Test for SectGrandStrategyManager.
 * Validates the harmonious convergence of Romance of the Three Kingdoms,
 * Civilization 4X, and The Sims systems in a single deterministic turn loop.
 */
public class SectGrandStrategyManagerMasterTest {

    private SectGrandStrategyManager grandManager;
    private SectData sectData;

    @Before
    public void setUp() {
        grandManager = SectGrandStrategyManager.getInstance();
        sectData = SectData.getInstance();
        sectData.reset();
        sectData.spiritStones = 50000L;
        sectData.spiritHerbs = 10000L;
        sectData.spiritOres = 10000L;
        sectData.disciples.add(new Disciple("Master Ling", 1));
        sectData.disciples.add(new Disciple("Elder Xiao", 1));
    }

    @Test
    public void testFullGrandTurnAdvancement() {
        int initialTurns = grandManager.getTurnCount();
        String dayLog = grandManager.advanceTurn(sectData);

        assertNotNull(dayLog);
        assertTrue(dayLog.contains("JIANHU REALM ADVANCEMENT"));
        assertTrue(dayLog.contains("Weather"));
        assertTrue(dayLog.contains("Hex Outposts"));
        assertEquals(initialTurns + 1, grandManager.getTurnCount());
    }

    @Test
    public void testSequentialTurnProgressionResilience() {
        // Run 50 full continuous simulation days without any crashes or memory leaks
        for (int day = 1; day <= 50; day++) {
            String log = grandManager.advanceTurn(sectData);
            assertNotNull(log);
            assertFalse(log.contains("Error:"));
        }
        assertTrue("Grand strategy turn count must reach at least 50", grandManager.getTurnCount() >= 50);
    }
}
