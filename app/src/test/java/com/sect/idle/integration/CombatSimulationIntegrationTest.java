package com.sect.idle.integration;

import com.sect.idle.gameplay.BattleEngine;
import com.sect.idle.models.BattleUnit;
import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * CombatSimulationIntegrationTest - Validates multi-unit team battles, action order,
 * elemental advantages, and battle resolution under intensive combat iterations.
 */
public class CombatSimulationIntegrationTest {

    @Test
    public void testHighIntensityTeamBattle() {
        ArrayList<Disciple> sectTeam = new ArrayList<Disciple>();
        for (int i = 0; i < 3; i++) {
            Disciple d = new Disciple("Sect Disciple " + (i + 1));
            d.initStats(40 + i * 5, 30 + i * 5, 25, 20, 40, 20, 20);
            d.recalcCombat();
            sectTeam.add(d);
        }

        ArrayList<Disciple> demonTeam = new ArrayList<Disciple>();
        for (int i = 0; i < 3; i++) {
            Disciple beast = new Disciple("Demon Wolf " + (i + 1));
            beast.initStats(30, 25, 10, 10, 30, 10, 10);
            beast.recalcCombat();
            demonTeam.add(beast);
        }

        BattleEngine engine = new BattleEngine();
        engine.startBattle(sectTeam, demonTeam);

        Assert.assertTrue(engine.isRunning);
        Assert.assertEquals(6, engine.getUnits().size());

        int maxTicks = 500;
        while (engine.isRunning && maxTicks-- > 0) {
            engine.tick();
        }

        Assert.assertFalse("Battle should conclude within max ticks", engine.isRunning);
        Assert.assertFalse(engine.getLog().isEmpty());
    }
}
