package com.sect.idle.gameplay;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit Tests for Civilization Style 4X Systems (Hex Grid, Tech Tree, Policies, Wonders).
 */
public class Civilization4XSystemsTest {

    private RealmHexGridMap hexMap;
    private DaoTechTreeSystem techTree;
    private SectPolicySystem policies;
    private WonderMonumentSystem wonders;
    private SectData sectData;

    @Before
    public void setUp() {
        hexMap = RealmHexGridMap.getInstance();
        techTree = DaoTechTreeSystem.getInstance();
        policies = SectPolicySystem.getInstance();
        wonders = WonderMonumentSystem.getInstance();
        sectData = SectData.getInstance();
        sectData.reset();
        sectData.spiritStones = 100000L;
        sectData.spiritHerbs = 50000L;
        sectData.spiritOres = 50000L;
        sectData.jade = 1000L;
    }

    @Test
    public void testHexGridMapGenerationAndClaiming() {
        assertEquals(RealmHexGridMap.TOTAL_HEX_TILES, hexMap.grid.length);

        RealmHexGridMap.HexTile origin = hexMap.getTile(0, 0);
        assertNotNull(origin);
        assertTrue(origin.explored);
        assertEquals(RealmHexGridMap.OWNER_PLAYER_SECT, origin.owner);

        // Explore and claim a new tile
        hexMap.exploreTile(2, 2);
        RealmHexGridMap.HexTile target = hexMap.getTile(2, 2);
        assertTrue(target.explored);

        boolean claimed = hexMap.claimTerritory(sectData, 2, 2);
        assertTrue("Claiming explored tile with sufficient funds must succeed", claimed);
        assertEquals(RealmHexGridMap.OWNER_PLAYER_SECT, target.owner);
    }

    @Test
    public void testTechTreeResearchAndPrerequisites() {
        // Foundation tech already unlocked in init
        DaoTechTreeSystem.DaoTech breathing = techTree.getTech("TECH_BREATHING");
        assertNotNull(breathing);
        assertTrue(breathing.unlocked);

        // Research next tech
        boolean selected = techTree.selectResearch("TECH_HERB_FARMING");
        assertTrue(selected);

        boolean unlocked = techTree.addResearchProgress(500L);
        assertTrue("Sufficient research points must unlock tech", unlocked);
        assertTrue(techTree.getTech("TECH_HERB_FARMING").unlocked);
    }

    @Test
    public void testSectPoliciesToggle() {
        SectPolicySystem.SectPolicy martial = policies.getPolicy("POL_MARTIAL_EXPANSION");
        assertNotNull(martial);
        assertFalse(martial.active);

        boolean toggled = policies.togglePolicy("POL_MARTIAL_EXPANSION");
        assertTrue(toggled);
        assertTrue(martial.active);
        assertTrue(policies.isPolicyActive("POL_MARTIAL_EXPANSION"));
    }

    @Test
    public void testWonderConstructionAndVictoryConditions() {
        WonderMonumentSystem.AncientWonder tower = wonders.getWonder("WONDER_NINE_HEAVENS");
        assertNotNull(tower);

        boolean started = wonders.startConstruction(sectData, "WONDER_NINE_HEAVENS");
        assertTrue("Starting wonder construction with ample resources must succeed", started);
        assertEquals("WONDER_NINE_HEAVENS", wonders.currentUnderConstructionId);

        // Fast forward construction turns
        for (int i = 0; i < tower.constructionTurnsRequired; i++) {
            wonders.advanceConstructionTurn();
        }

        assertTrue("Wonder should be completed after required turns", tower.isCompleted);

        // Unlock final tech for Celestial Ascension Victory
        techTree.unlockTechDirectly("TECH_IMMORTAL_ASCENSION");
        int victory = wonders.checkVictoryCondition(sectData, FactionDiplomacySystem.getInstance());
        assertEquals(WonderMonumentSystem.VICTORY_CELESTIAL_ASCENSION, victory);
    }
}
