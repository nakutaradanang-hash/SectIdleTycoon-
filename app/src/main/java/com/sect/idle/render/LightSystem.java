package com.sect.idle.render;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import com.sect.idle.systems.CameraSystem;

/**
 * LightSystem v2.0 - 2D Dynamic Radial Lighting Engine with additive blending.
 */
public final class LightSystem {
    private static final int MAX_LIGHTS = 24;
    private static final int POOL_SIZE = 32;

    private final float[] lightX;
    private final float[] lightY;
    private final float[] lightRadius;
    private final int[] lightColor;
    private final float[] lightIntensity;
    private final boolean[] lightActive;
    private final int[] lightType; // 0=point, 1=pulse, 2=flicker
    private final float[] lightTimer;
    private int activeCount;

    public float ambientR = 0.08f;
    public float ambientG = 0.06f;
    public float ambientB = 0.12f;
    public float ambientIntensity = 0.4f;

    private final Paint lightPaint;
    private final Paint ambientPaint;
    private final RectF tempRect;
    private final RadialGradient[] gradientPool;
    private final boolean[] gradientUsed;
    private int gradientPoolIndex;

    private int screenW, screenH;

    public LightSystem(int screenW, int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;

        this.lightX = new float[MAX_LIGHTS];
        this.lightY = new float[MAX_LIGHTS];
        this.lightRadius = new float[MAX_LIGHTS];
        this.lightColor = new int[MAX_LIGHTS];
        this.lightIntensity = new float[MAX_LIGHTS];
        this.lightActive = new boolean[MAX_LIGHTS];
        this.lightType = new int[MAX_LIGHTS];
        this.lightTimer = new float[MAX_LIGHTS];
        this.activeCount = 0;

        this.lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.lightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        this.ambientPaint = new Paint();
        this.tempRect = new RectF();

        this.gradientPool = new RadialGradient[POOL_SIZE];
        this.gradientUsed = new boolean[POOL_SIZE];
    }

    public void resize(int w, int h) {
        this.screenW = w;
        this.screenH = h;
    }

    public int addLight(float x, float y, float radius, int color, float intensity, int type) {
        if (activeCount >= MAX_LIGHTS) return -1;
        int idx = activeCount;
        for (int i = 0; i < MAX_LIGHTS; i++) {
            if (!lightActive[i]) {
                idx = i;
                break;
            }
        }
        lightX[idx] = x;
        lightY[idx] = y;
        lightRadius[idx] = radius;
        lightColor[idx] = color;
        lightIntensity[idx] = intensity;
        lightType[idx] = type;
        lightTimer[idx] = 0f;
        lightActive[idx] = true;
        if (idx == activeCount) activeCount++;
        return idx;
    }

    public void removeLight(int id) {
        if (id < 0 || id >= MAX_LIGHTS) return;
        lightActive[id] = false;
        if (id == activeCount - 1) {
            while (activeCount > 0 && !lightActive[activeCount - 1]) {
                activeCount--;
            }
        }
    }

    public void updateLight(int id, float x, float y, float radius, int color, float intensity) {
        if (id < 0 || id >= MAX_LIGHTS || !lightActive[id]) return;
        lightX[id] = x;
        lightY[id] = y;
        lightRadius[id] = radius;
        lightColor[id] = color;
        lightIntensity[id] = intensity;
    }

    public void clearLights() {
        for (int i = 0; i < MAX_LIGHTS; i++) {
            lightActive[i] = false;
        }
        activeCount = 0;
    }

    public void update(float dt) {
        for (int i = 0; i < MAX_LIGHTS; i++) {
            if (!lightActive[i]) continue;
            lightTimer[i] += dt;
            switch (lightType[i]) {
                case 1:
                    float pulse = (float)Math.sin(lightTimer[i] * 3f) * 0.15f + 1f;
                    lightIntensity[i] = Math.max(0.1f, lightIntensity[i] * pulse);
                    break;
                case 2:
                    if (((int)(lightTimer[i] * 10) % 3) == 0) {
                        lightIntensity[i] = lightIntensity[i] * (0.85f + (float)Math.random() * 0.3f);
                    }
                    break;
            }
        }
    }

    public void render(Canvas canvas, CameraSystem cam) {
        if (canvas == null || cam == null) return;

        for (int i = 0; i < POOL_SIZE; i++) gradientUsed[i] = false;
        gradientPoolIndex = 0;

        for (int i = 0; i < MAX_LIGHTS; i++) {
            if (!lightActive[i]) continue;

            float sx = cam.worldToScreenX(lightX[i]);
            float sy = cam.worldToScreenY(lightY[i]);
            float sr = lightRadius[i] * cam.zoom;

            if (sx + sr < 0 || sx - sr > screenW || sy + sr < 0 || sy - sr > screenH) continue;

            int alpha = (int)(Math.min(1f, lightIntensity[i]) * 180);
            int centerColor = (alpha << 24) | (lightColor[i] & 0x00FFFFFF);
            int edgeColor = 0x00000000;

            RadialGradient rg = obtainGradient(sx, sy, sr, centerColor, edgeColor);
            lightPaint.setShader(rg);
            tempRect.set(sx - sr, sy - sr, sx + sr, sy + sr);
            canvas.drawOval(tempRect, lightPaint);
        }

        lightPaint.setShader(null);
    }

    private RadialGradient obtainGradient(float cx, float cy, float r, int c1, int c2) {
        for (int i = 0; i < POOL_SIZE; i++) {
            if (!gradientUsed[i]) {
                gradientUsed[i] = true;
                gradientPool[i] = new RadialGradient(cx, cy, Math.max(1f, r), c1, c2, Shader.TileMode.CLAMP);
                return gradientPool[i];
            }
        }
        int idx = gradientPoolIndex % POOL_SIZE;
        gradientPoolIndex++;
        gradientPool[idx] = new RadialGradient(cx, cy, Math.max(1f, r), c1, c2, Shader.TileMode.CLAMP);
        return gradientPool[idx];
    }

    public void renderAmbient(Canvas canvas) {
        if (canvas == null) return;
        int a = (int)(ambientIntensity * 255);
        int color = (a << 24) | (((int)(ambientR * 255) & 0xFF) << 16) | (((int)(ambientG * 255) & 0xFF) << 8) | ((int)(ambientB * 255) & 0xFF);
        ambientPaint.setColor(color);
        canvas.drawRect(0, 0, screenW, screenH, ambientPaint);
    }

    public int getActiveCount() {
        int c = 0;
        for (int i = 0; i < MAX_LIGHTS; i++) if (lightActive[i]) c++;
        return c;
    }

    public void shutdown() {
        clearLights();
    }
}
