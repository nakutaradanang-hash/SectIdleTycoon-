package com.sect.idle.render;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import com.sect.idle.systems.CameraSystem;
import com.sect.idle.systems.RNG;

/**
 * ParticleEngineV2 - Particle system with SIMD-like array processing.
 */
public final class ParticleEngineV2 {
    public static final int MAX_PARTICLES = 512;
    public static final int TYPE_SPRITE = 0;
    public static final int TYPE_GLOW = 1;
    public static final int TYPE_SMOKE = 2;
    public static final int TYPE_FIRE = 3;
    public static final int TYPE_SPARKLE = 4;
    public static final int TYPE_QI_PETAL = 5; // Peach blossom / Celestial petal
    public static final int TYPE_SWORD_AURA = 6; // Sword intent streak
    public static final int TYPE_LIGHTNING = 7; // Heavenly tribulation spark

    private final float[] px;
    private final float[] py;
    private final float[] pvx;
    private final float[] pvy;
    private final float[] pLife;
    private final float[] pMaxLife;
    private final float[] pSize;
    private final int[] pColor;
    private final int[] pType;
    private final boolean[] pActive;
    private int activeCount;

    public float gravityX = 0f;
    public float gravityY = 9.8f;
    public float windX = 0f;
    public float windY = 0f;
    public float turbulence = 0f;

    private final Paint particlePaint;
    private final Paint additivePaint;
    private final Paint glowPaint;
    private int screenW, screenH;

    public ParticleEngineV2(int screenW, int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;
        this.px = new float[MAX_PARTICLES];
        this.py = new float[MAX_PARTICLES];
        this.pvx = new float[MAX_PARTICLES];
        this.pvy = new float[MAX_PARTICLES];
        this.pLife = new float[MAX_PARTICLES];
        this.pMaxLife = new float[MAX_PARTICLES];
        this.pSize = new float[MAX_PARTICLES];
        this.pColor = new int[MAX_PARTICLES];
        this.pType = new int[MAX_PARTICLES];
        this.pActive = new boolean[MAX_PARTICLES];
        this.activeCount = 0;

        this.particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.additivePaint = new Paint();
        this.additivePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        this.glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.glowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
    }

    public void resize(int w, int h) {
        this.screenW = w;
        this.screenH = h;
    }

