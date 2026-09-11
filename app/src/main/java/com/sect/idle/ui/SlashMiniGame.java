package com.sect.idle.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.sect.idle.core.GameConfig;
import com.sect.idle.gameplay.AlchemySystem;
import com.sect.idle.gameplay.FarmingSystem;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;

/**
 * SlashMiniGame v1.0 - Fruit-Ninja style swipe minigame.
 * Dipicu dari tap building Garden/Alchemy. Hasil combo menentukan bonus
 * hasil panen/racikan lewat FarmingSystem/AlchemySystem overload baru.
 * Zero-allocation per frame: semua array pre-alokasi di constructor.
 */
public final class SlashMiniGame {
    private static final int PHASE_IDLE = 0, PHASE_PLAYING = 1, PHASE_RESULT = 2;
    private int phase = PHASE_IDLE;

    private static final int MAX_FRUIT = 10;
    private static final int MAX_TRAIL = 24;
    private static final int MAX_BURST = 24;
    private static final float SESSION_DURATION = 16f;
    private static final float COMBO_WINDOW = 1.1f;
    private static final float BOMB_CHANCE = 12f;

    // Fruit pool
    private final float[] frX = new float[MAX_FRUIT], frY = new float[MAX_FRUIT];
    private final float[] frVX = new float[MAX_FRUIT], frVY = new float[MAX_FRUIT];
    private final float[] frRadius = new float[MAX_FRUIT];
    private final boolean[] frBomb = new boolean[MAX_FRUIT];
    private final boolean[] frAlive = new boolean[MAX_FRUIT];
    private final int[] frColor = new int[MAX_FRUIT];

    // Swipe trail (ring buffer)
    private final float[] trX = new float[MAX_TRAIL], trY = new float[MAX_TRAIL];
    private final float[] trLife = new float[MAX_TRAIL];
    private int trHead = 0;
    private boolean trailActive = false;
    private float lastTrailX, lastTrailY;

    // Hit-burst particles (dipakai ulang, bukan effect system global)
    private final float[] buX = new float[MAX_BURST], buY = new float[MAX_BURST];
    private final float[] buVX = new float[MAX_BURST], buVY = new float[MAX_BURST];
    private final float[] buLife = new float[MAX_BURST];
    private final int[] buColor = new int[MAX_BURST];
    private int buCount = 0;

    private float sessionTimer, spawnTimer, spawnInterval;
    private int goodSlices, misses, bombHits, combo, maxCombo;
    private float comboTimer;
    private float bombFlashTimer;
    private float resultTimer;
    private String resultText = "";
    private Disciple worker;
    private int buildingType;
    private int lastW = 1080, lastH = 1920;

    private final Paint dimPaint = new Paint();
    private final Paint fruitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bombPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bombXPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint burstPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timerBgPaint = new Paint();
    private final Paint timerBarPaint = new Paint();
    private final Paint resultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint flashPaint = new Paint();

    private static final int[] HERB_COLORS = {0xFF4CAF50, 0xFFFFD700, 0xFF00E5FF, 0xFFFF6EC7};

    public SlashMiniGame() {
        dimPaint.setColor(0xCC000000);
        trailPaint.setColor(0xFFFFFFFF);
        trailPaint.setStrokeWidth(10f);
        trailPaint.setStrokeCap(Paint.Cap.ROUND);
        bombXPaint.setColor(0xFFFFFFFF);
        bombXPaint.setStrokeWidth(4f);
        bombPaint.setColor(0xFF3A3A3A);
        hudPaint.setColor(0xFFFFD700);
        hudPaint.setTextAlign(Paint.Align.CENTER);
        hudPaint.setTypeface(Typeface.DEFAULT_BOLD);
        hudPaint.setTextSize(38f);
        hudPaint.setShadowLayer(4f, 0f, 2f, 0xFF000000);
        timerBgPaint.setColor(0x66FFFFFF);
        timerBarPaint.setColor(0xFF00E5FF);
        resultPaint.setColor(0xFFFFFFFF);
        resultPaint.setTextAlign(Paint.Align.CENTER);
        resultPaint.setTypeface(Typeface.DEFAULT_BOLD);
        resultPaint.setTextSize(30f);
        resultPaint.setShadowLayer(4f, 0f, 2f, 0xFF000000);
        flashPaint.setColor(0xFFFF1744);
    }

    public boolean isActive() { return phase != PHASE_IDLE; }

    public void start(Disciple w, int bType) {
        if (w == null || phase != PHASE_IDLE) return;
        worker = w; buildingType = bType;
        phase = PHASE_PLAYING;
        sessionTimer = SESSION_DURATION;
        spawnInterval = 0.9f;
        spawnTimer = 0.3f;
        goodSlices = 0; misses = 0; bombHits = 0; combo = 0; maxCombo = 0;
        comboTimer = 0f; bombFlashTimer = 0f;
        for (int i = 0; i < MAX_FRUIT; i++) frAlive[i] = false;
        trHead = 0; trailActive = false;
        for (int i = 0; i < MAX_TRAIL; i++) trLife[i] = 0f;
        buCount = 0;
    }

