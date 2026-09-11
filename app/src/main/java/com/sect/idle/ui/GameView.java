package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.sect.idle.R;
import com.sect.idle.core.GameConfig;
import com.sect.idle.core.GameState;
import com.sect.idle.core.MathUtils;
import com.sect.idle.core.Vector2;
import com.sect.idle.render.RenderEngine;
import com.sect.idle.systems.AssetManager;
import com.sect.idle.systems.CameraSystem;

/**
 * GameView v7.0 — Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class GameView extends SurfaceView implements SurfaceHolder.Callback, Choreographer.FrameCallback {

    public static final int LAYER_BG = 0, LAYER_PARALLAX = 1, LAYER_TILEMAP = 2, LAYER_SHADOW = 3;
    public static final int LAYER_WORLD = 4, LAYER_ENTITY = 5, LAYER_EFFECT = 6, LAYER_LIGHT = 7, LAYER_UI = 8;
    public static final int LAYER_COUNT = 9;

    private static final int TIER_FULL = 0, TIER_LITE = 1, TIER_DIRECT = 2;
    private int currentTier = TIER_FULL;

    private static final float FIXED_DT = 1f / 30f;
    private static final float MAX_DT = 0.05f;
    private static final int MAX_UPDATES_PER_FRAME = 2;
    private long lastFrameTimeNs, fpsTimerNs;
    private int frameCount, currentFps;
    private float accumulator;
    private volatile boolean running, surfaceReady, paused;

    private static final long QUALITY_WINDOW_NS = 4_000_000_000L;
    private static final int FPS_DOWNGRADE = 28;
    private static final int BAD_STREAK_TO_DOWNGRADE = 2;
    private long qualityTimerNs;
    private int badStreak;

    private final Paint paintSolid, paintAdd, paintDebugText, paintDebugBg;
    private final Rect rectSrc, rectDst;
    private final RectF rectF1;
    private final StringBuilder sbDebug;

    private CameraSystem camera;
    private SceneManager sceneManager;
    private com.sect.idle.gameplay.EffectManager effects;

    private RenderEngine renderEngine;
    private boolean advancedSubsystemReady;

    private Bitmap liteBuffer;
    private Canvas liteCanvas;

    private Bitmap atlasBitmap, tilesetBitmap, parallaxBitmap, sheetWalk, sheetBattle, sheetEffects;
    private volatile boolean assetsLoaded;

    private final Vector2 vTouchWorld, vTouchStart;
    private float lastTouchX, lastTouchY, pinchStartDist, pinchBaseZoom;
    private boolean isDragging, isPinching;
    private long lastTouchTimeMs, lastTapTimeMs, lastMoveTimeMs;
    private float lastTapX, lastTapY, flingVelX, flingVelY;

    private float shakeIntensity, shakeDecay, flashIntensity;
    private int flashColor;

    private int consecutiveCrashCount;
    private long firstCrashTimeMs;
    private static final int CRASH_THRESHOLD = 3;
    private static final long CRASH_WINDOW_MS = 10000;

    private long lastMemUpdateFrame, cachedUsedMemMB;

    private static final long TOUCH_THROTTLE_MS = 16;
    private static final float TOUCH_SLOP = 10f;
    private static final long DOUBLE_TAP_MS = 250;
    private static final float FLING_DECAY = 0.92f, FLING_MIN = 5f, FLING_MAX = 2000f;
    private static final boolean DBG = GameConfig.DEBUG;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        setKeepScreenOn(true);

        paintSolid = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paintSolid.setTypeface(Typeface.DEFAULT_BOLD);
        paintAdd = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintAdd.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));

        paintDebugText = new Paint();
        paintDebugText.setColor(0xFF00FF00);
        paintDebugText.setTextSize(18f);
        paintDebugText.setAntiAlias(true);
        paintDebugText.setTypeface(Typeface.DEFAULT_BOLD);

        paintDebugBg = new Paint();
        paintDebugBg.setColor(0x80000000);

        rectSrc = new Rect(); rectDst = new Rect(); rectF1 = new RectF();
        sbDebug = new StringBuilder(192);

        vTouchWorld = new Vector2(); vTouchStart = new Vector2();

        camera = new CameraSystem(720, 1280);
        camera.setBounds(0, 0, 4000, 4000);
        camera.pos.set(SectScene.MAP_CENTER_X, SectScene.MAP_CENTER_Y);
        camera.targetPos.set(SectScene.MAP_CENTER_X, SectScene.MAP_CENTER_Y);
        shakeDecay = GameConfig.CAM_SHAKE_DECAY;

        selectInitialTier();
        com.sect.idle.gameplay.GameplayFeedbackDispatcher.getInstance().init(context);
        loadAssetsAsync(context);
    }

    private void selectInitialTier() {
        int q = GameConfig.currentQuality;
        if (q <= GameConfig.QUALITY_LOW) currentTier = TIER_DIRECT;
        else if (q == GameConfig.QUALITY_MEDIUM) currentTier = TIER_LITE;
        else currentTier = TIER_FULL;
    }

    private void downgradeTier() {
        if (currentTier == TIER_FULL) {
            shutdownAdvancedSubsystem();
            currentTier = TIER_LITE;
            releaseLiteBuffer();
        } else if (currentTier == TIER_LITE) {
            releaseLiteBuffer();
            currentTier = TIER_DIRECT;
        }
    }

    private void forceDirectTier() {
        shutdownAdvancedSubsystem();
        releaseLiteBuffer();
        currentTier = TIER_DIRECT;
    }

    private void loadAssetsAsync(final Context ctx) {
        final AssetManager am = AssetManager.get(ctx);
        final int sampleSize = (GameConfig.deviceTier == GameConfig.TIER_LOW) ? 2 : 1;
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Load heavy backgrounds in RGB_565 to cut memory footprint by 50%
                    final Bitmap atlas = am.loadBitmapRes(R.drawable.atlas_game, 1024 / sampleSize, 1024 / sampleSize, Bitmap.Config.ARGB_8888);
                    final Bitmap tileset = am.loadBitmapRes(R.drawable.tileset_fantasy, 1024 / sampleSize, 1024 / sampleSize, Bitmap.Config.RGB_565);
                    final Bitmap parallax = am.loadBitmapRes(R.drawable.bg_parallax, 1024 / sampleSize, 1024 / sampleSize, Bitmap.Config.RGB_565);
                    final Bitmap walk = am.loadBitmapRes(R.drawable.spritesheet_walk, 512 / sampleSize, 512 / sampleSize, Bitmap.Config.ARGB_8888);
                    final Bitmap battle = am.loadBitmapRes(R.drawable.spritesheet_battle, 512 / sampleSize, 512 / sampleSize, Bitmap.Config.ARGB_8888);
                    final Bitmap effects = am.loadBitmapRes(R.drawable.spritesheet_effects, 512 / sampleSize, 512 / sampleSize, Bitmap.Config.ARGB_8888);

                    post(new Runnable() {
                        @Override
                        public void run() {
                            atlasBitmap = atlas;
                            tilesetBitmap = tileset;
                            parallaxBitmap = parallax;
                            sheetWalk = walk;
                            sheetBattle = battle;
                            sheetEffects = effects;
                            assetsLoaded = (atlasBitmap != null);
                            applyAssetsToScenes();
                        }
                    });
                } catch (OutOfMemoryError oom) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            handleOOM();
                        }
                    });
                } catch (Exception e) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            assetsLoaded = false;
                        }
                    });
                }
            }
        }, "GameAssetLoader").start();
    }

    private void loadAssetsSync(Context ctx) {
        AssetManager am = AssetManager.get(ctx);
        int sampleSize = (GameConfig.deviceTier == GameConfig.TIER_LOW) ? 2 : 1;
        try {
            atlasBitmap = am.loadBitmapRes(R.drawable.atlas_game, 1024 / sampleSize, 1024 / sampleSize, Bitmap.Config.ARGB_8888);
            tilesetBitmap = am.loadBitmapRes(R.drawable.tileset_fantasy, 1024 / sampleSize, 1024 / sampleSize, Bitmap.Config.RGB_565);
            parallaxBitmap = am.loadBitmapRes(R.drawable.bg_parallax, 1024 / sampleSize, 1024 / sampleSize, Bitmap.Config.RGB_565);
            sheetWalk = am.loadBitmapRes(R.drawable.spritesheet_walk, 512 / sampleSize, 512 / sampleSize, Bitmap.Config.ARGB_8888);
            sheetBattle = am.loadBitmapRes(R.drawable.spritesheet_battle, 512 / sampleSize, 512 / sampleSize, Bitmap.Config.ARGB_8888);
            sheetEffects = am.loadBitmapRes(R.drawable.spritesheet_effects, 512 / sampleSize, 512 / sampleSize, Bitmap.Config.ARGB_8888);
            assetsLoaded = (atlasBitmap != null);
        } catch (OutOfMemoryError oom) {
            handleOOM();
        } catch (Exception e) {
            assetsLoaded = false;
        }
        applyAssetsToScenes();
    }

    private Bitmap decodeChecked(Context ctx, int resId, BitmapFactory.Options opts) {
        try { return BitmapFactory.decodeResource(ctx.getResources(), resId, opts); }
        catch (OutOfMemoryError oom) { return null; }
        catch (Exception e) { return null; }
    }

    private void handleOOM() {
        recycleAllBitmaps();
        forceDirectTier();
        assetsLoaded = false;
    }

    private void recycleAllBitmaps() {
        recycleBmp(atlasBitmap); atlasBitmap = null;
        recycleBmp(tilesetBitmap); tilesetBitmap = null;
        recycleBmp(parallaxBitmap); parallaxBitmap = null;
        recycleBmp(sheetWalk); sheetWalk = null;
        recycleBmp(sheetBattle); sheetBattle = null;
        recycleBmp(sheetEffects); sheetEffects = null;
    }

    private void recycleBmp(Bitmap b) { if (b != null && !b.isRecycled()) b.recycle(); }

    public void setSceneManager(SceneManager sm) {
        this.sceneManager = sm;
        if (sm == null) return;
        if (sm.getMenuScene() != null) {
            sm.getMenuScene().setCamera(camera);
            if (renderEngine != null) sm.getMenuScene().setRenderEngine(renderEngine);
        }
        applyAssetsToScenes();
    }

    private void applyAssetsToScenes() {
        if (sceneManager == null) return;
        if (sceneManager.getSectScene() != null)
            sceneManager.getSectScene().setAtlas(atlasBitmap, tilesetBitmap, parallaxBitmap, sheetWalk);
        if (sceneManager.getBattleScene() != null)
            sceneManager.getBattleScene().setAtlas(atlasBitmap, sheetBattle, sheetEffects);
        if (sceneManager.getMenuScene() != null)
            sceneManager.getMenuScene().setAtlas(atlasBitmap, parallaxBitmap);
        if (assetsLoaded) {
            com.sect.idle.apm.StartupMetricsTracker.get().recordFullAssetsLoaded();
        }
    }

    public void setEffectManager(com.sect.idle.gameplay.EffectManager em) { this.effects = em; }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceReady = true;
        running = true;
        if (!paused) {
            lastFrameTimeNs = System.nanoTime();
            fpsTimerNs = lastFrameTimeNs;
            qualityTimerNs = lastFrameTimeNs;
            try {
                Choreographer.getInstance().removeFrameCallback(this);
                Choreographer.getInstance().postFrameCallback(this);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (w <= 0 || h <= 0) return;
        camera.setViewport(w, h);
        resizeSubsystems(w, h);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
        running = false;
        try {
            Choreographer.getInstance().removeFrameCallback(this);
        } catch (Exception ignored) {}
        releaseLiteBuffer();
        shutdownAdvancedSubsystem();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        surfaceReady = false;
        running = false;
        try {
            Choreographer.getInstance().removeFrameCallback(this);
        } catch (Exception ignored) {}
        releaseLiteBuffer();
        shutdownAdvancedSubsystem();
    }

    private void resizeSubsystems(int w, int h) {
        if (currentTier == TIER_FULL) {
            if (renderEngine != null) renderEngine.resize(w, h);
            else initAdvancedSubsystem(w, h);
        } else if (currentTier == TIER_LITE) {
            ensureLiteBuffer(w, h);
        }
    }

    private void initAdvancedSubsystem(int w, int h) {
        shutdownAdvancedSubsystem();
        try {
            renderEngine = new RenderEngine(w, h);
            advancedSubsystemReady = true;
            if (sceneManager != null && sceneManager.getMenuScene() != null) {
                sceneManager.getMenuScene().setRenderEngine(renderEngine);
            }
        } catch (OutOfMemoryError oom) {
            advancedSubsystemReady = false;
            downgradeTier();
        } catch (Exception e) {
            advancedSubsystemReady = false;
            downgradeTier();
        }
    }

    private void shutdownAdvancedSubsystem() {
        if (renderEngine != null) { renderEngine.shutdown(); renderEngine = null; }
        advancedSubsystemReady = false;
    }

    private void ensureLiteBuffer(int w, int h) {
        if (liteBuffer != null && liteBuffer.getWidth() == w && liteBuffer.getHeight() == h) return;
        releaseLiteBuffer();
        try {
            liteBuffer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            liteCanvas = new Canvas(liteBuffer);
        } catch (OutOfMemoryError oom) {
            liteBuffer = null; liteCanvas = null;
            downgradeTier();
        }
    }

    private void releaseLiteBuffer() {
        if (liteBuffer != null && !liteBuffer.isRecycled()) liteBuffer.recycle();
        liteBuffer = null; liteCanvas = null;
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!running || !surfaceReady || paused) {
            return;
        }
        try {
            long now = frameTimeNanos;
            float dt = Math.min((now - lastFrameTimeNs) / 1_000_000_000f, MAX_DT);
            lastFrameTimeNs = now;

            accumulator += dt;
            int updates = 0;
            while (accumulator >= FIXED_DT && updates < MAX_UPDATES_PER_FRAME) {
                update(FIXED_DT);
                accumulator -= FIXED_DT;
                updates++;
            }
            if (accumulator > FIXED_DT) accumulator = FIXED_DT;

            render();
            trackFpsAndQuality(now);
            consecutiveCrashCount = 0;
        } catch (Exception e) {
            handleRenderException(e);
        }
        if (running && surfaceReady && !paused) {
            try {
                Choreographer.getInstance().postFrameCallback(this);
            } catch (Exception ignored) {}
        }
    }

    private void handleRenderException(Exception e) {
        long now = System.currentTimeMillis();
        if (firstCrashTimeMs == 0 || now - firstCrashTimeMs > CRASH_WINDOW_MS) {
            firstCrashTimeMs = now; consecutiveCrashCount = 1;
        } else consecutiveCrashCount++;
        if (consecutiveCrashCount >= CRASH_THRESHOLD) forceDirectTier();
        else downgradeTier();
    }

    private void update(float dt) {
        if (camera != null) { applyFling(dt); camera.update(dt); }
        if (effects != null) effects.update(dt);
        if (sceneManager != null) sceneManager.update(dt);

        com.sect.idle.render.StateAnimationSystem sas = com.sect.idle.render.StateAnimationSystem.getInstance();
        sas.update(dt);
        if (sas.requestedShakeIntensity > 0.1f) {
            shakeIntensity = Math.max(shakeIntensity, sas.requestedShakeIntensity);
            sas.requestedShakeIntensity = 0f;
        }
        if (sas.requestedFlashIntensity > 0.01f) {
            flashIntensity = Math.max(flashIntensity, sas.requestedFlashIntensity);
            flashColor = sas.requestedFlashColor;
            sas.requestedFlashIntensity = 0f;
        }

        if (shakeIntensity > 0.001f) {
            shakeIntensity *= shakeDecay;
            if (shakeIntensity < 0.1f) shakeIntensity = 0f;
        }
        if (flashIntensity > 0.001f) {
            flashIntensity *= 0.85f;
            if (flashIntensity < 0.01f) flashIntensity = 0f;
        }
    }

    private void applyFling(float dt) {
        if (isDragging || isPinching) return;
        if (Math.abs(flingVelX) < FLING_MIN && Math.abs(flingVelY) < FLING_MIN) {
            flingVelX = 0f; flingVelY = 0f; return;
        }
        if (camera != null) {
            float invZoom = 1f / Math.max(0.01f, camera.zoom);
            camera.pos.x -= flingVelX * dt * invZoom;
            camera.pos.y -= flingVelY * dt * invZoom;
        }
        flingVelX *= FLING_DECAY; flingVelY *= FLING_DECAY;
    }

    private void render() {
        if (!surfaceReady || paused || !running) return;
        SurfaceHolder holder = getHolder();
        if (holder == null || holder.getSurface() == null || !holder.getSurface().isValid()) return;

        Canvas canvas = null; boolean locked = false;
        try {
            canvas = holder.lockCanvas();
            if (canvas == null) return;
            locked = true;

            if (!surfaceReady || paused || !running || holder.getSurface() == null || !holder.getSurface().isValid()) {
                return;
            }

            int w = canvas.getWidth(), h = canvas.getHeight();
            if (w <= 0 || h <= 0) return;

            int saveCount = -1;
            if (shakeIntensity > 0.5f) {
                saveCount = canvas.save();
                canvas.translate((float) (Math.random() * 2.0 - 1.0) * shakeIntensity,
                                  (float) (Math.random() * 2.0 - 1.0) * shakeIntensity);
            }

            switch (currentTier) {
                case TIER_FULL: renderTierFull(canvas, w, h); break;
                case TIER_LITE: renderTierLite(canvas, w, h); break;
                default: renderTierDirect(canvas, w, h); break;
            }

            if (saveCount >= 0) canvas.restoreToCount(saveCount);

            if (flashIntensity > 0.01f) {
                paintSolid.setColor(flashColor);
                paintSolid.setAlpha((int) (flashIntensity * 255));
                canvas.drawRect(0, 0, w, h, paintSolid);
                paintSolid.setAlpha(255);
            }
            if (DBG) renderDebug(canvas);
        } catch (Exception e) {
        } finally {
            if (locked && canvas != null) {
                try {
                    if (holder != null && holder.getSurface() != null && holder.getSurface().isValid()) {
                        holder.unlockCanvasAndPost(canvas);
                        com.sect.idle.apm.StartupMetricsTracker.get().recordFirstFrameDrawn();
                    }
                } catch (IllegalArgumentException | IllegalStateException ignored) {
                    // BufferQueueProducer disconnected during lifecycle teardown
                } catch (Exception e) {}
            }
        }
    }

    private void renderTierFull(Canvas canvas, int w, int h) {
        if (renderEngine == null) { downgradeTier(); renderTierDirect(canvas, w, h); return; }
        renderEngine.beginFrame();
        renderEngine.clearAllLayers();
        if (sceneManager != null) sceneManager.renderAdvanced(renderEngine, paintSolid, camera);
        if (effects != null) {
            Canvas fxCanvas = renderEngine.getLayerCanvas(RenderEngine.LAYER_EFFECT);
            if (fxCanvas != null) effects.render(fxCanvas, paintSolid);
        }
        renderEngine.endFrame(canvas);
        com.sect.idle.render.StateAnimationSystem.getInstance().render(canvas, camera, w, h);
    }

    private void renderTierLite(Canvas canvas, int w, int h) {
        if (liteCanvas == null || liteBuffer == null) { downgradeTier(); renderTierDirect(canvas, w, h); return; }
        liteBuffer.eraseColor(Color.TRANSPARENT);
        if (sceneManager != null) sceneManager.render(liteCanvas, paintSolid, camera);
        if (effects != null) effects.render(liteCanvas, paintSolid);
        rectSrc.set(0, 0, liteBuffer.getWidth(), liteBuffer.getHeight());
        rectDst.set(0, 0, w, h);
        canvas.drawBitmap(liteBuffer, rectSrc, rectDst, paintSolid);
        com.sect.idle.render.StateAnimationSystem.getInstance().render(canvas, camera, w, h);
    }

    private void renderTierDirect(Canvas canvas, int w, int h) {
        canvas.drawColor(0xFF0D0D15);
        if (sceneManager != null) sceneManager.render(canvas, paintSolid, camera);
        if (effects != null) effects.render(canvas, paintSolid);
        com.sect.idle.render.StateAnimationSystem.getInstance().render(canvas, camera, w, h);
    }

    private void renderDebug(Canvas canvas) {
        int pad = 10, lineH = 20, lines = 6, boxW = 300;
        int boxH = pad * 2 + lines * lineH;
        rectF1.set(pad, pad, boxW, boxH);
        canvas.drawRoundRect(rectF1, 6, 6, paintDebugBg);

        int x = pad + 8, y = pad + lineH;
        if ((frameCount - lastMemUpdateFrame) >= 30) {
            lastMemUpdateFrame = frameCount;
            cachedUsedMemMB = getUsedMemMB();
        }

        sbDebug.setLength(0);
        sbDebug.append("FPS:").append(currentFps).append(" TIER:").append(tierLabel());
        canvas.drawText(sbDebug.toString(), x, y, paintDebugText); y += lineH;

        sbDebug.setLength(0);
        sbDebug.append("State:").append(sceneManager != null ? sceneManager.getCurrentState() : "NULL");
        canvas.drawText(sbDebug.toString(), x, y, paintDebugText); y += lineH;

        sbDebug.setLength(0);
        sbDebug.append("FX:").append(effects != null ? effects.getActiveCount() : 0);
        canvas.drawText(sbDebug.toString(), x, y, paintDebugText); y += lineH;

        sbDebug.setLength(0);
        sbDebug.append("Assets:").append(assetsLoaded ? "OK" : "FAIL").append(" Mem:").append(cachedUsedMemMB).append("MB");
        canvas.drawText(sbDebug.toString(), x, y, paintDebugText); y += lineH;

        sbDebug.setLength(0);
        sbDebug.append("Cam:").append((int) camera.pos.x).append(",").append((int) camera.pos.y);
        canvas.drawText(sbDebug.toString(), x, y, paintDebugText); y += lineH;

        sbDebug.setLength(0);
        sbDebug.append("Size:").append(canvas.getWidth()).append("x").append(canvas.getHeight());
        canvas.drawText(sbDebug.toString(), x, y, paintDebugText);
    }

    private String tierLabel() {
        switch (currentTier) { case TIER_FULL: return "FULL"; case TIER_LITE: return "LITE"; default: return "DIRECT"; }
    }

    private long getUsedMemMB() {
        Runtime r = Runtime.getRuntime();
        return (r.totalMemory() - r.freeMemory()) / 1048576L;
    }

    private void trackFpsAndQuality(long nowNs) {
        frameCount++;
        if (nowNs - fpsTimerNs >= 1_000_000_000L) {
            currentFps = frameCount; frameCount = 0; fpsTimerNs = nowNs;
        }
        if (nowNs - qualityTimerNs < QUALITY_WINDOW_NS) return;
        qualityTimerNs = nowNs;

        if (currentFps > 0 && currentFps < FPS_DOWNGRADE) {
            badStreak++;
            if (badStreak >= BAD_STREAK_TO_DOWNGRADE && currentTier != TIER_DIRECT) {
                downgradeTier();
                badStreak = 0;
            }
        } else {
            badStreak = 0;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        long now = System.currentTimeMillis();
        int action = event.getActionMasked();

        // Rate limit only MOVE events so DOWN and UP taps are never dropped
        if (action == MotionEvent.ACTION_MOVE) {
            if (now - lastTouchTimeMs < TOUCH_THROTTLE_MS) return true;
            lastTouchTimeMs = now;
        }

        int pointers = event.getPointerCount();
        float x = event.getX(), y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN: return onTouchDown(x, y, now);
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointers == 2) {
                    isPinching = true; isDragging = false; flingVelX = 0f; flingVelY = 0f;
                    pinchStartDist = distance(event);
                    pinchBaseZoom = (camera != null) ? camera.targetZoom : 1f;
                }
                return true;
            case MotionEvent.ACTION_MOVE: return onTouchMove(event, x, y, pointers, now);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: return onTouchUp(x, y);
            case MotionEvent.ACTION_POINTER_UP:
                if (pointers == 2) { isPinching = false; flingVelX = 0f; flingVelY = 0f; }
                return true;
        }
        return true;
    }

    private boolean onTouchDown(float x, float y, long now) {
        screenToWorld(x, y, vTouchWorld);
        lastTouchX = x; lastTouchY = y; vTouchStart.set(x, y);
        isDragging = false; isPinching = false; flingVelX = 0f; flingVelY = 0f;
        lastMoveTimeMs = now;

        com.sect.idle.systems.HapticManager.get(getContext()).canvasTouch();

        if (now - lastTapTimeMs < DOUBLE_TAP_MS &&
            Math.abs(x - lastTapX) < TOUCH_SLOP && Math.abs(y - lastTapY) < TOUCH_SLOP) {
            onDoubleTap(vTouchWorld.x, vTouchWorld.y);
        } else if (sceneManager != null) {
            sceneManager.onTouchDown(x, y);
        }
        lastTapX = x; lastTapY = y; lastTapTimeMs = now;
        return true;
    }

    private boolean onTouchMove(MotionEvent event, float x, float y, int pointers, long now) {
        if (pointers == 2 && isPinching) {
            float dist = distance(event);
            if (pinchStartDist > 10f && camera != null) camera.setZoom(pinchBaseZoom * (dist / pinchStartDist));
        } else if (pointers == 1) {
            float dx = x - lastTouchX, dy = y - lastTouchY;
            if (!isDragging && (Math.abs(dx) > TOUCH_SLOP || Math.abs(dy) > TOUCH_SLOP)) isDragging = true;
            if (isDragging && camera != null) {
                float invZoom = 1f / Math.max(0.01f, camera.zoom);
                camera.pos.x -= dx * invZoom; camera.pos.y -= dy * invZoom;
                long dtMs = Math.max(1, now - lastMoveTimeMs);
                flingVelX = MathUtils.clamp((dx / dtMs) * 1000f, -FLING_MAX, FLING_MAX);
                flingVelY = MathUtils.clamp((dy / dtMs) * 1000f, -FLING_MAX, FLING_MAX);
                lastMoveTimeMs = now;
            }
            if (sceneManager != null) {
                sceneManager.onTouchMove(x, y);
            }
        }
        lastTouchX = x; lastTouchY = y;
        return true;
    }

    private boolean onTouchUp(float x, float y) {
        screenToWorld(x, y, vTouchWorld);
        if (!isDragging && !isPinching && sceneManager != null) {
            sceneManager.onTouchUp(x, y);
        }
        flingVelX = 0f; flingVelY = 0f;
        isDragging = false; isPinching = false;
        return true;
    }
    
    private void onDoubleTap(float worldX, float worldY) {
        if (sceneManager != null && sceneManager.getCurrentState() != GameState.MENU && camera != null) {
            camera.setZoom(1.0f);
        }
    }

    private float distance(MotionEvent e) {
        if (e.getPointerCount() < 2) return 0f;
        float dx = e.getX(0) - e.getX(1), dy = e.getY(0) - e.getY(1);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void screenToWorld(float sx, float sy, Vector2 out) {
        if (camera != null) out.set(camera.screenToWorldX(sx), camera.screenToWorldY(sy));
        else out.set(sx, sy);
    }

    public void triggerScreenShake(float intensity) { shakeIntensity = Math.min(intensity, 20f); }
    public void triggerFlash(int color, float intensity) { flashColor = color; flashIntensity = Math.min(intensity, 1f); }
    public CameraSystem getCamera() { return camera; }
    public com.sect.idle.gameplay.EffectManager getEffects() { return effects; }
    public RenderEngine getRenderEngine() { return renderEngine; }
    public int getFps() { return currentFps; }
    public boolean isUsingAdvancedRenderer() { return currentTier == TIER_FULL && advancedSubsystemReady; }
    public GameState getState() { return sceneManager != null ? sceneManager.getCurrentState() : GameState.MENU; }
    public void setPaused(boolean p) {
        this.paused = p;
        if (p) {
            try {
                Choreographer.getInstance().removeFrameCallback(this);
            } catch (Exception ignored) {}
        } else {
            if (surfaceReady && running) {
                lastFrameTimeNs = System.nanoTime();
                try {
                    Choreographer.getInstance().removeFrameCallback(this);
                    Choreographer.getInstance().postFrameCallback(this);
                } catch (Exception ignored) {}
            }
        }
    }
    public boolean isPaused() { return paused; }

    public Bitmap getAtlas() { return atlasBitmap; }
    public Bitmap getTileset() { return tilesetBitmap; }
    public Bitmap getParallax() { return parallaxBitmap; }
    public Bitmap getSpritesheetWalk() { return sheetWalk; }
    public Bitmap getSpritesheetBattle() { return sheetBattle; }
    public Bitmap getEffectSheet() { return sheetEffects; }

    public void shutdown() {
        running = false; surfaceReady = false;
        Choreographer.getInstance().removeFrameCallback(this);
        shutdownAdvancedSubsystem();
        releaseLiteBuffer();
        recycleAllBitmaps();
    }

    public void trimForLowMemory() {
        forceDirectTier();
        if (effects != null) effects.clear();
    }
}
