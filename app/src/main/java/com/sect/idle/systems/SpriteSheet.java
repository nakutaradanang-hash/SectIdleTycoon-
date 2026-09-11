package com.sect.idle.systems;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public final class SpriteSheet {
    private static final String TAG = "SpriteSheet";

    public Bitmap sheet;
    private final int frameWidth, frameHeight;
    private final int marginX, marginY;
    private final int spacingX, spacingY;
    private final int cols, rows, totalFrames;
    private final boolean valid;

    private final Rect srcRect = new Rect();
    private final RectF dstRect = new RectF();

    public SpriteSheet(Bitmap sheet, int fw, int fh) {
        this(sheet, fw, fh, 0, 0, 0, 0);
    }

    public SpriteSheet(Bitmap sheet, int fw, int fh, int marginX, int marginY, int spacingX, int spacingY) {
        this.sheet = sheet;
        this.frameWidth = fw;
        this.frameHeight = fh;
        this.marginX = marginX;
        this.marginY = marginY;
        this.spacingX = spacingX;
        this.spacingY = spacingY;

        if (sheet == null || sheet.isRecycled() || fw <= 0 || fh <= 0) {
            Log.w(TAG, "Invalid sheet or frame size");
            this.cols = 0; this.rows = 0; this.totalFrames = 0; this.valid = false;
            return;
        }

        int usableW = sheet.getWidth() - marginX * 2 + spacingX;
        int usableH = sheet.getHeight() - marginY * 2 + spacingY;
        this.cols = Math.max(0, usableW / (fw + spacingX));
        this.rows = Math.max(0, usableH / (fh + spacingY));
        this.totalFrames = cols * rows;
        this.valid = totalFrames > 0;
    }

    public void drawFrame(Canvas canvas, int frameIndex, float x, float y, float scale, Paint paint, boolean flipX, boolean flipY) {
        if (!valid || sheet == null || sheet.isRecycled() || frameIndex < 0 || frameIndex >= totalFrames || canvas == null) return;

        int col = frameIndex % cols;
        int row = frameIndex / cols;
        int left = marginX + col * (frameWidth + spacingX);
        int top = marginY + row * (frameHeight + spacingY);
        srcRect.set(left, top, left + frameWidth, top + frameHeight);

        float w = frameWidth * scale;
        float h = frameHeight * scale;
        float right = flipX ? x - w : x + w;
        float bottom = flipY ? y - h : y + h;
        dstRect.set(Math.min(x, right), Math.min(y, bottom), Math.max(x, right), Math.max(y, bottom));

        canvas.drawBitmap(sheet, srcRect, dstRect, paint);
    }

    public void drawFrame(Canvas canvas, int frameIndex, float x, float y, float scale, Paint paint, boolean flipX) {
        drawFrame(canvas, frameIndex, x, y, scale, paint, flipX, false);
    }

    public void drawFrame(Canvas canvas, int frameIndex, float x, float y, float scale, Paint paint) {
        drawFrame(canvas, frameIndex, x, y, scale, paint, false, false);
    }

    public boolean getFrameRect(int frameIndex, Rect out) {
        if (!valid || frameIndex < 0 || frameIndex >= totalFrames || out == null) return false;
        int col = frameIndex % cols;
        int row = frameIndex / cols;
        int left = marginX + col * (frameWidth + spacingX);
        int top = marginY + row * (frameHeight + spacingY);
        out.set(left, top, left + frameWidth, top + frameHeight);
        return true;
    }

    public int getFrameIndex(int row, int col) {
        return row * cols + col;
    }

    public void detachBitmap() {
        this.sheet = null;
    }

    public boolean isValid() { return valid; }
    public int getTotalFrames() { return totalFrames; }
    public int getFrameWidth() { return frameWidth; }
    public int getFrameHeight() { return frameHeight; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }
}
