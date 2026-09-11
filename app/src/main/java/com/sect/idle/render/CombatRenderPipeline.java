package com.sect.idle.render;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import com.sect.idle.models.BattleUnit;
import java.util.ArrayList;

/**
 * CombatRenderPipeline - Low-level optimized canvas buffer rendering for high-FPS combat animations on low-end Android devices.
 * Zero-allocation in draw loops, pure Java 7, and Sketchware Pro v7.0.0 compatible.
 */
public final class CombatRenderPipeline {
    private final Paint slashPaint;
    private final Paint shadowPaint;
    private final Paint auraPaint;
    private final RectF tempRect;
    private final SpiritAuraBufferRenderer spiritAuraRenderer;

    public CombatRenderPipeline() {
        this.slashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.slashPaint.setStyle(Paint.Style.STROKE);
        this.slashPaint.setStrokeWidth(4f);
        this.slashPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        this.shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.shadowPaint.setColor(0x40000000);
        this.shadowPaint.setStyle(Paint.Style.FILL);

        this.auraPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.auraPaint.setStyle(Paint.Style.FILL);

        this.tempRect = new RectF();
        this.spiritAuraRenderer = new SpiritAuraBufferRenderer();
    }

    public SpiritAuraBufferRenderer getSpiritAuraRenderer() {
        return spiritAuraRenderer;
    }

    public void renderCombatants(Canvas canvas, ArrayList<BattleUnit> units, float animTime) {
        if (canvas == null || units == null) return;
        int size = units.size();
        for (int i = 0; i < size; i++) {
            BattleUnit u = units.get(i);
            if (u == null || !u.isAlive) continue;

            float x = u.pos.x;
            float y = u.pos.y;

            // Fast ground shadow
            tempRect.set(x - 20f, y + 25f, x + 20f, y + 35f);
            canvas.drawOval(tempRect, shadowPaint);

            // Aura ring
            if (u.actionBar >= u.maxActionBar) {
                spiritAuraRenderer.renderSingleUnitAura(canvas, u, x, y, animTime, i);
            }
        }
    }

    public void renderSlashEffect(Canvas canvas, float x1, float y1, float x2, float y2, int color, float progress) {
        if (canvas == null || progress <= 0f || progress >= 1f) return;
        slashPaint.setColor(color);
        slashPaint.setAlpha((int)(255 * (1f - progress)));
        slashPaint.setStrokeWidth(6f * (1f - progress));
        canvas.drawLine(x1, y1, x2, y2, slashPaint);
    }
}