    public int spawn(float x, float y, float vx, float vy, float life, float size, int color, int type) {
        if (activeCount >= MAX_PARTICLES) {
            int oldest = findOldest();
            if (oldest < 0) return -1;
            initParticle(oldest, x, y, vx, vy, life, size, color, type);
            return oldest;
        }
        int idx = activeCount;
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (!pActive[i]) { idx = i; break; }
        }
        initParticle(idx, x, y, vx, vy, life, size, color, type);
        if (idx == activeCount) activeCount++;
        return idx;
    }

    public void spawnBurst(float x, float y, int count, float speedMin, float speedMax, float lifeMin, float lifeMax, float sizeMin, float sizeMax, int color, int type) {
        for (int i = 0; i < count; i++) {
            float angle = RNG.nextRadian();
            float speed = RNG.nextFloat(speedMin, speedMax);
            float vx = (float)Math.cos(angle) * speed;
            float vy = (float)Math.sin(angle) * speed;
            float life = RNG.nextFloat(lifeMin, lifeMax);
            float size = RNG.nextFloat(sizeMin, sizeMax);
            spawn(x, y, vx, vy, life, size, color, type);
        }
    }

    public void spawnTrail(float x, float y, float vx, float vy, int count, int color) {
        for (int i = 0; i < count; i++) {
            float t = i / (float)count;
            spawn(x - vx * t * 0.1f, y - vy * t * 0.1f,
                  vx * 0.3f + RNG.nextFloat(-5f, 5f),
                  vy * 0.3f + RNG.nextFloat(-5f, 5f),
                  0.3f + RNG.nextFloat(0f, 0.2f),
                  2f + RNG.nextFloat(0f, 3f),
                  color, TYPE_GLOW);
        }
    }

    private void initParticle(int idx, float x, float y, float vx, float vy, float life, float size, int color, int type) {
        px[idx] = x;
        py[idx] = y;
        pvx[idx] = vx;
        pvy[idx] = vy;
        pLife[idx] = life;
        pMaxLife[idx] = life;
        pSize[idx] = size;
        pColor[idx] = color;
        pType[idx] = type;
        pActive[idx] = true;
    }

    private int findOldest() {
        float minLife = Float.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (!pActive[i]) return i;
            if (pLife[i] < minLife) {
                minLife = pLife[i];
                idx = i;
            }
        }
        return idx;
    }

    public void update(float dt) {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (!pActive[i]) continue;
            pLife[i] -= dt;
            if (pLife[i] <= 0) {
                pActive[i] = false;
                continue;
            }

            pvx[i] += (gravityX + windX) * dt;
            pvy[i] += (gravityY + windY) * dt;
            if (turbulence > 0) {
                pvx[i] += RNG.nextFloat(-turbulence, turbulence) * dt;
                pvy[i] += RNG.nextFloat(-turbulence, turbulence) * dt;
            }

            switch (pType[i]) {
                case TYPE_FIRE:
                    pvx[i] *= 0.98f;
                    pvy[i] *= 0.95f;
                    pvy[i] -= 20f * dt;
                    break;
                case TYPE_SMOKE:
                    pvx[i] *= 0.99f;
                    pvy[i] *= 0.99f;
                    pvy[i] -= 10f * dt;
                    break;
                case TYPE_SPARKLE:
                    pvx[i] *= 0.96f;
                    pvy[i] *= 0.96f;
                    break;
                case TYPE_GLOW:
                    pvx[i] *= 0.94f;
                    pvy[i] *= 0.94f;
                    break;
                case TYPE_QI_PETAL:
                    pvx[i] = (float) (Math.sin(pLife[i] * 3.0f + i) * 15.0f) + windX;
                    pvy[i] = 18.0f + windY * 0.5f;
                    break;
                case TYPE_SWORD_AURA:
                    pvx[i] *= 0.92f;
                    pvy[i] *= 0.92f;
                    break;
                case TYPE_LIGHTNING:
                    pvx[i] += RNG.nextFloat(-120f, 120f) * dt;
                    pvy[i] += RNG.nextFloat(-120f, 120f) * dt;
                    pvx[i] *= 0.85f;
                    pvy[i] *= 0.85f;
                    break;
            }

            px[i] += pvx[i] * dt;
            py[i] += pvy[i] * dt;
        }

        while (activeCount > 0 && !pActive[activeCount - 1]) {
            activeCount--;
        }
    }

    public void render(Canvas canvas, CameraSystem cam) {
        if (canvas == null) return;
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (!pActive[i]) continue;

            float sx, sy, ss;
            if (cam != null) {
                sx = cam.worldToScreenX(px[i]);
                sy = cam.worldToScreenY(py[i]);
                ss = pSize[i] * cam.zoom;
            } else {
                sx = px[i];
                sy = py[i];
                ss = pSize[i];
            }

            if (sx + ss < 0 || sx - ss > screenW || sy + ss < 0 || sy - ss > screenH) continue;

            float lifeRatio = pLife[i] / pMaxLife[i];
            int alpha = (int)(lifeRatio * 255);
            int color = (alpha << 24) | (pColor[i] & 0x00FFFFFF);

            switch (pType[i]) {
                case TYPE_FIRE:
                case TYPE_GLOW:
                case TYPE_SPARKLE:
                case TYPE_LIGHTNING:
                    glowPaint.setColor(color);
                    canvas.drawCircle(sx, sy, ss, glowPaint);
                    break;
                case TYPE_SWORD_AURA:
                    glowPaint.setColor(color);
                    canvas.drawLine(sx - pvx[i] * 0.04f, sy - pvy[i] * 0.04f, sx + pvx[i] * 0.04f, sy + pvy[i] * 0.04f, glowPaint);
                    canvas.drawCircle(sx, sy, ss * 0.8f, glowPaint);
                    break;
                case TYPE_QI_PETAL:
                    particlePaint.setColor(color);
                    canvas.drawOval(new android.graphics.RectF(sx - ss * 1.5f, sy - ss * 0.8f, sx + ss * 1.5f, sy + ss * 0.8f), particlePaint);
                    break;
                case TYPE_SMOKE:
                    particlePaint.setColor(color);
                    particlePaint.setAlpha(alpha / 2);
                    canvas.drawCircle(sx, sy, ss * 2f, particlePaint);
                    break;
                default:
                    particlePaint.setColor(color);
                    canvas.drawCircle(sx, sy, ss, particlePaint);
                    break;
            }
        }
    }

    public void renderScreenSpace(Canvas canvas) {
        render(canvas, null);
    }

    public void clear() {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            pActive[i] = false;
        }
        activeCount = 0;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void shutdown() {
        clear();
    }
}
