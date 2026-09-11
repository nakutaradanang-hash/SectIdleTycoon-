package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import com.sect.idle.models.Fairy;
import com.sect.idle.systems.RNG;

/**
 * FairySystem - Spiritual companion summoning and nurturing.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class FairySystem {
    private FairySystem() {}

    private static final String[] FAIRY_NAMES = {
        "Snow Sprite", "Jade Butterfly", "Flame Pixie", "Thunder Wisp",
        "Lotus Spirit", "Moonlight Fae", "Breeze Nymph", "Golden Cicada"
    };

    private static final String[] FAIRY_ELEMENTS = {
        "Ice", "Wood", "Fire", "Lightning", "Water", "Light", "Wind", "Metal"
    };

    public static void summonFairy(Disciple d) {
        if (d == null || d.hasFairy) return;
        int idx = RNG.nextInt(FAIRY_NAMES.length);
        d.fairy = new Fairy(FAIRY_NAMES[idx], FAIRY_ELEMENTS[idx]);
        d.hasFairy = true;
    }
}
