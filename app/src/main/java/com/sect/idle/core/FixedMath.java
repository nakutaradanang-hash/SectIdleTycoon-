package com.sect.idle.core;

/**
 * FixedMath - Zero-GC Deterministic Fixed-Point Arithmetic (16.16 & 32.32 format).
 * Ensures deterministic simulation for 4X turn calculations, combat trajectory math,
 * and physics without IEEE-754 floating-point drift across different Android CPUs.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class FixedMath {

    public static final int SHIFT = 16;
    public static final int ONE = 1 << SHIFT;
    public static final int HALF = ONE >> 1;
    public static final int ZERO = 0;
    public static final int PI = (int) (3.14159265358979323846 * ONE);
    public static final int TWO_PI = PI << 1;
    public static final int HALF_PI = PI >> 1;

    private FixedMath() {}

    public static int fromFloat(float value) {
        return (int) (value * ONE);
    }

    public static int fromInt(int value) {
        return value << SHIFT;
    }

    public static float toFloat(int fixed) {
        return (float) fixed / ONE;
    }

    public static int toInt(int fixed) {
        return fixed >> SHIFT;
    }

    public static int multiply(int a, int b) {
        return (int) (((long) a * (long) b + HALF) >> SHIFT);
    }

    public static int divide(int a, int b) {
        if (b == 0) return 0;
        return (int) ((((long) a << SHIFT) + (b >> 1)) / b);
    }

    public static int clamp(int val, int min, int max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    public static int abs(int a) {
        return (a < 0) ? -a : a;
    }

    public static int sqrt(int a) {
        if (a <= 0) return 0;
        long n = (long) a << SHIFT;
        long res = 0;
        long add = 0x4000000000000000L;
        while (add > 0) {
            long temp = res + add;
            if (n >= temp) {
                n -= temp;
                res = temp + add;
            }
            res >>= 1;
            add >>= 2;
        }
        return (int) res;
    }

    public static int sin(int radiansFixed) {
        // Fast Taylor series approximation for sin(x)
        int x = radiansFixed % TWO_PI;
        if (x < 0) x += TWO_PI;
        if (x > PI) {
            return -sin(x - PI);
        }
        if (x > HALF_PI) {
            x = PI - x;
        }
        int x2 = multiply(x, x);
        int x3 = multiply(x, x2);
        int x5 = multiply(x3, x2);
        int term1 = x;
        int term2 = divide(x3, fromInt(6));
        int term3 = divide(x5, fromInt(120));
        return term1 - term2 + term3;
    }

    public static int min(int a, int b) {
        return (a <= b) ? a : b;
    }

    public static int max(int a, int b) {
        return (a >= b) ? a : b;
    }

    public static int lerp(int start, int end, int tFixed) {
        return start + multiply(end - start, clamp(tFixed, 0, ONE));
    }

    public static int hypot(int dx, int dy) {
        long dxSq = ((long) dx * dx) >> SHIFT;
        long dySq = ((long) dy * dy) >> SHIFT;
        return sqrt((int) (dxSq + dySq));
    }

    public static int cos(int radiansFixed) {
        return sin(radiansFixed + HALF_PI);
    }
}
