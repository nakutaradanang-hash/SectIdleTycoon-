package com.sect.idle.core;

/**
 * Physics2D v2.0 - Lightweight physics system for entity movement and forces.
 */
public final class Physics2D {
    private Physics2D() {}
    
    public static final float GRAVITY = 98f;
    public static final float AIR_FRICTION = 0.98f;
    public static final float GROUND_FRICTION = 0.85f;
    
    public static class Body {
        public final Vector2 pos = new Vector2();
        public final Vector2 vel = new Vector2();
        public final Vector2 acc = new Vector2();
        public final Vector2 force = new Vector2();
        public float mass = 1f;
        public float invMass = 1f;
        public float radius = 10f;
        public float restitution = 0.3f;
        public float friction = GROUND_FRICTION;
        public boolean isStatic = false;
        public boolean isTrigger = false;
        public int collisionLayer = 0;
        public int collisionMask = 0xFFFFFFFF;
        
        public void setMass(float m) {
            mass = Math.max(0.001f, m);
            invMass = 1f / mass;
        }
        
        public void applyForce(float fx, float fy) {
            force.add(fx, fy);
        }
        
        public void applyForce(Vector2 f) {
            force.add(f);
        }
        
        public void applyImpulse(float ix, float iy) {
            vel.add(ix * invMass, iy * invMass);
        }
        
        public void applyImpulse(Vector2 impulse) {
            vel.add(impulse.x * invMass, impulse.y * invMass);
        }
        
        public void update(float dt) {
            if (isStatic) return;
            
            acc.set(force.x * invMass, force.y * invMass);
            vel.add(acc.x * dt, acc.y * dt);
            vel.mul(friction);
            pos.add(vel.x * dt, vel.y * dt);
            force.zero();
        }
        
        public void moveTowards(float tx, float ty, float speed, float dt) {
            float dx = tx - pos.x, dy = ty - pos.y;
            float distSq = dx * dx + dy * dy;
            if (distSq > 1f) {
                float dist = MathUtils.fastSqrt(distSq);
                vel.add((dx / dist) * speed * dt, (dy / dist) * speed * dt);
            }
        }
        
        public boolean intersects(Body other) {
            if (collisionLayer != 0 && (collisionMask & other.collisionLayer) == 0) return false;
            float dx = pos.x - other.pos.x, dy = pos.y - other.pos.y;
            float r = radius + other.radius;
            return dx * dx + dy * dy < r * r;
        }
        
        public void resolveCollision(Body other) {
            if (isStatic && other.isStatic) return;
            
            float dx = other.pos.x - pos.x;
            float dy = other.pos.y - pos.y;
            float distSq = dx * dx + dy * dy;
            float minDist = radius + other.radius;
            
            if (distSq >= minDist * minDist || distSq < 0.0001f) return;
            
            float dist = MathUtils.fastSqrt(distSq);
            float overlap = minDist - dist;
            float nx = dx / dist, ny = dy / dist;
            
            float totalInvMass = invMass + other.invMass;
            if (totalInvMass > 0) {
                float sep = overlap / totalInvMass;
                pos.sub(nx * sep * invMass, ny * sep * invMass);
                other.pos.add(nx * sep * other.invMass, ny * sep * other.invMass);
            }
            
            float rvx = other.vel.x - vel.x;
            float rvy = other.vel.y - vel.y;
            float velAlongNormal = rvx * nx + rvy * ny;
            
            if (velAlongNormal > 0) return;
            
            float e = Math.min(restitution, other.restitution);
            float j = -(1f + e) * velAlongNormal;
            j /= totalInvMass;
            
            float impulseX = nx * j, impulseY = ny * j;
            vel.sub(impulseX * invMass, impulseY * invMass);
            other.vel.add(impulseX * other.invMass, impulseY * other.invMass);
        }
    }
    
    public static void simpleProjectile(Vector2 pos, Vector2 vel, float gravity, float dt) {
        vel.y += gravity * dt;
        pos.add(vel.x * dt, vel.y * dt);
    }
    
    public static void springForce(Vector2 pos, Vector2 vel, float anchorX, float anchorY, float k, float damping, float dt) {
        float dx = pos.x - anchorX, dy = pos.y - anchorY;
        float fx = -k * dx - damping * vel.x;
        float fy = -k * dy - damping * vel.y;
        vel.add(fx * dt, fy * dt);
    }
    
    public static void orbitalMotion(Vector2 pos, Vector2 vel, float centerX, float centerY, float strength, float dt) {
        float dx = pos.x - centerX, dy = pos.y - centerY;
        float dist = MathUtils.fastSqrt(dx * dx + dy * dy);
        if (dist < 0.1f) return;
        float fx = -dy / dist * strength;
        float fy = dx / dist * strength;
        vel.add(fx * dt, fy * dt);
    }
}
