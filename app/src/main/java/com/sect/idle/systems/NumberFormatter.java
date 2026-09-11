package com.sect.idle.systems;

import java.text.DecimalFormat;

public final class NumberFormatter {
    private static final String[] SUFFIXES = {"","K","M","B","T","Qa","Qi","Sx","Sp","Oc","No","Dc"};
    private static final DecimalFormat DF = new DecimalFormat("0.0");
    private static final DecimalFormat DF2 = new DecimalFormat("0.00");
    private static final DecimalFormat DF_INT = new DecimalFormat("#,###");

    private NumberFormatter() {}

    public static String format(long num) {
        if (num < 1000L) return String.valueOf(num);
        if (num < 1000000L) return DF_INT.format(num);
        int idx = 0;
        double d = num;
        while (d >= 1000.0 && idx < SUFFIXES.length - 1) { d /= 1000.0; idx++; }
        return DF.format(d) + SUFFIXES[idx];
    }

    public static String format(long num, StringBuilder sb) {
        if (sb == null) return format(num);
        sb.setLength(0);
        if (num < 1000L) return sb.append(num).toString();
        if (num < 1000000L) return sb.append(DF_INT.format(num)).toString();
        int idx = 0;
        double d = num;
        while (d >= 1000.0 && idx < SUFFIXES.length - 1) { d /= 1000.0; idx++; }
        return sb.append(DF.format(d)).append(SUFFIXES[idx]).toString();
    }

    public static String format(double num) {
        if (num < 1000.0) return DF2.format(num);
        int idx = 0;
        double d = num;
        while (d >= 1000.0 && idx < SUFFIXES.length - 1) { d /= 1000.0; idx++; }
        return DF.format(d) + SUFFIXES[idx];
    }

    public static String format(double num, StringBuilder sb) {
        if (sb == null) return format(num);
        sb.setLength(0);
        if (num < 1000.0) return sb.append(DF2.format(num)).toString();
        int idx = 0;
        double d = num;
        while (d >= 1000.0 && idx < SUFFIXES.length - 1) { d /= 1000.0; idx++; }
        return sb.append(DF.format(d)).append(SUFFIXES[idx]).toString();
    }

    public static String formatInt(long num) {
        return DF_INT.format(num);
    }

    public static String formatPercent(float f) {
        return (int)(f * 100f) + "%";
    }

    public static String formatPercent(float f, int decimals) {
        if (decimals <= 0) return (int)(f * 100f) + "%";
        float pow = 1f;
        for (int i = 0; i < decimals; i++) pow *= 10f;
        return ((int)(f * 100f * pow) / pow) + "%";
    }

    public static String formatTime(int seconds) {
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + "m " + (seconds % 60) + "s";
        if (seconds < 86400) return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
        return (seconds / 86400) + "d " + ((seconds % 86400) / 3600) + "h";
    }

    public static String formatRoman(int num) {
        if (num <= 0) return "";
        StringBuilder sb = new StringBuilder(16);
        int[] values = {1000,900,500,400,100,90,50,40,10,9,5,4,1};
        String[] symbols = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};
        for (int i = 0; i < values.length; i++) {
            while (num >= values[i]) { sb.append(symbols[i]); num -= values[i]; }
        }
        return sb.toString();
    }

    public static String formatCompact(long num, StringBuilder sb) {
        return format(num, sb);
    }
}
