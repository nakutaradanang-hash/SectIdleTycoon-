package com.sect.idle.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import com.sect.idle.core.GameConfig;
import com.sect.idle.systems.CameraSystem;

/**
 * RenderEngine v4.3 - 2D Rendering Engine with layer compositing, culling, batching.
 */
public final class RenderEngine {
    public static final int LAYER_BG = 0;
    public static final int LAYER_SHADOW = 1;
    public static final int LAYER_WORLD = 2;
    public static final int LAYER_ENTITY = 3;
    public static final int LAYER_EFFECT = 4;
    public static final int LAYER_LIGHT = 5;
    public static final int LAYER_UI = 6;
    public static final int LAYER_COUNT = 7;

    private final RenderLayer[] layers;
    private final Paint layerPaint;
    private final Paint additivePaint;
    private final Paint multiplyPaint;
    private final Paint debugPaint;
    private final Rect srcRect;
    private final Rect dstRect;
    private final RectF tempRectF;
    private final StringBuilder sb;
    private final StringBuilder memSb;

    public int statDrawCalls;
    public int statEntities;
    public int statParticles;
    public int statLights;
    public int statCulled;
    public float statFrameTimeMs;
    private long frameStartNs;

    public boolean enableShadows;
    public boolean enableLighting;
    public boolean enableParticles;
    public boolean enablePostProcess;
    public boolean enableDebug;
    public int maxLights;
    public int maxParticles;

    private Bitmap compositeBuffer;
    private Canvas compositeCanvas;
    private int screenW;
    private int screenH;

    public RenderEngine(int w, int h) {
        this.screenW = Math.max(1, w);
        this.screenH = Math.max(1, h);

        this.layers = new RenderLayer[LAYER_COUNT];
        for (int i = 0; i < LAYER_COUNT; i++) {
            layers[i] = new RenderLayer(i, getLayerName(i), screenW, screenH);
        }
        this.layerPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        this.additivePaint = new Paint();
        this.additivePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        this.multiplyPaint = new Paint();
        this.multiplyPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        this.debugPaint = new Paint();
        this.debugPaint.setColor(0xFF00FF00);
        this.debugPaint.setTextSize(18f);
        this.debugPaint.setTypeface(Typeface.DEFAULT_BOLD);
        this.srcRect = new Rect();
        this.dstRect = new Rect();
        this.tempRectF = new RectF();
        this.sb = new StringBuilder(128);
        this.memSb = new StringBuilder(32);

        applyQualityDefaults();
        this.enableDebug = GameConfig.DEBUG;

        compositeBuffer = null;
        compositeCanvas = null;
    }

    private void applyQualityDefaults() {
        int q = GameConfig.currentQuality;
        if (q <= GameConfig.QUALITY_LOW) {
            enableShadows = false;
            enableLighting = false;
            enableParticles = true;
            enablePostProcess = false;
            maxLights = 4;
            maxParticles = 80;
        } else if (q == GameConfig.QUALITY_MEDIUM) {
            enableShadows = true;
            enableLighting = false;
            enableParticles = true;
            enablePostProcess = false;
            maxLights = 8;
            maxParticles = 160;
        } else {
            enableShadows = true;
            enableLighting = GameConfig.ENABLE_LIGHTING;
            enableParticles = true;
            enablePostProcess = GameConfig.ENABLE_POST_PROCESS;
            maxLights = 16;
            maxParticles = 300;
        }
    }

    private String getLayerName(int id) {
        switch (id) {
            case LAYER_BG: return "Background";
            case LAYER_SHADOW: return "Shadow";
            case LAYER_WORLD: return "World";
            case LAYER_ENTITY: return "Entity";
            case LAYER_EFFECT: return "Effect";
            case LAYER_LIGHT: return "Light";
            case LAYER_UI: return "UI";
            default: return "Unknown";
        }
    }

    private void ensureComposite() {
        if (compositeBuffer != null && !compositeBuffer.isRecycled()) return;
        try {
            compositeBuffer = Bitmap.createBitmap(screenW, screenH, Bitmap.Config.ARGB_8888);
            compositeCanvas = new Canvas(compositeBuffer);
        } catch (OutOfMemoryError e) {
            compositeBuffer = null;
            compositeCanvas = null;
            enablePostProcess = false;
        }
    }

    private void releaseComposite() {
        if (compositeBuffer != null && !compositeBuffer.isRecycled()) {
            compositeBuffer.recycle();
        }
        compositeBuffer = null;
        compositeCanvas = null;
    }

