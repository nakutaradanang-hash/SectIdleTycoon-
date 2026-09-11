package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

/**
 * BehaviorEngine - Social interactions, mood dynamics, and autonomous behavior.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BehaviorEngine {
    private BehaviorEngine() {}

    public static void updateBehavior(Disciple d, ArrayList<Disciple> allDisciples) {
        if (d == null || !d.isAlive() || allDisciples == null || allDisciples.size() <= 1) return;

        // Random social interaction
        if (RNG.chance(20)) {
            int targetIdx = RNG.nextInt(allDisciples.size());
            Disciple target = allDisciples.get(targetIdx);
            if (target != null && target != d && target.isAlive()) {
                if (d.personality == target.personality || RNG.chance(60)) {
                    // Positive chat
                    d.mood = Math.min(100, d.mood + 5);
                    d.stress = Math.max(0, d.stress - 5);
                    target.mood = Math.min(100, target.mood + 5);
                } else {
                    // Minor dispute
                    d.stress = Math.min(100, d.stress + 5);
                    target.stress = Math.min(100, target.stress + 5);
                }
            }
        }
    }
}
