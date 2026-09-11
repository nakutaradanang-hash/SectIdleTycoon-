package com.sect.idle.utils;

import com.sect.idle.core.Vector2;
import com.sect.idle.core.Rect;

/**
 * MemoryPool v2.0 - Object pooling for zero-allocation performance on low-end devices.
 */
public final class MemoryPool {
    private MemoryPool() {}
    
    // Vector2 Pool
    private static final int VECTOR_POOL_SIZE = 64;
    private static final Vector2[] VECTOR_POOL = new Vector2[VECTOR_POOL_SIZE];
    private static int vectorIndex = 0;
    static {
        for (int i = 0; i < VECTOR_POOL_SIZE; i++) VECTOR_POOL[i] = new Vector2();
    }
    
    public static Vector2 obtainVector() {
        Vector2 v = VECTOR_POOL[vectorIndex];
        v.zero();
        vectorIndex = (vectorIndex + 1) % VECTOR_POOL_SIZE;
        return v;
    }
    
    public static Vector2 obtainVector(float x, float y) {
        Vector2 v = obtainVector();
        v.set(x, y);
        return v;
    }
    
    // Rect Pool
    private static final int RECT_POOL_SIZE = 32;
    private static final Rect[] RECT_POOL = new Rect[RECT_POOL_SIZE];
    private static int rectIndex = 0;
    static {
        for (int i = 0; i < RECT_POOL_SIZE; i++) RECT_POOL[i] = new Rect();
    }
    
    public static Rect obtainRect() {
        Rect r = RECT_POOL[rectIndex];
        r.set(0, 0, 0, 0);
        rectIndex = (rectIndex + 1) % RECT_POOL_SIZE;
        return r;
    }
    
    public static Rect obtainRect(float x, float y, float w, float h) {
        Rect r = obtainRect();
        r.set(x, y, w, h);
        return r;
    }
    
    // Float Array Pool
    private static final int ARRAY_POOL_SIZE = 16;
    private static final float[][] FLOAT_ARRAY_POOL = new float[ARRAY_POOL_SIZE][];
    private static final int[] FLOAT_ARRAY_SIZES = new int[ARRAY_POOL_SIZE];
    private static int arrayIndex = 0;
    
    public static float[] obtainFloatArray(int size) {
        int idx = arrayIndex;
        for (int i = 0; i < ARRAY_POOL_SIZE; i++) {
            int checkIdx = (idx + i) % ARRAY_POOL_SIZE;
            if (FLOAT_ARRAY_POOL[checkIdx] != null && FLOAT_ARRAY_SIZES[checkIdx] >= size) {
                arrayIndex = (checkIdx + 1) % ARRAY_POOL_SIZE;
                java.util.Arrays.fill(FLOAT_ARRAY_POOL[checkIdx], 0f);
                return FLOAT_ARRAY_POOL[checkIdx];
            }
        }
        int newIdx = arrayIndex;
        FLOAT_ARRAY_POOL[newIdx] = new float[size];
        FLOAT_ARRAY_SIZES[newIdx] = size;
        arrayIndex = (newIdx + 1) % ARRAY_POOL_SIZE;
        return FLOAT_ARRAY_POOL[newIdx];
    }
    
    // StringBuilder Pool
    private static final int SB_POOL_SIZE = 8;
    private static final StringBuilder[] SB_POOL = new StringBuilder[SB_POOL_SIZE];
    private static int sbIndex = 0;
    static {
        for (int i = 0; i < SB_POOL_SIZE; i++) SB_POOL[i] = new StringBuilder(64);
    }
    
    public static StringBuilder obtainStringBuilder() {
        StringBuilder sb = SB_POOL[sbIndex];
        sb.setLength(0);
        sbIndex = (sbIndex + 1) % SB_POOL_SIZE;
        return sb;
    }
    
    public static void reset() {
        vectorIndex = 0;
        rectIndex = 0;
        arrayIndex = 0;
        sbIndex = 0;
    }
}
