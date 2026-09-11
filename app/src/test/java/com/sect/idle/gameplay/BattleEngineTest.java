package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * BattleEngineTest - Unit tests for ATB turn simulation, combat action ticking,
 * victory/defeat evaluation, damage application, and battle logging.
 */
public class BattleEngineTest {

    @Test
    public void testBattleInitialization() {
        BattleEngine engine = new BattleEngine();

        ArrayList<Disciple> playerTeam = new ArrayList<Disciple>();
        playerTeam.add(new Disciple("Player Leader"));

        ArrayList<Disciple> enemyTeam = new ArrayList<Disciple>();
        enemyTeam.add(new Disciple("Demon Beast"));

        engine.startBattle(playerTeam, enemyTeam);

        Assert.assertTrue(engine.isRunning);
        Assert.assertEquals(2, engine.getUnits().size());
        Assert.assertFalse(engine.getLog().isEmpty());
    }

    @Test
    public void testBattleTickAndResolution() {
        BattleEngine engine = new BattleEngine();

        ArrayList<Disciple> playerTeam = new ArrayList<Disciple>();
        Disciple godDisciple = new Disciple("Immortal Hero");
        godDisciple.initStats(100, 100, 100, 100, 100, 100, 100);
        godDisciple.hp = 10000;
        godDisciple.maxHp = 10000;
        godDisciple.atk = 5000;
        godDisciple.spd = 100;
        playerTeam.add(godDisciple);

        ArrayList<Disciple> enemyTeam = new ArrayList<Disciple>();
        Disciple weakDemon = new Disciple("Goblin");
        weakDemon.hp = 10;
        weakDemon.maxHp = 10;
        weakDemon.def = 0;
        enemyTeam.add(weakDemon);

        engine.startBattle(playerTeam, enemyTeam);

        // Run ticks until battle finishes
        int maxTicks = 200;
        while (engine.isRunning && maxTicks-- > 0) {
            engine.tick();
        }

        Assert.assertFalse(engine.isRunning);
        Assert.assertTrue(engine.playerWon);
        Assert.assertTrue(weakDemon.hp <= 0 || !engine.getUnits().get(1).isAlive);
    }

    @Test
    public void testEmptyTeams() {
        BattleEngine engine = new BattleEngine();
        engine.startBattle(null, null);
        engine.tick();
        Assert.assertFalse(engine.isRunning);
    }
}
