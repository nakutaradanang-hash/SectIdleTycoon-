package com.sect.idle.systems;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.sect.idle.core.Vector2;

public final class TileMap {
    private final int[][] tiles;
    private final int width, height, tileSize;
    private TextureAtlas atlas;
    private final String[] tileNames;

    public TileMap(int w, int h, int tileSize) {
        this.width = w;
        this.height = h;
        this.tileSize = tileSize;
        this.tiles = new int[h][w];
        this.tileNames = new String[256];
    }

    public void setAtlas(TextureAtlas atlas) { this.atlas = atlas; }

    public void registerTile(int id, String name) {
        if (id >= 0 && id < tileNames.length) tileNames[id] = name;
    }

    public void setTile(int x, int y, int id) {
        if (x >= 0 && x < width && y >= 0 && y < height) tiles[y][x] = id;
    }

    public int getTile(int x, int y) {
        return (x >= 0 && x < width && y >= 0 && y < height) ? tiles[y][x] : 0;
    }

    public void fillRect(int x, int y, int w, int h, int id) {
        int x1 = Math.max(0, x);
        int y1 = Math.max(0, y);
        int x2 = Math.min(width, x + w);
        int y2 = Math.min(height, y + h);
        for (int yy = y1; yy < y2; yy++) {
            for (int xx = x1; xx < x2; xx++) {
                tiles[yy][xx] = id;
            }
        }
    }

    public void render(Canvas canvas, Vector2 camPos, float zoom, int screenW, int screenH, Paint paint) {
        if (atlas == null || camPos == null || canvas == null || zoom <= 0.001f) return;

        float worldTileSize = tileSize * zoom;
        float invZoom = 1f / zoom;

        int startX = (int)(camPos.x * invZoom / tileSize);
        int startY = (int)(camPos.y * invZoom / tileSize);
        int endX = startX + (int)(screenW * invZoom / tileSize) + 2;
        int endY = startY + (int)(screenH * invZoom / tileSize) + 2;

        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        endX = Math.min(width, endX);
        endY = Math.min(height, endY);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int tile = tiles[y][x];
                if (tile > 0 && tile < tileNames.length && tileNames[tile] != null) {
                    float px = (x * worldTileSize) - camPos.x;
                    float py = (y * worldTileSize) - camPos.y;
                    atlas.drawRegion(canvas, tileNames[tile], px, py, zoom, paint);
                }
            }
        }
    }

    public int getPixelWidth() { return width * tileSize; }
    public int getPixelHeight() { return height * tileSize; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getTileSize() { return tileSize; }
}
