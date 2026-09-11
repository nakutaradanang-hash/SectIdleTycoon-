package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;

/**
 * AgeSystem - Manages disciple aging and lifespan progression.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class AgeSystem {
    private AgeSystem() {}

    public static void ageTick(Disciple d) {
        if (d == null) return;
        d.age++;
        if (d.age >= d.lifespan) {
            d.hp = 0; // Natural passing
        }
    }

    public static void extendLifespan(Disciple d, int years) {
        if (d != null && years > 0) {
            d.lifespan += years;
        }
    }
}
