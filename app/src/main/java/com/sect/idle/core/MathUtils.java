package com.sect.idle.core;

/**
 * MathUtils v3.0 - Fast mathematical utilities with lookup tables and helper methods.
 */
public final class MathUtils {
    private MathUtils() {}

    private static final int SIN_TABLE_SIZE = 1024;
    private static final float SIN_TABLE_SCALE = SIN_TABLE_SIZE / (2f * 3.14159265f);
    private static final float[] SIN_TABLE = new float[SIN_TABLE_SIZE + 1];
    private static final float[] COS_TABLE = new float[SIN_TABLE_SIZE + 1];
    private static final float[] SQRT_TABLE = new float[4097];

    private static final int NOISE_PERM_SIZE = 256;
    private static final int[] NOISE_PERM = new int[NOISE_PERM_SIZE * 2];
    private static final float[] NOISE_GRAD_2D = new float[NOISE_PERM_SIZE * 2];

    private static final int EASE_TABLE_SIZE = 256;
    private static final float[] EASE_IN_OUT_CUBIC = new float[EASE_TABLE_SIZE + 1];
    private static final float[] EASE_OUT_BACK = new float[EASE_TABLE_SIZE + 1];
    private static final float[] EASE_OUT_ELASTIC = new float[EASE_TABLE_SIZE + 1];
    private static final float[] EASE_OUT_BOUNCE = new float[EASE_TABLE_SIZE + 1];

    static {
        for (int i = 0; i <= SIN_TABLE_SIZE; i++) {
            float rad = (i / (float)SIN_TABLE_SIZE) * 2f * 3.14159265f;
            SIN_TABLE[i] = (float)Math.sin(rad);
            COS_TABLE[i] = (float)Math.cos(rad);
        }

        for (int i = 0; i <= 4096; i++) {
            float v = i / 64f;
            SQRT_TABLE[i] = (float)Math.sqrt(v);
        }

        java.util.Random rand = new java.util.Random(12345);
        for (int i = 0; i < NOISE_PERM_SIZE; i++) {
            NOISE_PERM[i] = i;
            float angle = rand.nextFloat() * 6.2831853f;
            NOISE_GRAD_2D[i * 2] = (float)Math.cos(angle);
            NOISE_GRAD_2D[i * 2 + 1] = (float)Math.sin(angle);
        }
        for (int i = NOISE_PERM_SIZE - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = NOISE_PERM[i]; NOISE_PERM[i] = NOISE_PERM[j]; NOISE_PERM[j] = tmp;
        }
        for (int i = 0; i < NOISE_PERM_SIZE; i++) NOISE_PERM[i + NOISE_PERM_SIZE] = NOISE_PERM[i];

        for (int i = 0; i <= EASE_TABLE_SIZE; i++) {
            float t = i / (float)EASE_TABLE_SIZE;
            EASE_IN_OUT_CUBIC[i] = t < 0.5f ? 4f * t * t : 1f - (float)Math.pow(-2f * t + 2f, 3f) / 2f;
            EASE_OUT_BACK[i] = 1f + 2.70158f * (float)Math.pow(t - 1f, 3f) + 1.70158f * (float)Math.pow(t - 1f, 2f);
            EASE_OUT_ELASTIC[i] = t == 0 ? 0 : t == 1 ? 1 : (float)Math.pow(2f, -10f * t) * (float)Math.sin((t * 10f - 0.75f) * 2.0943951f) + 1f;
            float bounce;
            if (t < 1f / 2.75f) bounce = 7.5625f * t * t;
            else if (t < 2f / 2.75f) { t -= 1.5f / 2.75f; bounce = 7.5625f * t * t + 0.75f; }
            else if (t < 2.5f / 2.75f) { t -= 2.25f / 2.75f; bounce = 7.5625f * t * t + 0.9375f; }
            else { t -= 2.625f / 2.75f; bounce = 7.5625f * t * t + 0.984375f; }
            EASE_OUT_BOUNCE[i] = bounce;
        }
    }

    public static float sin(float rad) {
        int idx = (int)((rad * SIN_TABLE_SCALE) % SIN_TABLE_SIZE);
        if (idx < 0) idx += SIN_TABLE_SIZE;
        return SIN_TABLE[idx];
    }

    public static float cos(float rad) {
        int idx = (int)((rad * SIN_TABLE_SCALE) % SIN_TABLE_SIZE);
        if (idx < 0) idx += SIN_TABLE_SIZE;
        return COS_TABLE[idx];
    }

    public static float fastSqrt(float v) {
        if (v <= 0) return 0;
        if (v >= 64f) return (float)Math.sqrt(v);
        int idx = (int)(v * 64f);
        if (idx > 4096) idx = 4096;
        return SQRT_TABLE[idx];
    }

    public static float fastInvSqrt(float x) {
        if (x <= 0) return 0;
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat(i);
        x = x * (1.5f - xhalf * x * x);
        return x;
    }

