package com.sect.idle.apm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ApmAlert - Data model representing a performance anomaly or system health alert.
 */
public final class ApmAlert {
    public enum Severity {
        INFO,
        WARNING,
        CRITICAL,
        EMERGENCY
    }

    public enum AlertType {
        HIGH_MEMORY,
        HIGH_CPU,
        FRAME_JANK,
        HIGH_ERROR_RATE,
        MEMORY_LEAK_CANDIDATE,
        OOM_HAZARD,
        FREQUENT_GC
    }

    public final long timestamp;
    public final AlertType type;
    public final Severity severity;
    public final String title;
    public final String message;
    public final String recommendation;
    public final double metricValue;
    public final double thresholdValue;

    public ApmAlert(AlertType type, Severity severity, String title, String message, 
                    String recommendation, double metricValue, double thresholdValue) {
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.severity = severity;
        this.title = title != null ? title : "";
        this.message = message != null ? message : "";
        this.recommendation = recommendation != null ? recommendation : "";
        this.metricValue = metricValue;
        this.thresholdValue = thresholdValue;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return sdf.format(new Date(timestamp));
    }

    public String getDisplaySummary() {
        return "[" + getFormattedTime() + "] [" + severity.name() + "] " + title + ": " + message;
    }

    @Override
    public String toString() {
        return getDisplaySummary();
    }
}
