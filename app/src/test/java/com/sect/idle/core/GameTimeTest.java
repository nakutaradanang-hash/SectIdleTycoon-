package com.sect.idle.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * GameTimeTest - Unit tests for in-game celestial time progression, seasons, day/night cycle, and display formatting.
 */
public class GameTimeTest {

    @Test
    public void testInitialTimeAndTick() {
        GameTime time = new GameTime();
        time.set(1, 1, 1, 6);

        Assert.assertEquals(1, time.year);
        Assert.assertEquals(1, time.month);
        Assert.assertEquals(1, time.day);
        Assert.assertEquals(6, time.hour);
        Assert.assertEquals(0, time.season); // Spring

        // Tick 18 hours -> 24:00 (next day)
        for (int i = 0; i < 18; i++) {
            time.tick();
        }
        Assert.assertEquals(0, time.hour);
        Assert.assertEquals(2, time.day);
    }

    @Test
    public void testDayNightCycle() {
        GameTime time = new GameTime();

        time.set(1, 1, 1, 2);
        Assert.assertTrue(time.isNight());
        Assert.assertFalse(time.isDay());

        time.set(1, 1, 1, 6);
        Assert.assertTrue(time.isDawn());

        time.set(1, 1, 1, 12);
        Assert.assertTrue(time.isDay());
        Assert.assertFalse(time.isNight());

        time.set(1, 1, 1, 18);
        Assert.assertTrue(time.isDusk());

        time.set(1, 1, 1, 22);
        Assert.assertTrue(time.isNight());
    }

    @Test
    public void testMonthAndSeasonProgression() {
        GameTime time = new GameTime();
        time.set(1, 1, 30, 23);

        time.tick(); // Day rolled over
        Assert.assertEquals(2, time.month);
        Assert.assertEquals(1, time.day);
        Assert.assertEquals(0, time.season); // Still Spring (Months 1, 2, 3)

        // Advance to Month 4 -> Summer (Season 1)
        time.set(1, 4, 1, 6);
        Assert.assertEquals(1, time.season); // Summer

        // Advance to Month 7 -> Autumn (Season 2)
        time.set(1, 7, 1, 6);
        Assert.assertEquals(2, time.season); // Autumn

        // Advance to Month 10 -> Winter (Season 3)
        time.set(1, 10, 1, 6);
        Assert.assertEquals(3, time.season); // Winter

        // Advance to Year 2
        time.set(1, 12, 30, 23);
        time.tick();
        Assert.assertEquals(2, time.year);
        Assert.assertEquals(1, time.month);
        Assert.assertEquals(1, time.day);
    }

    @Test
    public void testDisplayFormatting() {
        GameTime time = new GameTime();
        time.set(10, 5, 15, 8);

        String display = time.getDisplay();
        Assert.assertNotNull(display);
        Assert.assertTrue(display.contains("Y10"));
        Assert.assertTrue(display.contains("M5"));
        Assert.assertTrue(display.contains("D15"));
        Assert.assertTrue(display.contains("08:00"));
        Assert.assertTrue(display.contains("Summer"));
    }
}
