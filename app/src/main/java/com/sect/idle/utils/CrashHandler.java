package com.sect.idle.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * CrashHandler - Centralized UncaughtExceptionHandler for Sect Idle.
 * Catches application-wide exceptions and writes stack traces to local log files,
 * ensuring no silent failures occur during gameplay cycles.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible (No Lambdas, Zero External Dependencies).
 */
public final class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private static final String LOG_FILE_NAME = "crash_log.txt";
    private static final String RECENT_LOG_FILE_NAME = "crash_log_latest.txt";
    private static final int MAX_LOG_SIZE_BYTES = 512 * 1024; // 512 KB

    private static volatile CrashHandler instance;
    private static final Object INIT_LOCK = new Object();

    private Context appContext;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private boolean isInstalled = false;

    private CrashHandler(Context context) {
        if (context != null) {
            this.appContext = context.getApplicationContext();
        }
    }

    public static CrashHandler init(Context context) {
        if (instance == null) {
            synchronized (INIT_LOCK) {
                if (instance == null) {
                    instance = new CrashHandler(context);
                }
            }
        }
        return instance;
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            synchronized (INIT_LOCK) {
                if (instance == null) {
                    instance = new CrashHandler(null);
                }
            }
        }
        return instance;
    }

    public synchronized void install(Context context) {
        if (isInstalled) return;

        if (this.appContext == null && context != null) {
            this.appContext = context.getApplicationContext();
        }

        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        this.isInstalled = true;

        if (this.appContext != null) {
            ExceptionManager.init(this.appContext).installGlobalHandler(this.appContext);
        }

        Log.i(TAG, "Centralized CrashHandler installed successfully.");
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            String report = buildCrashReport(thread, ex);
            Log.e(TAG, "FATAL UNCAUGHT EXCEPTION:\n" + report);

            writeCrashToFile(report);

            try {
                com.sect.idle.apm.ApmManager.getInstance().recordException(ex, true);
            } catch (Throwable ignored) {}

            if (appContext != null) {
                ExceptionManager.get().reportFatal(ex, "FATAL_CRASH",
                        "Uncaught exception on thread " + (thread != null ? thread.getName() : "unknown"));
            }
        } catch (Throwable inner) {
            Log.e(TAG, "Error while saving crash dump: " + inner.getMessage());
        } finally {
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex);
            }
        }
    }

    /**
     * Safely executes gameplay cycle operations with full logging to prevent silent failures.
     */
    public static void safeRun(String tag, Runnable runnable) {
        if (runnable == null) return;
        try {
            runnable.run();
        } catch (Throwable t) {
            logException(tag, t);
        }
    }

    /**
     * Logs handled or captured exceptions into local storage without crashing the application.
     */
    public static void logException(String tag, Throwable t) {
        if (t == null) return;
        Log.e(TAG, "[" + tag + "] Handled gameplay exception: " + t.getMessage(), t);

        CrashHandler handler = getInstance();
        try {
            com.sect.idle.apm.ApmManager.getInstance().recordException(t, false);
        } catch (Throwable ignored) {}
        if (handler != null && handler.appContext != null) {
            String report = handler.buildHandledReport(tag, t);
            handler.writeCrashToFile(report);
            ExceptionManager.get().report(t, tag, "Handled exception in gameplay loop");
        }
    }

    private String buildCrashReport(Thread t, Throwable ex) {
        StringBuilder sb = new StringBuilder(2048);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

        sb.append("\n======================================================\n");
        sb.append("   SECT IDLE CULTIVATION - UNCAUGHT EXCEPTION LOG      \n");
        sb.append("======================================================\n");
        sb.append("Timestamp:    ").append(sdf.format(new Date())).append("\n");
        sb.append("Thread:       ").append(t != null ? t.getName() : "Unknown").append(" (ID: ")
                .append(t != null ? t.getId() : -1).append(")\n");
        sb.append("Device:       ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        sb.append("Android SDK:  ").append(Build.VERSION.SDK_INT).append(" (").append(Build.VERSION.RELEASE).append(")\n");
        sb.append("Memory:       ").append(ExceptionManager.getMemoryUsageString()).append("\n");
        sb.append("------------------------------------------------------\n");
        sb.append("EXCEPTION:\n");
        if (ex != null) {
            sb.append("Class:   ").append(ex.getClass().getName()).append("\n");
            sb.append("Message: ").append(ex.getMessage()).append("\n");
            sb.append("Stack Trace:\n").append(extractStackTrace(ex)).append("\n");
        } else {
            sb.append("No throwable attached.\n");
        }
        sb.append("======================================================\n");
        return sb.toString();
    }

    private String buildHandledReport(String tag, Throwable ex) {
        StringBuilder sb = new StringBuilder(1024);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

        sb.append("\n--- [HANDLED EXCEPTION: ").append(tag).append("] ---\n");
        sb.append("Timestamp: ").append(sdf.format(new Date())).append("\n");
        sb.append("Thread:    ").append(Thread.currentThread().getName()).append("\n");
        if (ex != null) {
            sb.append("Class:     ").append(ex.getClass().getName()).append("\n");
            sb.append("Message:   ").append(ex.getMessage()).append("\n");
            sb.append("StackTrace:\n").append(extractStackTrace(ex)).append("\n");
        }
        sb.append("------------------------------------------------------\n");
        return sb.toString();
    }

    private synchronized void writeCrashToFile(String logText) {
        if (appContext == null || logText == null || logText.isEmpty()) return;

        FileOutputStream fos = null;
        BufferedWriter writer = null;
        try {
            File filesDir = appContext.getFilesDir();
            if (filesDir == null) return;

            File logFile = new File(filesDir, LOG_FILE_NAME);
            // Rotate log file if it exceeds maximum size
            if (logFile.exists() && logFile.length() > MAX_LOG_SIZE_BYTES) {
                File backupFile = new File(filesDir, "crash_log_old.txt");
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                logFile.renameTo(backupFile);
            }

            fos = new FileOutputStream(logFile, true);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            writer.write(logText);
            writer.newLine();
            writer.flush();

            // Also write latest standalone dump for quick query
            FileOutputStream latestFos = null;
            BufferedWriter latestWriter = null;
            try {
                File latestFile = new File(filesDir, RECENT_LOG_FILE_NAME);
                latestFos = new FileOutputStream(latestFile, false);
                latestWriter = new BufferedWriter(new OutputStreamWriter(latestFos, "UTF-8"));
                latestWriter.write(logText);
                latestWriter.flush();
            } finally {
                if (latestWriter != null) {
                    try { latestWriter.close(); } catch (Throwable ignored) {}
                }
                if (latestFos != null) {
                    try { latestFos.close(); } catch (Throwable ignored) {}
                }
            }

        } catch (Throwable t) {
            Log.e(TAG, "Failed to write to local crash log file: " + t.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Throwable ignored) {}
            }
            if (fos != null) {
                try { fos.close(); } catch (Throwable ignored) {}
            }
        }
    }

    public File getLogFile() {
        if (appContext == null) return null;
        return new File(appContext.getFilesDir(), LOG_FILE_NAME);
    }

    public String readLatestCrashLog() {
        if (appContext == null) return "No application context.";
        File latestFile = new File(appContext.getFilesDir(), RECENT_LOG_FILE_NAME);
        if (!latestFile.exists()) {
            latestFile = new File(appContext.getFilesDir(), LOG_FILE_NAME);
            if (!latestFile.exists()) {
                return "No crash logs recorded.";
            }
        }

        FileInputStream fis = null;
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = new FileInputStream(latestFile);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Throwable t) {
            return "Failed to read crash log: " + t.getMessage();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Throwable ignored) {}
            }
            if (fis != null) {
                try { fis.close(); } catch (Throwable ignored) {}
            }
        }
    }

    private static String extractStackTrace(Throwable t) {
        if (t == null) return "";
        try {
            StringWriter sw = new StringWriter(512);
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.flush();
            return sw.toString();
        } catch (Throwable e) {
            return "Unable to parse trace: " + e.getMessage();
        }
    }
}
