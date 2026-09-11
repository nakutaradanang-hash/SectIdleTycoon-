package com.sect.idle.systems;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import java.util.HashMap;

public final class TextureAtlas {
    private Bitmap atlas;
    private final HashMap<String, Rect> regions;
    private final RectF dstRect;
    private final Rect tempSrc;

    public TextureAtlas(Bitmap atlas) {
        this.atlas = atlas;
        this.regions = new HashMap<String, Rect>(64);
        this.dstRect = new RectF();
        this.tempSrc = new Rect();
    }

    public void defineRegion(String name, int x, int y, int w, int h) {
        if (name == null || w <= 0 || h <= 0) return;
        regions.put(name, new Rect(x, y, x + w, y + h));
    }

    public void drawRegion(Canvas canvas, String name, float x, float y, float scale, Paint paint) {
        Rect src = regions.get(name);
        if (src == null || atlas == null || atlas.isRecycled() || canvas == null) return;
        float w = src.width() * scale;
        float h = src.height() * scale;
        dstRect.set(x, y, x + w, y + h);
        canvas.drawBitmap(atlas, src, dstRect, paint);
    }

    public void drawRegion(Canvas canvas, String name, float x, float y, float w, float h, Paint paint) {
        Rect src = regions.get(name);
        if (src == null || atlas == null || atlas.isRecycled() || canvas == null) return;
        dstRect.set(x, y, x + w, y + h);
        canvas.drawBitmap(atlas, src, dstRect, paint);
    }

    public void drawRegionCentered(Canvas canvas, String name, float cx, float cy, float scale, Paint paint) {
        Rect src = regions.get(name);
        if (src == null || atlas == null || atlas.isRecycled() || canvas == null) return;
        float w = src.width() * scale;
        float h = src.height() * scale;
        dstRect.set(cx - w * 0.5f, cy - h * 0.5f, cx + w * 0.5f, cy + h * 0.5f);
        canvas.drawBitmap(atlas, src, dstRect, paint);
    }

    public boolean getRegionRect(String name, Rect out) {
        Rect src = regions.get(name);
        if (src == null || out == null) return false;
        out.set(src);
        return true;
    }

    public int getRegionWidth(String name) {
        Rect src = regions.get(name);
        return src != null ? src.width() : 0;
    }

    public int getRegionHeight(String name) {
        Rect src = regions.get(name);
        return src != null ? src.height() : 0;
    }

    public void recycle() {
        if (atlas != null && !atlas.isRecycled()) atlas.recycle();
        atlas = null;
        regions.clear();
    }

    public boolean hasRegion(String name) {
        return regions.containsKey(name);
    }

    public int getRegionCount() {
        return regions.size();
    }
}
