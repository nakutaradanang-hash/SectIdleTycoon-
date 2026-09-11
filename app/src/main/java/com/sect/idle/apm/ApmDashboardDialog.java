package com.sect.idle.apm;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.sect.idle.ui.UINavigationManager;
import com.sect.idle.utils.HapticManager;
import java.util.List;
import java.util.Locale;

/**
 * ApmDashboardDialog - Interactive In-App APM & Telemetry Console.
 * Allows developers and QA to inspect real-time CPU, RAM, FPS, Leaks, GC pauses,
 * and triggered Early-Warning Alerts.
 */
public final class ApmDashboardDialog {

    public static void show(final Context context) {
        if (context == null) return;

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        final ApmManager apm = ApmManager.getInstance();

        // Root Container
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xF010141D); // Dark slate background
        bg.setCornerRadius(24f);
        bg.setStroke(2, 0xFF00E5FF); // Neon cyan border
        root.setBackground(bg);

        // Header Title
        TextView tvTitle = new TextView(context);
        tvTitle.setText("⚡ APM Real-Time Performance Monitor");
        tvTitle.setTextColor(0xFF00E5FF);
        tvTitle.setTextSize(18f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setGravity(Gravity.CENTER);
        root.addView(tvTitle);

        TextView tvSub = new TextView(context);
        tvSub.setText("Live System Telemetry, Error Velocity & Leak Watcher");
        tvSub.setTextColor(0xFF88A0B8);
        tvSub.setTextSize(12f);
        tvSub.setGravity(Gravity.CENTER);
        tvSub.setPadding(0, 4, 0, 16);
        root.addView(tvSub);

        // Scrollable Content
        ScrollView scrollView = new ScrollView(context);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        // Telemetry Card
        final TextView tvMetrics = new TextView(context);
        tvMetrics.setTextColor(0xFFE2E8F0);
        tvMetrics.setTextSize(13f);
        tvMetrics.setTypeface(Typeface.MONOSPACE);
        tvMetrics.setPadding(16, 16, 16, 16);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0xFF1E293B);
        cardBg.setCornerRadius(16f);
        tvMetrics.setBackground(cardBg);
        content.addView(tvMetrics);

        // Alerts Header
        TextView tvAlertHeader = new TextView(context);
        tvAlertHeader.setText("🚨 Early Warning Alerts & Anomalies");
        tvAlertHeader.setTextColor(0xFFFFD700);
        tvAlertHeader.setTextSize(14f);
        tvAlertHeader.setTypeface(Typeface.DEFAULT_BOLD);
        tvAlertHeader.setPadding(4, 16, 4, 8);
        content.addView(tvAlertHeader);

        // Alerts List Container
        final LinearLayout layoutAlerts = new LinearLayout(context);
        layoutAlerts.setOrientation(LinearLayout.VERTICAL);
        content.addView(layoutAlerts);

        scrollView.addView(content);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        root.addView(scrollView, scrollParams);

        // Action Buttons Row
        LinearLayout actionRow = new LinearLayout(context);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setPadding(0, 16, 0, 0);

        Button btnGc = new Button(context);
        btnGc.setText("🧹 Force GC");
        btnGc.setTextSize(12f);
        btnGc.setBackgroundColor(0xFF334155);
        btnGc.setTextColor(0xFF38BDF8);

        Button btnExport = new Button(context);
        btnExport.setText("📋 Export JSON");
        btnExport.setTextSize(12f);
        btnExport.setBackgroundColor(0xFF334155);
        btnExport.setTextColor(0xFF34D399);

        Button btnClose = new Button(context);
        btnClose.setText("✕ Close");
        btnClose.setTextSize(12f);
        btnClose.setBackgroundColor(0xFF475569);
        btnClose.setTextColor(Color.WHITE);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        btnParams.setMargins(4, 0, 4, 0);
        actionRow.addView(btnGc, btnParams);
        actionRow.addView(btnExport, btnParams);
        actionRow.addView(btnClose, btnParams);
        root.addView(actionRow);

        // Periodic Refresh
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (!dialog.isShowing()) return;

                ApmSnapshot snap = apm.sampleAndEvaluate();
                StringBuilder sb = new StringBuilder();
                sb.append("📊 CPU Usage:\n");
                sb.append("  • Process: ").append(String.format(Locale.US, "%.1f%%", snap.processCpuPercent));
                sb.append(" | System: ").append(String.format(Locale.US, "%.1f%%\n", snap.systemCpuPercent));
                sb.append("  • Active Threads: ").append(snap.activeThreadCount).append("\n\n");

