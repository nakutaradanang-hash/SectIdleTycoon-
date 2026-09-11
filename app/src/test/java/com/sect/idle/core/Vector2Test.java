package com.sect.idle.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * Vector2Test - Comprehensive unit tests for 2D vectors, NaN-safety, distance,
 * normalization, interpolation, division-by-zero protection, and serialization.
 */
public class Vector2Test {

    private static final float DELTA = 0.001f;

    @Test
    public void testConstructorsAndNaNSafety() {
        Vector2 v1 = new Vector2(3f, 4f);
        Assert.assertEquals(3f, v1.x, DELTA);
        Assert.assertEquals(4f, v1.y, DELTA);

        // NaN safety
        Vector2 vNaN = new Vector2(Float.NaN, Float.NaN);
        Assert.assertEquals(0f, vNaN.x, DELTA);
        Assert.assertEquals(0f, vNaN.y, DELTA);

        // Default constructor
        Vector2 vDefault = new Vector2();
        Assert.assertEquals(0f, vDefault.x, DELTA);
        Assert.assertEquals(0f, vDefault.y, DELTA);
        Assert.assertTrue(vDefault.isZero());
    }

    @Test
    public void testBasicArithmetic() {
        Vector2 v = new Vector2(10f, 20f);

        // Add
        v.add(5f, -10f);
        Assert.assertEquals(15f, v.x, DELTA);
        Assert.assertEquals(10f, v.y, DELTA);

        v.add(new Vector2(5f, 10f));
        Assert.assertEquals(20f, v.x, DELTA);
        Assert.assertEquals(20f, v.y, DELTA);

        // Sub
        v.sub(10f, 5f);
        Assert.assertEquals(10f, v.x, DELTA);
        Assert.assertEquals(15f, v.y, DELTA);

        v.sub(new Vector2(5f, 5f));
        Assert.assertEquals(5f, v.x, DELTA);
        Assert.assertEquals(10f, v.y, DELTA);

        // Mul
        v.mul(2f);
        Assert.assertEquals(10f, v.x, DELTA);
        Assert.assertEquals(20f, v.y, DELTA);

        // Div
        v.div(2f);
        Assert.assertEquals(5f, v.x, DELTA);
        Assert.assertEquals(10f, v.y, DELTA);

        // Div by zero / NaN safety
        v.div(0f);
        Assert.assertEquals(5f, v.x, DELTA); // Should remain unchanged
        Assert.assertEquals(10f, v.y, DELTA);

        v.mul(Float.NaN);
        Assert.assertEquals(5f, v.x, DELTA); // Should remain unchanged
    }

    @Test
    public void testDistanceAndLength() {
        Vector2 v1 = new Vector2(0f, 0f);
        Vector2 v2 = new Vector2(3f, 4f);

        Assert.assertEquals(5f, v1.dist(v2), DELTA);
        Assert.assertEquals(5f, v1.dist(3f, 4f), DELTA);
        Assert.assertEquals(25f, v1.distSq(v2), DELTA);
        Assert.assertEquals(25f, v1.distSq(3f, 4f), DELTA);

        Assert.assertEquals(5f, v2.length(), DELTA);
        Assert.assertEquals(25f, v2.lengthSq(), DELTA);

        // Null safety
        Assert.assertEquals(0f, v1.dist(null), DELTA);
        Assert.assertEquals(0f, v1.distSq(null), DELTA);
    }

    @Test
    public void testNormalizationAndZero() {
        Vector2 v = new Vector2(10f, 0f);
        v.normalize();
        Assert.assertEquals(1f, v.x, DELTA);
        Assert.assertEquals(0f, v.y, DELTA);
        Assert.assertEquals(1f, v.length(), DELTA);

        // Normalize zero vector shouldn't produce NaN
        Vector2 vZero = new Vector2(0f, 0f);
        vZero.normalize();
        Assert.assertEquals(0f, vZero.x, DELTA);
        Assert.assertEquals(0f, vZero.y, DELTA);
        Assert.assertFalse(Float.isNaN(vZero.x));
    }

    @Test
    public void testLerpAndClamp() {
        Vector2 v = new Vector2(0f, 0f);
        v.lerp(100f, 200f, 0.5f);
        Assert.assertEquals(50f, v.x, DELTA);
        Assert.assertEquals(100f, v.y, DELTA);

        // Rect clamp
        Rect r = new Rect(10f, 10f, 30f, 30f); // bounds: [10, 10] to [40, 40]
        Vector2 vOutside = new Vector2(100f, -50f);
        vOutside.clamp(r);
        Assert.assertEquals(40f, vOutside.x, DELTA);
        Assert.assertEquals(10f, vOutside.y, DELTA);
    }

    @Test
    public void testStringParsingAndToString() {
        Vector2 v = new Vector2(12.5f, 34.8f);
        String str = v.toString();
        Assert.assertEquals("12.5,34.8", str);

        Vector2 parsed = Vector2.parse("12.5, 34.8");
        Assert.assertEquals(12.5f, parsed.x, DELTA);
        Assert.assertEquals(34.8f, parsed.y, DELTA);

        // Invalid parsing fallbacks
        Vector2 malformed = Vector2.parse("invalid");
        Assert.assertEquals(0f, malformed.x, DELTA);
        Assert.assertEquals(0f, malformed.y, DELTA);

        Vector2 nullParse = Vector2.parse(null);
        Assert.assertEquals(0f, nullParse.x, DELTA);
        Assert.assertEquals(0f, nullParse.y, DELTA);
    }
}
