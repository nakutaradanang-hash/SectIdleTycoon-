package com.sect.idle.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.sect.idle.core.GameState;
import com.sect.idle.render.RenderEngine;
import com.sect.idle.systems.CameraSystem;

/**
 * SceneManager v3.5 - Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class SceneManager {
    private final android.content.Context context;
    private GameState currentState;
    private GameState targetState;
    private GameState previousState;

    // Scenes
    private final SectScene sectScene;
    private final BattleScene battleScene;
    private final MenuScene menuScene;

    // Transition system
    private final Paint transitionPaint;
    private final Paint wipePaint;
    private float transitionAlpha = 0f;
    private float transitionProgress = 0f;
    private boolean transitioning = false;
    private int transitionType = 0; // 0=fade, 1=slide, 2=wipe
    private static final float TRANSITION_SPEED = 2.5f;

    // Scene stack (for modal overlays)
    private final GameState[] stateStack = new GameState[4];
    private int stackTop = -1;

    // Screen dimensions cache
    private int screenW = 720;
    private int screenH = 1280;

    // Performance
    private float renderTime = 0f;
    private final RectF tempRect;

    private final Canvas[] advancedLayerBridge = new Canvas[GameView.LAYER_COUNT];

    public SceneManager(android.content.Context ctx) {
        this.context = ctx;
        this.transitionPaint = new Paint();
        this.transitionPaint.setColor(0xFF000000);
        this.transitionPaint.setAntiAlias(true);
        this.wipePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.wipePaint.setColor(0xFF0A0A15);
        this.currentState = GameState.MENU;
        this.previousState = GameState.MENU;
        this.sectScene = new SectScene(ctx);
        this.battleScene = new BattleScene(ctx);
        this.menuScene = new MenuScene(ctx);
        this.tempRect = new RectF();
    }

    public void setAtlas(Bitmap atlas, Bitmap tileset, Bitmap parallax, Bitmap walkSheet, Bitmap battleSheet, Bitmap effectSheet) {
        if (menuScene != null) menuScene.setAtlas(atlas, parallax);
        if (sectScene != null) sectScene.setAtlas(atlas, tileset, parallax, walkSheet);
        if (battleScene != null) battleScene.setAtlas(atlas, battleSheet, effectSheet);
    }

    public GameState getCurrentState() { return currentState; }
    public GameState getState() { return currentState; }
    public GameState getPreviousState() { return previousState; }

    public void setState(GameState state) {
        if (transitioning || currentState == state) return;

        previousState = currentState;
        transitioning = true;
        targetState = state;
        transitionAlpha = 0f;
        transitionProgress = 0f;
        transitionType = (int)(System.currentTimeMillis() % 3);

        if (stackTop >= 0) {
            stackTop = -1;
            for (int i = 0; i < stateStack.length; i++) stateStack[i] = null;
        }

        if (state == GameState.BATTLE && battleScene != null) battleScene.reset();
        if (state == GameState.SECT && sectScene != null) sectScene.resetSelection();
    }

    public void pushState(GameState state) {
        if (stackTop < stateStack.length - 1 && !transitioning) {
            stateStack[++stackTop] = currentState;
        }
        setState(state);
    }

    public void popState() {
        if (transitioning) return;
        if (stackTop >= 0) {
            GameState s = stateStack[stackTop--];
            if (s != null) setState(s);
        } else if (previousState != null && previousState != currentState) {
            setState(previousState);
        }
    }

    public void update(float dt) {
        dt = Math.min(dt, 0.033f);

        if (transitioning) {
            transitionProgress += dt * TRANSITION_SPEED;
            if (transitionProgress >= 1f) {
                transitionProgress = 1f;
                currentState = targetState;
                transitioning = false;
                transitionAlpha = 0f;
            } else {
                float t = transitionProgress;
                transitionAlpha = t < 0.5f ? 2f * t * t : 1f - (float)Math.pow(-2f * t + 2f, 2) / 2f;
            }
        }

        switch (currentState) {
            case MENU: if (menuScene != null) menuScene.update(dt); break;
            case SECT: if (sectScene != null) sectScene.update(dt); break;
            case BATTLE: if (battleScene != null) battleScene.update(dt); break;
            default: break;
        }
    }

    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        if (canvas == null || paint == null || cam == null) return;
        screenW = canvas.getWidth();
        screenH = canvas.getHeight();
        if (screenW <= 0 || screenH <= 0) return;

        long start = System.nanoTime();

        switch (currentState) {
            case MENU: if (menuScene != null) menuScene.render(canvas, paint, cam); break;
            case SECT: if (sectScene != null) sectScene.render(canvas, paint, cam); break;
            case BATTLE: if (battleScene != null) battleScene.render(canvas, paint, cam); break;
            default:
                paint.setColor(0xFF000000);
                canvas.drawRect(0, 0, screenW, screenH, paint);
                break;
        }

        if (transitioning) {
            renderTransition(canvas);
        }

        renderTime = (System.nanoTime() - start) / 1_000_000f;
    }

    public void renderLayered(Canvas[] layers, Paint paint, CameraSystem cam, int w, int h) {
        if (layers == null || paint == null || cam == null) return;
        if (layers.length < GameView.LAYER_COUNT) return;
        screenW = w;
        screenH = h;
        if (screenW <= 0 || screenH <= 0) return;

        switch (currentState) {
            case MENU:
                for (int i = 0; i < GameView.LAYER_COUNT; i++) {
                    if (layers[i] != null) menuScene.render(layers[i], paint, cam);
                }
                break;
            case SECT:
                if (sectScene != null) sectScene.renderLayered(layers, paint, cam, w, h);
                break;
            case BATTLE:
                renderToBgLayer(layers, new SinglePassScene() {
                    public void render(Canvas c, Paint p, CameraSystem cm) {
                        if (battleScene != null) battleScene.render(c, p, cm);
                    }
                }, paint, cam);
                break;
            default:
                if (layers[GameView.LAYER_BG] != null) {
                    paint.setColor(0xFF000000);
                    layers[GameView.LAYER_BG].drawRect(0, 0, w, h, paint);
                }
                break;
        }

        if (transitioning && GameView.LAYER_UI < layers.length && layers[GameView.LAYER_UI] != null) {
            renderTransition(layers[GameView.LAYER_UI]);
        }
    }

    private interface SinglePassScene {
        void render(Canvas canvas, Paint paint, CameraSystem cam);
    }

    private void renderToBgLayer(Canvas[] layers, SinglePassScene scene, Paint paint, CameraSystem cam) {
        if (layers == null || GameView.LAYER_BG >= layers.length) return;
        Canvas bg = layers[GameView.LAYER_BG];
        if (bg != null && scene != null) scene.render(bg, paint, cam);
    }

    public void renderAdvanced(RenderEngine engine, Paint paint, CameraSystem cam) {
        if (engine == null || paint == null || cam == null) return;

        for (int i = 0; i < advancedLayerBridge.length; i++) advancedLayerBridge[i] = null;

        int w = engine.getScreenW();
        int h = engine.getScreenH();
        if (w <= 0 || h <= 0) return;

        Canvas bg = engine.getLayerCanvas(RenderEngine.LAYER_BG);
        advancedLayerBridge[GameView.LAYER_BG] = bg;
        advancedLayerBridge[GameView.LAYER_PARALLAX] = bg;
        advancedLayerBridge[GameView.LAYER_TILEMAP] = bg;
        advancedLayerBridge[GameView.LAYER_SHADOW] = engine.getLayerCanvas(RenderEngine.LAYER_SHADOW);
        advancedLayerBridge[GameView.LAYER_WORLD] = engine.getLayerCanvas(RenderEngine.LAYER_WORLD);
        advancedLayerBridge[GameView.LAYER_ENTITY] = engine.getLayerCanvas(RenderEngine.LAYER_ENTITY);
        advancedLayerBridge[GameView.LAYER_EFFECT] = engine.getLayerCanvas(RenderEngine.LAYER_EFFECT);
        advancedLayerBridge[GameView.LAYER_LIGHT] = engine.getLayerCanvas(RenderEngine.LAYER_LIGHT);
        advancedLayerBridge[GameView.LAYER_UI] = engine.getLayerCanvas(RenderEngine.LAYER_UI);

        renderLayered(advancedLayerBridge, paint, cam, w, h);
    }

    private void renderTransition(Canvas canvas) {
        if (canvas == null) return;
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        int alpha = (int)(transitionAlpha * 255);
        alpha = Math.max(0, Math.min(255, alpha));

        switch (transitionType) {
            case 0: // Fade
                transitionPaint.setAlpha(alpha);
                canvas.drawRect(0, 0, w, h, transitionPaint);
                break;
            case 1: // Slide
                float offset = (1f - transitionAlpha) * w;
                transitionPaint.setAlpha(255);
                if (targetState.ordinal() > currentState.ordinal()) {
                    canvas.drawRect(w - offset, 0, w, h, transitionPaint);
                } else {
                    canvas.drawRect(0, 0, offset, h, transitionPaint);
                }
                break;
            case 2: // Circle wipe
                float radius = (float)Math.sqrt(w*w + h*h) * 0.5f * transitionAlpha;
                wipePaint.setAlpha(alpha);
                canvas.drawCircle(w * 0.5f, h * 0.5f, radius, wipePaint);
                break;
        }
    }

    public void onTouchDown(float x, float y) {
        if (transitioning) return;
        switch (currentState) {
            case MENU: if (menuScene != null) menuScene.onTouchDown(x, y); break;
            case SECT: if (sectScene != null) sectScene.onTouchDown(x, y); break;
            case BATTLE: if (battleScene != null) battleScene.onTouchDown(x, y); break;
            default: break;
        }
    }

    public void onTouchMove(float x, float y) {
        if (transitioning) return;
        switch (currentState) {
            case SECT: if (sectScene != null) sectScene.onTouchMove(x, y); break;
            default: break;
        }
    }

    public void onTouchUp(float x, float y) {
        if (transitioning) return;
        switch (currentState) {
            case MENU: if (menuScene != null) menuScene.onTouchUp(x, y); break;
            case SECT: if (sectScene != null) sectScene.onTouchUp(x, y); break;
            case BATTLE: if (battleScene != null) battleScene.onTouchUp(x, y); break;
            default: break;
        }
    }

    public void onBackPressed() {
        if (transitioning) return;
        if (stackTop >= 0) {
            popState();
        } else if (currentState != GameState.MENU) {
            setState(GameState.MENU);
        }
    }

    public BattleScene getBattleScene() { return battleScene; }
    public SectScene getSectScene() { return sectScene; }
    public MenuScene getMenuScene() { return menuScene; }
    public boolean isTransitioning() { return transitioning; }
    public float getRenderTime() { return renderTime; }

    public void destroy() {
        if (sectScene != null) {
            sectScene.destroy();
        }
        if (battleScene != null) {
            battleScene.destroy();
        }
        if (menuScene != null) {
            menuScene.destroy();
        }
    }
}
