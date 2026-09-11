package com.sect.idle.systems;

import android.content.Context;
import android.content.SharedPreferences;
import com.sect.idle.core.GameConfig;
import com.sect.idle.utils.DataValidator;
import java.util.ArrayList;

/**
 * LogManager v3.1 - Logging with categories, colors, filtering and persistent storage.
 */
public final class LogManager {
    private static final String PREFS = "IdleSectLogs";
    private static final String KEY_LOGS = "log_entries";
    private static final int MAX_LOGS = 200;
    private static final int MAX_LOG_LENGTH = 256;
    private SharedPreferences prefs;
    private ArrayList<LogEntry> logs;
    private static LogManager instance;
    private int[] categoryFilters;
    private boolean filterEnabled;

    private final StringBuilder sbPool = new StringBuilder(1024 * 8);

    public static class LogEntry {
        public long time;
        public String category;
        public String message;
        public int color;
        public int level; // 0=DEBUG, 1=INFO, 2=WARN, 3=ERROR, 4=FATAL

        public LogEntry(long t, String c, String m, int col, int lvl) {
            time = t; category = c; message = m; color = col; level = lvl;
        }
    }

    private LogManager(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        logs = new ArrayList<LogEntry>();
        categoryFilters = new int[16];
        load();
    }

    public static synchronized LogManager get(Context ctx) {
        if (instance == null) instance = new LogManager(ctx);
        return instance;
    }

    public void log(String category, String message, int color, int level) {
        if (!GameConfig.DEBUG && level < 1) return;

        if (message == null) return;
        if (category == null) category = "UNKNOWN";

        message = DataValidator.sanitizeString(message, MAX_LOG_LENGTH);
        category = DataValidator.sanitizeString(category, 16);

        if (filterEnabled && !passesFilter(category, level)) return;

        logs.add(new LogEntry(System.currentTimeMillis(), category, message, color, level));
        if (logs.size() > MAX_LOGS) logs.remove(0);

        if (logs.size() % 10 == 0) save();
    }

    public void log(String category, String message) { log(category, message, 0xFFFFFFFF, 1); }
    public void debug(String m) { log("DEBUG", m, 0xFF888888, 0); }
    public void info(String m) { 
        log("INFO", m, 0xFFAAAAAA, 1); 
        com.sect.idle.utils.ExceptionManager.get().addBreadcrumb("INFO", m);
    }
    public void success(String m) { 
        log("SUCCESS", m, 0xFF4CAF50, 1); 
        com.sect.idle.utils.ExceptionManager.get().addBreadcrumb("SUCCESS", m);
    }
    public void warning(String m) { 
        log("WARN", m, 0xFFFF9800, 2); 
        com.sect.idle.utils.ExceptionManager.get().reportWarning("WARN", m);
    }
    public void error(String m) { 
        log("ERROR", m, 0xFFFF5252, 3); 
        com.sect.idle.utils.ExceptionManager.get().reportException(null, "ERROR", m, com.sect.idle.utils.ExceptionManager.LEVEL_ERROR);
    }
    public void error(String m, Throwable t) {
        log("ERROR", m != null ? m : (t != null ? t.getMessage() : "Error"), 0xFFFF5252, 3);
        com.sect.idle.utils.ExceptionManager.get().reportException(t, "ERROR", m, com.sect.idle.utils.ExceptionManager.LEVEL_ERROR);
    }
    public void fatal(String m) { 
        log("FATAL", m, 0xFFD50000, 4); 
        com.sect.idle.utils.ExceptionManager.get().reportFatal(null, "FATAL", m);
    }
    public void fatal(String m, Throwable t) {
        log("FATAL", m != null ? m : (t != null ? t.getMessage() : "Fatal Error"), 0xFFD50000, 4);
        com.sect.idle.utils.ExceptionManager.get().reportFatal(t, "FATAL", m);
    }
    public void event(String m) { log("EVENT", m, 0xFFFFD700, 1); }
    public void battle(String m) { log("BATTLE", m, 0xFFFF5722, 1); }
    public void story(String m) { log("STORY", m, 0xFFB388FF, 1); }
    public void system(String m) { log("SYSTEM", m, 0xFF00E5FF, 1); }
    public void perf(String m) { log("PERF", m, 0xFF69F0AE, 0); }

    public void setFilter(String[] categories, int minLevel) {
        if (categories == null) return;
        filterEnabled = true;
        java.util.Arrays.fill(categoryFilters, 0);
        for (String cat : categories) {
            if (cat == null) continue;
            int hash = Math.abs(cat.hashCode() % 16);
            categoryFilters[hash] = Math.max(categoryFilters[hash], minLevel);
        }
    }

    public void clearFilter() { filterEnabled = false; }

    private boolean passesFilter(String category, int level) {
        if (category == null) return false;
        int hash = Math.abs(category.hashCode() % 16);
        return level >= categoryFilters[hash];
    }

    public ArrayList<LogEntry> getLogs() { return new ArrayList<LogEntry>(logs); }
    public ArrayList<LogEntry> getLogsByCategory(String cat) {
        ArrayList<LogEntry> filtered = new ArrayList<LogEntry>();
        if (cat == null) return filtered;
        for (LogEntry e : logs) if (e.category.equals(cat)) filtered.add(e);
        return filtered;
    }
    public ArrayList<LogEntry> getLogsByLevel(int minLevel) {
        ArrayList<LogEntry> filtered = new ArrayList<LogEntry>();
        for (LogEntry e : logs) if (e.level >= minLevel) filtered.add(e);
        return filtered;
    }
    public void clear() { logs.clear(); save(); }
    public int getCount() { return logs.size(); }

    private void save() {
        if (prefs == null) return;
        sbPool.setLength(0);

        for (LogEntry e : logs) {
            sbPool.append(e.time).append('|').append(e.category).append('|')
             .append(e.level).append('|').append(e.color).append('|').append(e.message).append('\n');
        }
        prefs.edit().putString(KEY_LOGS, sbPool.toString()).apply();
    }

    private void load() {
        if (prefs == null) return;
        String data = prefs.getString(KEY_LOGS, "");
        if (data.isEmpty()) return;
        String[] lines = data.split("\n");
        for (String line : lines) {
            if (line.length() == 0) continue;
            String[] p = line.split("\\|", 5);
            if (p.length == 5) {
                try {
                    long time = Long.parseLong(p[0]);
                    int level = Integer.parseInt(p[2]);
                    int color = Integer.parseInt(p[3]);
                    level = Math.max(0, Math.min(4, level));
                    logs.add(new LogEntry(time, p[1], p[4], color, level));
                } catch (Exception ignored) {}
            }
        }
        if (logs.size() > MAX_LOGS) {
            logs = new ArrayList<LogEntry>(logs.subList(logs.size() - MAX_LOGS, logs.size()));
        }
    }
}
