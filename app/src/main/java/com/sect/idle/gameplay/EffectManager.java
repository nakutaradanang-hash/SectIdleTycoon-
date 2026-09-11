package com.sect.idle.gameplay;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.sect.idle.systems.RNG;

/**
 * EffectManager - High-performance particle & visual FX manager.
 * Uses low-level primitive arrays to achieve zero garbage collection during gameplay.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class EffectManager {
    private static final int MAX_EFFECTS = 128;

    private final float[] posX = new float[MAX_EFFECTS];
    private final float[] posY = new float[MAX_EFFECTS];
    private final float[] velX = new float[MAX_EFFECTS];
    private final float[] velY = new float[MAX_EFFECTS];
    private final float[] life = new float[MAX_EFFECTS];
    private final float[] maxLife = new float[MAX_EFFECTS];
    private final float[] size = new float[MAX_EFFECTS];
    private final int[] color = new int[MAX_EFFECTS];

    private int activeCount = 0;
    private final Paint fxPaint;

    public EffectManager() {
        this.fxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.fxPaint.setStyle(Paint.Style.FILL);
    }

    public void spawn(float x, float y, int fxColor, int count) {
        for (int i = 0; i < count && activeCount < MAX_EFFECTS; i++) {
            int idx = activeCount++;
            posX[idx] = x;
            posY[idx] = y;
            float angle = RNG.nextFloat(0f, 6.28318f);
            float spd = RNG.nextFloat(20f, 80f);
            velX[idx] = (float) Math.cos(angle) * spd;
            velY[idx] = (float) Math.sin(angle) * spd;
            float l = RNG.nextFloat(0.4f, 1.2f);
            life[idx] = l;
            maxLife[idx] = l;
            size[idx] = RNG.nextFloat(3f, 8f);
            color[idx] = fxColor;
        }
    }

    public void update(float dt) {
        for (int i = activeCount - 1; i >= 0; i--) {
            life[i] -= dt;
            if (life[i] <= 0f) {
                // Swap with last active element to avoid shifting
                int last = activeCount - 1;
                if (i != last) {
                    posX[i] = posX[last];
                    posY[i] = posY[last];
                    velX[i] = velX[last];
                    velY[i] = velY[last];
                    life[i] = life[last];
                    maxLife[i] = maxLife[last];
                    size[i] = size[last];
                    color[i] = color[last];
                }
                activeCount--;
                continue;
            }
            posX[i] += velX[i] * dt;
            posY[i] += velY[i] * dt;
        }
    }

    public void render(Canvas canvas, Paint paint) {
        if (canvas == null || activeCount == 0) return;
        for (int i = 0; i < activeCount; i++) {
            float progress = life[i] / maxLife[i];
            int alpha = (int)(255 * progress);
            fxPaint.setColor(color[i]);
            fxPaint.setAlpha(alpha);
            canvas.drawCircle(posX[i], posY[i], size[i] * progress, fxPaint);
        }
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void clear() {
        activeCount = 0;
    }
}
