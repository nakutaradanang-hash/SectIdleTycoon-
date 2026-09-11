package com.sect.idle.systems;

import com.sect.idle.core.GameConfig;
import com.sect.idle.core.MathUtils;
import com.sect.idle.core.Rect;
import com.sect.idle.core.Vector2;

/**
 * CameraSystem v5.1 - Camera with smooth follow, multi-target, bounds clamping,
 * shake with decay, and screen-world coordinate transform.
 */
public final class CameraSystem {
    public final Vector2 pos;
    public final Vector2 targetPos;
    public final Vector2 velocity;
    public float zoom;
    public float targetZoom;
    public float rotation;
    public float targetRotation;
    public int viewportW, viewportH;
    public final Rect bounds;
    public float shakeIntensity;
    public int shakeDuration;
    public float shakeDecay;

    private final Vector2 shakeOffset;
    private final Vector2 smoothVel;
    private float lerpSpeed = GameConfig.CAM_LERP;
    private float zoomSpeed = 3.0f;
    private float rotSpeed = 2.0f;
    private float trauma;
    private final java.util.Random rand;

    private final Vector2[] targets;
    private final float[] targetWeights;
    private int targetCount;
    private float targetWeightSum;

    private final Rect rectPool = new Rect();
    private final Vector2 vecPool = new Vector2();

    public CameraSystem(int vw, int vh) {
        this.pos = new Vector2();
        this.targetPos = new Vector2();
        this.velocity = new Vector2();
        this.shakeOffset = new Vector2();
        this.smoothVel = new Vector2();
        this.targets = new Vector2[16];
        this.targetWeights = new float[16];
        for (int i = 0; i < 16; i++) {
            targets[i] = new Vector2();
            targetWeights[i] = 1f;
        }
        this.zoom = 1.0f;
        this.targetZoom = 1.0f;
        this.rotation = 0f;
        this.targetRotation = 0f;
        this.viewportW = Math.max(1, vw);
        this.viewportH = Math.max(1, vh);
        this.bounds = new Rect(0, 0, 4000, 4000);
        this.shakeDecay = GameConfig.CAM_SHAKE_DECAY;
        this.rand = new java.util.Random();
        this.targetCount = 0;
        this.targetWeightSum = 0f;
    }

    public void setBounds(float x, float y, float w, float h) {
        bounds.set(x, y, Math.max(0f, w), Math.max(0f, h));
    }

    public void setViewport(int w, int h) {
        viewportW = Math.max(1, w);
        viewportH = Math.max(1, h);
    }

    public void addTarget(float x, float y, float weight) {
        if (targetCount < targets.length && weight > 0) {
            targets[targetCount].set(x, y);
            targetWeights[targetCount] = weight;
            targetWeightSum += weight;
            targetCount++;
        }
    }

    public void clearTargets() {
        targetCount = 0;
        targetWeightSum = 0f;
    }

    public void update(float dt) {
        dt = MathUtils.clamp(dt, 0.001f, 0.05f);

        if (targetCount > 0 && targetWeightSum > 0.001f) {
            targetPos.zero();
            float invSum = 1f / targetWeightSum;
            for (int i = 0; i < targetCount; i++) {
                float w = targetWeights[i] * invSum;
                targetPos.x += targets[i].x * w;
                targetPos.y += targets[i].y * w;
            }
            clearTargets();
        }

        float omega = 2.0f / Math.max(0.01f, lerpSpeed);
        float t = MathUtils.clamp01(omega * dt);
        if (GameConfig.ENABLE_SMOOTH_CAMERA) {
            pos.lerp(targetPos.x, targetPos.y, t);
        } else {
            pos.set(targetPos.x, targetPos.y);
        }

        zoom = MathUtils.approach(zoom, targetZoom, zoomSpeed * dt);
        zoom = MathUtils.clamp(zoom, GameConfig.CAM_MIN_ZOOM, GameConfig.CAM_MAX_ZOOM);

        rotation = MathUtils.approach(rotation, targetRotation, rotSpeed * dt);

        float safeZoom = Math.max(0.001f, zoom);
        float halfW = (viewportW * 0.5f) / safeZoom;
        float halfH = (viewportH * 0.5f) / safeZoom;
        pos.x = MathUtils.clamp(pos.x, bounds.left() + halfW, bounds.right() - halfW);
        pos.y = MathUtils.clamp(pos.y, bounds.top() + halfH, bounds.bottom() - halfH);

        shakeOffset.set(0, 0);
        if (!GameConfig.ENABLE_FAKE_3D) return;

        if (trauma > 0.001f) {
            float shake = trauma * trauma;
            shakeOffset.x = (rand.nextFloat() - 0.5f) * 2f * shakeIntensity * shake;
            shakeOffset.y = (rand.nextFloat() - 0.5f) * 2f * shakeIntensity * shake;
            trauma *= shakeDecay;
            if (trauma < 0.001f) trauma = 0;
        } else if (shakeDuration > 0) {
            shakeOffset.x = (rand.nextFloat() - 0.5f) * 2f * shakeIntensity;
            shakeOffset.y = (rand.nextFloat() - 0.5f) * 2f * shakeIntensity;
            shakeDuration--;
            if (shakeDuration <= 0) shakeIntensity = 0;
        }
    }