    public static float lerp(float a, float b, float t) { return a + (b - a) * clamp01(t); }
    public static float clamp(float v, float min, float max) { return v < min ? min : (v > max ? max : v); }
    public static float clamp01(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }
    public static int clamp(int v, int min, int max) { return v < min ? min : (v > max ? max : v); }
    public static float smoothstep(float e0, float e1, float x) {
        if (e1 == e0) return 0;
        float t = clamp01((x - e0) / (e1 - e0));
        return t * t * (3f - 2f * t);
    }
    public static float smootherstep(float e0, float e1, float x) {
        if (e1 == e0) return 0;
        float t = clamp01((x - e0) / (e1 - e0));
        return t * t * t * (t * (t * 6f - 15f) + 10f);
    }
    public static float easeInOutCubic(float t) { return EASE_IN_OUT_CUBIC[(int)(clamp01(t) * EASE_TABLE_SIZE)]; }
    public static float easeOutBack(float t) { return EASE_OUT_BACK[(int)(clamp01(t) * EASE_TABLE_SIZE)]; }
    public static float easeOutElastic(float t) { return EASE_OUT_ELASTIC[(int)(clamp01(t) * EASE_TABLE_SIZE)]; }
    public static float easeOutBounce(float t) { return EASE_OUT_BOUNCE[(int)(clamp01(t) * EASE_TABLE_SIZE)]; }
    public static float map(float v, float fMin, float fMax, float tMin, float tMax) {
        if (fMax == fMin) return tMin;
        return tMin + (v - fMin) * (tMax - tMin) / (fMax - fMin);
    }
    public static float dist(float x1, float y1, float x2, float y2) { float dx = x2 - x1, dy = y2 - y1; return fastSqrt(dx * dx + dy * dy); }
    public static float distSq(float x1, float y1, float x2, float y2) { float dx = x2 - x1, dy = y2 - y1; return dx * dx + dy * dy; }
    public static float angle(float x1, float y1, float x2, float y2) { return (float)Math.atan2(y2 - y1, x2 - x1); }
    public static float approach(float c, float t, float d) { return c < t ? Math.min(c + d, t) : Math.max(c - d, t); }

    public static int mixColor(int c1, int c2, float ratio) {
        ratio = clamp01(ratio); float inv = 1f - ratio;
        int a = (int)(((c1 >>> 24) & 0xFF) * inv + ((c2 >>> 24) & 0xFF) * ratio);
        int r = (int)(((c1 >>> 16) & 0xFF) * inv + ((c2 >>> 16) & 0xFF) * ratio);
        int g = (int)(((c1 >>> 8) & 0xFF) * inv + ((c2 >>> 8) & 0xFF) * ratio);
        int b = (int)((c1 & 0xFF) * inv + (c2 & 0xFF) * ratio);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int darken(int color, float factor) {
        factor = clamp01(factor);
        int a = (color >>> 24) & 0xFF;
        int r = (int)(((color >>> 16) & 0xFF) * factor);
        int g = (int)(((color >>> 8) & 0xFF) * factor);
        int b = (int)((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int lighten(int color, float factor) {
        factor = clamp01(factor);
        int a = (color >>> 24) & 0xFF;
        int r = (int)(((color >>> 16) & 0xFF) * (1f - factor) + 255f * factor);
        int g = (int)(((color >>> 8) & 0xFF) * (1f - factor) + 255f * factor);
        int b = (int)((color & 0xFF) * (1f - factor) + 255f * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int setAlpha(int color, float alpha) {
        return ((int)(clamp01(alpha) * 255) << 24) | (color & 0x00FFFFFF);
    }

    public static int getElementColor(int element) {
        return GameConfig.getElementColor(element);
    }

    public static float noise2D(float x, float y) {
        int X = ((int)x) & 255, Y = ((int)y) & 255;
        float xf = x - (int)x, yf = y - (int)y;
        float u = xf * xf * (3f - 2f * xf), v = yf * yf * (3f - 2f * yf);
        int aa = NOISE_PERM[X] + Y, ab = NOISE_PERM[X] + Y + 1;
        int ba = NOISE_PERM[X + 1] + Y, bb = NOISE_PERM[X + 1] + Y + 1;
        float x1 = lerp(grad2D(NOISE_PERM[aa], xf, yf), grad2D(NOISE_PERM[ba], xf - 1f, yf), u);
        float x2 = lerp(grad2D(NOISE_PERM[ab], xf, yf - 1f), grad2D(NOISE_PERM[bb], xf - 1f, yf - 1f), u);
        return lerp(x1, x2, v);
    }

    private static float grad2D(int hash, float x, float y) {
        int idx = hash & 255;
        return NOISE_GRAD_2D[idx * 2] * x + NOISE_GRAD_2D[idx * 2 + 1] * y;
    }

    public static float fbm2D(float x, float y, int octaves, float persistence) {
        if (octaves <= 0) return 0;
        float total = 0f, amplitude = 1f, frequency = 1f, maxValue = 0f;
        for (int i = 0; i < octaves; i++) {
            total += noise2D(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2f;
        }
        return maxValue > 0 ? total / maxValue : 0;
    }

    public static int nextPowerOfTwo(int x) {
        if (x <= 0) return 1;
        x--; x |= x >> 1; x |= x >> 2; x |= x >> 4; x |= x >> 8; x |= x >> 16;
        return x + 1;
    }

    public static boolean isPowerOfTwo(int x) { return x > 0 && (x & (x - 1)) == 0; }
    public static float wrap(float v, float min, float max) {
        float range = max - min;
        if (range == 0) return min;
        v = (v - min) % range;
        if (v < 0) v += range;
        return v + min;
    }
}
