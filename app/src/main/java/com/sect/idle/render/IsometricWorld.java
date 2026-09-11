package com.sect.idle.render;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.sect.idle.systems.CameraSystem;

/**
 * IsometricWorld v1.0 - Isometric tilemap renderer with depth sorting.
 */
public final class IsometricWorld {
    private final int mapW, mapH;
    private final int tileW, tileH;
    private final int[][] tileData;
    private final int[][] heightData;
    private final int[] tileColors;
    private final String[] tileNames;

    private final Paint tilePaint;
    private final Paint heightPaint;
    private final Rect srcRect;
    private final RectF dstRect;

    private float camX, camY, camZoom;
    private int screenW, screenH;

    public IsometricWorld(int mapW, int mapH, int tileW, int tileH) {
        this.mapW = mapW;
        this.mapH = mapH;
        this.tileW = tileW;
        this.tileH = tileH;
        this.tileData = new int[mapH][mapW];
        this.heightData = new int[mapH][mapW];
        this.tileColors = new int[256];
        this.tileNames = new String[256];
        this.tilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.heightPaint = new Paint();
        this.srcRect = new Rect();
        this.dstRect = new RectF();

        tileColors[0] = 0xFF2D1B4E; // Void
        tileColors[1] = 0xFF3A2A60; // Ground
        tileColors[2] = 0xFF4CAF50; // Grass
        tileColors[3] = 0xFF795548; // Dirt
        tileColors[4] = 0xFF2196F3; // Water
        tileColors[5] = 0xFF607D8B; // Stone
    }

    public void setTileColor(int id, int color) {
        if (id >= 0 && id < 256) tileColors[id] = color;
    }

    public void setTile(int x, int y, int id) {
        if (x >= 0 && x < mapW && y >= 0 && y < mapH) tileData[y][x] = id;
    }

    public void setHeight(int x, int y, int h) {
        if (x >= 0 && x < mapW && y >= 0 && y < mapH) heightData[y][x] = h;
    }

    public int getTile(int x, int y) {
        if (x >= 0 && x < mapW && y >= 0 && y < mapH) return tileData[y][x];
        return 0;
    }

    public void fill(int id) {
        for (int y = 0; y < mapH; y++) {
            for (int x = 0; x < mapW; x++) {
                tileData[y][x] = id;
            }
        }
    }

    public void fillRect(int x, int y, int w, int h, int id) {
        int x1 = Math.max(0, x);
        int y1 = Math.max(0, y);
        int x2 = Math.min(mapW, x + w);
        int y2 = Math.min(mapH, y + h);
        for (int yy = y1; yy < y2; yy++) {
            for (int xx = x1; xx < x2; xx++) {
                tileData[yy][xx] = id;
            }
        }
    }

    public float isoToScreenX(float ix, float iy) {
        return (ix - iy) * tileW * 0.5f * camZoom;
    }

    public float isoToScreenY(float ix, float iy) {
        return (ix + iy) * tileH * 0.5f * camZoom;
    }

    public void render(Canvas canvas, CameraSystem cam, int screenW, int screenH) {
        if (canvas == null || cam == null) return;
        this.camX = cam.pos.x;
        this.camY = cam.pos.y;
        this.camZoom = cam.zoom;
        this.screenW = screenW;
        this.screenH = screenH;

        float halfSW = screenW * 0.5f / camZoom;
        float halfSH = screenH * 0.5f / camZoom;

        int startX = Math.max(0, (int)((camX - halfSW) / tileW) - 2);
        int startY = Math.max(0, (int)((camY - halfSH) / tileH) - 2);
        int endX = Math.min(mapW, (int)((camX + halfSW) / tileW) + 3);
        int endY = Math.min(mapH, (int)((camY + halfSH) / tileH) + 3);

        for (int sum = startX + startY; sum <= endX + endY; sum++) {
            for (int x = startX; x <= endX; x++) {
                int y = sum - x;
                if (y < startY || y > endY) continue;
                renderTile(canvas, x, y);
            }
        }
    }

    private void renderTile(Canvas canvas, int tx, int ty) {
        int tile = tileData[ty][tx];
        if (tile == 0) return;

        float wx = tx * tileW;
        float wy = ty * tileH;
        float sx = isoToScreenX(wx, wy) + screenW * 0.5f - camX * camZoom;
        float sy = isoToScreenY(wx, wy) + screenH * 0.5f - camY * camZoom;
        float h = heightData[ty][tx] * camZoom * 4f;

        float halfTW = tileW * camZoom * 0.5f;
        float halfTH = tileH * camZoom * 0.5f;
        if (sx + halfTW < 0 || sx - halfTW > screenW || sy + halfTH < 0 || sy - halfTH > screenH) return;

        int color = tileColors[tile];

        tilePaint.setColor(color);
        float top = sy - halfTH - h;
        float left = sx - halfTW;
        float right = sx + halfTW;
        float bottom = sy + halfTH - h;

        dstRect.set(left, top, right, bottom);
        canvas.drawOval(dstRect, tilePaint);

        if (heightData[ty][tx] > 0) {
            heightPaint.setColor(darkenColor(color, 0.7f));
            canvas.drawRect(left, bottom, right, bottom + h, heightPaint);
        }
    }

    private int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int)(((color >> 16) & 0xFF) * factor);
        int g = (int)(((color >> 8) & 0xFF) * factor);
        int b = (int)((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void renderObject(Canvas canvas, CameraSystem cam, float ix, float iy, float iw, float ih, int color, float objHeight) {
        if (canvas == null || cam == null) return;
        float sx = isoToScreenX(ix * tileW, iy * tileH) + screenW * 0.5f - camX * camZoom;
        float sy = isoToScreenY(ix * tileW, iy * tileH) + screenH * 0.5f - camY * camZoom;
        float w = iw * camZoom;
        float h = ih * camZoom;
        float oh = objHeight * camZoom;

        tilePaint.setColor(0x40000000);
        dstRect.set(sx - w * 0.5f, sy - h * 0.1f, sx + w * 0.5f, sy + h * 0.1f);
        canvas.drawOval(dstRect, tilePaint);

        tilePaint.setColor(color);
        dstRect.set(sx - w * 0.5f, sy - h - oh, sx + w * 0.5f, sy - oh);
        canvas.drawRect(dstRect, tilePaint);
    }
}
