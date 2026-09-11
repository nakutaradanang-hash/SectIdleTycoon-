package com.sect.idle.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * MathUtilsTest - Comprehensive unit tests for fast mathematical utilities,
 * lookup tables, color arithmetic, Perlin/FBM noise, easing functions, and clamp/wrap operations.
 */
public class MathUtilsTest {

    private static final float DELTA = 0.05f;

    @Test
    public void testSinAndCosLookupTables() {
        // Test fundamental angles: 0, PI/2, PI, 3PI/2, 2PI
        Assert.assertEquals(0f, MathUtils.sin(0f), DELTA);
        Assert.assertEquals(1f, MathUtils.sin((float) Math.PI / 2f), DELTA);
        Assert.assertEquals(0f, MathUtils.sin((float) Math.PI), DELTA);
        Assert.assertEquals(-1f, MathUtils.sin(3f * (float) Math.PI / 2f), DELTA);

        Assert.assertEquals(1f, MathUtils.cos(0f), DELTA);
        Assert.assertEquals(0f, MathUtils.cos((float) Math.PI / 2f), DELTA);
        Assert.assertEquals(-1f, MathUtils.cos((float) Math.PI), DELTA);
        Assert.assertEquals(0f, MathUtils.cos(3f * (float) Math.PI / 2f), DELTA);

        // Test periodic wrapping and negative radians
        Assert.assertEquals(MathUtils.sin(0f), MathUtils.sin((float) (2 * Math.PI)), DELTA);
        Assert.assertEquals(MathUtils.cos(0f), MathUtils.cos((float) (2 * Math.PI)), DELTA);
        Assert.assertEquals(MathUtils.sin(- (float) Math.PI / 2f), -1f, DELTA);
    }

    @Test
    public void testFastSqrtAndFastInvSqrt() {
        // 0 and boundary negative inputs
        Assert.assertEquals(0f, MathUtils.fastSqrt(0f), DELTA);
        Assert.assertEquals(0f, MathUtils.fastSqrt(-10f), DELTA);
        Assert.assertEquals(0f, MathUtils.fastInvSqrt(0f), DELTA);
        Assert.assertEquals(0f, MathUtils.fastInvSqrt(-5f), DELTA);

        // Within LUT table (0 to 64)
        Assert.assertEquals(2f, MathUtils.fastSqrt(4f), DELTA);
        Assert.assertEquals(3f, MathUtils.fastSqrt(9f), DELTA);
        Assert.assertEquals(4f, MathUtils.fastSqrt(16f), DELTA);
        Assert.assertEquals(5f, MathUtils.fastSqrt(25f), DELTA);
        Assert.assertEquals(7f, MathUtils.fastSqrt(49f), DELTA);

        // Values above 64
        Assert.assertEquals(10f, MathUtils.fastSqrt(100f), DELTA);
        Assert.assertEquals(16f, MathUtils.fastSqrt(256f), DELTA);

        // Fast inverse square root (1 / sqrt(x))
        Assert.assertEquals(0.5f, MathUtils.fastInvSqrt(4f), DELTA);
        Assert.assertEquals(0.2f, MathUtils.fastInvSqrt(25f), DELTA);
        Assert.assertEquals(0.1f, MathUtils.fastInvSqrt(100f), DELTA);
    }

    @Test
    public void testClampingAndInterpolation() {
        // Float clamp
        Assert.assertEquals(5f, MathUtils.clamp(10f, 0f, 5f), 0.001f);
        Assert.assertEquals(0f, MathUtils.clamp(-5f, 0f, 5f), 0.001f);
        Assert.assertEquals(3f, MathUtils.clamp(3f, 0f, 5f), 0.001f);

        // Float clamp01
        Assert.assertEquals(1f, MathUtils.clamp01(1.5f), 0.001f);
        Assert.assertEquals(0f, MathUtils.clamp01(-0.5f), 0.001f);
        Assert.assertEquals(0.7f, MathUtils.clamp01(0.7f), 0.001f);

        // Int clamp
        Assert.assertEquals(10, MathUtils.clamp(15, 0, 10));
        Assert.assertEquals(0, MathUtils.clamp(-2, 0, 10));
        Assert.assertEquals(7, MathUtils.clamp(7, 0, 10));

        // Lerp
        Assert.assertEquals(0f, MathUtils.lerp(0f, 100f, 0f), 0.001f);
        Assert.assertEquals(50f, MathUtils.lerp(0f, 100f, 0.5f), 0.001f);
        Assert.assertEquals(100f, MathUtils.lerp(0f, 100f, 1.0f), 0.001f);
        Assert.assertEquals(100f, MathUtils.lerp(0f, 100f, 1.5f), 0.001f); // Clamped to 1.0

        // Smoothstep and Smootherstep
        Assert.assertEquals(0f, MathUtils.smoothstep(0f, 10f, 0f), 0.001f);
        Assert.assertEquals(1f, MathUtils.smoothstep(0f, 10f, 10f), 0.001f);
        Assert.assertEquals(0.5f, MathUtils.smoothstep(0f, 10f, 5f), 0.001f);
        Assert.assertEquals(0f, MathUtils.smoothstep(5f, 5f, 5f), 0.001f); // Equal boundaries

        Assert.assertEquals(0f, MathUtils.smootherstep(0f, 10f, 0f), 0.001f);
        Assert.assertEquals(1f, MathUtils.smootherstep(0f, 10f, 10f), 0.001f);
        Assert.assertEquals(0.5f, MathUtils.smootherstep(0f, 10f, 5f), 0.001f);
    }

