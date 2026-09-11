package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;
import com.sect.idle.core.MathUtils;
import com.sect.idle.gameplay.BattleEngine;
import com.sect.idle.gameplay.SectData;
import com.sect.idle.models.BattleUnit;
import com.sect.idle.models.Disciple;
import com.sect.idle.render.SpiritAuraBufferRenderer;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

/**
 * BattleCustomView - High-performance, non-blocking Java custom View class for the battle scene.
 * Uses low-level Canvas drawing and buffered bitmap operations to render spirit aura effects,
 * minimizing garbage collection and memory footprint for smooth combat execution on low-end Android hardware.
 *
 * 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BattleCustomView extends View implements Choreographer.FrameCallback {

    // Gameplay and Engine
    private BattleEngine battleEngine;
    private SpiritAuraBufferRenderer auraRenderer;
    private final SectData sectData;

    // Pre-allocated Paints (Zero allocation during draw)
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
    private final Paint textSubtitlePaint;
    private final Paint textValuePaint;
    private final Paint logBgPaint;
    private final Paint logTextPaint;
    private final Paint damageTextPaint;
    private final Paint particlePaint;
    private final Paint flashPaint;
    private final Paint buttonBgPaint;
    private final Paint buttonTextPaint;

    // Pre-allocated Geometry Pools
    private final RectF rectF1;
    private final RectF rectF2;
    private final Rect srcRect;
    private final Rect dstRect;
    private final StringBuilder stringBuilder;

    // View dimensions & State
    private int viewWidth = 720;
    private int viewHeight = 1280;
    private float animTime = 0f;
    private float simulationSpeed = 1.0f; // 1x, 2x, 4x speed multiplier
    private boolean isPaused = false;
    private boolean autoBattle = true;
    private int selectedUnitIndex = -1;

    // Screen Shake & Flash Effects
    private float shakeIntensity = 0f;
    private float shakeDecay = 0.88f;
    private float flashIntensity = 0f;
    private int flashColor = 0xFFFFFFFF;

    // Particle System (Fixed-size primitive arrays to avoid GC)
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

    // Floating Damage Texts (Zero GC pool)
    private static final int MAX_FLOATING_TEXTS = 16;
    private final String[] floatTexts = new String[MAX_FLOATING_TEXTS];
    private final float[] floatX = new float[MAX_FLOATING_TEXTS];
    private final float[] floatY = new float[MAX_FLOATING_TEXTS];
    private final float[] floatLife = new float[MAX_FLOATING_TEXTS];
    private final float[] floatMaxLife = new float[MAX_FLOATING_TEXTS];
    private final int[] floatColor = new int[MAX_FLOATING_TEXTS];
    private int activeFloatingTexts = 0;

    // Touch & Interactive Rects
    private final RectF btnSpeedRect;
    private final RectF btnPauseRect;
    private final RectF btnAutoRect;

    // Animation Ticking
    private boolean isRunning = false;
    private long lastFrameTimeNanos = 0L;
    private Choreographer choreographer;

    // Combat Layout Constants
    private static final float UNIT_WIDTH = 76f;
    private static final float UNIT_HEIGHT = 76f;
    private static final float BAR_HEIGHT_HP = 7f;
    private static final float BAR_HEIGHT_MP = 4f;
    private static final float BAR_HEIGHT_ACTION = 4f;

    public BattleCustomView(Context context) {
        this(context, null);
    }

    public BattleCustomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BattleCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.sectData = SectData.getInstance();

        // 1. Initialize Render Pipelines
        this.auraRenderer = new SpiritAuraBufferRenderer();
        this.battleEngine = new BattleEngine();

        // 2. Geometry Pools
        this.rectF1 = new RectF();
        this.rectF2 = new RectF();
        this.srcRect = new Rect();
        this.dstRect = new Rect();
        this.stringBuilder = new StringBuilder(64);

        this.btnSpeedRect = new RectF();
        this.btnPauseRect = new RectF();
        this.btnAutoRect = new RectF();

        // 3. Pre-allocate All Paints
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

        this.textSubtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textSubtitlePaint.setColor(0xFFB0BEC5);
        this.textSubtitlePaint.setTextSize(10f);
        this.textSubtitlePaint.setTextAlign(Paint.Align.CENTER);

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

        this.particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.particlePaint.setStyle(Paint.Style.FILL);

        this.flashPaint = new Paint();

        this.buttonBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.buttonBgPaint.setColor(0xFF263238);

        this.buttonTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.buttonTextPaint.setColor(0xFFFFFFFF);
        this.buttonTextPaint.setTextSize(12f);
        this.buttonTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        this.buttonTextPaint.setTextAlign(Paint.Align.CENTER);

        // Auto initialize default test combat if needed
        initDefaultSectBattle();
    }

    /**
     * Initializes default battle setup using sect disciples and wild rogue cultivators.
     */
    public void initDefaultSectBattle() {
        ArrayList<Disciple> playerTeam = new ArrayList<Disciple>();
        if (sectData != null && !sectData.disciples.isEmpty()) {
            for (int i = 0; i < Math.min(3, sectData.disciples.size()); i++) {
                playerTeam.add(sectData.disciples.get(i));
            }
        } else {
            Disciple d1 = new Disciple();
            d1.name = "Senior Brother Han";
            d1.hp = 300; d1.maxHp = 300; d1.atk = 45; d1.def = 25; d1.element = 4;
            
            Disciple d2 = new Disciple();
            d2.name = "Junior Sister Li";
            d2.hp = 220; d2.maxHp = 220; d2.atk = 55; d2.def = 18; d2.element = 2;
            
            playerTeam.add(d1);
            playerTeam.add(d2);
        }

        ArrayList<Disciple> enemyTeam = new ArrayList<Disciple>();
        Disciple e1 = new Disciple();
        e1.name = "Blood Ghost Cultist";
        e1.hp = 280; e1.maxHp = 280; e1.atk = 40; e1.def = 20; e1.element = 3;
        
        Disciple e2 = new Disciple();
        e2.name = "Iron Corpse Puppet";
        e2.hp = 400; e2.maxHp = 400; e2.atk = 30; e2.def = 35; e2.element = 5;
        
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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startLoop();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopLoop();
        if (auraRenderer != null) {
            auraRenderer.releaseBuffer();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            startLoop();
        } else {
            stopLoop();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.viewWidth = w > 0 ? w : 720;
        this.viewHeight = h > 0 ? h : 1280;

        if (auraRenderer != null) {
            auraRenderer.updateBufferSize(viewWidth, viewHeight);
        }

        // Layout interactive action buttons at bottom right
        float btnW = 90f;
        float btnH = 36f;
        float margin = 12f;
        float startY = viewHeight - btnH - margin;

        btnAutoRect.set(viewWidth - (btnW * 3f + margin * 3f), startY, viewWidth - (btnW * 2f + margin * 3f), startY + btnH);
        btnSpeedRect.set(viewWidth - (btnW * 2f + margin * 2f), startY, viewWidth - (btnW + margin * 2f), startY + btnH);
        btnPauseRect.set(viewWidth - (btnW + margin), startY, viewWidth - margin, startY + btnH);
    }

    public void startLoop() {
        if (isRunning) return;
        isRunning = true;
        choreographer = Choreographer.getInstance();
        if (choreographer != null) {
            choreographer.postFrameCallback(this);
        } else {
            postInvalidateOnAnimation();
        }
    }

    public void stopLoop() {
        isRunning = false;
        if (choreographer != null) {
            choreographer.removeFrameCallback(this);
        }
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!isRunning) return;

        float dt = 0.033f;
        if (lastFrameTimeNanos > 0L) {
            long delta = frameTimeNanos - lastFrameTimeNanos;
            dt = delta / 1_000_000_000.0f;
            if (dt > 0.1f) dt = 0.1f; // Clamp to avoid spiral
        }
        lastFrameTimeNanos = frameTimeNanos;

        updateLogic(dt);
        invalidate();

        if (choreographer != null && isRunning) {
            choreographer.postFrameCallback(this);
        }
    }

    private void updateLogic(float dt) {
        if (!isPaused && battleEngine != null && battleEngine.isRunning) {
            float simDt = dt * simulationSpeed;
            animTime += simDt;

            // Combat turn ticking
            battleEngine.tick();

            // Trigger hit particles & shake if log has changed
            ArrayList<String> logs = battleEngine.getLog();
            if (!logs.isEmpty() && RNG.chance(15)) {
                spawnHitVFX(viewWidth * 0.5f + RNG.nextFloat(-100f, 100f), viewHeight * 0.4f + RNG.nextFloat(-80f, 80f), 0xFFFFC107);
            }
        } else {
            animTime += dt;
        }

        // Screen Shake decay
        if (shakeIntensity > 0.1f) {
            shakeIntensity *= shakeDecay;
        } else {
            shakeIntensity = 0f;
        }

        // Flash decay
        if (flashIntensity > 0.01f) {
            flashIntensity *= 0.88f;
        } else {
            flashIntensity = 0f;
        }

        updateParticlePhysics(dt);
        updateFloatingTexts(dt);
    }

    private void updateParticlePhysics(float dt) {
        for (int i = activeParticles - 1; i >= 0; i--) {
            partLife[i] -= dt;
            if (partLife[i] <= 0f) {
                // Fast swap with last active element
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
            partVY[i] += 20f * dt; // Gravity
        }
    }

    private void updateFloatingTexts(float dt) {
        for (int i = activeFloatingTexts - 1; i >= 0; i--) {
            floatLife[i] -= dt;
            floatY[i] -= 35f * dt;
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) return;

        canvas.save();

        // 1. Screen Shake Transform
        if (shakeIntensity > 0.05f) {
            float sx = (RNG.nextFloat() * 2f - 1f) * shakeIntensity;
            float sy = (RNG.nextFloat() * 2f - 1f) * shakeIntensity;
            canvas.translate(sx, sy);
        }

        // 2. Clear & Draw Arena Background
        canvas.drawRect(0, 0, viewWidth, viewHeight, bgPaint);
        drawArenaGrid(canvas);

        if (battleEngine != null) {
            ArrayList<BattleUnit> units = battleEngine.getUnits();

            // 3. Render High-Performance Buffered Spirit Aura Effects
            if (auraRenderer != null) {
                auraRenderer.renderSpiritAuras(canvas, units, animTime, viewWidth, viewHeight);
            }

            // 4. Render Battle Combatants
            renderUnits(canvas, units);

            // 5. Render Floating Damage Numbers & Particles
            renderParticles(canvas);
            renderFloatingNumbers(canvas);

            // 6. Render Combat Status & Logs
            renderCombatLogs(canvas);
        }

        // 7. Render UI Action Controls
        renderControls(canvas);

        // 8. Screen Flash Overlay
        if (flashIntensity > 0.02f) {
            flashPaint.setColor(flashColor);
            flashPaint.setAlpha((int) (flashIntensity * 180));
            canvas.drawRect(0, 0, viewWidth, viewHeight, flashPaint);
        }

        canvas.restore();
    }

    private void drawArenaGrid(Canvas canvas) {
        float centerX = viewWidth * 0.5f;
        float centerY = viewHeight * 0.38f;

        // Draw Daoist concentric formation circles
        canvas.drawCircle(centerX, centerY, 80f, arenaGridPaint);
        canvas.drawCircle(centerX, centerY, 160f, arenaGridPaint);
        canvas.drawCircle(centerX, centerY, 240f, arenaGridPaint);
    }

    private void renderUnits(Canvas canvas, ArrayList<BattleUnit> units) {
        if (units == null || units.isEmpty()) return;

        int size = units.size();
        float centerY = viewHeight * 0.38f;
        float rowSpacing = UNIT_HEIGHT + 35f;

        int playerIdx = 0;
        int enemyIdx = 0;

        for (int i = 0; i < size; i++) {
            BattleUnit u = units.get(i);
            if (u == null) continue;

            boolean isPlayer = (u.team == 0);
            float posX = isPlayer ? (viewWidth * 0.22f) : (viewWidth * 0.78f);
            int sideIndex = isPlayer ? playerIdx++ : enemyIdx++;
            float posY = centerY + (sideIndex - 0.8f) * rowSpacing;

            // Update unit internal pos for aura & effects
            u.pos.set(posX, posY);

            // Shadow
            rectF1.set(posX - UNIT_WIDTH * 0.4f, posY + UNIT_HEIGHT * 0.35f, posX + UNIT_WIDTH * 0.4f, posY + UNIT_HEIGHT * 0.5f);
            canvas.drawOval(rectF1, unitShadowPaint);

            // Unit Body (Color coded by realm / status)
            int bodyColor = !u.isAlive ? 0xFF616161 : (isPlayer ? 0xFF1E88E5 : 0xFFE53935);
            unitBodyPaint.setColor(bodyColor);
            rectF1.set(posX - UNIT_WIDTH * 0.5f, posY - UNIT_HEIGHT * 0.5f, posX + UNIT_WIDTH * 0.5f, posY + UNIT_HEIGHT * 0.5f);
            canvas.drawRoundRect(rectF1, 10f, 10f, unitBodyPaint);

            // Selected Highlight
            if (i == selectedUnitIndex) {
                rectF2.set(rectF1.left - 4f, rectF1.top - 4f, rectF1.right + 4f, rectF1.bottom + 4f);
                canvas.drawRoundRect(rectF2, 14f, 14f, selectionPaint);
            }

            // Name & Level
            canvas.drawText(u.name, posX, posY - UNIT_HEIGHT * 0.55f, textTitlePaint);

            // Health Bar
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

            // HP text
            stringBuilder.setLength(0);
            stringBuilder.append(u.hp).append("/").append(u.maxHp);
            canvas.drawText(stringBuilder.toString(), posX, barY + BAR_HEIGHT_HP - 1f, textValuePaint);

            // Action / Qi Gauge
            barY += BAR_HEIGHT_HP + 3f;
            rectF1.set(posX - barW * 0.5f, barY, posX + barW * 0.5f, barY + BAR_HEIGHT_ACTION);
            canvas.drawRect(rectF1, mpBgPaint);

            float actionRatio = u.maxActionBar > 0 ? (float) u.actionBar / (float) u.maxActionBar : 0f;
            if (actionRatio > 1f) actionRatio = 1f;
            rectF1.set(posX - barW * 0.5f, barY, posX - barW * 0.5f + (barW * actionRatio), barY + BAR_HEIGHT_ACTION);
            canvas.drawRect(rectF1, actionFillPaint);
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

    private void renderParticles(Canvas canvas) {
        for (int i = 0; i < activeParticles; i++) {
            float progress = partLife[i] / partMaxLife[i];
            particlePaint.setColor(partColor[i]);
            particlePaint.setAlpha((int) (255 * progress));
            canvas.drawCircle(partX[i], partY[i], partSize[i] * progress, particlePaint);
        }
    }

    private void renderFloatingNumbers(Canvas canvas) {
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

    private void renderControls(Canvas canvas) {
        // Auto button
        buttonBgPaint.setColor(autoBattle ? 0xFF2E7D32 : 0xFF455A64);
        canvas.drawRoundRect(btnAutoRect, 6f, 6f, buttonBgPaint);
        canvas.drawText(autoBattle ? "AUTO: ON" : "AUTO: OFF", btnAutoRect.centerX(), btnAutoRect.centerY() + 4f, buttonTextPaint);

        // Speed button
        buttonBgPaint.setColor(0xFF1565C0);
        canvas.drawRoundRect(btnSpeedRect, 6f, 6f, buttonBgPaint);
        stringBuilder.setLength(0);
        stringBuilder.append((int) simulationSpeed).append("X SPEED");
        canvas.drawText(stringBuilder.toString(), btnSpeedRect.centerX(), btnSpeedRect.centerY() + 4f, buttonTextPaint);

        // Pause button
        buttonBgPaint.setColor(isPaused ? 0xFFC62828 : 0xFF37474F);
        canvas.drawRoundRect(btnPauseRect, 6f, 6f, buttonBgPaint);
        canvas.drawText(isPaused ? "PLAY" : "PAUSE", btnPauseRect.centerX(), btnPauseRect.centerY() + 4f, buttonTextPaint);
    }

    public void spawnHitVFX(float x, float y, int color) {
        if (activeParticles >= MAX_PARTICLES) return;
        for (int i = 0; i < 6 && activeParticles < MAX_PARTICLES; i++) {
            int idx = activeParticles++;
            partX[idx] = x;
            partY[idx] = y;
            float angle = RNG.nextFloat(0f, 6.28318f);
            float spd = RNG.nextFloat(40f, 120f);
            partVX[idx] = MathUtils.cos(angle) * spd;
            partVY[idx] = MathUtils.sin(angle) * spd;
            float l = RNG.nextFloat(0.3f, 0.8f);
            partLife[idx] = l;
            partMaxLife[idx] = l;
            partSize[idx] = RNG.nextFloat(3f, 6f);
            partColor[idx] = color;
        }
        shakeIntensity = 4f;
    }

    public void spawnDamageNumber(float x, float y, int amount, boolean isCrit) {
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
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float tx = event.getX();
            float ty = event.getY();

            // Check button clicks
            if (btnAutoRect.contains(tx, ty)) {
                autoBattle = !autoBattle;
                return true;
            }
            if (btnSpeedRect.contains(tx, ty)) {
                if (simulationSpeed == 1.0f) simulationSpeed = 2.0f;
                else if (simulationSpeed == 2.0f) simulationSpeed = 4.0f;
                else simulationSpeed = 1.0f;
                return true;
            }
            if (btnPauseRect.contains(tx, ty)) {
                isPaused = !isPaused;
                return true;
            }

            // Check unit selection
            if (battleEngine != null) {
                ArrayList<BattleUnit> units = battleEngine.getUnits();
                for (int i = 0; i < units.size(); i++) {
                    BattleUnit u = units.get(i);
                    if (u != null && Math.abs(u.pos.x - tx) < UNIT_WIDTH * 0.6f && Math.abs(u.pos.y - ty) < UNIT_HEIGHT * 0.6f) {
                        selectedUnitIndex = i;
                        spawnHitVFX(u.pos.x, u.pos.y, 0xFF00E5FF);
                        return true;
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
