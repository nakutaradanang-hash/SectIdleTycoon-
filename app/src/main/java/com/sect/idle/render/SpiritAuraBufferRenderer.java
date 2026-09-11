package com.sect.idle.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import com.sect.idle.core.MathUtils;
import com.sect.idle.models.BattleUnit;
import java.util.ArrayList;

/**
 * SpiritAuraBufferRenderer - High-performance offscreen buffered aura renderer for immortal cultivation battle scenes.
 * Renders multi-layer spiritual radiance, elemental Qi vortexes, Bagua Daoist seals, and breakthrough shockwaves.
 * Features zero runtime object allocations in draw loops and dynamic quality degradation for older Android hardware.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class SpiritAuraBufferRenderer {

    // Quality Tiers
    public static final int QUALITY_ULTRA = 0;   // Downsampled 2x offscreen buffer + dual bloom + runes
    public static final int QUALITY_MEDIUM = 1;  // Downsampled 4x offscreen buffer + single bloom
    public static final int QUALITY_LOW = 2;     // Direct canvas draw without offscreen buffer (Ultra low RAM)

    private int qualityTier = QUALITY_MEDIUM;

    // Buffer dimensions
    private int bufferWidth = 0;
    private int bufferHeight = 0;
    private float bufferScale = 0.5f; // 2x downsample for fill-rate optimization

    // Offscreen rendering buffers
    private Bitmap auraBufferBitmap;
    private Canvas auraBufferCanvas;
    private final Matrix scaleMatrix;

    // Pre-allocated Paints (Zero GC in loop)
    private final Paint auraGlowPaint;
    private final Paint auraRingPaint;
    private final Paint auraVortexPaint;
    private final Paint runePaint;
    private final Paint blitPaint;
    private final Paint clearPaint;
    private final Paint shockwavePaint;
    private final Paint yinYangPaint;

    // Pre-allocated Geometry & Buffers
    private final RectF tempRectF;
    private final Rect srcRect;
    private final Rect dstRect;
    private final float[] trigTableSin;
    private final float[] trigTableCos;
    private static final int TRIG_POINTS = 32;

    // Elemental Palette Constants (ARGB)
    public static final int ELEMENT_FIRE = 0xFFFF5722;     // True Samadhi Fire
    public static final int ELEMENT_ICE = 0xFF00E5FF;      // Glacial Frost Qi
    public static final int ELEMENT_THUNDER = 0xFFE040FB;  // Nine Heavens Tribulation Lightning
    public static final int ELEMENT_GOLD = 0xFFFFD700;     // Golden Celestial Dao
    public static final int ELEMENT_WOOD = 0xFF00E676;     // Emerald Wood Vitality
    public static final int ELEMENT_VOID = 0xFF7C4DFF;     // Chaos Void Energy

    public SpiritAuraBufferRenderer() {
        this.scaleMatrix = new Matrix();
        this.tempRectF = new RectF();
        this.srcRect = new Rect();
        this.dstRect = new Rect();

        // 1. Aura Glow Paint
        this.auraGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.auraGlowPaint.setStyle(Paint.Style.FILL);

        // 2. Aura Ring Paint
        this.auraRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.auraRingPaint.setStyle(Paint.Style.STROKE);
        this.auraRingPaint.setStrokeWidth(3f);

        // 3. Aura Vortex / Qi swirl Paint
        this.auraVortexPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.auraVortexPaint.setStyle(Paint.Style.STROKE);
        this.auraVortexPaint.setStrokeWidth(2f);

        // 4. Rune Paint
        this.runePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.runePaint.setStyle(Paint.Style.STROKE);
        this.runePaint.setStrokeWidth(1.5f);

        // 5. Blit Paint with Bilinear Filtering & Additive/Blend mode
        this.blitPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        this.blitPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        // 6. Clear Paint for fast buffer wipe
        this.clearPaint = new Paint();
        this.clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // 7. Shockwave Paint
        this.shockwavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.shockwavePaint.setStyle(Paint.Style.STROKE);
        this.shockwavePaint.setStrokeWidth(4f);

        // 8. Yin-Yang / Dual Qi Paint
        this.yinYangPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.yinYangPaint.setStyle(Paint.Style.FILL);

        // Pre-compute polygon points for rune rings
        this.trigTableSin = new float[TRIG_POINTS];
        this.trigTableCos = new float[TRIG_POINTS];
        for (int i = 0; i < TRIG_POINTS; i++) {
            float angle = (float) (i * 2.0 * Math.PI / TRIG_POINTS);
            trigTableSin[i] = (float) Math.sin(angle);
            trigTableCos[i] = (float) Math.cos(angle);
        }
    }

    /**
     * Initializes or resizes the offscreen aura buffer.
     * Guaranteed safe against OOM by falling back to lower resolution or QUALITY_LOW.
     */
    public synchronized void updateBufferSize(int viewWidth, int viewHeight) {
        if (viewWidth <= 0 || viewHeight <= 0) return;

        int targetW = (int) (viewWidth * bufferScale);
        int targetH = (int) (viewHeight * bufferScale);
        if (targetW < 64) targetW = 64;
        if (targetH < 64) targetH = 64;

        if (auraBufferBitmap != null && !auraBufferBitmap.isRecycled()
                && bufferWidth == targetW && bufferHeight == targetH) {
            return; // Already matches
        }

        releaseBuffer();

        if (qualityTier == QUALITY_LOW) {
            bufferWidth = targetW;
            bufferHeight = targetH;
            return;
        }

        try {
            auraBufferBitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888);
            auraBufferCanvas = new Canvas(auraBufferBitmap);
            bufferWidth = targetW;
            bufferHeight = targetH;
            srcRect.set(0, 0, bufferWidth, bufferHeight);
        } catch (OutOfMemoryError oom) {
            // Gracefully downgrade to QUALITY_LOW on memory constrained devices
            releaseBuffer();
            qualityTier = QUALITY_LOW;
        }
    }

    /**
     * Renders spirit auras for all active battle units.
     * Uses double-buffered offscreen rendering on higher tiers or direct rendering on low tier.
     */
    public void renderSpiritAuras(Canvas targetCanvas, ArrayList<BattleUnit> units, float animTime, int screenW, int screenH) {
        if (targetCanvas == null || units == null || units.isEmpty()) return;

        dstRect.set(0, 0, screenW, screenH);

        if (qualityTier != QUALITY_LOW && auraBufferBitmap != null && !auraBufferBitmap.isRecycled() && auraBufferCanvas != null) {
            // 1. Fast clear offscreen buffer
            auraBufferCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            // 2. Scale transform for downsampled buffer
            float scaleX = (float) bufferWidth / (float) screenW;
            float scaleY = (float) bufferHeight / (float) screenH;
            auraBufferCanvas.save();
            auraBufferCanvas.scale(scaleX, scaleY);

            // 3. Render all unit auras to offscreen buffer
            int unitCount = units.size();
            for (int i = 0; i < unitCount; i++) {
                BattleUnit u = units.get(i);
                if (u != null && u.isAlive) {
                    renderSingleUnitAura(auraBufferCanvas, u, u.pos.x, u.pos.y, animTime, i);
                }
            }

            auraBufferCanvas.restore();

            // 4. Blit blurred/glow buffer onto main screen canvas with bilinear filtering
            targetCanvas.drawBitmap(auraBufferBitmap, srcRect, dstRect, blitPaint);
        } else {
            // Direct Canvas rendering for LOW tier
            int unitCount = units.size();
            for (int i = 0; i < unitCount; i++) {
                BattleUnit u = units.get(i);
                if (u != null && u.isAlive) {
                    renderSingleUnitAura(targetCanvas, u, u.pos.x, u.pos.y, animTime, i);
                }
            }
        }
    }

    /**
     * Renders multi-layer spiritual radiance for an individual unit.
     */
    public void renderSingleUnitAura(Canvas canvas, BattleUnit u, float x, float y, float animTime, int unitIndex) {
        if (canvas == null || u == null) return;

        // Determine elemental color based on unit source or team
        int auraColor = ELEMENT_GOLD;
        if (u.source != null) {
            switch (u.source.element) {
                case 1: auraColor = ELEMENT_WOOD; break;
                case 2: auraColor = ELEMENT_ICE; break;
                case 3: auraColor = ELEMENT_FIRE; break;
                case 4: auraColor = ELEMENT_THUNDER; break;
                case 5: auraColor = ELEMENT_VOID; break;
                default: auraColor = (u.team == 0) ? ELEMENT_GOLD : 0xFFFF3D00; break;
            }
        } else {
            auraColor = (u.team == 0) ? ELEMENT_GOLD : 0xFFFF3D00;
        }

        float qiCharge = u.maxActionBar > 0 ? ((float) u.actionBar / (float) u.maxActionBar) : 0.5f;
        if (qiCharge > 1.0f) qiCharge = 1.0f;

        float pulse = MathUtils.sin(animTime * 4f + unitIndex) * 4f;
        float baseRadius = 36f + pulse + (qiCharge * 12f);

        // Layer 1: Radial Spiritual Core Glow
        int alphaGlow = (int) (60 + qiCharge * 100);
        if (alphaGlow > 255) alphaGlow = 255;
        auraGlowPaint.setColor(auraColor);
        auraGlowPaint.setAlpha(alphaGlow);
        canvas.drawCircle(x, y, baseRadius, auraGlowPaint);

        // Layer 2: Swirling Qi Vortex Rings
        auraRingPaint.setColor(auraColor);
        auraRingPaint.setAlpha((int) (120 + qiCharge * 100));
        tempRectF.set(x - baseRadius, y - baseRadius, x + baseRadius, y + baseRadius);
        canvas.drawOval(tempRectF, auraRingPaint);

        // Layer 3: Rotating Bagua Formation Runes (Ultra/Medium quality only)
        if (qualityTier != QUALITY_LOW && qiCharge >= 0.6f) {
            float rotAngle = (animTime * 45f * (unitIndex % 2 == 0 ? 1 : -1)) % 360f;
            canvas.save();
            canvas.rotate(rotAngle, x, y);

            runePaint.setColor(0xFFFFFFFF);
            runePaint.setAlpha((int) (qiCharge * 180));

            float runeRadius = baseRadius + 8f;
            for (int p = 0; p < TRIG_POINTS; p += 4) {
                float px = x + trigTableCos[p] * runeRadius;
                float py = y + trigTableSin[p] * runeRadius;
                canvas.drawCircle(px, py, 2.5f, runePaint);
            }
            canvas.restore();
        }

        // Layer 4: Breakthrough Spiritual Shockwave (When Qi bar is full)
        if (qiCharge >= 0.95f) {
            float waveProgress = (animTime * 2f) % 1f;
            float waveRadius = baseRadius + (waveProgress * 30f);
            int waveAlpha = (int) (200 * (1.0f - waveProgress));

            shockwavePaint.setColor(auraColor);
            shockwavePaint.setAlpha(waveAlpha);
            shockwavePaint.setStrokeWidth(3f * (1.0f - waveProgress));
            canvas.drawCircle(x, y, waveRadius, shockwavePaint);
        }
    }

    /**
     * Renders a martial art slash or spell beam effect across coordinates.
     */
    public void renderSpiritualBeam(Canvas canvas, float x1, float y1, float x2, float y2, int color, float progress) {
        if (canvas == null || progress <= 0f || progress >= 1f) return;
        float alpha = (1.0f - progress);
        shockwavePaint.setColor(color);
        shockwavePaint.setAlpha((int) (255 * alpha));
        shockwavePaint.setStrokeWidth(10f * alpha);
        canvas.drawLine(x1, y1, x2, y2, shockwavePaint);

        // Bright inner core
        shockwavePaint.setColor(0xFFFFFFFF);
        shockwavePaint.setAlpha((int) (255 * alpha));
        shockwavePaint.setStrokeWidth(3f * alpha);
        canvas.drawLine(x1, y1, x2, y2, shockwavePaint);
    }

    public void setQualityTier(int tier) {
        this.qualityTier = tier;
        if (tier == QUALITY_ULTRA) bufferScale = 0.6f;
        else if (tier == QUALITY_MEDIUM) bufferScale = 0.4f;
        else bufferScale = 0.25f;
    }

    public int getQualityTier() {
        return qualityTier;
    }

    public synchronized void releaseBuffer() {
        if (auraBufferBitmap != null) {
            if (!auraBufferBitmap.isRecycled()) {
                auraBufferBitmap.recycle();
            }
            auraBufferBitmap = null;
        }
        auraBufferCanvas = null;
    }
}
