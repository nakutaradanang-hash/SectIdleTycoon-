package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * BehaviorEngineTest - Unit tests for autonomous social interactions, mood dynamics,
 * and disciple relationship updates.
 */
public class BehaviorEngineTest {

    @Test
    public void testSocialInteractionUpdates() {
        ArrayList<Disciple> disciples = new ArrayList<Disciple>();
        Disciple d1 = new Disciple("Disciple A");
        Disciple d2 = new Disciple("Disciple B");
        disciples.add(d1);
        disciples.add(d2);

        d1.mood = 50;
        d1.stress = 50;

        // Run multiple behavior update passes
        for (int i = 0; i < 50; i++) {
            BehaviorEngine.updateBehavior(d1, disciples);
        }

        Assert.assertTrue(d1.mood >= 0 && d1.mood <= 100);
        Assert.assertTrue(d1.stress >= 0 && d1.stress <= 100);
    }

    @Test
    public void testNullSafety() {
        // Must handle null or single-element list safely without throwing exceptions
        BehaviorEngine.updateBehavior(null, null);
        BehaviorEngine.updateBehavior(new Disciple("Lonely Disciple"), new ArrayList<Disciple>());
    }
}
