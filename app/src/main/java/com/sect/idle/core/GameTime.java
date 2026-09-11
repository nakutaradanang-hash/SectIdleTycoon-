package com.sect.idle.core;

public final class GameTime {
    public int year = 1;
    public int month = 1;
    public int day = 1;
    public int hour = 6;
    public int totalDays = 0;
    public int season = 0;
    public float dayProgress = 0f;

    private static final int DAYS_PER_MONTH = 30;
    private static final int MONTHS_PER_YEAR = 12;
    private static final int HOURS_PER_DAY = 24;
    private static final int SEASONS_PER_YEAR = 4;
    private static final int MONTHS_PER_SEASON = 3;
    private static final int MAX_YEAR = 9999;

    private final StringBuilder sb = new StringBuilder(48);
    private static final String[] SEASON_NAMES = {"Spring", "Summer", "Autumn", "Winter"};
    private static final String[] HOUR_PAD = {"00","01","02","03","04","05","06","07","08","09",
        "10","11","12","13","14","15","16","17","18","19","20","21","22","23"};

    public final void tick() {
        hour++;
        if (hour >= HOURS_PER_DAY) {
            hour = 0;
            day++;
            totalDays++;
            dayProgress = 0f;
        }
        dayProgress = hour / (float) HOURS_PER_DAY;

        if (day > DAYS_PER_MONTH) {
            day = 1;
            month++;
        }
        if (month > MONTHS_PER_YEAR) {
            month = 1;
            year = Math.min(year + 1, MAX_YEAR);
        }
        season = (month - 1) / MONTHS_PER_SEASON;
    }

    public final String getDisplay() {
        sb.setLength(0);
        sb.append('Y').append(year)
          .append(" M").append(month)
          .append(" D").append(day)
          .append(' ').append(HOUR_PAD[hour]).append(":00 | ")
          .append(SEASON_NAMES[season]);
        return sb.toString();
    }

    public final boolean isNight() { return hour < 6 || hour >= 20; }
    public final boolean isDawn() { return hour >= 5 && hour < 7; }
    public final boolean isDusk() { return hour >= 17 && hour < 19; }
    public final boolean isDay() { return hour >= 7 && hour < 17; }

    public final void set(int y, int m, int d, int h) {
        year = Math.max(1, Math.min(MAX_YEAR, y));
        month = Math.max(1, Math.min(MONTHS_PER_YEAR, m));
        day = Math.max(1, Math.min(DAYS_PER_MONTH, d));
        hour = Math.max(0, Math.min(HOURS_PER_DAY - 1, h));
        season = (month - 1) / MONTHS_PER_SEASON;
    }
}
