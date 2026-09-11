package com.sect.idle.systems;

import org.junit.Assert;
import org.junit.Test;

/**
 * NumberFormatterTest - Unit tests for Xianxia notation formatting, decimal precision,
 * percentages, time formatting, and Roman numeral conversions.
 */
public class NumberFormatterTest {

    @Test
    public void testLargeNumberFormatting() {
        Assert.assertEquals("0", NumberFormatter.format(0L));
        Assert.assertEquals("500", NumberFormatter.format(500L));
        Assert.assertEquals("999", NumberFormatter.format(999L));
        Assert.assertEquals("1,000", NumberFormatter.format(1000L));
        Assert.assertEquals("999,999", NumberFormatter.format(999999L));

        // Millions (M), Billions (B), Trillions (T)
        String formattedM = NumberFormatter.format(1500000L);
        Assert.assertTrue(formattedM.contains("M"));

        String formattedB = NumberFormatter.format(2500000000L);
        Assert.assertTrue(formattedB.contains("B"));

        String formattedT = NumberFormatter.format(3000000000000L);
        Assert.assertTrue(formattedT.contains("T"));
    }

    @Test
    public void testDoubleFormatting() {
        String formatted = NumberFormatter.format(123.456);
        Assert.assertNotNull(formatted);
        Assert.assertTrue(formatted.contains("123"));

        String bigDouble = NumberFormatter.format(5000000.0);
        Assert.assertTrue(bigDouble.contains("M"));
    }

    @Test
    public void testPercentFormatting() {
        Assert.assertEquals("50%", NumberFormatter.formatPercent(0.5f));
        Assert.assertEquals("100%", NumberFormatter.formatPercent(1.0f));
        Assert.assertEquals("0%", NumberFormatter.formatPercent(0f));
        Assert.assertEquals("75%", NumberFormatter.formatPercent(0.75f, 0));
    }

    @Test
    public void testTimeFormatting() {
        Assert.assertEquals("45s", NumberFormatter.formatTime(45));
        Assert.assertEquals("2m 30s", NumberFormatter.formatTime(150));
        Assert.assertEquals("1h 15m", NumberFormatter.formatTime(4500));
        Assert.assertEquals("1d 2h", NumberFormatter.formatTime(93600));
    }

    @Test
    public void testRomanNumeralFormatting() {
        Assert.assertEquals("", NumberFormatter.formatRoman(0));
        Assert.assertEquals("I", NumberFormatter.formatRoman(1));
        Assert.assertEquals("IV", NumberFormatter.formatRoman(4));
        Assert.assertEquals("V", NumberFormatter.formatRoman(5));
        Assert.assertEquals("IX", NumberFormatter.formatRoman(9));
        Assert.assertEquals("X", NumberFormatter.formatRoman(10));
        Assert.assertEquals("XIV", NumberFormatter.formatRoman(14));
        Assert.assertEquals("L", NumberFormatter.formatRoman(50));
        Assert.assertEquals("C", NumberFormatter.formatRoman(100));
        Assert.assertEquals("M", NumberFormatter.formatRoman(1000));
    }
}
