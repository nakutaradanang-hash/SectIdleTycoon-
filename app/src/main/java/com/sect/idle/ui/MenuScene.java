package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import com.sect.idle.core.GameState;
import com.sect.idle.render.RenderEngine;
import com.sect.idle.systems.CameraSystem;
import com.sect.idle.systems.RNG;

/**
 * MenuScene v6.0 - REFACTOR: title glow di-bake ke Bitmap sekali (bukan 4x
 * setShadowLayer/frame -> penyebab utama stutter di GPU Mali-G57 MP1),
 * shadow judul di-set sekali di constructor, parallax metrics di-cache.
 * Semua API publik tidak berubah (kompatibel dengan SceneManager/GameView).
 */
public final class MenuScene {
    private final Context context;
    private CameraSystem camera;
    private RenderEngine renderEngine;

    private float titleY = -150f;
    private float targetTitleY = 200f;
    private float animProgress = 0f;
    private float time = 0f;
    private float parallaxOffset = 0f;

    private final Paint bgPaint;
    private final Paint titlePaint;
    private final Paint titleGlowPaint;
    private final Paint glowBitmapPaint;
    private final Paint subTitlePaint;
    private final Paint btnPaint;
    private final Paint btnHoverPaint;
    private final Paint btnBorderPaint;
    private final Paint btnGlowPaint;
    private final Paint textPaint;
    private final Paint textShadowPaint;
    private final Paint particlePaint;
    private final Paint versionPaint;
    private final Paint parallaxPaint;
    private final Paint vignettePaint;
    private int lastSizeW = -1, lastSizeH = -1;

    private final RectF[] buttonRects;
    private final String[] buttons = {"Continue", "New Game", "Settings", "Exit"};
    private final int[] buttonStates = {0, 0, 0, 0};

    private static final int PARTICLE_COUNT = 40;
    private static final int MAX_PARTICLES = 60;
    private static final float DEFAULT_SCREEN_W = 1080f;
    private static final float DEFAULT_SCREEN_H = 1920f;
    private float screenW = DEFAULT_SCREEN_W;
    private float screenH = DEFAULT_SCREEN_H;
    private final float[] pX, pY, pSpeed, pSize, pAlpha, pLife;
    private final int[] pColor;

    private Bitmap parallaxBg;

    private float centerX;
    private float btnY;
    private int hoverIndex = -1;
    private boolean hasSave = false;

    private static final float BTN_W = 280f;
    private static final float BTN_H = 64f;
    private static final float BTN_SPACING = 80f;
    private static final int BG_COLOR = 0xFF0A0A15;
    private static final int TITLE_COLOR = 0xFFFFD700;
    private static final int SUBTITLE_COLOR = 0xFF00E5FF;
    private static final int BTN_COLOR = 0xFF1A1A2E;
    private static final int BTN_HOVER_COLOR = 0xFF252545;
    private static final int BTN_BORDER_COLOR = 0xFF448AFF;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int[] PARTICLE_COLORS = {
        0xFFFFD700, 0xFF00E5FF, 0xFFB388FF, 0xFFFF5252, 0xFF69F0AE
    };
    private static final String TITLE_TEXT = "IDLE SECT";

    private final RectF tempRect;
    private final Rect srcRect;
    private final RectF dstRect;

    private Bitmap titleGlowBitmap;
    private static final int GLOW_BMP_H = 220;
    private static final int GLOW_BASELINE_Y = 160;

    private int cachedParallaxDrawW = -1, cachedParallaxDrawH = -1;

