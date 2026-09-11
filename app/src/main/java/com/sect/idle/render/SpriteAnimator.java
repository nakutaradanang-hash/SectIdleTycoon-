package com.sect.idle.render;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.sect.idle.core.MathUtils;
import com.sect.idle.systems.CameraSystem;
import com.sect.idle.systems.SpriteSheet;

/**
 * SpriteAnimator v3.0 - State-driven animation controller.
 */
public final class SpriteAnimator {
    public static final int STATE_IDLE = 0, STATE_WALK = 1, STATE_ATTACK = 2;
    public static final int STATE_SKILL = 3, STATE_DEFEND = 4, STATE_DODGE = 5;
    public static final int STATE_HURT = 6, STATE_DIE = 7, STATE_COUNT = 8;
    
    public static final int DIR_DOWN = 0, DIR_DOWN_LEFT = 1, DIR_LEFT = 2;
    public static final int DIR_UP_LEFT = 3, DIR_UP = 4, DIR_UP_RIGHT = 5;
    public static final int DIR_RIGHT = 6, DIR_DOWN_RIGHT = 7;
    
    private SpriteSheet sheet;
    private final int[] stateStartFrame, stateFrameCount, statePriority;
    private final float[] stateFps;
    private final boolean[] stateLoop;
    
    private int currentState, currentDir, currentFrame;
    private float frameTimer, speedMultiplier;
    private boolean isPlaying, stateFinished;
    private int blendState = -1;
    private float blendAlpha = 0f;
    
    public float x, y, scale, pivotX, pivotY;
    public boolean flipX;
    public int colorTint;
    
    private final Rect srcRect;
    private final RectF dstRect;
    private final Paint paint;
    private AnimationListener listener;
    
    public interface AnimationListener {
        void onFrame(int state, int frame);
        void onComplete(int state);
        void onStateChange(int oldState, int newState);
    }
    
    public SpriteAnimator(SpriteSheet sheet) {
        this.sheet = sheet;
        this.stateStartFrame = new int[STATE_COUNT];
        this.stateFrameCount = new int[STATE_COUNT];
        this.stateFps = new float[STATE_COUNT];
        this.stateLoop = new boolean[STATE_COUNT];
        this.statePriority = new int[STATE_COUNT];
        this.srcRect = new Rect();
        this.dstRect = new RectF();
        this.paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        this.scale = 1.0f;
        this.pivotX = 0.5f;
        this.pivotY = 1.0f;
        this.colorTint = 0xFFFFFFFF;
        this.speedMultiplier = 1.0f;
        this.currentDir = DIR_DOWN;
        for (int i = 0; i < STATE_COUNT; i++) {
            stateStartFrame[i] = 0; stateFrameCount[i] = 1;
            stateFps[i] = 8f; stateLoop[i] = (i == STATE_IDLE || i == STATE_WALK);
            statePriority[i] = i;
        }
    }
    
    public void defineState(int state, int startFrame, int frameCount, float fps, boolean loop, int priority) {
        if (state < 0 || state >= STATE_COUNT) return;
        stateStartFrame[state] = startFrame;
        stateFrameCount[state] = Math.max(1, frameCount);
        stateFps[state] = fps;
        stateLoop[state] = loop;
        statePriority[state] = priority;
    }
    
    public void setListener(AnimationListener l) { this.listener = l; }
    
    public void play(int state) {
        if (state == currentState && isPlaying) return;
        if (statePriority[state] < statePriority[currentState] && !stateFinished && isPlaying) return;
        if (listener != null && currentState != state) listener.onStateChange(currentState, state);
        this.blendState = currentState;
        this.blendAlpha = 1.0f;
        this.currentState = state;
        this.currentFrame = 0;
        this.frameTimer = 0f;
        this.isPlaying = true;
        this.stateFinished = false;
    }
    