    @Test
    public void testEasingFunctions() {
        // Ease In Out Cubic
        Assert.assertEquals(0f, MathUtils.easeInOutCubic(0f), 0.01f);
        Assert.assertEquals(0.5f, MathUtils.easeInOutCubic(0.5f), 0.01f);
        Assert.assertEquals(1f, MathUtils.easeInOutCubic(1f), 0.01f);

        // Ease Out Back
        Assert.assertEquals(0f, MathUtils.easeOutBack(0f), 0.1f);
        Assert.assertEquals(1f, MathUtils.easeOutBack(1f), 0.05f);

        // Ease Out Bounce
        Assert.assertEquals(0f, MathUtils.easeOutBounce(0f), 0.05f);
        Assert.assertEquals(1f, MathUtils.easeOutBounce(1f), 0.05f);

        // Ease Out Elastic
        Assert.assertEquals(0f, MathUtils.easeOutElastic(0f), 0.05f);
        Assert.assertEquals(1f, MathUtils.easeOutElastic(1f), 0.05f);
    }

    @Test
    public void testMapDistanceAngleApproach() {
        // Map value from [0, 100] to [0, 1]
        Assert.assertEquals(0.5f, MathUtils.map(50f, 0f, 100f, 0f, 1f), 0.001f);
        Assert.assertEquals(0f, MathUtils.map(50f, 50f, 50f, 0f, 1f), 0.001f); // equal min/max

        // Dist & distSq
        Assert.assertEquals(25f, MathUtils.distSq(0f, 0f, 3f, 4f), 0.001f);
        Assert.assertEquals(5f, MathUtils.dist(0f, 0f, 3f, 4f), DELTA);

        // Angle
        Assert.assertEquals(0f, MathUtils.angle(0f, 0f, 10f, 0f), 0.001f);
        Assert.assertEquals((float) Math.PI / 2f, MathUtils.angle(0f, 0f, 0f, 10f), 0.001f);

        // Approach
        Assert.assertEquals(5f, MathUtils.approach(0f, 10f, 5f), 0.001f);
        Assert.assertEquals(10f, MathUtils.approach(8f, 10f, 5f), 0.001f);
        Assert.assertEquals(5f, MathUtils.approach(10f, 0f, 5f), 0.001f);
        Assert.assertEquals(0f, MathUtils.approach(2f, 0f, 5f), 0.001f);
    }

    @Test
    public void testColorManipulation() {
        int white = 0xFFFFFFFF;
        int black = 0xFF000000;

        // Mix 50%
        int gray = MathUtils.mixColor(white, black, 0.5f);
        int r = (gray >> 16) & 0xFF;
        int g = (gray >> 8) & 0xFF;
        int b = gray & 0xFF;
        Assert.assertTrue(r >= 120 && r <= 135);
        Assert.assertTrue(g >= 120 && g <= 135);
        Assert.assertTrue(b >= 120 && b <= 135);

        // Darken
        int darkened = MathUtils.darken(white, 0.5f);
        Assert.assertTrue(((darkened >> 16) & 0xFF) <= 135);

        // Lighten
        int lightened = MathUtils.lighten(black, 0.5f);
        Assert.assertTrue(((lightened >> 16) & 0xFF) >= 120);

        // Set Alpha
        int semiTransparent = MathUtils.setAlpha(white, 0.5f);
        Assert.assertEquals(127, (semiTransparent >>> 24) & 0xFF, 2);
    }

    @Test
    public void testNoiseAndFbm() {
        // Noise 2D returns bounded values
        float n1 = MathUtils.noise2D(12.5f, 45.2f);
        float n2 = MathUtils.noise2D(12.5f, 45.2f);
        Assert.assertEquals(n1, n2, 0.0001f); // Deterministic

        // FBM 2D
        float fbm0 = MathUtils.fbm2D(10f, 20f, 0, 0.5f);
        Assert.assertEquals(0f, fbm0, 0.001f);

        float fbm = MathUtils.fbm2D(10f, 20f, 4, 0.5f);
        Assert.assertTrue(fbm >= -1.0f && fbm <= 1.0f);
    }

    @Test
    public void testPowerOfTwoAndWrap() {
        // Next power of two
        Assert.assertEquals(1, MathUtils.nextPowerOfTwo(0));
        Assert.assertEquals(1, MathUtils.nextPowerOfTwo(1));
        Assert.assertEquals(2, MathUtils.nextPowerOfTwo(2));
        Assert.assertEquals(4, MathUtils.nextPowerOfTwo(3));
        Assert.assertEquals(16, MathUtils.nextPowerOfTwo(9));
        Assert.assertEquals(256, MathUtils.nextPowerOfTwo(200));

        // Is power of two
        Assert.assertFalse(MathUtils.isPowerOfTwo(0));
        Assert.assertTrue(MathUtils.isPowerOfTwo(1));
        Assert.assertTrue(MathUtils.isPowerOfTwo(2));
        Assert.assertFalse(MathUtils.isPowerOfTwo(3));
        Assert.assertTrue(MathUtils.isPowerOfTwo(64));
        Assert.assertFalse(MathUtils.isPowerOfTwo(65));

        // Wrap
        Assert.assertEquals(5f, MathUtils.wrap(5f, 0f, 10f), 0.001f);
        Assert.assertEquals(2f, MathUtils.wrap(12f, 0f, 10f), 0.001f);
        Assert.assertEquals(8f, MathUtils.wrap(-2f, 0f, 10f), 0.001f);
        Assert.assertEquals(5f, MathUtils.wrap(5f, 5f, 5f), 0.001f); // Equal boundaries
    }
}