    public MenuScene(Context ctx) {
        this.context = ctx;

        bgPaint = new Paint();
        bgPaint.setColor(BG_COLOR);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(TITLE_COLOR);
        titlePaint.setTextSize(72f);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setShadowLayer(8f, 0f, 4f, 0x40000000);

        titleGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titleGlowPaint.setColor(TITLE_COLOR);
        titleGlowPaint.setTextSize(72f);
        titleGlowPaint.setTextAlign(Paint.Align.CENTER);
        titleGlowPaint.setTypeface(Typeface.DEFAULT_BOLD);

        glowBitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

        subTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subTitlePaint.setColor(SUBTITLE_COLOR);
        subTitlePaint.setTextSize(24f);
        subTitlePaint.setTextAlign(Paint.Align.CENTER);

        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnPaint.setColor(BTN_COLOR);
        btnPaint.setStyle(Paint.Style.FILL);

        btnHoverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnHoverPaint.setColor(BTN_HOVER_COLOR);
        btnHoverPaint.setStyle(Paint.Style.FILL);

        btnBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnBorderPaint.setColor(BTN_BORDER_COLOR);
        btnBorderPaint.setStyle(Paint.Style.STROKE);
        btnBorderPaint.setStrokeWidth(2f);

        btnGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnGlowPaint.setColor(0x33448AFF);
        btnGlowPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        textShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textShadowPaint.setColor(0x80000000);
        textShadowPaint.setTextSize(24f);
        textShadowPaint.setTextAlign(Paint.Align.CENTER);
        textShadowPaint.setTypeface(Typeface.DEFAULT_BOLD);

        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        versionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        versionPaint.setColor(0xFF666666);
        versionPaint.setTextSize(12f);
        versionPaint.setTextAlign(Paint.Align.RIGHT);

        parallaxPaint = new Paint();
        parallaxPaint.setFilterBitmap(true);

        vignettePaint = new Paint();

        buttonRects = new RectF[4];
        for (int i = 0; i < 4; i++) buttonRects[i] = new RectF();

        pX = new float[MAX_PARTICLES];
        pY = new float[MAX_PARTICLES];
        pSpeed = new float[MAX_PARTICLES];
        pSize = new float[MAX_PARTICLES];
        pAlpha = new float[MAX_PARTICLES];
        pLife = new float[MAX_PARTICLES];
        pColor = new int[MAX_PARTICLES];

        for (int i = 0; i < PARTICLE_COUNT; i++) resetParticle(i, true);

        tempRect = new RectF();
        srcRect = new Rect();
        dstRect = new RectF();
    }

    public void setCamera(CameraSystem cam) { this.camera = cam; }
    public void setRenderEngine(RenderEngine engine) { this.renderEngine = engine; }

    private void onSizeChanged(int w, int h) {
        if (w <= 0 || h <= 0 || (w == lastSizeW && h == lastSizeH)) return;
        lastSizeW = w; lastSizeH = h;

        float cx = w * 0.5f, cy = h * 0.5f;
        float radius = (float) Math.sqrt(cx * cx + cy * cy) * 1.15f;
        vignettePaint.setShader(new RadialGradient(
            cx, cy, radius,
            new int[]{0x00000000, 0x00000000, 0xFF000000},
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        ));

        bakeTitleGlow(w);
    }

    private void bakeTitleGlow(int w) {
        if (titleGlowBitmap != null && !titleGlowBitmap.isRecycled()) titleGlowBitmap.recycle();
        titleGlowBitmap = Bitmap.createBitmap(Math.max(1, w), GLOW_BMP_H, Bitmap.Config.ARGB_8888);
        Canvas bakeCanvas = new Canvas(titleGlowBitmap);
        float bx = w * 0.5f;
        for (int g = 3; g >= 1; g--) {
            titleGlowPaint.setShadowLayer(16f * g, 0f, 0f, 0x60FFD700);
            bakeCanvas.drawText(TITLE_TEXT, bx, GLOW_BASELINE_Y, titleGlowPaint);
        }
    }

    public void setAtlas(Bitmap atlas, Bitmap parallax) {
        this.parallaxBg = parallax;
        cachedParallaxDrawW = -1;
    }

    private void resetParticle(int i, boolean randomY) {
        pX[i] = RNG.nextFloat(0f, screenW);
        pY[i] = randomY ? RNG.nextFloat(0f, screenH) : -20f;
        pSpeed[i] = RNG.nextFloat(0.3f, 2.5f);
        pSize[i] = RNG.nextFloat(1f, 4f);
        pAlpha[i] = RNG.nextFloat(50f, 200f);
        pLife[i] = RNG.nextFloat(3f, 8f);
        pColor[i] = PARTICLE_COLORS[RNG.nextInt(PARTICLE_COLORS.length)];
    }

    public void setHasSave(boolean has) { this.hasSave = has; }

    public void update(float dt) {
        time += dt;
        parallaxOffset += dt * 5f;
        if (parallaxOffset > 1920f) parallaxOffset = 0f;

        titleY += (targetTitleY - titleY) * 0.05f;
        if (animProgress < 1f) animProgress = Math.min(1f, animProgress + dt * 1.2f);

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            pY[i] += pSpeed[i];
            pLife[i] -= dt;
            pAlpha[i] = Math.max(0, Math.min(255, pAlpha[i] + (float) Math.sin(time * 2f + i) * 2f));
            if (pY[i] > screenH || pLife[i] <= 0) resetParticle(i, false);
        }

