package com.sect.idle.systems;

import java.util.Random;
import java.util.List;

public final class RNG {
    private static final Random global = new Random();
    private static long seed = System.currentTimeMillis();

    private RNG() {}

    public static void setSeed(long s) { seed = s; global.setSeed(s); }
    public static long getSeed() { return seed; }

    public static int nextInt(int bound) {
        return bound > 1 ? global.nextInt(bound) : 0;
    }

    public static int nextInt(int min, int max) {
        if (min >= max) return min;
        return min + global.nextInt(max - min + 1);
    }

    public static float nextFloat() { return global.nextFloat(); }

    public static float nextFloat(float min, float max) {
        return min + global.nextFloat() * (max - min);
    }

    public static double nextDouble() { return global.nextDouble(); }

    public static boolean chance(float percent) {
        return global.nextFloat() * 100f < percent;
    }

    public static boolean roll(float probability) {
        return global.nextFloat() < probability;
    }

    public static int rollDice(int count, int sides) {
        int sum = 0;
        for (int i = 0; i < count; i++) sum += 1 + global.nextInt(sides);
        return sum;
    }

    public static int weightedChoice(int[] weights) {
        if (weights == null || weights.length == 0) return 0;
        int total = 0;
        for (int i = 0; i < weights.length; i++) total += weights[i];
        if (total <= 0) return 0;
        int roll = global.nextInt(total);
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0) return i;
        }
        return weights.length - 1;
    }

    public static int weightedChoice(float[] weights) {
        if (weights == null || weights.length == 0) return 0;
        float total = 0f;
        for (int i = 0; i < weights.length; i++) total += weights[i];
        if (total <= 0f) return 0;
        float roll = global.nextFloat() * total;
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0f) return i;
        }
        return weights.length - 1;
    }

    public static <T> T pick(T[] array) {
        return array != null && array.length > 0 ? array[global.nextInt(array.length)] : null;
    }

    public static <T> T pick(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(global.nextInt(list.size())) : null;
    }

    public static int pick(int[] array) {
        return array != null && array.length > 0 ? array[global.nextInt(array.length)] : 0;
    }

    public static void shuffle(int[] arr) {
        if (arr == null) return;
        for (int i = arr.length - 1; i > 0; i--) {
            int j = global.nextInt(i + 1);
            int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
    }

    public static void shuffle(Object[] arr) {
        if (arr == null) return;
        for (int i = arr.length - 1; i > 0; i--) {
            int j = global.nextInt(i + 1);
            Object tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
    }

    public static float nextGaussian() {
        return (float) global.nextGaussian();
    }

    public static float nextGaussian(float mean, float stdDev) {
        return mean + (float) global.nextGaussian() * stdDev;
    }

    public static float nextAngle() {
        return global.nextFloat() * 360f;
    }

    public static float nextRadian() {
        return global.nextFloat() * 6.2831853f;
    }

    public static int nextSign() {
        return global.nextBoolean() ? 1 : -1;
    }

    public static boolean nextBool() {
        return global.nextBoolean();
    }
}
