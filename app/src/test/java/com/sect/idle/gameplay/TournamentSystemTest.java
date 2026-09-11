package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TournamentSystemTest - Unit tests for sect tournament duels, power evaluation,
 * reward distribution, and victory/defeat outcomes.
 */
public class TournamentSystemTest {

    private SectData sectData;

    @Before
    public void setUp() {
        sectData = SectData.getInstance();
        sectData.spiritStones = 1000;
        sectData.jade = 10;
    }

    @Test
    public void testTournamentEntry() {
        TournamentSystem tournament = TournamentSystem.getInstance();
        Disciple champion = new Disciple("Sword Immortal");
        champion.initStats(80, 80, 80, 50, 80, 50, 50);
        champion.realm = 5;
        champion.recalcCombat();

        TournamentSystem.TournamentResult result = tournament.enterTournament(champion, sectData, 0);

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.summary);
        Assert.assertNotNull(result.opponentName);
        Assert.assertTrue(result.prizeStones > 0);
    }

    @Test
    public void testNullDiscipleEntry() {
        TournamentSystem tournament = TournamentSystem.getInstance();
        TournamentSystem.TournamentResult result = tournament.enterTournament(null, sectData, 0);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.won);
        Assert.assertEquals("No disciple selected.", result.summary);
    }
}