    public void addTrauma(float amount) {
        if (!GameConfig.ENABLE_FAKE_3D) return;
        trauma = MathUtils.clamp01(trauma + amount);
    }

    public void moveTo(float x, float y) { targetPos.set(x, y); }
    public void follow(float x, float y) { targetPos.set(x, y); }
    public void setZoom(float z) { targetZoom = MathUtils.clamp(z, GameConfig.CAM_MIN_ZOOM, GameConfig.CAM_MAX_ZOOM); }
    public void zoomIn(float amount) { setZoom(targetZoom + Math.max(0, amount)); }
    public void zoomOut(float amount) { setZoom(targetZoom - Math.max(0, amount)); }

    public void shake(float intensity, int frames) {
        if (!GameConfig.ENABLE_FAKE_3D) return;
        shakeIntensity = Math.max(0f, intensity);
        shakeDuration = Math.max(0, frames);
        trauma = MathUtils.clamp01(intensity / 10f);
    }

    public void setLerpSpeed(float s) { lerpSpeed = MathUtils.clamp(s, 0.01f, 1f); }

    public float screenToWorldX(float sx) {
        float safeZoom = Math.max(0.001f, zoom);
        return pos.x + shakeOffset.x - (viewportW * 0.5f) / safeZoom + sx / safeZoom;
    }

    public float screenToWorldY(float sy) {
        float safeZoom = Math.max(0.001f, zoom);
        return pos.y + shakeOffset.y - (viewportH * 0.5f) / safeZoom + sy / safeZoom;
    }

    public float worldToScreenX(float wx) {
        float safeZoom = Math.max(0.001f, zoom);
        return (wx - pos.x - shakeOffset.x + (viewportW * 0.5f) / safeZoom) * zoom;
    }

    public float worldToScreenY(float wy) {
        float safeZoom = Math.max(0.001f, zoom);
        return (wy - pos.y - shakeOffset.y + (viewportH * 0.5f) / safeZoom) * zoom;
    }

    public void worldToScreen(float wx, float wy, Vector2 out) {
        if (out == null) return;
        out.set(worldToScreenX(wx), worldToScreenY(wy));
    }

    public void screenToWorld(float sx, float sy, Vector2 out) {
        if (out == null) return;
        out.set(screenToWorldX(sx), screenToWorldY(sy));
    }

    public boolean isVisible(float wx, float wy, float margin) {
        float sx = worldToScreenX(wx), sy = worldToScreenY(wy);
        return sx > -margin && sx < viewportW + margin && sy > -margin && sy < viewportH + margin;
    }

    public Rect getViewRect(Rect out) {
        if (out == null) out = rectPool;
        float safeZoom = Math.max(0.001f, zoom);
        float hw = (viewportW * 0.5f) / safeZoom;
        float hh = (viewportH * 0.5f) / safeZoom;
        out.set(pos.x - hw, pos.y - hh, hw * 2f, hh * 2f);
        return out;
    }
}