    private void spawnFruit() {
        int slot = -1;
        for (int i = 0; i < MAX_FRUIT; i++) if (!frAlive[i]) { slot = i; break; }
        if (slot < 0) return;
        frAlive[slot] = true;
        frBomb[slot] = RNG.chance(BOMB_CHANCE);
        frX[slot] = RNG.nextFloat(lastW * 0.15f, lastW * 0.85f);
        frY[slot] = lastH + 40f;
        frVX[slot] = RNG.nextFloat(-60f, 60f);
        frVY[slot] = -(RNG.nextFloat(900f, 1150f));
        frRadius[slot] = RNG.nextFloat(34f, 46f);
        frColor[slot] = frBomb[slot] ? 0xFF3A3A3A : HERB_COLORS[RNG.nextInt(HERB_COLORS.length)];
    }

    public void update(float dt) {
        if (phase == PHASE_RESULT) {
            resultTimer -= dt;
            if (resultTimer <= 0f) phase = PHASE_IDLE;
            return;
        }
        if (phase != PHASE_PLAYING) return;

        sessionTimer -= dt;
        if (sessionTimer <= 0f) { endSession(); return; }

        spawnTimer -= dt;
        if (spawnTimer <= 0f) {
            spawnFruit();
            spawnTimer = spawnInterval;
            spawnInterval = Math.max(0.35f, spawnInterval - 0.025f);
        }

        for (int i = 0; i < MAX_FRUIT; i++) {
            if (!frAlive[i]) continue;
            frVY[i] += 1400f * dt;
            frX[i] += frVX[i] * dt;
            frY[i] += frVY[i] * dt;
            if (frY[i] > lastH + 100f) {
                frAlive[i] = false;
                if (!frBomb[i]) { misses++; combo = 0; }
            }
        }

        for (int i = 0; i < MAX_TRAIL; i++) if (trLife[i] > 0f) trLife[i] -= dt * 2.2f;

        if (comboTimer > 0f) { comboTimer -= dt; if (comboTimer <= 0f) combo = 0; }
        if (bombFlashTimer > 0f) bombFlashTimer -= dt;

        for (int i = buCount - 1; i >= 0; i--) {
            buLife[i] -= dt;
            if (buLife[i] <= 0f) {
                buCount--;
                if (i < buCount) {
                    buX[i]=buX[buCount]; buY[i]=buY[buCount]; buVX[i]=buVX[buCount];
                    buVY[i]=buVY[buCount]; buLife[i]=buLife[buCount]; buColor[i]=buColor[buCount];
                }
                continue;
            }
            buX[i] += buVX[i] * dt; buY[i] += buVY[i] * dt; buVY[i] += 500f * dt;
        }
    }

    public void onTouchDown(float x, float y) {
        if (phase != PHASE_PLAYING) return;
        trailActive = true;
        lastTrailX = x; lastTrailY = y;
        pushTrail(x, y);
    }

    public void onTouchMove(float x, float y) {
        if (phase != PHASE_PLAYING || !trailActive) return;
        pushTrail(x, y);
        checkSlice(lastTrailX, lastTrailY, x, y);
        lastTrailX = x; lastTrailY = y;
    }

    public void onTouchUp() {
        trailActive = false;
    }

    private void pushTrail(float x, float y) {
        trX[trHead] = x; trY[trHead] = y; trLife[trHead] = 1f;
        trHead = (trHead + 1) % MAX_TRAIL;
    }

    private void checkSlice(float x1, float y1, float x2, float y2) {
        float segDx = x2 - x1;
        float segDy = y2 - y1;
        float lenSq = segDx * segDx + segDy * segDy;

        for (int i = 0; i < MAX_FRUIT; i++) {
            if (!frAlive[i]) continue;
            float r = frRadius[i] + 24f;
            float distSq;
            if (lenSq <= 0.001f) {
                float dx = frX[i] - x1;
                float dy = frY[i] - y1;
                distSq = dx * dx + dy * dy;
            } else {
                float t = Math.max(0f, Math.min(1f, ((frX[i] - x1) * segDx + (frY[i] - y1) * segDy) / lenSq));
                float closeX = x1 + t * segDx;
                float closeY = y1 + t * segDy;
                float dx = frX[i] - closeX;
                float dy = frY[i] - closeY;
                distSq = dx * dx + dy * dy;
            }

            if (distSq <= r * r) {
                frAlive[i] = false;
                spawnBurst(frX[i], frY[i], frColor[i]);
                if (frBomb[i]) {
                    bombHits++; combo = 0; bombFlashTimer = 0.25f;
                } else {
                    goodSlices++; combo++; if (combo > maxCombo) maxCombo = combo;
                    comboTimer = COMBO_WINDOW;
                }
            }
        }
    }

