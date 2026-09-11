package com.sect.idle.apm;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.Locale;

/**
 * ApmFloatingOverlay - Minimalist floating HUD overlay showing real-time FPS, CPU, and RAM.
 */
public final class ApmFloatingOverlay {

    private final Context context;
    private final FrameLayout container;
    private TextView tvHud;
    private boolean isAttached = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateRunnable;

    public ApmFloatingOverlay(Context context, FrameLayout parentContainer) {
        this.context = context;
        this.container = parentContainer;

        tvHud = new TextView(context);
        tvHud.setTextSize(10f);
        tvHud.setTextColor(0xFF00FFCC);
        tvHud.setPadding(12, 6, 12, 6);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xCC000000);
        bg.setCornerRadius(12f);
        bg.setStroke(1, 0xFF00FFCC);
        tvHud.setBackground(bg);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.TOP | Gravity.END;
        lp.setMargins(16, 48, 16, 16);
        tvHud.setLayoutParams(lp);

        tvHud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApmDashboardDialog.show(ApmFloatingOverlay.this.context);
            }
        });

        this.updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAttached && tvHud != null) {
                    ApmSnapshot snap = ApmManager.getInstance().sampleAndEvaluate();
                    tvHud.setText(String.format(Locale.US, "FPS: %.0f | CPU: %.0f%% | RAM: %dM",
                            snap.fps, snap.processCpuPercent, snap.usedHeapMb));
                    handler.postDelayed(this, 800L);
                }
            }
        };
    }

    public void show() {
        if (!isAttached && container != null && tvHud != null) {
            container.addView(tvHud);
            isAttached = true;
            handler.post(updateRunnable);
        }
    }

    public void hide() {
        if (isAttached && container != null && tvHud != null) {
            container.removeView(tvHud);
            isAttached = false;
            handler.removeCallbacks(updateRunnable);
        }
    }

    public boolean isVisible() {
        return isAttached;
    }
}
