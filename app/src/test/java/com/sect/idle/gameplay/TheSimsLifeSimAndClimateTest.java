package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit Tests for The Sims Style Life Simulation & Dynamic Climate/Weather Systems.
 */
public class TheSimsLifeSimAndClimateTest {

    private DiscipleLifeSimSystem lifeSim;
    private ClimateSeasonSystem climate;
    private SectData sectData;

    @Before
    public void setUp() {
        lifeSim = DiscipleLifeSimSystem.getInstance();
        climate = ClimateSeasonSystem.getInstance();
        sectData = SectData.getInstance();
        sectData.reset();
        sectData.spiritHerbs = 1000L;
    }

    @Test
    public void testLifeSimNeedsDecayAndRestoration() {
        Disciple d = new Disciple("Bella Goth", 1);
        d.energy = 50;
        d.stress = 40;
        d.currentTask = 1; // Busy working

        sectData.disciples.clear();
        sectData.disciples.add(d);

        lifeSim.tickDiscipleLifeNeeds(sectData);
        assertTrue("Working disciple should consume energy", d.energy < 50);
        assertTrue("Working disciple should accumulate stress", d.stress > 40);

        // Feed disciple herbal tonic
        boolean fed = lifeSim.feedDisciple(d, "Spirit Ginseng Broth", 20, sectData);
        assertTrue(fed);
        assertTrue("Feeding must reduce stress", d.stress < 50);
    }

    @Test
    public void testSocialInteractionsAndDaoCompanionship() {
        Disciple d1 = new Disciple("Mortimer", 1);
        Disciple d2 = new Disciple("Bella", 1);
        d1.mood = 80;
        d2.mood = 80;

        // Chat
        String chatResult = lifeSim.performSocialInteraction(d1, d2, 1);
        assertNotNull(chatResult);
        assertTrue(chatResult.contains("discussed the Dao"));

        // Dao Companionship Proposal (Marriage)
        String proposalResult = lifeSim.performSocialInteraction(d1, d2, 3);
        assertNotNull(proposalResult);
        assertTrue(proposalResult.contains("DAO COMPANIONSHIP PLEDGED"));
        assertEquals(100, d1.loyalty);
        assertEquals(100, d2.loyalty);
    }

    @Test
    public void testClimateSeasonsAndWeatherProgression() {
        climate.currentSeason = ClimateSeasonSystem.SEASON_SPRING;
        climate.seasonDay = 1;

        assertEquals("Spring Blossom", climate.getSeasonName());

        // Fast forward full 30 days of season
        for (int i = 0; i < 30; i++) {
            climate.advanceDay(sectData);
        }

        assertEquals("Summer Solstice", climate.getSeasonName());
        assertNotNull(climate.getWeatherName());
    }
}