    private void spawnBurst(float x, float y, int color) {
        for (int j = 0; j < 6 && buCount < MAX_BURST; j++) {
            int i = buCount++;
            float angle = RNG.nextFloat(0f, 6.283f);
            float speed = RNG.nextFloat(80f, 220f);
            buX[i] = x; buY[i] = y;
            buVX[i] = (float) Math.cos(angle) * speed;
            buVY[i] = (float) Math.sin(angle) * speed;
            buColor[i] = color;
            buLife[i] = 0.4f;
        }
    }

    private void endSession() {
        float totalAttempts = goodSlices + misses + bombHits;
        float successRatio = totalAttempts > 0f ? goodSlices / totalAttempts : 0f;
        StringBuilder sb = new StringBuilder(64);
        if (buildingType == GameConfig.BUILD_GARDEN) {
            int hours = Math.max(1, Math.min(8, 1 + goodSlices / 3));
            float bonus = Math.max(0f, Math.min(1.5f, maxCombo * 0.06f + successRatio * 0.3f));
            FarmingSystem.HarvestResult r = FarmingSystem.harvest(worker, hours, bonus);
            sb.append("Panen selesai! ").append(r.herbs).append(" herbs (Q").append(r.quality).append(")");
        } else if (buildingType == GameConfig.BUILD_ALCHEMY) {
            int recipeIndex = Math.max(0, Math.min(AlchemySystem.RECIPES.length - 1, maxCombo / 3));
            float bonusSuccess = Math.max(0f, Math.min(30f, goodSlices * 2f - misses * 3f - bombHits * 8f));
            AlchemySystem.PillResult r = AlchemySystem.craft(worker, recipeIndex, bonusSuccess);
            sb.append(r.message);
        } else {
            sb.append("Sesi selesai. Combo terbaik: ").append(maxCombo);
        }
        resultText = sb.toString();
        phase = PHASE_RESULT;
        resultTimer = 2.6f;
    }

    public void render(Canvas canvas, int W, int H) {
        lastW = W; lastH = H;
        if (phase == PHASE_IDLE) return;

        canvas.drawRect(0, 0, W, H, dimPaint);

        for (int i = 0; i < MAX_FRUIT; i++) {
            if (!frAlive[i]) continue;
            if (frBomb[i]) {
                canvas.drawCircle(frX[i], frY[i], frRadius[i], bombPaint);
                float r = frRadius[i] * 0.4f;
                canvas.drawLine(frX[i]-r, frY[i]-r, frX[i]+r, frY[i]+r, bombXPaint);
                canvas.drawLine(frX[i]-r, frY[i]+r, frX[i]+r, frY[i]-r, bombXPaint);
            } else {
                fruitPaint.setColor(frColor[i]);
                canvas.drawCircle(frX[i], frY[i], frRadius[i], fruitPaint);
            }
        }

        for (int i = 0; i < MAX_TRAIL; i++) {
            if (trLife[i] <= 0f) continue;
            int next = (i + 1) % MAX_TRAIL;
            if (trLife[next] <= 0f) continue;
            trailPaint.setAlpha((int) (255 * trLife[i]));
            canvas.drawLine(trX[i], trY[i], trX[next], trY[next], trailPaint);
        }
        trailPaint.setAlpha(255);

        for (int i = 0; i < buCount; i++) {
            burstPaint.setColor(buColor[i]);
            burstPaint.setAlpha((int) (255 * Math.max(0f, buLife[i] / 0.4f)));
            canvas.drawCircle(buX[i], buY[i], 6f, burstPaint);
        }
        burstPaint.setAlpha(255);

        if (bombFlashTimer > 0f) {
            flashPaint.setAlpha((int) (180 * (bombFlashTimer / 0.25f)));
            canvas.drawRect(0, 0, W, H, flashPaint);
        }

        float barW = W * 0.6f, barX = (W - barW) * 0.5f, barY = 40f;
        canvas.drawRect(barX, barY, barX + barW, barY + 14f, timerBgPaint);
        float ratio = phase == PHASE_PLAYING ? Math.max(0f, sessionTimer / SESSION_DURATION) : 0f;
        canvas.drawRect(barX, barY, barX + barW * ratio, barY + 14f, timerBarPaint);

        if (combo > 1) {
            hudPaint.setAlpha(255);
            canvas.drawText(combo + "x COMBO!", W * 0.5f, barY + 70f, hudPaint);
        }

        if (phase == PHASE_RESULT) {
            float a = Math.max(0f, Math.min(1f, resultTimer / 2.6f));
            resultPaint.setAlpha((int) (255 * a));
            canvas.drawText(resultText, W * 0.5f, H * 0.5f, resultPaint);
        }
    }
}