    public void update(float dt) {
        if (!isPlaying) return;
        if (sheet == null || !sheet.isValid()) {
            return;
        }
        
        frameTimer += dt * stateFps[currentState] * speedMultiplier;
        if (frameTimer >= 1f) {
            frameTimer -= 1f;
            int prevFrame = currentFrame;
            currentFrame++;
            if (currentFrame >= stateFrameCount[currentState]) {
                if (stateLoop[currentState]) currentFrame = 0;
                else {
                    currentFrame = stateFrameCount[currentState] - 1;
                    stateFinished = true; isPlaying = false;
                    if (listener != null) listener.onComplete(currentState);
                }
            }
            if (listener != null && prevFrame != currentFrame && !stateFinished) {
                listener.onFrame(currentState, currentFrame);
            }
        }
        
        if (blendAlpha > 0) blendAlpha = Math.max(0f, blendAlpha - dt * 10f);
    }
    
    public void render(Canvas canvas, CameraSystem cam) {
        if (canvas == null) return;
        
        if (sheet != null && sheet.isValid()) {
            int frameIndex = stateStartFrame[currentState] + currentFrame;
            frameIndex += currentDir * stateFrameCount[currentState];
            if (!sheet.getFrameRect(frameIndex, srcRect)) return;
            
            float sx, sy, w, h;
            if (cam != null) {
                sx = cam.worldToScreenX(x); sy = cam.worldToScreenY(y);
                w = srcRect.width() * scale * cam.zoom; h = srcRect.height() * scale * cam.zoom;
            } else {
                sx = x; sy = y; w = srcRect.width() * scale; h = srcRect.height() * scale;
            }
            
            float x1 = sx - w * pivotX, y1 = sy - h * pivotY;
            float x2 = x1 + w, y2 = y1 + h;
            if (flipX) { float tmp = x1; x1 = x2; x2 = tmp; }
            
            dstRect.set(x1, y1, x2, y2);
            paint.setColor(colorTint);
            canvas.drawBitmap(sheet.sheet, srcRect, dstRect, paint);
            
            if (blendAlpha > 0.01f && blendState >= 0) {
                int blendFrame = stateStartFrame[blendState];
                blendFrame += currentDir * stateFrameCount[blendState];
                if (sheet.getFrameRect(blendFrame, srcRect)) {
                    paint.setAlpha((int)(blendAlpha * 128));
                    canvas.drawBitmap(sheet.sheet, srcRect, dstRect, paint);
                    paint.setAlpha(255);
                }
            }
        } else {
            renderProceduralFallback(canvas, cam);
        }
    }
    
    private void renderProceduralFallback(Canvas canvas, CameraSystem cam) {
        float sx = cam != null ? cam.worldToScreenX(x) : x;
        float sy = cam != null ? cam.worldToScreenY(y) : y;
        float r = 16f * scale * (cam != null ? cam.zoom : 1f);
        paint.setColor(colorTint);
        canvas.drawCircle(sx, sy - r * 0.5f, r, paint);
        if (currentState == STATE_WALK) {
            float bob = MathUtils.sin(frameTimer * 6.28f) * r * 0.2f;
            canvas.drawCircle(sx, sy - r * 0.5f + bob, r * 0.3f, paint);
        }
    }
    
    public void renderScreenSpace(Canvas canvas) { render(canvas, null); }
    
    public void setDirection(float dx, float dy) {
        float angle = (float)Math.toDegrees(Math.atan2(-dy, dx));
        angle = (angle + 360) % 360;
        if (angle >= 337.5f || angle < 22.5f) currentDir = DIR_RIGHT;
        else if (angle < 67.5f) currentDir = DIR_DOWN_RIGHT;
        else if (angle < 112.5f) currentDir = DIR_DOWN;
        else if (angle < 157.5f) currentDir = DIR_DOWN_LEFT;
        else if (angle < 202.5f) currentDir = DIR_LEFT;
        else if (angle < 247.5f) currentDir = DIR_UP_LEFT;
        else if (angle < 292.5f) currentDir = DIR_UP;
        else currentDir = DIR_UP_RIGHT;
    }
    
    public boolean isStateFinished() { return stateFinished; }
    public int getCurrentState() { return currentState; }
    public int getCurrentFrame() { return currentFrame; }
    public boolean isPlaying() { return isPlaying; }
}
