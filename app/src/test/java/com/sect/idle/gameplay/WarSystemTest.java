package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * WarSystemTest - Unit tests for territory conquest, battle calculations, and war logs.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public class WarSystemTest {

    private WarSystem warSystem;

    @Before
    public void setUp() {
        warSystem = WarSystem.getInstance();
    }

    @Test
    public void testSectCampaignExecution() {
        SectData sectData = SectData.getInstance();
        sectData.reset();
        sectData.addDisciple(new Disciple("Sword Elder"));

        WarSystem.WarResult result = warSystem.launchSectCampaign(sectData, 1);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.targetSectName);
        Assert.assertTrue("Sect power should be greater than 0", result.sectPowerTotal > 0);
        Assert.assertTrue("Round logs should be generated", result.roundLogs.size() > 0);
    }

    @Test
    public void testBattleLogFormatting() {
        WarSystem.WarBattleLogEntry entry = new WarSystem.WarBattleLogEntry(
            1,
            "Critical Strike",
            "Elder Meng",
            "Demonic Beast",
            450,
            "Heavenly Thunder Slash dealt 450 critical damage!",
            0xFFFFD700
        );

        String formatted = entry.getFormattedLog();
        Assert.assertNotNull(formatted);
        Assert.assertTrue(formatted.contains("[Round 1]"));
        Assert.assertTrue(formatted.contains("Elder Meng ➔ Demonic Beast"));
        Assert.assertTrue(formatted.contains("450 critical damage"));
    }
}
