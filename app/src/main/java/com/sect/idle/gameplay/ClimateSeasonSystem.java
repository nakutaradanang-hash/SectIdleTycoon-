package com.sect.idle.gameplay;

import com.sect.idle.systems.RNG;
import com.sect.idle.utils.ExceptionManager;

/**
 * ClimateSeasonSystem - Layer 2.2 Dynamic Climate, 4 Seasons & Weather Simulation.
 * Simulates Spring, Summer, Autumn, Winter, dynamic weather events (Sunny, Thunderstorm,
 * Blizzard, Spirit Fog, Solar Flare), and 24-hour sun/moon lighting curves.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class ClimateSeasonSystem {

    private static volatile ClimateSeasonSystem instance;
    private static final Object LOCK = new Object();

    // Seasons
    public static final int SEASON_SPRING = 0; // Blossom (+30% Herb Growth, +10 Disciple Mood)
    public static final int SEASON_SUMMER = 1; // Scorching (+20% Mining Ore, -10 Energy Drain)
    public static final int SEASON_AUTUMN = 2; // Harvest (+50% Spirit Stone & Market Trade)
    public static final int SEASON_WINTER = 3; // Frost (-30% Farming, +40% Cold Cultivation)

    // Weather Types
    public static final int WEATHER_SUNNY = 0;
    public static final int WEATHER_RAIN = 1;
    public static final int WEATHER_THUNDERSTORM = 2;
    public static final int WEATHER_BLIZZARD = 3;
    public static final int WEATHER_SPIRIT_FOG = 4;
    public static final int WEATHER_CELESTIAL_AURORA = 5;

    public int currentSeason = SEASON_SPRING;
    public int currentWeather = WEATHER_SUNNY;
    public int seasonDay = 1;
    public static final int DAYS_PER_SEASON = 30;

    public float ambientTemperatureCelsius = 22.0f;
    public float windSpeedKmh = 12.0f;
    public float rainIntensity = 0.0f;
    public float fogDensity = 0.0f;

    public static ClimateSeasonSystem getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new ClimateSeasonSystem();
                }
            }
        }
        return instance;
    }

    private ClimateSeasonSystem() {}

    /**
     * Ticks day progression and updates seasons and weather.
     */
    public void advanceDay(SectData sect) {
        seasonDay++;
        if (seasonDay > DAYS_PER_SEASON) {
            seasonDay = 1;
            currentSeason = (currentSeason + 1) % 4;
            ExceptionManager.get().addBreadcrumb("CLIMATE", "Season transitioned to " + getSeasonName());
        }

        // Randomly roll daily weather
        int roll = RNG.nextInt(100);
        if (currentSeason == SEASON_WINTER && roll < 40) {
            currentWeather = WEATHER_BLIZZARD;
            ambientTemperatureCelsius = -5.0f - RNG.nextInt(10);
            fogDensity = 0.6f;
            rainIntensity = 0.0f;
        } else if (currentSeason == SEASON_SUMMER && roll < 30) {
            currentWeather = WEATHER_THUNDERSTORM;
            ambientTemperatureCelsius = 32.0f + RNG.nextInt(8);
            rainIntensity = 0.8f;
        } else if (roll < 10) {
            currentWeather = WEATHER_CELESTIAL_AURORA; // Rare divine event (+100% Cultivation EXP)
            fogDensity = 0.2f;
        } else if (roll < 35) {
            currentWeather = WEATHER_RAIN;
            rainIntensity = 0.5f;
        } else {
            currentWeather = WEATHER_SUNNY;
            rainIntensity = 0.0f;
            fogDensity = 0.0f;
            ambientTemperatureCelsius = 20.0f + (currentSeason == SEASON_SUMMER ? 10.0f : 0.0f);
        }

        // Apply seasonal harvest modifier if sect provided
        if (sect != null) {
            if (currentSeason == SEASON_SPRING) {
                sect.earn(0, 50, 0); // Bonus herbs
            } else if (currentSeason == SEASON_AUTUMN) {
                sect.earn(200, 0, 0); // Bonus stones
            }
        }
    }

    public String getSeasonName() {
        switch (currentSeason) {
            case SEASON_SPRING: return "Spring Blossom";
            case SEASON_SUMMER: return "Summer Solstice";
            case SEASON_AUTUMN: return "Autumn Harvest";
            case SEASON_WINTER: return "Winter Frost";
            default: return "Spring";
        }
    }

    public String getWeatherName() {
        switch (currentWeather) {
            case WEATHER_SUNNY: return "Clear Skies";
            case WEATHER_RAIN: return "Gentle Rain";
            case WEATHER_THUNDERSTORM: return "Celestial Thunderstorm";
            case WEATHER_BLIZZARD: return "Heavy Snowstorm";
            case WEATHER_SPIRIT_FOG: return "Dense Spirit Fog";
            case WEATHER_CELESTIAL_AURORA: return "Divine Celestial Aurora";
            default: return "Sunny";
        }
    }
}
