package com.sect.idle.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * RectTest - Unit tests for 2D bounding boxes, contains, intersects, expansion, clamping, and edge cases.
 */
public class RectTest {

    private static final float DELTA = 0.001f;

    @Test
    public void testRectCoordinatesAndDimensions() {
        Rect r = new Rect(10f, 20f, 30f, 40f);
        Assert.assertEquals(10f, r.x, DELTA);
        Assert.assertEquals(20f, r.y, DELTA);
        Assert.assertEquals(30f, r.w, DELTA);
        Assert.assertEquals(40f, r.h, DELTA);

        Assert.assertEquals(10f, r.left(), DELTA);
        Assert.assertEquals(40f, r.right(), DELTA);
        Assert.assertEquals(20f, r.top(), DELTA);
        Assert.assertEquals(60f, r.bottom(), DELTA);

        Assert.assertEquals(25f, r.cx(), DELTA);
        Assert.assertEquals(40f, r.cy(), DELTA);
        Assert.assertEquals(1200f, r.area(), DELTA);
        Assert.assertTrue(r.isValid());
    }

    @Test
    public void testContains() {
        Rect r = new Rect(10f, 10f, 50f, 50f);

        Assert.assertTrue(r.contains(20f, 20f));
        Assert.assertTrue(r.contains(10f, 10f)); // On boundary
        Assert.assertTrue(r.contains(60f, 60f)); // On boundary
        Assert.assertFalse(r.contains(5f, 20f));
        Assert.assertFalse(r.contains(20f, 65f));

        Assert.assertTrue(r.contains(new Vector2(25f, 25f)));
        Assert.assertFalse(r.contains((Vector2) null));
    }

    @Test
    public void testIntersects() {
        Rect r1 = new Rect(0f, 0f, 20f, 20f);
        Rect r2 = new Rect(10f, 10f, 20f, 20f); // Overlapping
        Rect r3 = new Rect(30f, 30f, 10f, 10f); // Non-overlapping

        Assert.assertTrue(r1.intersects(r2));
        Assert.assertTrue(r2.intersects(r1));
        Assert.assertFalse(r1.intersects(r3));
        Assert.assertFalse(r1.intersects(null));
    }

    @Test
    public void testExpandAndClamp() {
        Rect r = new Rect(10f, 10f, 20f, 20f);
        r.expand(5f);

        Assert.assertEquals(5f, r.x, DELTA);
        Assert.assertEquals(5f, r.y, DELTA);
        Assert.assertEquals(30f, r.w, DELTA);
        Assert.assertEquals(30f, r.h, DELTA);

        // Clamp Vector2
        Vector2 v = new Vector2(-10f, 100f);
        r.clamp(v);
        Assert.assertEquals(5f, v.x, DELTA);
        Assert.assertEquals(35f, v.y, DELTA);
    }
}
