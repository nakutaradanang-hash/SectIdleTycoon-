package com.sect.idle.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * PostProcessor v1.0 - Screen-space post-processing effects (vignette, flash, bloom, fade).
 */
public final class PostProcessor {
    private Bitmap buffer;
    private Canvas bufferCanvas;
    private int screenW, screenH;
    private boolean enabled;

    public float vignetteIntensity;
    public int flashColor;
    public float flashAlpha;
    public float fadeAlpha;
    public int fadeColor;
    public float bloomIntensity;
    public float chromaticOffset;

    private final Paint vignettePaint;
    private final Paint flashPaint;
    private final Paint fadePaint;
    private final Paint bloomPaint;
    private final Rect srcRect;
    private final Rect dstRect;
    private final RectF tempRect;
    private RadialGradient vignetteGradient;

    public PostProcessor(int w, int h) {
        this.screenW = w;
        this.screenH = h;
        this.enabled = true;
        this.vignetteIntensity = 0.6f;
        this.flashAlpha = 0f;
        this.fadeAlpha = 0f;
        this.fadeColor = 0xFF000000;
        this.bloomIntensity = 0.3f;
        this.chromaticOffset = 0f;

        this.vignettePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.flashPaint = new Paint();
        this.fadePaint = new Paint();
        this.bloomPaint = new Paint();
        this.bloomPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        this.srcRect = new Rect();
        this.dstRect = new Rect();
        this.tempRect = new RectF();

        allocate(w, h);
        updateVignette();
    }

    private void allocate(int w, int h) {
        release();
        try {
            buffer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bufferCanvas = new Canvas(buffer);
        } catch (OutOfMemoryError e) {
            buffer = null;
            bufferCanvas = null;
            enabled = false;
        }
    }

    private void updateVignette() {
        int centerColor = 0x00000000;
        int edgeColor = ((int)(vignetteIntensity * 180) << 24);
        float cx = screenW * 0.5f;
        float cy = screenH * 0.5f;
        float radius = (float)Math.hypot(cx, cy);
        vignetteGradient = new RadialGradient(cx, cy, Math.max(1f, radius), centerColor, edgeColor, Shader.TileMode.CLAMP);
        vignettePaint.setShader(vignetteGradient);
    }

    public void resize(int w, int h) {
        if (w == screenW && h == screenH) return;
        this.screenW = w;
        this.screenH = h;
        allocate(w, h);
        updateVignette();
    }

    public void process(Canvas target, Bitmap source) {
        if (!enabled || bufferCanvas == null || buffer == null || target == null || source == null) {
            if (target != null && source != null) {
                srcRect.set(0, 0, source.getWidth(), source.getHeight());
                dstRect.set(0, 0, screenW, screenH);
                target.drawBitmap(source, srcRect, dstRect, null);
            }
            return;
        }

        bufferCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        srcRect.set(0, 0, source.getWidth(), source.getHeight());
        dstRect.set(0, 0, screenW, screenH);
        bufferCanvas.drawBitmap(source, srcRect, dstRect, null);

        if (flashAlpha > 0.01f) {
            flashPaint.setColor(flashColor);
            flashPaint.setAlpha((int)(flashAlpha * 255));
            bufferCanvas.drawRect(0, 0, screenW, screenH, flashPaint);
            flashAlpha = Math.max(0f, flashAlpha - 0.05f);
        }

        if (bloomIntensity > 0.01f) {
            bloomPaint.setAlpha((int)(bloomIntensity * 80));
            bufferCanvas.drawBitmap(source, srcRect, dstRect, bloomPaint);
        }

        if (vignetteIntensity > 0.01f) {
            tempRect.set(0, 0, screenW, screenH);
            bufferCanvas.drawRect(tempRect, vignettePaint);
        }

        if (fadeAlpha > 0.01f) {
            fadePaint.setColor(fadeColor);
            fadePaint.setAlpha((int)(fadeAlpha * 255));
            bufferCanvas.drawRect(0, 0, screenW, screenH, fadePaint);
        }

        target.drawBitmap(buffer, srcRect, dstRect, null);
    }

    public void triggerFlash(int color, float intensity) {
        this.flashColor = color;
        this.flashAlpha = Math.min(1f, intensity);
    }

    public void setFade(float alpha, int color) {
        this.fadeAlpha = Math.max(0f, Math.min(1f, alpha));
        this.fadeColor = color;
    }

    public void release() {
        if (buffer != null && !buffer.isRecycled()) buffer.recycle();
        buffer = null;
        bufferCanvas = null;
    }

    public void setEnabled(boolean e) { this.enabled = e; }
    public boolean isEnabled() { return enabled; }
}
