package com.sect.idle.systems;

import org.junit.Assert;
import org.junit.Test;

/**
 * RNGTest - Unit tests for random number generation, seed determinism,
 * weighted choices, dice rolls, array picking, and distributions.
 */
public class RNGTest {

    @Test
    public void testSeedDeterminism() {
        RNG.setSeed(42L);
        int val1 = RNG.nextInt(100);
        int val2 = RNG.nextInt(100);

        RNG.setSeed(42L);
        int check1 = RNG.nextInt(100);
        int check2 = RNG.nextInt(100);

        Assert.assertEquals(val1, check1);
        Assert.assertEquals(val2, check2);
    }

    @Test
    public void testRangeAndBounds() {
        for (int i = 0; i < 100; i++) {
            int val = RNG.nextInt(10, 20);
            Assert.assertTrue(val >= 10 && val <= 20);

            float f = RNG.nextFloat(5.0f, 15.0f);
            Assert.assertTrue(f >= 5.0f && f <= 15.0f);
        }

        // Min >= Max safety
        Assert.assertEquals(10, RNG.nextInt(10, 5));
    }

    @Test
    public void testWeightedChoices() {
        int[] intWeights = {0, 0, 100};
        Assert.assertEquals(2, RNG.weightedChoice(intWeights));

        float[] floatWeights = {0f, 100f, 0f};
        Assert.assertEquals(1, RNG.weightedChoice(floatWeights));

        // Empty/Null safety
        Assert.assertEquals(0, RNG.weightedChoice((int[]) null));
        Assert.assertEquals(0, RNG.weightedChoice((float[]) null));
    }

    @Test
    public void testDiceAndShuffling() {
        int diceResult = RNG.rollDice(3, 6); // 3d6 (range 3 to 18)
        Assert.assertTrue(diceResult >= 3 && diceResult <= 18);

        Integer[] arr = {1, 2, 3, 4, 5};
        Integer picked = RNG.pick(arr);
        Assert.assertNotNull(picked);
        Assert.assertTrue(picked >= 1 && picked <= 5);

        RNG.shuffle(arr);
        Assert.assertEquals(5, arr.length);
    }
}