        for (int i = 0; i < 4; i++) {
            buttonStates[i] = (i == hoverIndex)
                ? Math.min(10, buttonStates[i] + 1)
                : Math.max(0, buttonStates[i] - 1);
        }
    }

    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        if (canvas == null) return;
        int w = canvas.getWidth(), h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        screenW = w; screenH = h;
        centerX = w * 0.5f;
        btnY = titleY + 160f;

        onSizeChanged(w, h);

        canvas.drawRect(0, 0, w, h, bgPaint);
        renderParallax(canvas, w, h);
        canvas.drawRect(0, 0, w, h, vignettePaint);
        renderParticles(canvas);
        renderTitle(canvas);
        renderButtons(canvas, w, h);
    }

    private void renderParallax(Canvas canvas, int w, int h) {
        if (parallaxBg == null || parallaxBg.isRecycled()) return;
        int pw = parallaxBg.getWidth(), ph = parallaxBg.getHeight();
        if (pw <= 0 || ph <= 0) return;

        if (cachedParallaxDrawW < 0) {
            float scale = Math.max((float) w / pw, (float) h / ph);
            cachedParallaxDrawW = (int) (pw * scale);
            cachedParallaxDrawH = (int) (ph * scale);
        }
        int drawW = cachedParallaxDrawW, drawH = cachedParallaxDrawH;
        int offsetY = (int) (parallaxOffset * 0.3f) % drawH;

        srcRect.set(0, 0, pw, ph);
        dstRect.set((w - drawW) / 2f, -offsetY, (w + drawW) / 2f, drawH - offsetY);
        canvas.drawBitmap(parallaxBg, srcRect, dstRect, parallaxPaint);
        if (offsetY > 0) {
            dstRect.set((w - drawW) / 2f, drawH - offsetY, (w + drawW) / 2f, drawH * 2 - offsetY);
            canvas.drawBitmap(parallaxBg, srcRect, dstRect, parallaxPaint);
        }
    }

    private void renderParticles(Canvas canvas) {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particlePaint.setColor(pColor[i]);
            particlePaint.setAlpha((int) pAlpha[i]);
            float pulse = 1f + (float) Math.sin(time * 3f + i * 0.5f) * 0.2f;
            canvas.drawCircle(pX[i], pY[i], pSize[i] * pulse, particlePaint);
        }
        particlePaint.setAlpha(255);
    }

    private void renderTitle(Canvas canvas) {
        int alpha = (int) (255 * easeOutCubic(animProgress));
        titlePaint.setAlpha(alpha);
        subTitlePaint.setAlpha(alpha);
        textPaint.setAlpha(alpha);

        if (titleGlowBitmap != null && !titleGlowBitmap.isRecycled()) {
            float glowPulse = 0.85f + (float) Math.sin(time * 2f) * 0.15f;
            glowBitmapPaint.setAlpha((int) (alpha * glowPulse * 0.55f));
            canvas.drawBitmap(titleGlowBitmap, 0f, titleY - GLOW_BASELINE_Y, glowBitmapPaint);
        }
        canvas.drawText(TITLE_TEXT, centerX, titleY, titlePaint);

        int shimmer = (int) (128 + 127 * Math.sin(time * 1.5f));
        subTitlePaint.setAlpha((alpha * shimmer) / 255);
        canvas.drawText("Xianxia Immortal Cultivation", centerX, titleY + 55, subTitlePaint);
        subTitlePaint.setAlpha(alpha);
    }

    private void renderButtons(Canvas canvas, int w, int h) {
        int alpha = titlePaint.getAlpha();
        for (int i = 0; i < buttons.length; i++) {
            float by = btnY + i * BTN_SPACING;
            float stateFactor = buttonStates[i] / 10f;
            float currentW = BTN_W + stateFactor * 20f;
            float currentH = BTN_H + stateFactor * 8f;

            buttonRects[i].set(centerX - currentW * 0.5f, by, centerX + currentW * 0.5f, by + currentH);

            if (stateFactor > 0f) {
                btnGlowPaint.setAlpha((int) (40 * stateFactor));
                tempRect.set(buttonRects[i]);
                tempRect.inset(-8f * stateFactor, -4f * stateFactor);
                canvas.drawRoundRect(tempRect, 18f, 18f, btnGlowPaint);
            }

            canvas.drawRoundRect(buttonRects[i], 16f, 16f, stateFactor > 0.5f ? btnHoverPaint : btnPaint);

            if (stateFactor > 0f || i == hoverIndex) {
                btnBorderPaint.setAlpha((int) (100 + stateFactor * 155));
                btnBorderPaint.setStrokeWidth(2f + stateFactor);
                canvas.drawRoundRect(buttonRects[i], 16f, 16f, btnBorderPaint);
                btnBorderPaint.setStrokeWidth(2f);
            }

            textShadowPaint.setAlpha((int) (100 * alpha / 255));
            canvas.drawText(buttons[i], centerX + 2f, by + currentH * 0.5f + 10f, textShadowPaint);
            canvas.drawText(buttons[i], centerX, by + currentH * 0.5f + 8f, textPaint);

            if (i == 0 && !hasSave) {
                textPaint.setAlpha((int) (100 * alpha / 255));
                canvas.drawText("[Locked]", centerX + 80f, by + currentH * 0.5f + 8f, textPaint);
                textPaint.setAlpha(alpha);
            }
        }
        versionPaint.setAlpha(alpha);
        canvas.drawText("v6.0 - Pro Edition", w - 24f, h - 24f, versionPaint);
    }

    private static float easeOutCubic(float t) {
        float inv = 1f - t;
        return 1f - inv * inv * inv;
    }

    private static boolean isInside(float x, float y, RectF rect) {
        return rect != null && x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom;
    }

    public void onTouchDown(float x, float y) {
        hoverIndex = -1;
        for (int i = 0; i < buttonRects.length; i++) {
            if (isInside(x, y, buttonRects[i])) { hoverIndex = i; break; }
        }
    }

    public void onTouchUp(float x, float y) {
        for (int i = 0; i < buttonRects.length; i++) {
            if (isInside(x, y, buttonRects[i])) { handleButtonClick(i); break; }
        }
        hoverIndex = -1;
    }

    private void handleButtonClick(int index) {
        if (!(context instanceof GameActivity)) return;
        final GameActivity activity = (GameActivity) context;
        try {
            com.sect.idle.systems.AudioManager.getInstance(context).playSfx(com.sect.idle.systems.AudioManager.SFX_CLICK);
        } catch (Exception ignored) {}

        switch (index) {
            case 0:
                if (hasSave) {
                    activity.sceneManager.setState(GameState.SECT);
                    try {
                        com.sect.idle.systems.AudioManager.getInstance(context).playBgm(com.sect.idle.systems.AudioManager.THEME_SECT_PEACE);
                    } catch (Exception ignored) {}
                }
                break;
            case 1:
                if (hasSave && activity.getDialogManager() != null) {
                    activity.getDialogManager().showConfirm("Start New Cultivation Path?",
                        "A previous save exists. Starting a new game will overwrite your sect progress. Proceed?",
                        new DialogManager.ConfirmCallback() {
                            @Override
                            public void onConfirm(boolean confirmed) {
                                if (confirmed) {
                                    activity.initNewGame();
                                    activity.sceneManager.setState(GameState.SECT);
                                    try {
                                        com.sect.idle.systems.AudioManager.getInstance(context).playBgm(com.sect.idle.systems.AudioManager.THEME_SECT_PEACE);
                                    } catch (Exception ignored) {}
                                }
                            }
                        });
                } else {
                    activity.initNewGame();
                    activity.sceneManager.setState(GameState.SECT);
                    try {
                        com.sect.idle.systems.AudioManager.getInstance(context).playBgm(com.sect.idle.systems.AudioManager.THEME_SECT_PEACE);
                    } catch (Exception ignored) {}
                }
                break;
            case 2:
                if (activity.getDialogManager() != null) {
                    activity.getDialogManager().showSettingsDialog(new DialogManager.SettingsCallback() {
                        @Override
                        public void onSettingsSaved() {
                            if (activity.getSaveManager() != null) {
                                try { activity.getSaveManager().save(); } catch (Exception ignored) {}
                            }
                        }
                    });
                }
                break;
            case 3:
                if (activity.getDialogManager() != null) {
                    activity.getDialogManager().showConfirm("Exit Game", "Depart from the Immortal Realm?",
                        new DialogManager.ConfirmCallback() {
                            @Override
                            public void onConfirm(boolean confirmed) {
                                if (confirmed) {
                                    activity.finish();
                                }
                            }
                        });
                } else {
                    activity.finish();
                }
                break;
        }
    }
}