                sb.append("🧠 Memory & Heap:\n");
                sb.append("  • JVM Heap: ").append(snap.usedHeapMb).append("MB / ").append(snap.maxHeapMb).append("MB (");
                sb.append(String.format(Locale.US, "%.1f%%", snap.heapUsagePercent)).append(")\n");
                sb.append("  • Native Heap: ").append(snap.nativeHeapAllocatedMb).append("MB\n");
                sb.append("  • GC Cycles: ").append(snap.gcCountTotal);
                sb.append(" (~").append(snap.gcPauseEstimateMs).append("ms total pause)\n\n");

                sb.append("⚡ Frame Latency & Quality:\n");
                sb.append("  • FPS: ").append(String.format(Locale.US, "%.1f", snap.fps));
                sb.append(" | Avg Frame: ").append(String.format(Locale.US, "%.1fms", snap.avgFrameMs)).append("\n");
                sb.append("  • Max Frame: ").append(String.format(Locale.US, "%.1fms", snap.maxFrameMs)).append("\n");
                sb.append("  • Jank Frames: ").append(snap.jankFrameCount);
                sb.append(" | Freezes: ").append(snap.frozenFrameCount).append("\n\n");

                sb.append("🛑 Error Velocity:\n");
                sb.append("  • Total Exceptions: ").append(snap.totalExceptions).append("\n");
                sb.append("  • NPE: ").append(snap.npeCount).append(" | OOM: ").append(snap.oomCount);
                sb.append(" | Crash: ").append(snap.crashCount).append("\n");
                sb.append("  • Error Rate: ").append(String.format(Locale.US, "%.1f / min", snap.errorsPerMinute)).append("\n\n");

                sb.append("🔍 Leaks & Retention:\n");
                sb.append("  • Retained Leak Candidates: ").append(snap.activeLeakCandidates);

                tvMetrics.setText(sb.toString());

                // Update alerts
                layoutAlerts.removeAllViews();
                List<ApmAlert> alerts = apm.getAlertHistory();
                if (alerts.isEmpty()) {
                    TextView noAlert = new TextView(context);
                    noAlert.setText("✓ All telemetry parameters are within healthy thresholds.");
                    noAlert.setTextColor(0xFF4ADE80);
                    noAlert.setTextSize(12f);
                    noAlert.setPadding(8, 4, 8, 4);
                    layoutAlerts.addView(noAlert);
                } else {
                    for (int i = alerts.size() - 1; i >= Math.max(0, alerts.size() - 5); i--) {
                        ApmAlert a = alerts.get(i);
                        TextView aView = new TextView(context);
                        aView.setText("• " + a.getDisplaySummary() + "\n  → " + a.recommendation);
                        int col = a.severity == ApmAlert.Severity.CRITICAL ? 0xFFFF4444 : 0xFFFFAA00;
                        aView.setTextColor(col);
                        aView.setTextSize(11f);
                        aView.setPadding(6, 4, 6, 6);
                        layoutAlerts.addView(aView);
                    }
                }

                handler.postDelayed(this, 1000L);
            }
        };

        btnGc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HapticManager.getInstance().performClick(v);
                apm.getMemoryWatchdog().triggerSystemGc();
                Toast.makeText(context, "System GC Requested & Memory Sampled", Toast.LENGTH_SHORT).show();
            }
        });

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HapticManager.getInstance().performClick(v);
                String json = apm.exportJsonDiagnostics();
                try {
                    android.content.ClipboardManager cm = (android.content.ClipboardManager) 
                            context.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setText(json);
                        Toast.makeText(context, "APM JSON Diagnostics copied to clipboard!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Throwable ignored) {}
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HapticManager.getInstance().performClick(v);
                UINavigationManager.getInstance().handleClose();
            }
        });

        dialog.setContentView(root);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.92),
                    (int) (context.getResources().getDisplayMetrics().heightPixels * 0.85)
            );
        }

        UINavigationManager.getInstance().pushPanel(
            UINavigationManager.PanelType.APM_CONSOLE,
            "APM Monitor",
            dialog,
            null,
            null
        );

        dialog.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(android.content.DialogInterface d) {
                handler.removeCallbacks(refreshRunnable);
                UINavigationManager.getInstance().onDialogDismissed(dialog);
            }
        });

        dialog.show();
        refreshRunnable.run();
    }
}
