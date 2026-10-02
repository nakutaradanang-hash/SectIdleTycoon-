package com.sect.idle.security;

import android.content.Context;
import com.sect.idle.utils.ExceptionManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SecurityAuditLogger - Forensic Auditing & Intrusion Detection Logger.
 *
 * Capabilities:
 * - Thread-safe circular audit buffer for in-app security dashboard.
 * - Integration with structured ExceptionManager logging.
 * - Severity-graded logging for authentication, cryptographic operations, and attacks (SQLi, XSS).
 *
 * 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class SecurityAuditLogger {

    public static final String EVENT_PIN_UPDATED = "PIN_UPDATED";
    public static final String EVENT_PIN_VERIFIED = "PIN_VERIFIED";
    public static final String EVENT_TOTP_VERIFIED = "TOTP_VERIFIED";
    public static final String EVENT_BACKUP_CODES_GENERATED = "BACKUP_CODES_GENERATED";
    public static final String EVENT_BACKUP_CODE_USED = "BACKUP_CODE_USED";
    public static final String EVENT_MFA_ENABLED = "MFA_ENABLED";
    public static final String EVENT_MFA_DISABLED = "MFA_DISABLED";
    public static final String EVENT_AUTH_FAILED = "AUTH_FAILED";
    public static final String EVENT_AUTH_LOCKOUT = "AUTH_LOCKOUT";
    public static final String EVENT_SQLI_BLOCKED = "SQLI_BLOCKED";
    public static final String EVENT_XSS_BLOCKED = "XSS_BLOCKED";
    public static final String EVENT_TAMPER_DETECTED = "TAMPER_DETECTED";
    public static final String EVENT_DATA_ENCRYPTED = "DATA_ENCRYPTED";
    public static final String EVENT_DATA_DECRYPTED = "DATA_DECRYPTED";

    public static final int SEVERITY_INFO = 1;
    public static final int SEVERITY_WARN = 2;
    public static final int SEVERITY_CRITICAL = 3;

    private static final int MAX_AUDIT_LOGS = 60;
    private static final AuditLogEntry[] LOG_BUFFER = new AuditLogEntry[MAX_AUDIT_LOGS];
    private static int logHead = 0;
    private static int logCount = 0;
    private static final Object BUFFER_LOCK = new Object();

    public static final class AuditLogEntry {
        public final long timestamp;
        public final String eventType;
        public final String details;
        public final int severity;

        public AuditLogEntry(long timestamp, String eventType, String details, int severity) {
            this.timestamp = timestamp;
            this.eventType = eventType != null ? eventType : "SECURITY";
            this.details = details != null ? details : "";
            this.severity = severity;
        }

        public String getFormattedTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
            return sdf.format(new Date(timestamp));
        }

        public String getSeverityTag() {
            switch (severity) {
                case SEVERITY_CRITICAL: return "CRITICAL";
                case SEVERITY_WARN: return "WARN";
                default: return "INFO";
            }
        }
    }

    private SecurityAuditLogger() {}

    public static void logEvent(Context ctx, String eventType, String details, int severity) {
        long now = System.currentTimeMillis();
        AuditLogEntry entry = new AuditLogEntry(now, eventType, details, severity);

        synchronized (BUFFER_LOCK) {
            LOG_BUFFER[logHead] = entry;
            logHead = (logHead + 1) % MAX_AUDIT_LOGS;
            if (logCount < MAX_AUDIT_LOGS) {
                logCount++;
            }
        }

        String logMsg = "[" + entry.getSeverityTag() + "][" + eventType + "] " + details;
        if (severity == SEVERITY_CRITICAL) {
            ExceptionManager.logE("SecurityAudit", logMsg);
        } else if (severity == SEVERITY_WARN) {
            ExceptionManager.logW("SecurityAudit", logMsg);
        } else {
            ExceptionManager.logI("SecurityAudit", logMsg);
        }
    }

    public static List<AuditLogEntry> getRecentLogs() {
        List<AuditLogEntry> list = new ArrayList<AuditLogEntry>();
        synchronized (BUFFER_LOCK) {
            int start = (logHead - logCount + MAX_AUDIT_LOGS) % MAX_AUDIT_LOGS;
            for (int i = 0; i < logCount; i++) {
                int idx = (start + i) % MAX_AUDIT_LOGS;
                if (LOG_BUFFER[idx] != null) {
                    list.add(LOG_BUFFER[idx]);
                }
            }
        }
        return list;
    }

    public static void clear() {
        synchronized (BUFFER_LOCK) {
            logHead = 0;
            logCount = 0;
            for (int i = 0; i < MAX_AUDIT_LOGS; i++) {
                LOG_BUFFER[i] = null;
            }
        }
    }
}
