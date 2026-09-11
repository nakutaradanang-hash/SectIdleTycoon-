package com.sect.idle.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * Physics2DTest - Unit tests for 2D rigid bodies, force application, impulses,
 * kinematic updates, circle collisions, collision resolution, and projectile/orbital dynamics.
 */
public class Physics2DTest {

    private static final float DELTA = 0.05f;

    @Test
    public void testBodyForceAndKinematics() {
        Physics2D.Body body = new Physics2D.Body();
        body.setMass(2f);
        Assert.assertEquals(2f, body.mass, DELTA);
        Assert.assertEquals(0.5f, body.invMass, DELTA);

        // Apply force: F = 10 -> a = F/m = 5
        body.applyForce(10f, 0f);
        body.update(1.0f);

        // vel = (0 + 5 * 1.0) * friction(0.85) = 4.25
        Assert.assertTrue(body.vel.x > 0f);
        Assert.assertTrue(body.pos.x > 0f);
        Assert.assertEquals(0f, body.force.x, DELTA); // Force should be cleared after update
    }

    @Test
    public void testImpulseApplication() {
        Physics2D.Body body = new Physics2D.Body();
        body.setMass(1f);

        body.applyImpulse(10f, -5f);
        Assert.assertEquals(10f, body.vel.x, DELTA);
        Assert.assertEquals(-5f, body.vel.y, DELTA);
    }

    @Test
    public void testMoveTowards() {
        Physics2D.Body body = new Physics2D.Body();
        body.pos.set(0f, 0f);

        body.moveTowards(100f, 0f, 50f, 1.0f);
        Assert.assertTrue(body.vel.x > 0f);
        Assert.assertEquals(0f, body.vel.y, DELTA);
    }

    @Test
    public void testCollisionDetectionAndLayers() {
        Physics2D.Body b1 = new Physics2D.Body();
        b1.pos.set(0f, 0f);
        b1.radius = 10f;

        Physics2D.Body b2 = new Physics2D.Body();
        b2.pos.set(15f, 0f);
        b2.radius = 10f;

        // Radii sum = 20 > dist(15) -> Intersects
        Assert.assertTrue(b1.intersects(b2));

        // Move b2 out of range
        b2.pos.set(30f, 0f);
        Assert.assertFalse(b1.intersects(b2));

        // Test collision layers & masks
        b2.pos.set(15f, 0f);
        b1.collisionLayer = 0x01;
        b1.collisionMask = 0x02; // Only collides with layer 2
        b2.collisionLayer = 0x04; // Layer 4
        Assert.assertFalse(b1.intersects(b2));
    }

    @Test
    public void testCollisionResolution() {
        Physics2D.Body b1 = new Physics2D.Body();
        b1.pos.set(0f, 0f);
        b1.radius = 10f;
        b1.vel.set(5f, 0f);

        Physics2D.Body b2 = new Physics2D.Body();
        b2.pos.set(15f, 0f);
        b2.radius = 10f;
        b2.vel.set(-5f, 0f);

        b1.resolveCollision(b2);

        // After collision, b1 should move left (vel.x < 0) and b2 move right (vel.x > 0)
        Assert.assertTrue(b1.vel.x < 5f);
        Assert.assertTrue(b2.vel.x > -5f);
    }

    @Test
    public void testHelperPhysicsDynamics() {
        Vector2 pos = new Vector2(0f, 0f);
        Vector2 vel = new Vector2(10f, 0f);

        // Simple projectile gravity
        Physics2D.simpleProjectile(pos, vel, 9.8f, 1.0f);
        Assert.assertEquals(10f, pos.x, DELTA);
        Assert.assertEquals(9.8f, vel.y, DELTA);
        Assert.assertEquals(9.8f, pos.y, DELTA);

        // Spring force
        Vector2 springPos = new Vector2(10f, 0f);
        Vector2 springVel = new Vector2(0f, 0f);
        Physics2D.springForce(springPos, springVel, 0f, 0f, 2f, 0.1f, 1.0f);
        Assert.assertTrue(springVel.x < 0f); // Pulled back toward anchor (0,0)

        // Orbital motion
        Vector2 orbPos = new Vector2(10f, 0f);
        Vector2 orbVel = new Vector2(0f, 0f);
        Physics2D.orbitalMotion(orbPos, orbVel, 0f, 0f, 5f, 1.0f);
        Assert.assertTrue(orbVel.y > 0f); // Tangential force applied
    }
}
