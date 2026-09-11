package com.sect.idle.utils;

import com.sect.idle.core.Rect;
import com.sect.idle.core.Vector2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * MemoryPoolTest - Unit tests for zero-allocation pooling of Vector2, Rect,
 * float arrays, and StringBuilder objects.
 */
public class MemoryPoolTest {

    @Before
    public void setUp() {
        MemoryPool.reset();
    }

    @Test
    public void testVector2Pool() {
        Vector2 v1 = MemoryPool.obtainVector();
        Assert.assertNotNull(v1);
        Assert.assertEquals(0f, v1.x, 0.0001f);
        Assert.assertEquals(0f, v1.y, 0.0001f);

        Vector2 v2 = MemoryPool.obtainVector(15.5f, -24.3f);
        Assert.assertEquals(15.5f, v2.x, 0.0001f);
        Assert.assertEquals(-24.3f, v2.y, 0.0001f);
    }

    @Test
    public void testRectPool() {
        Rect r1 = MemoryPool.obtainRect();
        Assert.assertNotNull(r1);
        Assert.assertEquals(0f, r1.x, 0.0001f);
        Assert.assertEquals(0f, r1.w, 0.0001f);

        Rect r2 = MemoryPool.obtainRect(10f, 20f, 100f, 200f);
        Assert.assertEquals(10f, r2.x, 0.0001f);
        Assert.assertEquals(20f, r2.y, 0.0001f);
        Assert.assertEquals(100f, r2.w, 0.0001f);
        Assert.assertEquals(200f, r2.h, 0.0001f);
    }

    @Test
    public void testFloatArrayPool() {
        float[] arr1 = MemoryPool.obtainFloatArray(16);
        Assert.assertNotNull(arr1);
        Assert.assertTrue(arr1.length >= 16);

        arr1[0] = 99.5f;
        arr1[1] = -42f;

        float[] arr2 = MemoryPool.obtainFloatArray(16);
        Assert.assertNotNull(arr2);
        // Clean zero-initialized array returned
        Assert.assertEquals(0f, arr2[0], 0.0001f);
    }

    @Test
    public void testStringBuilderPool() {
        StringBuilder sb1 = MemoryPool.obtainStringBuilder();
        Assert.assertNotNull(sb1);
        Assert.assertEquals(0, sb1.length());

        sb1.append("TestCultivationText");
        Assert.assertTrue(sb1.length() > 0);

        StringBuilder sb2 = MemoryPool.obtainStringBuilder();
        Assert.assertNotNull(sb2);
        Assert.assertEquals(0, sb2.length());
    }
}