    public void resize(int w, int h) {
        w = Math.max(1, w);
        h = Math.max(1, h);
        if (w == screenW && h == screenH) return;
        this.screenW = w;
        this.screenH = h;
        for (int i = 0; i < LAYER_COUNT; i++) {
            layers[i].resize(w, h);
        }
        if (compositeBuffer != null) {
            ensureComposite();
        }
    }

    public Canvas getLayerCanvas(int layerId) {
        if (layerId < 0 || layerId >= LAYER_COUNT) return null;
        RenderLayer l = layers[layerId];
        if (!l.isReady()) return null;
        l.dirty = true;
        return l.canvas;
    }

    public void clearLayer(int layerId) {
        if (layerId < 0 || layerId >= LAYER_COUNT) return;
        layers[layerId].clear();
    }

    public void clearAllLayers() {
        for (int i = 0; i < LAYER_COUNT; i++) {
            if (!isLayerEnabled(i)) continue;
            layers[i].clear();
        }
    }

    private boolean isLayerEnabled(int layerId) {
        switch (layerId) {
            case LAYER_SHADOW: return enableShadows;
            case LAYER_LIGHT: return enableLighting;
            case LAYER_EFFECT: return enableParticles;
            default: return true;
        }
    }

    public void beginFrame() {
        frameStartNs = System.nanoTime();
        statDrawCalls = 0;
        statEntities = 0;
        statParticles = 0;
        statLights = 0;
        statCulled = 0;
    }

    public void endFrame(Canvas target) {
        if (target == null) return;

        if (enablePostProcess) {
            ensureComposite();
        } else if (compositeBuffer != null) {
            releaseComposite();
        }

        if (enablePostProcess && compositeCanvas != null && compositeBuffer != null) {
            compositeCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            for (int i = 0; i < LAYER_COUNT; i++) {
                blitLayer(compositeCanvas, i);
            }
            srcRect.set(0, 0, screenW, screenH);
            dstRect.set(0, 0, screenW, screenH);
            target.drawBitmap(compositeBuffer, srcRect, dstRect, layerPaint);
            statDrawCalls++;
        } else {
            for (int i = 0; i < LAYER_COUNT; i++) {
                blitLayer(target, i);
            }
        }

        if (enableDebug) {
            renderDebug(target);
        }

        long endNs = System.nanoTime();
        statFrameTimeMs = (endNs - frameStartNs) / 1_000_000f;
    }

    private void blitLayer(Canvas target, int layerId) {
        RenderLayer l = layers[layerId];
        if (!isLayerEnabled(layerId)) return;
        if (!l.visible || !l.isReady()) return;
        Paint p;
        switch (l.blendMode) {
            case 1: p = additivePaint; break;
            case 2: p = multiplyPaint; break;
            default: p = null; break;
        }
        srcRect.set(0, 0, l.buffer.getWidth(), l.buffer.getHeight());
        dstRect.set(0, 0, screenW, screenH);
        target.drawBitmap(l.buffer, srcRect, dstRect, p);
        statDrawCalls++;
    }

    public void drawLayerWithParallax(Canvas target, int layerId, CameraSystem cam) {
        RenderLayer l = layers[layerId];
        if (!isLayerEnabled(layerId)) return;
        if (!l.visible || !l.isReady() || cam == null) return;
        float sx = cam.pos.x - screenW * 0.5f;
        float sy = cam.pos.y - screenH * 0.5f;
        l.drawTo(target, sx, sy, screenW, screenH, null);
        statDrawCalls++;
    }

    public boolean drawWorldRect(Canvas canvas, CameraSystem cam, float wx, float wy, float ww, float wh, Paint paint) {
        if (canvas == null || cam == null || paint == null) return false;
        float sx = cam.worldToScreenX(wx);
        float sy = cam.worldToScreenY(wy);
        float sw = ww * cam.zoom;
        float sh = wh * cam.zoom;
        if (sx + sw < 0 || sx > screenW || sy + sh < 0 || sy > screenH) {
            statCulled++;
            return false;
        }
        tempRectF.set(sx, sy, sx + sw, sy + sh);
        canvas.drawRect(tempRectF, paint);
        statDrawCalls++;
        statEntities++;
        return true;
    }

    public boolean drawWorldRoundRect(Canvas canvas, CameraSystem cam, float wx, float wy, float ww, float wh, float rx, float ry, Paint paint) {
        if (canvas == null || cam == null || paint == null) return false;
        float sx = cam.worldToScreenX(wx);
        float sy = cam.worldToScreenY(wy);
        float sw = ww * cam.zoom;
        float sh = wh * cam.zoom;
        if (sx + sw < 0 || sx > screenW || sy + sh < 0 || sy > screenH) {
            statCulled++;
            return false;
        }
        tempRectF.set(sx, sy, sx + sw, sy + sh);
        canvas.drawRoundRect(tempRectF, rx * cam.zoom, ry * cam.zoom, paint);
        statDrawCalls++;
        statEntities++;
        return true;
    }

