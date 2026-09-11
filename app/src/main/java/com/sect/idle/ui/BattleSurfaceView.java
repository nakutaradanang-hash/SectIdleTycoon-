package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.sect.idle.core.MathUtils;
import com.sect.idle.gameplay.BattleEngine;
import com.sect.idle.gameplay.SectData;
import com.sect.idle.models.BattleUnit;
import com.sect.idle.models.Disciple;
import com.sect.idle.render.SpiritAuraBufferRenderer;
import com.sect.idle.systems.RNG;
import com.sect.idle.utils.ExceptionManager;
import java.util.ArrayList;

/**
 * BattleSurfaceView - High-Performance Double-Buffered SurfaceView for Cultivation Combat.
 *
 * Utilizes a dedicated rendering thread and double-buffered SurfaceHolder for drawing:
 * - Dynamic sword slashes with multi-layer glowing energy trails and spark physics.
 * - Multi-layer spiritual radiance and Qi vortex auras via SpiritAuraBufferRenderer.
 * - Zero Garbage Collection allocations during active frame loops.
 *
 * 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BattleSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    // Threading & Surface State
    private SurfaceHolder surfaceHolder;
    private Thread renderThread;
    private volatile boolean isRunning = false;
    private volatile boolean isPaused = false;
    private final Object pauseLock = new Object();

    // Engine & Data
    private BattleEngine battleEngine;
    private final SectData sectData;
    private final SpiritAuraBufferRenderer auraRenderer;

    // View Dimensions & Timing
    private int viewWidth = 720;
    private int viewHeight = 1280;
    private float animTime = 0f;
    private float simulationSpeed = 1.0f;
    private boolean autoBattle = true;
    private int selectedUnitIndex = -1;

    // Sword Slash System (Zero-allocation ring buffer)
    private static final int MAX_SLASH_POINTS = 32;
    private final float[] slashX = new float[MAX_SLASH_POINTS];
    private final float[] slashY = new float[MAX_SLASH_POINTS];
    private final float[] slashLife = new float[MAX_SLASH_POINTS];
    private final float[] slashWidth = new float[MAX_SLASH_POINTS];
    private int slashHead = 0;
    private int slashCount = 0;
    private boolean isSlashing = false;
    private float lastTouchX = 0f;
    private float lastTouchY = 0f;

    // Pre-allocated Slash Paths & Paints
    private final Path slashPathOuter;
    private final Path slashPathCore;
    private final Paint slashGlowPaint;
    private final Paint slashCorePaint;
    private final Paint slashSparkPaint;

    // Pre-allocated General Paints
    private final Paint bgPaint;
    private final Paint arenaGridPaint;
    private final Paint unitBodyPaint;
    private final Paint unitShadowPaint;
    private final Paint selectionPaint;
    private final Paint hpBgPaint;
    private final Paint hpFillPaint;
    private final Paint mpBgPaint;
    private final Paint mpFillPaint;
    private final Paint actionFillPaint;
    private final Paint textTitlePaint;
    private final Paint textValuePaint;
    private final Paint logBgPaint;
    private final Paint logTextPaint;
    private final Paint damageTextPaint;
    private final Paint buttonBgPaint;
    private final Paint buttonTextPaint;
    private final Paint flashPaint;

    // Geometry Pools
    private final RectF rectF1;
    private final RectF rectF2;
    private final Rect srcRect;
    private final Rect dstRect;
    private final StringBuilder stringBuilder;

    // Screen Shake & Flash FX
    private float shakeIntensity = 0f;
    private float flashIntensity = 0f;
    private int flashColor = 0xFFFFFFFF;

    // Particle Physics (Fixed primitive arrays)
    private static final int MAX_PARTICLES = 64;
    private final float[] partX = new float[MAX_PARTICLES];
    private final float[] partY = new float[MAX_PARTICLES];
    private final float[] partVX = new float[MAX_PARTICLES];
    private final float[] partVY = new float[MAX_PARTICLES];
    private final float[] partLife = new float[MAX_PARTICLES];
    private final float[] partMaxLife = new float[MAX_PARTICLES];
    private final float[] partSize = new float[MAX_PARTICLES];
    private final int[] partColor = new int[MAX_PARTICLES];
    private int activeParticles = 0;

    // Floating Numbers
    private static final int MAX_FLOATING_TEXTS = 16;
    private final String[] floatTexts = new String[MAX_FLOATING_TEXTS];
    private final float[] floatX = new float[MAX_FLOATING_TEXTS];
    private final float[] floatY = new float[MAX_FLOATING_TEXTS];
    private final float[] floatLife = new float[MAX_FLOATING_TEXTS];
    private final float[] floatMaxLife = new float[MAX_FLOATING_TEXTS];
    private final int[] floatColor = new int[MAX_FLOATING_TEXTS];
    private int activeFloatingTexts = 0;

    // UI Buttons
    private final RectF btnAutoRect;
    private final RectF btnSpeedRect;
    private final RectF btnPauseRect;

    // Combat Geometry
    private static final float UNIT_WIDTH = 76f;
    private static final float UNIT_HEIGHT = 76f;
    private static final float BAR_HEIGHT_HP = 7f;
    private static final float BAR_HEIGHT_ACTION = 4f;

    public BattleSurfaceView(Context context) {
        this(context, null);
    }

    public BattleSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BattleSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.sectData = SectData.getInstance();
        this.battleEngine = new BattleEngine();
        this.auraRenderer = new SpiritAuraBufferRenderer();

        // 1. SurfaceHolder Init
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);

        // 2. Geometry Pools
        this.rectF1 = new RectF();
        this.rectF2 = new RectF();
        this.srcRect = new Rect();
        this.dstRect = new Rect();
        this.stringBuilder = new StringBuilder(64);

        this.btnAutoRect = new RectF();
        this.btnSpeedRect = new RectF();
        this.btnPauseRect = new RectF();

        this.slashPathOuter = new Path();
        this.slashPathCore = new Path();

        // 3. Sword Slash Paints
        this.slashGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.slashGlowPaint.setStyle(Paint.Style.STROKE);
        this.slashGlowPaint.setStrokeCap(Paint.Cap.ROUND);
        this.slashGlowPaint.setStrokeJoin(Paint.Join.ROUND);

        this.slashCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.slashCorePaint.setStyle(Paint.Style.STROKE);
        this.slashCorePaint.setStrokeCap(Paint.Cap.ROUND);
        this.slashCorePaint.setStrokeJoin(Paint.Join.ROUND);
        this.slashCorePaint.setColor(0xFFFFFFFF);

        this.slashSparkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.slashSparkPaint.setStyle(Paint.Style.FILL);

        // 4. Standard Combat Paints
        this.bgPaint = new Paint();
        this.bgPaint.setColor(0xFF0D0E15);

        this.arenaGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.arenaGridPaint.setColor(0x1AFFFFFF);
        this.arenaGridPaint.setStyle(Paint.Style.STROKE);
        this.arenaGridPaint.setStrokeWidth(1.5f);

        this.unitBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.unitBodyPaint.setStyle(Paint.Style.FILL);

        this.unitShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.unitShadowPaint.setColor(0x60000000);
        this.unitShadowPaint.setStyle(Paint.Style.FILL);

        this.selectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.selectionPaint.setColor(0xFFFFD700);
        this.selectionPaint.setStyle(Paint.Style.STROKE);
        this.selectionPaint.setStrokeWidth(3f);

        this.hpBgPaint = new Paint();
        this.hpBgPaint.setColor(0xFF37474F);

        this.hpFillPaint = new Paint();
        this.hpFillPaint.setColor(0xFF4CAF50);

        this.mpBgPaint = new Paint();
        this.mpBgPaint.setColor(0xFF263238);

        this.mpFillPaint = new Paint();
        this.mpFillPaint.setColor(0xFF29B6F6);

        this.actionFillPaint = new Paint();
        this.actionFillPaint.setColor(0xFFFFD700);

        this.textTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textTitlePaint.setColor(0xFFFFFFFF);
        this.textTitlePaint.setTextSize(14f);
        this.textTitlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        this.textTitlePaint.setTextAlign(Paint.Align.CENTER);

        this.textValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textValuePaint.setColor(0xFFFFFFFF);
        this.textValuePaint.setTextSize(9f);
        this.textValuePaint.setTextAlign(Paint.Align.CENTER);

        this.logBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.logBgPaint.setColor(0xCC10121A);

        this.logTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.logTextPaint.setColor(0xFFECEFF1);
        this.logTextPaint.setTextSize(11f);

        this.damageTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.damageTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        this.damageTextPaint.setTextAlign(Paint.Align.CENTER);

        this.buttonBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.buttonBgPaint.setColor(0xFF263238);

        this.buttonTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.buttonTextPaint.setColor(0xFFFFFFFF);
        this.buttonTextPaint.setTextSize(12f);
        this.buttonTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        this.buttonTextPaint.setTextAlign(Paint.Align.CENTER);

        this.flashPaint = new Paint();

        initDefaultBattle();
    }

    public void initDefaultBattle() {
        ArrayList<Disciple> playerTeam = new ArrayList<Disciple>();
        if (sectData != null && !sectData.disciples.isEmpty()) {
            for (int i = 0; i < Math.min(3, sectData.disciples.size()); i++) {
                playerTeam.add(sectData.disciples.get(i));
            }
        } else {
            Disciple d1 = new Disciple();
            d1.name = "Immortal Han";
            d1.hp = 350; d1.maxHp = 350; d1.atk = 50; d1.def = 30; d1.element = 4;
            Disciple d2 = new Disciple();
            d2.name = "Fairy Nangong";
            d2.hp = 260; d2.maxHp = 260; d2.atk = 60; d2.def = 20; d2.element = 2;
            playerTeam.add(d1);
            playerTeam.add(d2);
        }

        ArrayList<Disciple> enemyTeam = new ArrayList<Disciple>();
        Disciple e1 = new Disciple();
        e1.name = "Nether Fiend General";
        e1.hp = 320; e1.maxHp = 320; e1.atk = 48; e1.def = 22; e1.element = 3;
        Disciple e2 = new Disciple();
        e2.name = "Nine Abyss Behemoth";
        e2.hp = 480; e2.maxHp = 480; e2.atk = 35; e2.def = 40; e2.element = 5;
        enemyTeam.add(e1);
        enemyTeam.add(e2);

        battleEngine.startBattle(playerTeam, enemyTeam);
    }

    public void setBattleEngine(BattleEngine engine) {
        if (engine != null) {
            this.battleEngine = engine;
        }
    }

    public BattleEngine getBattleEngine() {
        return battleEngine;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.surfaceHolder = holder;
        startRenderThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.surfaceHolder = holder;
        this.viewWidth = width > 0 ? width : 720;
        this.viewHeight = height > 0 ? height : 1280;

        if (auraRenderer != null) {
            auraRenderer.updateBufferSize(viewWidth, viewHeight);
        }

        float btnW = 90f;
        float btnH = 36f;
        float margin = 12f;
        float startY = viewHeight - btnH - margin;

        btnAutoRect.set(viewWidth - (btnW * 3f + margin * 3f), startY, viewWidth - (btnW * 2f + margin * 3f), startY + btnH);
        btnSpeedRect.set(viewWidth - (btnW * 2f + margin * 2f), startY, viewWidth - (btnW + margin * 2f), startY + btnH);
        btnPauseRect.set(viewWidth - (btnW + margin), startY, viewWidth - margin, startY + btnH);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopRenderThread();
        if (auraRenderer != null) {
            auraRenderer.releaseBuffer();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopRenderThread();
        if (auraRenderer != null) {
            auraRenderer.releaseBuffer();
        }
    }

    public synchronized void startRenderThread() {
        if (renderThread != null && renderThread.isAlive()) return;
        isRunning = true;
        isPaused = false;
        renderThread = new Thread(this, "BattleSurfaceView-RenderLoop");
        renderThread.setPriority(Thread.MAX_PRIORITY);
        renderThread.start();
    }

    public synchronized void stopRenderThread() {
        isRunning = false;
        if (renderThread != null) {
            renderThread.interrupt();
            try {
                renderThread.join(300);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            renderThread = null;
        }
    }

    public void pauseRender() {
        isPaused = true;
    }

    public void resumeRender() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final long targetFrameNanos = 16_666_666L; // ~60 FPS Target

        while (isRunning) {
            if (isPaused) {
                synchronized (pauseLock) {
                    while (isPaused && isRunning) {
                        try {
                            pauseLock.wait(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }

            long now = System.nanoTime();
            float dt = (now - lastTime) / 1_000_000_000.0f;
            if (dt > 0.1f) dt = 0.1f;
            lastTime = now;

            updatePhysicsAndCombat(dt);
            renderFrame();

            long elapsed = System.nanoTime() - now;
            long sleepNanos = targetFrameNanos - elapsed;
            if (sleepNanos > 1_000_000L) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void updatePhysicsAndCombat(float dt) {
        if (!isPaused && battleEngine != null && battleEngine.isRunning) {
            float simDt = dt * simulationSpeed;
            animTime += simDt;
            battleEngine.tick();

            // Random hit effects on active combat turns
            if (RNG.chance(12)) {
                spawnHitSparks(viewWidth * 0.5f + RNG.nextFloat(-80f, 80f), viewHeight * 0.38f + RNG.nextFloat(-60f, 60f), 0xFFFFD700);
            }
        } else {
            animTime += dt;
        }

        // Sword Slash Trail decay
        if (slashCount > 0) {
            for (int i = 0; i < slashCount; i++) {
                int idx = (slashHead - slashCount + i + MAX_SLASH_POINTS) % MAX_SLASH_POINTS;
                slashLife[idx] -= dt * 3.5f;
            }
            while (slashCount > 0) {
                int tailIdx = (slashHead - slashCount + MAX_SLASH_POINTS) % MAX_SLASH_POINTS;
                if (slashLife[tailIdx] <= 0f) {
                    slashCount--;
                } else {
                    break;
                }
            }
        }

        // Decay screen shake & flash
        if (shakeIntensity > 0.05f) shakeIntensity *= 0.86f;
        else shakeIntensity = 0f;

        if (flashIntensity > 0.01f) flashIntensity *= 0.85f;
        else flashIntensity = 0f;

        // Update particles & floating text
        updateParticles(dt);
        updateFloatingNumbers(dt);
    }

    private void updateParticles(float dt) {
        for (int i = activeParticles - 1; i >= 0; i--) {
            partLife[i] -= dt;
            if (partLife[i] <= 0f) {
                int last = activeParticles - 1;
                if (i != last) {
                    partX[i] = partX[last];
                    partY[i] = partY[last];
                    partVX[i] = partVX[last];
                    partVY[i] = partVY[last];
                    partLife[i] = partLife[last];
                    partMaxLife[i] = partMaxLife[last];
                    partSize[i] = partSize[last];
                    partColor[i] = partColor[last];
                }
                activeParticles--;
                continue;
            }
            partX[i] += partVX[i] * dt;
            partY[i] += partVY[i] * dt;
            partVY[i] += 25f * dt;
        }
    }

    private void updateFloatingNumbers(float dt) {
        for (int i = activeFloatingTexts - 1; i >= 0; i--) {
            floatLife[i] -= dt;
            floatY[i] -= 32f * dt;
            if (floatLife[i] <= 0f) {
                int last = activeFloatingTexts - 1;
                if (i != last) {
                    floatTexts[i] = floatTexts[last];
                    floatX[i] = floatX[last];
                    floatY[i] = floatY[last];
                    floatLife[i] = floatLife[last];
                    floatMaxLife[i] = floatMaxLife[last];
                    floatColor[i] = floatColor[last];
                }
                activeFloatingTexts--;
            }
        }
    }

    private void renderFrame() {
        if (!isRunning || isPaused || surfaceHolder == null || surfaceHolder.getSurface() == null || !surfaceHolder.getSurface().isValid()) return;

        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas();
            if (canvas == null) return;

            if (!isRunning || isPaused || surfaceHolder.getSurface() == null || !surfaceHolder.getSurface().isValid()) {
                return;
            }

            canvas.save();

            // 1. Shake Transform
            if (shakeIntensity > 0.05f) {
                float sx = (RNG.nextFloat() * 2f - 1f) * shakeIntensity;
                float sy = (RNG.nextFloat() * 2f - 1f) * shakeIntensity;
                canvas.translate(sx, sy);
            }

            // 2. Draw Arena Background
            canvas.drawRect(0, 0, viewWidth, viewHeight, bgPaint);
            drawArenaGeometry(canvas);

            if (battleEngine != null) {
                ArrayList<BattleUnit> units = battleEngine.getUnits();

                // 3. Render Spirit Aura Buffer Effects
                if (auraRenderer != null) {
                    auraRenderer.renderSpiritAuras(canvas, units, animTime, viewWidth, viewHeight);
                }

                // 4. Render Units
                renderCombatUnits(canvas, units);

                // 5. Render Sword Slashes (Glowing ribbons)
                renderSwordSlashes(canvas);

                // 6. Particles & Floating Texts
                renderParticles(canvas);
                renderDamageTexts(canvas);

                // 7. Combat Logs
                renderCombatLogs(canvas);
            }

            // 8. Interactive Controls
            renderControlButtons(canvas);

            // 9. Screen Flash
            if (flashIntensity > 0.02f) {
                flashPaint.setColor(flashColor);
                flashPaint.setAlpha((int) (flashIntensity * 180));
                canvas.drawRect(0, 0, viewWidth, viewHeight, flashPaint);
            }

            canvas.restore();

        } catch (Throwable t) {
            ExceptionManager.get().reportException(t, "BattleSurfaceView", "Error in renderFrame", ExceptionManager.LEVEL_ERROR);
        } finally {
            if (canvas != null) {
                try {
                    if (surfaceHolder != null && surfaceHolder.getSurface() != null && surfaceHolder.getSurface().isValid()) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                } catch (IllegalArgumentException | IllegalStateException ignored) {
                    // Surface disconnected during teardown
                } catch (Throwable ignored) {}
            }
        }
    }

    private void drawArenaGeometry(Canvas canvas) {
        float cx = viewWidth * 0.5f;
        float cy = viewHeight * 0.38f;
        canvas.drawCircle(cx, cy, 70f, arenaGridPaint);
        canvas.drawCircle(cx, cy, 140f, arenaGridPaint);
        canvas.drawCircle(cx, cy, 210f, arenaGridPaint);
    }

    private void renderCombatUnits(Canvas canvas, ArrayList<BattleUnit> units) {
        if (units == null || units.isEmpty()) return;
        int size = units.size();
        float cy = viewHeight * 0.38f;
        float rowSpacing = UNIT_HEIGHT + 35f;

        int playerIdx = 0;
        int enemyIdx = 0;

        for (int i = 0; i < size; i++) {
            BattleUnit u = units.get(i);
            if (u == null) continue;

            boolean isPlayer = (u.team == 0);
            float posX = isPlayer ? (viewWidth * 0.22f) : (viewWidth * 0.78f);
            int sideIndex = isPlayer ? playerIdx++ : enemyIdx++;
            float posY = cy + (sideIndex - 0.8f) * rowSpacing;

            u.pos.set(posX, posY);

            // Shadow
            rectF1.set(posX - UNIT_WIDTH * 0.4f, posY + UNIT_HEIGHT * 0.35f, posX + UNIT_WIDTH * 0.4f, posY + UNIT_HEIGHT * 0.5f);
            canvas.drawOval(rectF1, unitShadowPaint);

            // Unit Body
            int bodyColor = !u.isAlive ? 0xFF616161 : (isPlayer ? 0xFF1E88E5 : 0xFFE53935);
            unitBodyPaint.setColor(bodyColor);
            rectF1.set(posX - UNIT_WIDTH * 0.5f, posY - UNIT_HEIGHT * 0.5f, posX + UNIT_WIDTH * 0.5f, posY + UNIT_HEIGHT * 0.5f);
            canvas.drawRoundRect(rectF1, 10f, 10f, unitBodyPaint);

            // Selection
            if (i == selectedUnitIndex) {
                rectF2.set(rectF1.left - 4f, rectF1.top - 4f, rectF1.right + 4f, rectF1.bottom + 4f);
                canvas.drawRoundRect(rectF2, 14f, 14f, selectionPaint);
            }

            // Name
            canvas.drawText(u.name, posX, posY - UNIT_HEIGHT * 0.55f, textTitlePaint);

            // HP Bar
            float barW = UNIT_WIDTH;
            float barY = posY + UNIT_HEIGHT * 0.55f;
            rectF1.set(posX - barW * 0.5f, barY, posX + barW * 0.5f, barY + BAR_HEIGHT_HP);
            canvas.drawRect(rectF1, hpBgPaint);

            float hpRatio = u.maxHp > 0 ? (float) u.hp / (float) u.maxHp : 0f;
            if (hpRatio < 0f) hpRatio = 0f;
            if (hpRatio > 1f) hpRatio = 1f;

            hpFillPaint.setColor(hpRatio < 0.3f ? 0xFFFF5722 : 0xFF4CAF50);
            rectF1.set(posX - barW * 0.5f, barY, posX - barW * 0.5f + (barW * hpRatio), barY + BAR_HEIGHT_HP);
            canvas.drawRect(rectF1, hpFillPaint);

            stringBuilder.setLength(0);
            stringBuilder.append(u.hp).append("/").append(u.maxHp);
            canvas.drawText(stringBuilder.toString(), posX, barY + BAR_HEIGHT_HP - 1f, textValuePaint);

            // Action Bar
            barY += BAR_HEIGHT_HP + 3f;
            rectF1.set(posX - barW * 0.5f, barY, posX + barW * 0.5f, barY + BAR_HEIGHT_ACTION);
            canvas.drawRect(rectF1, mpBgPaint);

            float actionRatio = u.maxActionBar > 0 ? (float) u.actionBar / (float) u.maxActionBar : 0f;
            if (actionRatio > 1f) actionRatio = 1f;
            rectF1.set(posX - barW * 0.5f, barY, posX - barW * 0.5f + (barW * actionRatio), barY + BAR_HEIGHT_ACTION);
            canvas.drawRect(rectF1, actionFillPaint);
        }
    }

    private void renderSwordSlashes(Canvas canvas) {
        if (slashCount < 2) return;

        slashPathOuter.rewind();
        slashPathCore.rewind();

        int startIdx = (slashHead - slashCount + MAX_SLASH_POINTS) % MAX_SLASH_POINTS;
        slashPathOuter.moveTo(slashX[startIdx], slashY[startIdx]);
        slashPathCore.moveTo(slashX[startIdx], slashY[startIdx]);

        for (int i = 1; i < slashCount; i++) {
            int prev = (slashHead - slashCount + i - 1 + MAX_SLASH_POINTS) % MAX_SLASH_POINTS;
            int curr = (slashHead - slashCount + i + MAX_SLASH_POINTS) % MAX_SLASH_POINTS;

            float midX = (slashX[prev] + slashX[curr]) * 0.5f;
            float midY = (slashY[prev] + slashY[curr]) * 0.5f;

            slashPathOuter.quadTo(slashX[prev], slashY[prev], midX, midY);
            slashPathCore.quadTo(slashX[prev], slashY[prev], midX, midY);
        }

        // Outer Ethereal Blue Glow
        slashGlowPaint.setColor(0xFF00E5FF);
        slashGlowPaint.setStrokeWidth(14f);
        slashGlowPaint.setAlpha(180);
        canvas.drawPath(slashPathOuter, slashGlowPaint);

        // Core Sharp White Blade Line
        slashCorePaint.setStrokeWidth(4f);
        slashCorePaint.setAlpha(255);
        canvas.drawPath(slashPathCore, slashCorePaint);
    }

    private void renderParticles(Canvas canvas) {
        for (int i = 0; i < activeParticles; i++) {
            float progress = partLife[i] / partMaxLife[i];
            slashSparkPaint.setColor(partColor[i]);
            slashSparkPaint.setAlpha((int) (255 * progress));
            canvas.drawCircle(partX[i], partY[i], partSize[i] * progress, slashSparkPaint);
        }
    }

    private void renderDamageTexts(Canvas canvas) {
        for (int i = 0; i < activeFloatingTexts; i++) {
            float progress = floatLife[i] / floatMaxLife[i];
            damageTextPaint.setColor(floatColor[i]);
            damageTextPaint.setAlpha((int) (255 * progress));
            damageTextPaint.setTextSize(16f * (0.8f + progress * 0.4f));
            if (floatTexts[i] != null) {
                canvas.drawText(floatTexts[i], floatX[i], floatY[i], damageTextPaint);
            }
        }
    }

    private void renderCombatLogs(Canvas canvas) {
        float logW = viewWidth - 32f;
        float logH = 160f;
        float logX = 16f;
        float logY = viewHeight - logH - 60f;

        rectF1.set(logX, logY, logX + logW, logY + logH);
        canvas.drawRoundRect(rectF1, 8f, 8f, logBgPaint);

        if (battleEngine != null) {
            ArrayList<String> logs = battleEngine.getLog();
            int lineCount = Math.min(6, logs.size());
            int startIdx = Math.max(0, logs.size() - 6);

            for (int i = 0; i < lineCount; i++) {
                String line = logs.get(startIdx + i);
                if (line != null) {
                    canvas.drawText(line, logX + 12f, logY + 22f + (i * 22f), logTextPaint);
                }
            }
        }
    }

    private void renderControlButtons(Canvas canvas) {
        buttonBgPaint.setColor(autoBattle ? 0xFF2E7D32 : 0xFF455A64);
        canvas.drawRoundRect(btnAutoRect, 6f, 6f, buttonBgPaint);
        canvas.drawText(autoBattle ? "AUTO: ON" : "AUTO: OFF", btnAutoRect.centerX(), btnAutoRect.centerY() + 4f, buttonTextPaint);

        buttonBgPaint.setColor(0xFF1565C0);
        canvas.drawRoundRect(btnSpeedRect, 6f, 6f, buttonBgPaint);
        stringBuilder.setLength(0);
        stringBuilder.append((int) simulationSpeed).append("X SPEED");
        canvas.drawText(stringBuilder.toString(), btnSpeedRect.centerX(), btnSpeedRect.centerY() + 4f, buttonTextPaint);

        buttonBgPaint.setColor(isPaused ? 0xFFC62828 : 0xFF37474F);
        canvas.drawRoundRect(btnPauseRect, 6f, 6f, buttonBgPaint);
        canvas.drawText(isPaused ? "PLAY" : "PAUSE", btnPauseRect.centerX(), btnPauseRect.centerY() + 4f, buttonTextPaint);
    }

    public void addSlashPoint(float x, float y) {
        int idx = slashHead;
        slashX[idx] = x;
        slashY[idx] = y;
        slashLife[idx] = 1.0f;
        slashWidth[idx] = 12f;
        slashHead = (slashHead + 1) % MAX_SLASH_POINTS;
        if (slashCount < MAX_SLASH_POINTS) slashCount++;

        spawnHitSparks(x, y, 0xFF00E5FF);
    }

    public void spawnHitSparks(float x, float y, int color) {
        if (activeParticles >= MAX_PARTICLES) return;
        for (int i = 0; i < 4 && activeParticles < MAX_PARTICLES; i++) {
            int idx = activeParticles++;
            partX[idx] = x;
            partY[idx] = y;
            float angle = RNG.nextFloat(0f, 6.28318f);
            float spd = RNG.nextFloat(50f, 140f);
            partVX[idx] = MathUtils.cos(angle) * spd;
            partVY[idx] = MathUtils.sin(angle) * spd;
            float l = RNG.nextFloat(0.2f, 0.6f);
            partLife[idx] = l;
            partMaxLife[idx] = l;
            partSize[idx] = RNG.nextFloat(2.5f, 5f);
            partColor[idx] = color;
        }
    }

    public void spawnFloatingDamage(float x, float y, int amount, boolean isCrit) {
        if (activeFloatingTexts >= MAX_FLOATING_TEXTS) return;
        int idx = activeFloatingTexts++;
        floatTexts[idx] = isCrit ? ("CRIT -" + amount) : ("-" + amount);
        floatX[idx] = x;
        floatY[idx] = y;
        floatLife[idx] = 1.2f;
        floatMaxLife[idx] = 1.2f;
        floatColor[idx] = isCrit ? 0xFFFFD700 : 0xFFFF5252;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float tx = event.getX();
        float ty = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (btnAutoRect.contains(tx, ty)) {
                    autoBattle = !autoBattle;
                    com.sect.idle.systems.HapticManager.get(getContext()).tap();
                    return true;
                }
                if (btnSpeedRect.contains(tx, ty)) {
                    if (simulationSpeed == 1.0f) simulationSpeed = 2.0f;
                    else if (simulationSpeed == 2.0f) simulationSpeed = 4.0f;
                    else simulationSpeed = 1.0f;
                    com.sect.idle.systems.HapticManager.get(getContext()).tap();
                    return true;
                }
                if (btnPauseRect.contains(tx, ty)) {
                    isPaused = !isPaused;
                    com.sect.idle.systems.HapticManager.get(getContext()).tap();
                    return true;
                }

                // Check unit selection
                if (battleEngine != null) {
                    ArrayList<BattleUnit> units = battleEngine.getUnits();
                    for (int i = 0; i < units.size(); i++) {
                        BattleUnit u = units.get(i);
                        if (u != null && Math.abs(u.pos.x - tx) < UNIT_WIDTH * 0.6f && Math.abs(u.pos.y - ty) < UNIT_HEIGHT * 0.6f) {
                            selectedUnitIndex = i;
                            spawnHitSparks(u.pos.x, u.pos.y, 0xFF00E5FF);
                            com.sect.idle.systems.HapticManager.get(getContext()).canvasTouch();
                            return true;
                        }
                    }
                }

                isSlashing = true;
                lastTouchX = tx;
                lastTouchY = ty;
                addSlashPoint(tx, ty);
                com.sect.idle.systems.HapticManager.get(getContext()).swordSlash();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isSlashing) {
                    float distSq = (tx - lastTouchX) * (tx - lastTouchX) + (ty - lastTouchY) * (ty - lastTouchY);
                    if (distSq > 144f) { // Min 12px drag distance
                        addSlashPoint(tx, ty);
                        lastTouchX = tx;
                        lastTouchY = ty;
                        com.sect.idle.systems.HapticManager.get(getContext()).swordSlash();

                        // Check if slash hit any enemy unit
                        if (battleEngine != null) {
                            ArrayList<BattleUnit> units = battleEngine.getUnits();
                            for (int i = 0; i < units.size(); i++) {
                                BattleUnit u = units.get(i);
                                if (u != null && u.team == 1 && u.isAlive &&
                                        Math.abs(u.pos.x - tx) < UNIT_WIDTH * 0.6f &&
                                        Math.abs(u.pos.y - ty) < UNIT_HEIGHT * 0.6f) {
                                    u.hp = Math.max(0, u.hp - 15);
                                    spawnFloatingDamage(u.pos.x, u.pos.y, 15, true);
                                    shakeIntensity = 5f;
                                    flashIntensity = 0.2f;
                                    com.sect.idle.systems.HapticManager.get(getContext()).impact();
                                }
                            }
                        }
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isSlashing = false;
                return true;
        }
        return super.onTouchEvent(event);
    }
}
