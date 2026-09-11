package com.sect.idle.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import com.sect.idle.core.MathUtils;
import com.sect.idle.systems.CameraSystem;
import com.sect.idle.systems.RNG;

/**
 * StateAnimationSystem - High-performance state-based UI & Canvas animation system.
 * Manages complex martial visual effects (Critical Strike, Tournament Clashes, War Conquests, Breakthroughs)
 * with zero-allocation pooling in render loops.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class StateAnimationSystem {
    private static volatile StateAnimationSystem instance;

    public static StateAnimationSystem getInstance() {
        if (instance == null) {
            synchronized (StateAnimationSystem.class) {
                if (instance == null) {
                    instance = new StateAnimationSystem();
                }
            }
        }
        return instance;
    }

    public static final int FX_CRITICAL_STRIKE = 1;
    public static final int FX_TOURNAMENT_CLASH = 2;
    public static final int FX_WAR_CONQUEST = 3;
    public static final int FX_BREAKTHROUGH = 4;
    public static final int FX_STAT_SURGE = 5;

    // States per effect
    public static final int STATE_INACTIVE = 0;
    public static final int STATE_ENTER = 1;
    public static final int STATE_PEAK = 2;
    public static final int STATE_HOLD = 3;
    public static final int STATE_EXIT = 4;

    private static final int MAX_EFFECTS = 16;
    private static final int MAX_SLASH_LINES = 8;
    private static final int MAX_SPARKS = 120;

    // Pooled effect properties
    private final int[] fxType = new int[MAX_EFFECTS];
    private final int[] fxState = new int[MAX_EFFECTS];
    private final float[] fxX = new float[MAX_EFFECTS];
    private final float[] fxY = new float[MAX_EFFECTS];
    private final float[] fxTimer = new float[MAX_EFFECTS];
    private final float[] fxDuration = new float[MAX_EFFECTS];
    private final float[] fxScale = new float[MAX_EFFECTS];
    private final float[] fxAlpha = new float[MAX_EFFECTS];
    private final int[] fxColor = new int[MAX_EFFECTS];
    private final int[] fxSecondaryColor = new int[MAX_EFFECTS];
    private final String[] fxTitle = new String[MAX_EFFECTS];
    private final String[] fxSubtitle = new String[MAX_EFFECTS];
    private final String[] fxDetail = new String[MAX_EFFECTS];
    private final boolean[] fxIsScreenSpace = new boolean[MAX_EFFECTS];
    private int activeFxCount = 0;

    // Slash lines
    private final float[] slashStartX = new float[MAX_SLASH_LINES];
    private final float[] slashStartY = new float[MAX_SLASH_LINES];
    private final float[] slashEndX = new float[MAX_SLASH_LINES];
    private final float[] slashEndY = new float[MAX_SLASH_LINES];
    private final float[] slashLife = new float[MAX_SLASH_LINES];
    private final float[] slashMaxLife = new float[MAX_SLASH_LINES];
    private final int[] slashColor = new int[MAX_SLASH_LINES];
    private int activeSlashCount = 0;

    // Particle Sparks
    private final float[] spX = new float[MAX_SPARKS];
    private final float[] spY = new float[MAX_SPARKS];
    private final float[] spVX = new float[MAX_SPARKS];
    private final float[] spVY = new float[MAX_SPARKS];
    private final float[] spLife = new float[MAX_SPARKS];
    private final float[] spMaxLife = new float[MAX_SPARKS];
    private final float[] spSize = new float[MAX_SPARKS];
    private final int[] spColor = new int[MAX_SPARKS];
    private int activeSparkCount = 0;

    // Screen Shake & Flash request output
    public float requestedShakeIntensity = 0f;
    public float requestedFlashIntensity = 0f;
    public int requestedFlashColor = 0xFFFFFFFF;

    // Pre-allocated Paints & Buffers
    private final Paint bannerPaint;
    private final Paint bannerBorderPaint;
    private final Paint glowPaint;
    private final Paint textTitlePaint;
    private final Paint textSubPaint;
    private final Paint textDetailPaint;
    private final Paint slashPaint;
    private final Paint sparkPaint;
    private final Paint ringPaint;
    private final RectF rectPool1;
    private final RectF rectPool2;
    private final Path pathPool;

    public StateAnimationSystem() {
        bannerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bannerPaint.setStyle(Paint.Style.FILL);

        bannerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bannerBorderPaint.setStyle(Paint.Style.STROKE);
        bannerBorderPaint.setStrokeWidth(3f);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);

        textTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textTitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textTitlePaint.setTextAlign(Paint.Align.CENTER);

        textSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textSubPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textSubPaint.setTextAlign(Paint.Align.CENTER);

        textDetailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textDetailPaint.setTextAlign(Paint.Align.CENTER);

        slashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slashPaint.setStyle(Paint.Style.STROKE);
        slashPaint.setStrokeCap(Paint.Cap.ROUND);

        sparkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sparkPaint.setStyle(Paint.Style.FILL);

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);

        rectPool1 = new RectF();
        rectPool2 = new RectF();
        pathPool = new Path();
    }

    public void triggerCriticalStrike(float x, float y, String actorName, String targetName, int damage, int repGain, boolean screenCenter) {
        int idx = allocEffect();
        if (idx < 0) return;

        fxType[idx] = FX_CRITICAL_STRIKE;
        fxState[idx] = STATE_ENTER;
        fxX[idx] = x;
        fxY[idx] = y;
        fxTimer[idx] = 0f;
        fxDuration[idx] = 1.8f;
        fxScale[idx] = 0.2f;
        fxAlpha[idx] = 1f;
        fxColor[idx] = 0xFFFF1744; // Crimson Red
        fxSecondaryColor[idx] = 0xFFFFD700; // Gold
        fxTitle[idx] = "CRITICAL STRIKE!";
        fxSubtitle[idx] = (actorName != null ? actorName : "Cultivator") + " strikes " + (targetName != null ? targetName : "Rival");
        fxDetail[idx] = "✦ " + damage + " CRITICAL DMG | +" + repGain + " REP ✦";
        fxIsScreenSpace[idx] = screenCenter;

        // Screen Impact feedback
        requestedShakeIntensity = Math.max(requestedShakeIntensity, 12f);
        requestedFlashIntensity = Math.max(requestedFlashIntensity, 0.4f);
        requestedFlashColor = 0xFFFFEE55;

        // Spawn crossing critical slash arcs
        float span = 180f;
        spawnSlash(x - span, y - span * 0.6f, x + span, y + span * 0.6f, 0xFFFF1744, 0.5f);
        spawnSlash(x - span * 0.8f, y + span * 0.7f, x + span * 0.8f, y - span * 0.7f, 0xFFFFD700, 0.55f);

        // Spawn burst of golden critical sparks
        spawnSparkBurst(x, y, 0xFFFFD700, 35, 120f, 320f);
        spawnSparkBurst(x, y, 0xFFFF1744, 20, 80f, 220f);
    }

    public void triggerTournamentClash(float x, float y, String discipleName, String opponentName, boolean won, int round, int prizeStones) {
        int idx = allocEffect();
        if (idx < 0) return;

        fxType[idx] = FX_TOURNAMENT_CLASH;
        fxState[idx] = STATE_ENTER;
        fxX[idx] = x;
        fxY[idx] = y;
        fxTimer[idx] = 0f;
        fxDuration[idx] = 2.2f;
        fxScale[idx] = 0.3f;
        fxAlpha[idx] = 1f;
        fxColor[idx] = won ? 0xFF00E5FF : 0xFFFF5252;
        fxSecondaryColor[idx] = won ? 0xFFFFD700 : 0xFF757575;
        fxTitle[idx] = won ? "TOURNAMENT VICTORY!" : "TOURNAMENT CLASH";
        fxSubtitle[idx] = "Round " + round + ": " + discipleName + " vs " + opponentName;
        fxDetail[idx] = won ? "Prize: +" + prizeStones + " Spirit Stones & Fame!" : "Eliminated in intense duel!";
        fxIsScreenSpace[idx] = true;

        requestedShakeIntensity = Math.max(requestedShakeIntensity, won ? 9f : 6f);
        requestedFlashIntensity = Math.max(requestedFlashIntensity, won ? 0.35f : 0.2f);
        requestedFlashColor = won ? 0xFF00E5FF : 0xFFFF5252;

        spawnSlash(x - 220f, y, x + 220f, y, won ? 0xFF00E5FF : 0xFFFF5252, 0.6f);
        spawnSparkBurst(x, y, won ? 0xFFFFD700 : 0xFFB0BEC5, 30, 90f, 260f);
    }

    public void triggerWarConquest(String targetSect, boolean won, int repChange, int stones, int jade) {
        int idx = allocEffect();
        if (idx < 0) return;

        fxType[idx] = FX_WAR_CONQUEST;
        fxState[idx] = STATE_ENTER;
        fxX[idx] = 0f;
        fxY[idx] = 0f;
        fxTimer[idx] = 0f;
        fxDuration[idx] = 2.8f;
        fxScale[idx] = 0.2f;
        fxAlpha[idx] = 1f;
        fxColor[idx] = won ? 0xFFFFD700 : 0xFFE53935;
        fxSecondaryColor[idx] = won ? 0xFFFF1744 : 0xFF212121;
        fxTitle[idx] = won ? "SECT CONQUEST CRUSHING VICTORY!" : "WAR CAMPAIGN RETREAT";
        fxSubtitle[idx] = "Battle against " + targetSect;
        fxDetail[idx] = won ? "Plundered: +" + stones + " Stones, +" + jade + " Jade, " + (repChange >= 0 ? "+" : "") + repChange + " Rep"
                            : "Forces withdrew to heal casualties (" + repChange + " Rep)";
        fxIsScreenSpace[idx] = true;

        requestedShakeIntensity = Math.max(requestedShakeIntensity, 14f);
        requestedFlashIntensity = Math.max(requestedFlashIntensity, 0.45f);
        requestedFlashColor = won ? 0xFFFFD700 : 0xFFB71C1C;

        spawnSparkBurst(fxX[idx], fxY[idx], won ? 0xFFFFD700 : 0xFFFF5252, 45, 140f, 380f);
    }

    public void triggerBreakthrough(float x, float y, String discipleName, String newRealm) {
        int idx = allocEffect();
        if (idx < 0) return;

        fxType[idx] = FX_BREAKTHROUGH;
        fxState[idx] = STATE_ENTER;
        fxX[idx] = x;
        fxY[idx] = y;
        fxTimer[idx] = 0f;
        fxDuration[idx] = 2.4f;
        fxScale[idx] = 0.2f;
        fxAlpha[idx] = 1f;
        fxColor[idx] = 0xFFFFD700;
        fxSecondaryColor[idx] = 0xFF7C4DFF;
        fxTitle[idx] = "REALM BREAKTHROUGH!";
        fxSubtitle[idx] = discipleName + " Ascended to " + newRealm;
        fxDetail[idx] = "✦ Soul Awakened & Power Multiplied ✦";
        fxIsScreenSpace[idx] = false;

        requestedShakeIntensity = Math.max(requestedShakeIntensity, 10f);
        requestedFlashIntensity = Math.max(requestedFlashIntensity, 0.35f);
        requestedFlashColor = 0xFFFFD700;

        spawnSparkBurst(x, y, 0xFFFFD700, 40, 100f, 280f);
        spawnSparkBurst(x, y, 0xFF00E5FF, 20, 60f, 180f);
    }

    public void triggerStatSurge(float x, float y, String text, int color) {
        int idx = allocEffect();
        if (idx < 0) return;

        fxType[idx] = FX_STAT_SURGE;
        fxState[idx] = STATE_ENTER;
        fxX[idx] = x;
        fxY[idx] = y;
        fxTimer[idx] = 0f;
        fxDuration[idx] = 1.4f;
        fxScale[idx] = 0.5f;
        fxAlpha[idx] = 1f;
        fxColor[idx] = color;
        fxSecondaryColor[idx] = 0xFFFFFFFF;
        fxTitle[idx] = text;
        fxSubtitle[idx] = null;
        fxDetail[idx] = null;
        fxIsScreenSpace[idx] = false;

        spawnSparkBurst(x, y, color, 10, 40f, 100f);
    }

    private int allocEffect() {
        if (activeFxCount >= MAX_EFFECTS) {
            // Free the oldest effect
            shiftEffects();
        }
        int idx = activeFxCount++;
        return idx;
    }

    private void shiftEffects() {
        if (activeFxCount <= 0) return;
        for (int i = 1; i < activeFxCount; i++) {
            fxType[i-1] = fxType[i];
            fxState[i-1] = fxState[i];
            fxX[i-1] = fxX[i];
            fxY[i-1] = fxY[i];
            fxTimer[i-1] = fxTimer[i];
            fxDuration[i-1] = fxDuration[i];
            fxScale[i-1] = fxScale[i];
            fxAlpha[i-1] = fxAlpha[i];
            fxColor[i-1] = fxColor[i];
            fxSecondaryColor[i-1] = fxSecondaryColor[i];
            fxTitle[i-1] = fxTitle[i];
            fxSubtitle[i-1] = fxSubtitle[i];
            fxDetail[i-1] = fxDetail[i];
            fxIsScreenSpace[i-1] = fxIsScreenSpace[i];
        }
        activeFxCount--;
    }

    private void spawnSlash(float x1, float y1, float x2, float y2, int color, float maxLife) {
        if (activeSlashCount >= MAX_SLASH_LINES) return;
        int idx = activeSlashCount++;
        slashStartX[idx] = x1;
        slashStartY[idx] = y1;
        slashEndX[idx] = x2;
        slashEndY[idx] = y2;
        slashLife[idx] = maxLife;
        slashMaxLife[idx] = maxLife;
        slashColor[idx] = color;
    }

    private void spawnSparkBurst(float x, float y, int color, int count, float minSpeed, float maxSpeed) {
        for (int i = 0; i < count && activeSparkCount < MAX_SPARKS; i++) {
            int idx = activeSparkCount++;
            spX[idx] = x;
            spY[idx] = y;
            float angle = RNG.nextFloat(0f, 6.28318f);
            float speed = RNG.nextFloat(minSpeed, maxSpeed);
            spVX[idx] = (float) Math.cos(angle) * speed;
            spVY[idx] = (float) Math.sin(angle) * speed;
            float l = RNG.nextFloat(0.4f, 1.0f);
            spLife[idx] = l;
            spMaxLife[idx] = l;
            spSize[idx] = RNG.nextFloat(3f, 8f);
            spColor[idx] = color;
        }
    }

    public void update(float dt) {
        // Update slash lines
        for (int i = activeSlashCount - 1; i >= 0; i--) {
            slashLife[i] -= dt;
            if (slashLife[i] <= 0f) {
                int last = activeSlashCount - 1;
                if (i != last) {
                    slashStartX[i] = slashStartX[last];
                    slashStartY[i] = slashStartY[last];
                    slashEndX[i] = slashEndX[last];
                    slashEndY[i] = slashEndY[last];
                    slashLife[i] = slashLife[last];
                    slashMaxLife[i] = slashMaxLife[last];
                    slashColor[i] = slashColor[last];
                }
                activeSlashCount--;
            }
        }

        // Update sparks
        for (int i = activeSparkCount - 1; i >= 0; i--) {
            spLife[i] -= dt;
            if (spLife[i] <= 0f) {
                int last = activeSparkCount - 1;
                if (i != last) {
                    spX[i] = spX[last];
                    spY[i] = spY[last];
                    spVX[i] = spVX[last];
                    spVY[i] = spVY[last];
                    spLife[i] = spLife[last];
                    spMaxLife[i] = spMaxLife[last];
                    spSize[i] = spSize[last];
                    spColor[i] = spColor[last];
                }
                activeSparkCount--;
                continue;
            }
            spX[i] += spVX[i] * dt;
            spY[i] += spVY[i] * dt;
            spVY[i] += 120f * dt; // gravity
        }

        // Update active animated state effects
        for (int i = activeFxCount - 1; i >= 0; i--) {
            fxTimer[i] += dt;
            float progress = fxTimer[i] / fxDuration[i];

            if (progress >= 1f) {
                // Remove finished effect
                int last = activeFxCount - 1;
                if (i != last) {
                    fxType[i] = fxType[last];
                    fxState[i] = fxState[last];
                    fxX[i] = fxX[last];
                    fxY[i] = fxY[last];
                    fxTimer[i] = fxTimer[last];
                    fxDuration[i] = fxDuration[last];
                    fxScale[i] = fxScale[last];
                    fxAlpha[i] = fxAlpha[last];
                    fxColor[i] = fxColor[last];
                    fxSecondaryColor[i] = fxSecondaryColor[last];
                    fxTitle[i] = fxTitle[last];
                    fxSubtitle[i] = fxSubtitle[last];
                    fxDetail[i] = fxDetail[last];
                    fxIsScreenSpace[i] = fxIsScreenSpace[last];
                }
                activeFxCount--;
                continue;
            }

            // State Machine transitions:
            // 0.0 - 0.2: ENTER (fast zoom & pop)
            // 0.2 - 0.4: PEAK (vibrating peak scale)
            // 0.4 - 0.8: HOLD (steady float)
            // 0.8 - 1.0: EXIT (fade out & rise)
            if (progress < 0.2f) {
                fxState[i] = STATE_ENTER;
                float t = progress / 0.2f;
                // Ease out back
                float c1 = 1.70158f;
                float c3 = c1 + 1f;
                float eased = 1f + c3 * (float) Math.pow(t - 1f, 3) + c1 * (float) Math.pow(t - 1f, 2);
                fxScale[i] = Math.max(0.1f, eased);
                fxAlpha[i] = Math.min(1f, t * 2f);
            } else if (progress < 0.4f) {
                fxState[i] = STATE_PEAK;
                fxScale[i] = 1.0f + (float) Math.sin((progress - 0.2f) * 20f) * 0.08f;
                fxAlpha[i] = 1f;
            } else if (progress < 0.8f) {
                fxState[i] = STATE_HOLD;
                fxScale[i] = 1.0f;
                fxAlpha[i] = 1f;
            } else {
                fxState[i] = STATE_EXIT;
                float t = (progress - 0.8f) / 0.2f;
                fxScale[i] = 1.0f + t * 0.15f;
                fxAlpha[i] = Math.max(0f, 1f - t);
            }
        }
    }

    public void render(Canvas canvas, CameraSystem cam, int screenW, int screenH) {
        if (canvas == null) return;

        // 1. Render Slash Lines (World or Screen)
        for (int i = 0; i < activeSlashCount; i++) {
            float ratio = slashLife[i] / slashMaxLife[i];
            slashPaint.setColor(slashColor[i]);
            slashPaint.setAlpha((int) (255 * ratio));
            slashPaint.setStrokeWidth(12f * ratio);

            float x1 = slashStartX[i], y1 = slashStartY[i];
            float x2 = slashEndX[i], y2 = slashEndY[i];
            if (cam != null && !Float.isNaN(x1) && (Math.abs(x1) > 1000f || Math.abs(y1) > 1000f)) {
                x1 = cam.worldToScreenX(x1); y1 = cam.worldToScreenY(y1);
                x2 = cam.worldToScreenX(x2); y2 = cam.worldToScreenY(y2);
            }
            canvas.drawLine(x1, y1, x2, y2, slashPaint);
        }

        // 2. Render Sparks
        for (int i = 0; i < activeSparkCount; i++) {
            float ratio = spLife[i] / spMaxLife[i];
            sparkPaint.setColor(spColor[i]);
            sparkPaint.setAlpha((int) (255 * ratio));

            float px = spX[i], py = spY[i];
            if (cam != null && (Math.abs(px) > 1000f || Math.abs(py) > 1000f)) {
                px = cam.worldToScreenX(px);
                py = cam.worldToScreenY(py);
            }
            canvas.drawCircle(px, py, spSize[i] * ratio, sparkPaint);
        }

        // 3. Render State Animation Banners / Overlays
        for (int i = 0; i < activeFxCount; i++) {
            renderSingleEffect(canvas, cam, i, screenW, screenH);
        }
    }

    private void renderSingleEffect(Canvas canvas, CameraSystem cam, int idx, int screenW, int screenH) {
        float cx, cy;
        if (fxIsScreenSpace[idx] || fxType[idx] == FX_WAR_CONQUEST) {
            cx = screenW * 0.5f;
            cy = screenH * 0.35f;
        } else {
            if (cam != null) {
                cx = cam.worldToScreenX(fxX[idx]);
                cy = cam.worldToScreenY(fxY[idx]) - 40f;
            } else {
                cx = fxX[idx];
                cy = fxY[idx];
            }
        }

        int alpha = (int) (fxAlpha[idx] * 255);
        if (alpha <= 0) return;

        float scale = fxScale[idx];
        int type = fxType[idx];

        canvas.save();
        canvas.translate(cx, cy);
        canvas.scale(scale, scale);

        switch (type) {
            case FX_CRITICAL_STRIKE:
                renderCriticalStrikeCard(canvas, idx, alpha);
                break;
            case FX_TOURNAMENT_CLASH:
                renderTournamentClashCard(canvas, idx, alpha);
                break;
            case FX_WAR_CONQUEST:
                renderWarConquestCard(canvas, idx, alpha);
                break;
            case FX_BREAKTHROUGH:
                renderBreakthroughCard(canvas, idx, alpha);
                break;
            case FX_STAT_SURGE:
                renderStatSurgeBadge(canvas, idx, alpha);
                break;
        }

        canvas.restore();
    }

    private void renderCriticalStrikeCard(Canvas canvas, int idx, int alpha) {
        float w = 380f, h = 130f;
        rectPool1.set(-w * 0.5f, -h * 0.5f, w * 0.5f, h * 0.5f);

        // Blazing Outer Aura Glow
        glowPaint.setColor(fxColor[idx]);
        glowPaint.setAlpha(alpha / 4);
        rectPool2.set(rectPool1.left - 16f, rectPool1.top - 16f, rectPool1.right + 16f, rectPool1.bottom + 16f);
        canvas.drawRoundRect(rectPool2, 22f, 22f, glowPaint);

        // Inner Card Body
        bannerPaint.setColor(0xEE1A050A);
        bannerPaint.setAlpha((int)(alpha * 0.95f));
        canvas.drawRoundRect(rectPool1, 16f, 16f, bannerPaint);

        // Golden & Crimson Stroke
        bannerBorderPaint.setColor(fxSecondaryColor[idx]);
        bannerBorderPaint.setAlpha(alpha);
        bannerBorderPaint.setStrokeWidth(3f);
        canvas.drawRoundRect(rectPool1, 16f, 16f, bannerBorderPaint);

        // Text Content
        textTitlePaint.setTextSize(26f);
        textTitlePaint.setColor(fxSecondaryColor[idx]);
        textTitlePaint.setAlpha(alpha);
        canvas.drawText(fxTitle[idx], 0f, -h * 0.5f + 36f, textTitlePaint);

        if (fxSubtitle[idx] != null) {
            textSubPaint.setTextSize(14f);
            textSubPaint.setColor(0xFFFFFFFF);
            textSubPaint.setAlpha(alpha);
            canvas.drawText(fxSubtitle[idx], 0f, 2f, textSubPaint);
        }

        if (fxDetail[idx] != null) {
            textDetailPaint.setTextSize(13f);
            textDetailPaint.setColor(0xFFFF8A80);
            textDetailPaint.setAlpha(alpha);
            canvas.drawText(fxDetail[idx], 0f, h * 0.5f - 20f, textDetailPaint);
        }
    }

    private void renderTournamentClashCard(Canvas canvas, int idx, int alpha) {
        float w = 400f, h = 130f;
        rectPool1.set(-w * 0.5f, -h * 0.5f, w * 0.5f, h * 0.5f);

        glowPaint.setColor(fxColor[idx]);
        glowPaint.setAlpha(alpha / 4);
        rectPool2.set(rectPool1.left - 14f, rectPool1.top - 14f, rectPool1.right + 14f, rectPool1.bottom + 14f);
        canvas.drawRoundRect(rectPool2, 20f, 20f, glowPaint);

        bannerPaint.setColor(0xEE0B192C);
        bannerPaint.setAlpha((int)(alpha * 0.95f));
        canvas.drawRoundRect(rectPool1, 16f, 16f, bannerPaint);

        bannerBorderPaint.setColor(fxColor[idx]);
        bannerBorderPaint.setAlpha(alpha);
        canvas.drawRoundRect(rectPool1, 16f, 16f, bannerBorderPaint);

        textTitlePaint.setTextSize(24f);
        textTitlePaint.setColor(fxSecondaryColor[idx]);
        textTitlePaint.setAlpha(alpha);
        canvas.drawText(fxTitle[idx], 0f, -h * 0.5f + 36f, textTitlePaint);

        if (fxSubtitle[idx] != null) {
            textSubPaint.setTextSize(14f);
            textSubPaint.setColor(0xFF00E5FF);
            textSubPaint.setAlpha(alpha);
            canvas.drawText(fxSubtitle[idx], 0f, 4f, textSubPaint);
        }

        if (fxDetail[idx] != null) {
            textDetailPaint.setTextSize(13f);
            textDetailPaint.setColor(0xFFFFD700);
            textDetailPaint.setAlpha(alpha);
            canvas.drawText(fxDetail[idx], 0f, h * 0.5f - 18f, textDetailPaint);
        }
    }

    private void renderWarConquestCard(Canvas canvas, int idx, int alpha) {
        float w = 440f, h = 140f;
        rectPool1.set(-w * 0.5f, -h * 0.5f, w * 0.5f, h * 0.5f);

        glowPaint.setColor(fxColor[idx]);
        glowPaint.setAlpha(alpha / 3);
        rectPool2.set(rectPool1.left - 18f, rectPool1.top - 18f, rectPool1.right + 18f, rectPool1.bottom + 18f);
        canvas.drawRoundRect(rectPool2, 22f, 22f, glowPaint);

        bannerPaint.setColor(0xEE2A080C);
        bannerPaint.setAlpha((int)(alpha * 0.95f));
        canvas.drawRoundRect(rectPool1, 16f, 16f, bannerPaint);

        bannerBorderPaint.setColor(fxColor[idx]);
        bannerBorderPaint.setAlpha(alpha);
        canvas.drawRoundRect(rectPool1, 16f, 16f, bannerBorderPaint);

        textTitlePaint.setTextSize(22f);
        textTitlePaint.setColor(fxColor[idx]);
        textTitlePaint.setAlpha(alpha);
        canvas.drawText(fxTitle[idx], 0f, -h * 0.5f + 38f, textTitlePaint);

        if (fxSubtitle[idx] != null) {
            textSubPaint.setTextSize(14f);
            textSubPaint.setColor(0xFFFF8A80);
            textSubPaint.setAlpha(alpha);
            canvas.drawText(fxSubtitle[idx], 0f, 6f, textSubPaint);
        }

        if (fxDetail[idx] != null) {
            textDetailPaint.setTextSize(13f);
            textDetailPaint.setColor(0xFFFFD700);
            textDetailPaint.setAlpha(alpha);
            canvas.drawText(fxDetail[idx], 0f, h * 0.5f - 18f, textDetailPaint);
        }
    }

    private void renderBreakthroughCard(Canvas canvas, int idx, int alpha) {
        float w = 340f, h = 110f;
        rectPool1.set(-w * 0.5f, -h * 0.5f, w * 0.5f, h * 0.5f);

        glowPaint.setColor(0xFFFFD700);
        glowPaint.setAlpha(alpha / 3);
        canvas.drawCircle(0f, 0f, 90f, glowPaint);

        bannerPaint.setColor(0xEE1E0D33);
        bannerPaint.setAlpha((int)(alpha * 0.95f));
        canvas.drawRoundRect(rectPool1, 14f, 14f, bannerPaint);

        bannerBorderPaint.setColor(0xFFFFD700);
        bannerBorderPaint.setAlpha(alpha);
        canvas.drawRoundRect(rectPool1, 14f, 14f, bannerBorderPaint);

        textTitlePaint.setTextSize(20f);
        textTitlePaint.setColor(0xFFFFD700);
        textTitlePaint.setAlpha(alpha);
        canvas.drawText(fxTitle[idx], 0f, -h * 0.5f + 32f, textTitlePaint);

        if (fxSubtitle[idx] != null) {
            textSubPaint.setTextSize(13f);
            textSubPaint.setColor(0xFF00E5FF);
            textSubPaint.setAlpha(alpha);
            canvas.drawText(fxSubtitle[idx], 0f, 6f, textSubPaint);
        }

        if (fxDetail[idx] != null) {
            textDetailPaint.setTextSize(11f);
            textDetailPaint.setColor(0xFFB388FF);
            textDetailPaint.setAlpha(alpha);
            canvas.drawText(fxDetail[idx], 0f, h * 0.5f - 14f, textDetailPaint);
        }
    }

    private void renderStatSurgeBadge(Canvas canvas, int idx, int alpha) {
        float w = 180f, h = 42f;
        rectPool1.set(-w * 0.5f, -h * 0.5f, w * 0.5f, h * 0.5f);

        bannerPaint.setColor(0xCC000000);
        bannerPaint.setAlpha((int)(alpha * 0.85f));
        canvas.drawRoundRect(rectPool1, 12f, 12f, bannerPaint);

        bannerBorderPaint.setColor(fxColor[idx]);
        bannerBorderPaint.setAlpha(alpha);
        bannerBorderPaint.setStrokeWidth(2f);
        canvas.drawRoundRect(rectPool1, 12f, 12f, bannerBorderPaint);

        textTitlePaint.setTextSize(16f);
        textTitlePaint.setColor(fxColor[idx]);
        textTitlePaint.setAlpha(alpha);
        canvas.drawText(fxTitle[idx], 0f, 6f, textTitlePaint);
    }

    public int getActiveCount() {
        return activeFxCount;
    }

    public void clear() {
        activeFxCount = 0;
        activeSlashCount = 0;
        activeSparkCount = 0;
    }
}
