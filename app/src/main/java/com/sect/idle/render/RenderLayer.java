package com.sect.idle.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * RenderLayer v1.1 - Dedicated render buffer per layer.
 */
public final class RenderLayer {
    public final int id;
    public final String name;
    public Bitmap buffer;
    public Canvas canvas;
    public boolean dirty;
    public boolean visible;
    public float parallaxX;
    public float parallaxY;
    public int blendMode; // 0=normal, 1=additive, 2=multiply

    private final Rect srcRect;
    private final Rect dstRect;
    private final RectF dstRectF;
    private int lastW = 0;
    private int lastH = 0;

    public RenderLayer(int id, String name, int w, int h) {
        this.id = id;
        this.name = name;
        this.dirty = true;
        this.visible = true;
        this.parallaxX = 1.0f;
        this.parallaxY = 1.0f;
        this.blendMode = 0;
        this.srcRect = new Rect();
        this.dstRect = new Rect();
        this.dstRectF = new RectF();
        allocate(w, h);
    }

    private void allocate(int w, int h) {
        if (w <= 0 || h <= 0) return;
        try {
            Bitmap.Config cfg = (id == 0) ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888;
            buffer = Bitmap.createBitmap(w, h, cfg);
            canvas = new Canvas(buffer);
            lastW = w;
            lastH = h;
        } catch (OutOfMemoryError e) {
            buffer = null;
            canvas = null;
        }
    }

    public void resize(int w, int h) {
        if (w == lastW && h == lastH && isReady()) return;
        release();
        allocate(w, h);
        dirty = true;
    }

    public void clear() {
        if (canvas != null && buffer != null) {
            canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
            dirty = false;
        }
    }

    public void drawTo(Canvas target, int x, int y, Paint paint) {
        if (!visible || !isReady() || target == null) return;
        srcRect.set(0, 0, buffer.getWidth(), buffer.getHeight());
        dstRect.set(x, y, x + buffer.getWidth(), y + buffer.getHeight());
        target.drawBitmap(buffer, srcRect, dstRect, paint);
    }

    public void drawTo(Canvas target, float scrollX, float scrollY, int screenW, int screenH, Paint paint) {
        if (!visible || !isReady() || target == null) return;
        float bx = scrollX * parallaxX;
        float by = scrollY * parallaxY;
        int srcW = Math.min(screenW, buffer.getWidth());
        int srcH = Math.min(screenH, buffer.getHeight());
        srcRect.set(0, 0, srcW, srcH);
        dstRectF.set(-bx, -by, -bx + srcW, -by + srcH);
        target.drawBitmap(buffer, srcRect, dstRectF, paint);
    }

    public void release() {
        if (buffer != null && !buffer.isRecycled()) {
            buffer.recycle();
        }
        buffer = null;
        canvas = null;
        lastW = 0;
        lastH = 0;
    }

    public boolean isReady() {
        return buffer != null && !buffer.isRecycled() && canvas != null;
    }
}
