package com.sect.idle.render;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.sect.idle.systems.CameraSystem;

/**
 * VisualFX v1.0 - Helper methods for triggering particle and light visual effects.
 */
public final class VisualFX {
    private final ParticleEngineV2 particles;
    private final LightSystem lights;
    private final Paint tempPaint;
    private final RectF tempRect;

    public VisualFX(ParticleEngineV2 particles, LightSystem lights) {
        this.particles = particles;
        this.lights = lights;
        this.tempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.tempRect = new RectF();
    }

    public void spawnExplosion(float x, float y, int color, int count, float radius) {
        if (particles == null) return;
        particles.spawnBurst(x, y, count, 50f, 200f, 0.2f, 0.8f, 2f, 8f, color, ParticleEngineV2.TYPE_FIRE);
        particles.spawnBurst(x, y, count / 2, 20f, 100f, 0.3f, 1.0f, 1f, 4f, 0xFFFFAA00, ParticleEngineV2.TYPE_SPARKLE);
    }

    public void spawnHealEffect(float x, float y) {
        if (particles == null) return;
        particles.spawnBurst(x, y, 12, 10f, 60f, 0.5f, 1.2f, 2f, 6f, 0xFF4CAF50, ParticleEngineV2.TYPE_GLOW);
        for (int i = 0; i < 6; i++) {
            float angle = (float)(Math.PI * 2 * i / 6f);
            particles.spawn(x, y,
                (float)Math.cos(angle) * 30f,
                (float)Math.sin(angle) * 30f - 40f,
                0.8f, 3f, 0xFF81C784, ParticleEngineV2.TYPE_GLOW);
        }
    }

    public void spawnLevelUp(float x, float y) {
        if (particles == null) return;
        particles.spawnBurst(x, y, 30, 20f, 150f, 0.5f, 1.5f, 2f, 8f, 0xFFFFD700, ParticleEngineV2.TYPE_SPARKLE);
        particles.spawnBurst(x, y, 15, 10f, 80f, 0.8f, 2.0f, 3f, 10f, 0xFFFFFFFF, ParticleEngineV2.TYPE_GLOW);
    }

    public void spawnAura(float x, float y, int color, float duration) {
        if (particles == null) return;
        for (int i = 0; i < 20; i++) {
            float angle = (float)(Math.random() * Math.PI * 2);
            float dist = 20f + (float)Math.random() * 30f;
            particles.spawn(x + (float)Math.cos(angle) * dist,
                          y + (float)Math.sin(angle) * dist,
                          (float)Math.cos(angle) * 10f,
                          (float)Math.sin(angle) * 10f - 20f,
                          duration * 0.5f + (float)Math.random() * duration * 0.5f,
                          2f + (float)Math.random() * 3f,
                          color, ParticleEngineV2.TYPE_GLOW);
        }
    }

    public void spawnTrail(float x, float y, float vx, float vy, int color, int count) {
        if (particles == null) return;
        particles.spawnTrail(x, y, vx, vy, count, color);
    }

    public void spawnRain(int screenW, int screenH, CameraSystem cam) {
        if (particles == null || cam == null) return;
        float cx = cam.pos.x;
        float cy = cam.pos.y;
        for (int i = 0; i < 5; i++) {
            float rx = cx + (float)(Math.random() - 0.5) * screenW * 1.5f;
            float ry = cy - screenH * 0.5f + (float)(Math.random()) * screenH;
            particles.spawn(rx, ry, -20f + (float)(Math.random() * 10f), 400f + (float)(Math.random() * 100f),
                           0.3f + (float)(Math.random() * 0.2f), 1f, 0xFF448AFF, ParticleEngineV2.TYPE_SPRITE);
        }
    }

    public int addTorch(float x, float y, float radius, int color) {
        if (lights == null) return -1;
        return lights.addLight(x, y, radius, color, 0.8f, 2);
    }

    public int addPulseLight(float x, float y, float radius, int color) {
        if (lights == null) return -1;
        return lights.addLight(x, y, radius, color, 1.0f, 1);
    }

    public int addStaticLight(float x, float y, float radius, int color, float intensity) {
        if (lights == null) return -1;
        return lights.addLight(x, y, radius, color, intensity, 0);
    }

    public void renderShockwave(Canvas canvas, float x, float y, float maxRadius, float progress, int color) {
        if (canvas == null) return;
        float r = maxRadius * progress;
        int alpha = (int)((1f - progress) * 150);
        tempPaint.setColor(color);
        tempPaint.setAlpha(alpha);
        tempPaint.setStyle(Paint.Style.STROKE);
        tempPaint.setStrokeWidth(4f * (1f - progress) + 1f);
        tempRect.set(x - r, y - r, x + r, y + r);
        canvas.drawOval(tempRect, tempPaint);
        tempPaint.setStyle(Paint.Style.FILL);
    }

    public void renderGroundRing(Canvas canvas, CameraSystem cam, float wx, float wy, float radius, float progress, int color) {
        if (canvas == null || cam == null) return;
        float sx = cam.worldToScreenX(wx);
        float sy = cam.worldToScreenY(wy);
        float sr = radius * cam.zoom;
        int alpha = (int)((1f - progress) * 200);
        tempPaint.setColor(color);
        tempPaint.setAlpha(alpha);
        tempPaint.setStyle(Paint.Style.STROKE);
        tempPaint.setStrokeWidth(3f * cam.zoom);
        tempRect.set(sx - sr, sy - sr * 0.5f, sx + sr, sy + sr * 0.5f);
        canvas.drawOval(tempRect, tempPaint);
        tempPaint.setStyle(Paint.Style.FILL);
    }

    public void renderDamageNumber(Canvas canvas, CameraSystem cam, float wx, float wy, int damage, int color, float floatY) {
        if (canvas == null || cam == null) return;
        float sx = cam.worldToScreenX(wx);
        float sy = cam.worldToScreenY(wy) + floatY;
        tempPaint.setColor(0xFF000000);
        tempPaint.setTextSize(24f * cam.zoom);
        tempPaint.setTextAlign(Paint.Align.CENTER);
        tempPaint.setShadowLayer(3f, 1f, 1f, 0xFF000000);
        String text = String.valueOf(damage);
        canvas.drawText(text, sx + 1, sy + 1, tempPaint);
        tempPaint.setColor(color);
        canvas.drawText(text, sx, sy, tempPaint);
        tempPaint.setShadowLayer(0f, 0f, 0f, 0);
    }
}
