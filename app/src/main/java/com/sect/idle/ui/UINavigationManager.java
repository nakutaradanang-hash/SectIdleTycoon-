package com.sect.idle.ui;

import android.app.Dialog;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Global UI Navigation Manager for Sect Idle.
 * Provides a unified navigation stack across all game panels and dialogs,
 * ensuring consistent 'Back' and 'Close' button handling throughout the game.
 */
public final class UINavigationManager {
    private static final String TAG = "UINavigationManager";
    private static UINavigationManager instance;

    public enum PanelType {
        NONE,
        BUILDING_DETAIL,
        BUILDING_MANAGEMENT,
        DISCIPLE_DETAIL,
        TASK_ASSIGN,
        RECRUIT,
        MARKET,
        MARKET_TRADING,
        SETTINGS,
        TOURNAMENT,
        WAR_CAMPAIGN,
        WAR_CONQUEST,
        STORY_EVENT,
        BATTLE_RESULT,
        RENAME_SECT,
        SECT_RENAME,
        REDEEM_CODE,
        REDEEM_DAO_CODE,
        SAVE_TRANSFER,
        DIAGNOSTICS,
        DIAGNOSTICS_TELEMETRY,
        APM_CONSOLE,
        ALERT_CONFIRM
    }

    public static class NavEntry {
        public final PanelType type;
        public final String title;
        public final Dialog dialog;
        public final Runnable onBackAction;
        public final Runnable onCloseAction;

        public NavEntry(PanelType type, String title, Dialog dialog, Runnable onBackAction, Runnable onCloseAction) {
            this.type = type != null ? type : PanelType.NONE;
            this.title = title != null ? title : "";
            this.dialog = dialog;
            this.onBackAction = onBackAction;
            this.onCloseAction = onCloseAction;
        }
    }

    public interface NavListener {
        void onPanelOpened(NavEntry entry, int depth);
        void onPanelClosed(NavEntry entry, int depth);
    }

    private final Stack<NavEntry> navStack = new Stack<NavEntry>();
    private final List<NavListener> listeners = new ArrayList<NavListener>();
    private boolean isNavigating = false;

    private UINavigationManager() {}

    public static synchronized UINavigationManager getInstance() {
        if (instance == null) {
            instance = new UINavigationManager();
        }
        return instance;
    }

    public synchronized void pushPanel(PanelType type, String title, Dialog dialog, Runnable onBackAction, Runnable onCloseAction) {
        NavEntry entry = new NavEntry(type, title, dialog, onBackAction, onCloseAction);
        navStack.push(entry);
        Log.d(TAG, "Pushed panel: " + type + " (" + title + "), stack depth: " + navStack.size());
        notifyOpened(entry);
    }

    public synchronized void pushPanel(PanelType type, Dialog dialog, String title, Runnable onBackAction, Runnable onCloseAction) {
        pushPanel(type, title, dialog, onBackAction, onCloseAction);
    }

    public synchronized boolean canNavigateBack() {
        return !navStack.isEmpty() || hasActivePanel();
    }

    public synchronized boolean handleBack() {
        if (navStack.isEmpty()) {
            return false;
        }

        if (isNavigating) {
            return true;
        }

        try {
            isNavigating = true;
            NavEntry top = navStack.pop();
            Log.d(TAG, "Popped panel: " + top.type + " (" + top.title + "), remaining depth: " + navStack.size());

            if (top.dialog != null && top.dialog.isShowing()) {
                try {
                    top.dialog.dismiss();
                } catch (Exception ignored) {}
            }

            notifyClosed(top);

            if (top.onBackAction != null) {
                top.onBackAction.run();
            }
            return true;
        } finally {
            isNavigating = false;
        }
    }

    public synchronized boolean handleClose() {
        if (navStack.isEmpty()) {
            return false;
        }

        if (isNavigating) {
            return true;
        }

        try {
            isNavigating = true;
            NavEntry top = navStack.pop();
            Log.d(TAG, "Closed panel: " + top.type + " (" + top.title + ")");

            if (top.dialog != null && top.dialog.isShowing()) {
                try {
                    top.dialog.dismiss();
                } catch (Exception ignored) {}
            }

            notifyClosed(top);

            if (top.onCloseAction != null) {
                top.onCloseAction.run();
            }
            return true;
        } finally {
            isNavigating = false;
        }
    }

    public synchronized void closeAll() {
        if (isNavigating) return;
        try {
            isNavigating = true;
            while (!navStack.isEmpty()) {
                NavEntry entry = navStack.pop();
                if (entry.dialog != null && entry.dialog.isShowing()) {
                    try {
                        entry.dialog.dismiss();
                    } catch (Exception ignored) {}
                }
                notifyClosed(entry);
                if (entry.onCloseAction != null) {
                    try {
                        entry.onCloseAction.run();
                    } catch (Exception ignored) {}
                }
            }
        } finally {
            isNavigating = false;
        }
    }

    public synchronized void onDialogDismissed(Dialog d) {
        if (isNavigating || d == null) return;
        for (int i = navStack.size() - 1; i >= 0; i--) {
            NavEntry entry = navStack.get(i);
            if (entry.dialog == d) {
                navStack.remove(i);
                notifyClosed(entry);
                break;
            }
        }
    }

    public synchronized boolean hasActivePanel() {
        for (int i = navStack.size() - 1; i >= 0; i--) {
            NavEntry entry = navStack.get(i);
            if (entry.dialog != null && entry.dialog.isShowing()) {
                return true;
            }
        }
        return false;
    }

    public synchronized NavEntry getTopEntry() {
        return navStack.isEmpty() ? null : navStack.peek();
    }

    public synchronized PanelType getCurrentPanelType() {
        return navStack.isEmpty() ? PanelType.NONE : navStack.peek().type;
    }

    public synchronized int getStackDepth() {
        return navStack.size();
    }

    public synchronized void addListener(NavListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public synchronized void removeListener(NavListener l) {
        if (l != null) {
            listeners.remove(l);
        }
    }

    private void notifyOpened(NavEntry entry) {
        for (int i = 0; i < listeners.size(); i++) {
            try {
                listeners.get(i).onPanelOpened(entry, navStack.size());
            } catch (Exception ignored) {}
        }
    }

    private void notifyClosed(NavEntry entry) {
        for (int i = 0; i < listeners.size(); i++) {
            try {
                listeners.get(i).onPanelClosed(entry, navStack.size());
            } catch (Exception ignored) {}
        }
    }
}
