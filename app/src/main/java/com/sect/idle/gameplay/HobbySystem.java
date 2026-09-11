package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;

/**
 * HobbySystem - Leisure activities and personality quirks.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class HobbySystem {
    private HobbySystem() {}

    public static final String[] HOBBIES = {
        "Tea Tasting",
        "Sword Dance",
        "Poetry & Calligraphy",
        "Zither Music",
        "Stargazing",
        "Gourmet Cooking",
        "Beast Taming",
        "Go (Weiqi) Chess",
        "Woodcarving",
        "Herbal Brewing"
    };

    public static String getHobbyName(int hobbyIndex) {
        if (hobbyIndex >= 0 && hobbyIndex < HOBBIES.length) {
            return HOBBIES[hobbyIndex];
        }
        return "None";
    }

    public static void assignRandomHobby(Disciple d) {
        if (d != null) {
            d.hobby = RNG.nextInt(HOBBIES.length);
        }
    }
}