    public boolean drawWorldCircle(Canvas canvas, CameraSystem cam, float wx, float wy, float radius, Paint paint) {
        if (canvas == null || cam == null || paint == null) return false;
        float sx = cam.worldToScreenX(wx);
        float sy = cam.worldToScreenY(wy);
        float sr = radius * cam.zoom;
        if (sx + sr < 0 || sx - sr > screenW || sy + sr < 0 || sy - sr > screenH) {
            statCulled++;
            return false;
        }
        canvas.drawCircle(sx, sy, sr, paint);
        statDrawCalls++;
        statEntities++;
        return true;
    }

    public boolean drawWorldBitmap(Canvas canvas, CameraSystem cam, Bitmap bmp, float wx, float wy, float anchorX, float anchorY, float scale, Paint paint) {
        if (canvas == null || cam == null || bmp == null || bmp.isRecycled()) return false;
        float sx = cam.worldToScreenX(wx);
        float sy = cam.worldToScreenY(wy);
        float w = bmp.getWidth() * scale * cam.zoom;
        float h = bmp.getHeight() * scale * cam.zoom;
        float x1 = sx - w * anchorX;
        float y1 = sy - h * anchorY;
        if (x1 + w < 0 || x1 > screenW || y1 + h < 0 || y1 > screenH) {
            statCulled++;
            return false;
        }
        srcRect.set(0, 0, bmp.getWidth(), bmp.getHeight());
        dstRect.set((int) x1, (int) y1, (int) (x1 + w), (int) (y1 + h));
        canvas.drawBitmap(bmp, srcRect, dstRect, paint);
        statDrawCalls++;
        statEntities++;
        return true;
    }

    public void renderDebug(Canvas canvas) {
        if (canvas == null) return;
        int x = 10;
        int y = 30;
        int lineH = 22;
        final int lineCount = 6;
        debugPaint.setColor(0x80000000);
        tempRectF.set(5, 5, 260, y + lineH * (lineCount - 1) + 5);
        canvas.drawRoundRect(tempRectF, 8, 8, debugPaint);
        debugPaint.setColor(0xFF00FF00);

        sb.setLength(0);
        sb.append("DrawCalls: ").append(statDrawCalls);
        canvas.drawText(sb.toString(), x, y, debugPaint); y += lineH;

        sb.setLength(0);
        sb.append("Entities: ").append(statEntities).append(" Culled: ").append(statCulled);
        canvas.drawText(sb.toString(), x, y, debugPaint); y += lineH;

        sb.setLength(0);
        sb.append("Particles: ").append(statParticles);
        canvas.drawText(sb.toString(), x, y, debugPaint); y += lineH;

        sb.setLength(0);
        sb.append("Lights: ").append(statLights);
        canvas.drawText(sb.toString(), x, y, debugPaint); y += lineH;

        sb.setLength(0);
        sb.append("FrameMs: ").append(String.format(java.util.Locale.US, "%.2f", statFrameTimeMs));
        canvas.drawText(sb.toString(), x, y, debugPaint); y += lineH;

        String memInfo = getMemoryInfo();
        sb.setLength(0);
        sb.append("Mem: ").append(memInfo);
        canvas.drawText(sb.toString(), x, y, debugPaint);
    }

    private String getMemoryInfo() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        long max = rt.maxMemory() / 1024 / 1024;
        memSb.setLength(0);
        memSb.append(used).append("/").append(max).append("MB");
        return memSb.toString();
    }

    public void setLayerVisible(int layerId, boolean visible) {
        if (layerId >= 0 && layerId < LAYER_COUNT) layers[layerId].visible = visible;
    }

    public void setLayerBlendMode(int layerId, int mode) {
        if (layerId >= 0 && layerId < LAYER_COUNT) layers[layerId].blendMode = mode;
    }

    public void setLayerParallax(int layerId, float px, float py) {
        if (layerId >= 0 && layerId < LAYER_COUNT) {
            layers[layerId].parallaxX = px;
            layers[layerId].parallaxY = py;
        }
    }

    public void setMaxLights(int count) {
        maxLights = Math.max(0, Math.min(count, 24));
    }

    public void setMaxParticles(int count) {
        maxParticles = Math.max(0, Math.min(count, 512));
    }

    public int getScreenW() { return screenW; }
    public int getScreenH() { return screenH; }

    public void shutdown() {
        for (int i = 0; i < LAYER_COUNT; i++) {
            layers[i].release();
        }
        releaseComposite();
    }
}
