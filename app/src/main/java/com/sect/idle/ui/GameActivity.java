package com.sect.idle.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.sect.idle.core.GameConfig;
import com.sect.idle.core.GameState;
import com.sect.idle.core.Vector2;
import com.sect.idle.gameplay.*;
import com.sect.idle.models.*;
import com.sect.idle.systems.*;
import android.util.Log;
import java.util.ArrayList;

public class GameActivity extends Activity {
    private static final String TAG = "GameActivity";
    private GameView gameView;
    private Handler gameHandler;
    private Runnable tickRunnable;

    private SectData data;
    private SaveManager saveManager;
    private DialogManager dialogManager;
    private MarketSystem market;
    private BattleEngine battleEngine;
    public SceneManager sceneManager;

    private int tickCounter = 0;
    private volatile boolean paused = false;
    private volatile boolean destroyed = false;
    private long lastFrameTime = 0;
    private static final long TICK_INTERVAL_MS = 1000;
    private static final int STARTER_COUNT = 3;
    private static final long SAVE_DELAY_MS = 5000;

    private final Vector2 activityTouchWorldPos = new Vector2();

    private final String[] starterNames = {
        "Li Yun","Zhang Wei","Wang Fang","Chen Ming","Liu Hua",
        "Zhao Kai","Sun Mei","Wu Jian","Huang Long","Xiao Feng"
    };
    private final String[] randomEvents = {
        "A wandering merchant visits the sect...",
        "Strange lights appear in the night sky...",
        "An ancient spirit awakens nearby...",
        "Rival sect scouts have been spotted...",
        "A mysterious cultivator seeks audience...",
        "Spiritual energy surges in the mountains..."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.sect.idle.apm.StartupMetricsTracker.get().recordGameActivityCreated();
        com.sect.idle.utils.CrashHandler.init(this).install(this);
        com.sect.idle.utils.ExceptionManager.init(this).installGlobalHandler(this);
        com.sect.idle.utils.ExceptionManager.get().addBreadcrumb("Lifecycle", "GameActivity onCreate started");
        LogManager.get(this).info("onCreate START");

        try {
            gameView = new GameView(this);
            setContentView(gameView);
            LogManager.get(this).info("GameView created");
            gameHandler = new Handler(Looper.getMainLooper());
            data = SectData.getInstance();
            LogManager.get(this).info("SectData instance: " + (data != null));

            saveManager = new SaveManager(this);
            dialogManager = new DialogManager(this);
            sceneManager = new SceneManager(this);
            gameView.setSceneManager(sceneManager);
            LogManager.get(this).info("Managers initialized");

            try {
                com.sect.idle.gameplay.CoroutineGameLoop.get().start();
            } catch (Throwable ignored) {}

            if (sceneManager.getSectScene() != null) {
                sceneManager.getSectScene().setSelectionListener(new SectScene.OnSelectionListener() {
                    @Override
                    public void onDiscipleSelected(final Disciple d) {
                        if (dialogManager == null || d == null) return;
                        dialogManager.showDiscipleDetail(d, new DialogManager.DetailCallback() {
                            @Override
                            public void onAssignTask() {
                                dialogManager.showTaskDialog(d, new DialogManager.TaskCallback() {
                                    @Override
                                    public void onTaskSelected(int task) {
                                        d.currentTask = task;
                                        d.updateEfficiency();
                                        if (data != null) data.recalculateEconomy();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onBuildingTapped(final Building b, boolean isBuilt) {
                        if (dialogManager == null || b == null) return;
                        if (isBuilt && (b.type == GameConfig.BUILD_GARDEN || b.type == GameConfig.BUILD_ALCHEMY)) {
                            Disciple worker = (data != null && data.disciples != null && !data.disciples.isEmpty())
                                ? data.disciples.get(0) : null;
                            if (sceneManager.getSectScene().getSlashMiniGame() != null) {
                                sceneManager.getSectScene().getSlashMiniGame().start(worker, b.type);
                                return;
                            }
                        }
                        dialogManager.showBuildingDialog(b, data, new DialogManager.BuildingCallback() {
                            @Override
                            public void onUpgraded() {
                                if (data != null) data.recalculateEconomy();
                            }
                        });
                    }

                    @Override
                    public void onBattleRequested() {
                        startBattle();
                    }

                    @Override
                    public void onTournamentRequested() {
                        if (dialogManager != null && data != null) {
                            Disciple selected = null;
                            int selIdx = sceneManager.getSectScene().getSelectedDisciple();
                            if (selIdx >= 0 && selIdx < data.disciples.size()) {
                                selected = data.disciples.get(selIdx);
                            }
                            dialogManager.showTournamentDialog(selected, data, new DialogManager.TournamentCallback() {
                                @Override
                                public void onTournamentFinished(TournamentSystem.TournamentResult result) {
                                    if (data != null) data.recalculateEconomy();
                                }
                            });
                        }
                    }

                    @Override
                    public void onWarRequested() {
                        if (dialogManager != null && data != null) {
                            dialogManager.showWarDialog(data, new DialogManager.WarCallback() {
                                @Override
                                public void onWarFinished(WarSystem.WarResult result) {
                                    if (data != null) data.recalculateEconomy();
                                }
                            });
                        }
                    }

                    @Override
                    public void onRecruitRequested() {
                        final Disciple candidate = generateRandomDisciple();
                        if (dialogManager != null) {
                            dialogManager.showRecruit(candidate, new DialogManager.RecruitCallback() {
                                @Override
                                public void onAccept() {
                                    if (data != null) {
                                        data.disciples.add(candidate);
                                        data.recalculateEconomy();
                                        LogManager.get(GameActivity.this).success("Recruited " + candidate.name);
                                    }
                                }
                                @Override
                                public void onReject() {}
                            });
                        }
                    }

                    @Override
                    public void onMarketRequested() {
                        if (dialogManager != null && market != null && data != null) {
                            dialogManager.showMarket(market, data, new DialogManager.MarketCallback() {
                                @Override
                                public void onItemPurchased(int itemIndex, int price) {
                                    if (data != null) data.recalculateEconomy();
                                }
                            });
                        }
                    }

                    @Override
                    public void onMenuRequested() {
                        if (dialogManager != null) {
                            dialogManager.showSettings(new DialogManager.SettingsCallback() {
                                @Override
                                public void onSettingsSaved() {
                                    scheduleSave();
                                }
                            });
                        }
                    }

                    @Override
                    public void onSectHubRequested(final String initialTab) {
                        try {
                            android.content.Intent intent = new android.content.Intent(GameActivity.this, com.sect.idle.hub.SectHubActivity.class);
                            if (initialTab != null) {
                                intent.putExtra(com.sect.idle.hub.SectHubActivity.EXTRA_INITIAL_TAB, initialTab);
                            }
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        } catch (Exception e) {
                            LogManager.get(GameActivity.this).error("Failed to open Sect Hub: " + e.getMessage());
                        }
                    }
                });
            }

            sceneManager.setState(GameState.MENU);
            LogManager.get(this).info("State forced to MENU");

            SaveManager sm = new SaveManager(this);
            boolean hasSave = false;
            try { hasSave = sm.load(); } catch (Exception e) {}
            if (sceneManager.getMenuScene() != null) {
                sceneManager.getMenuScene().setHasSave(hasSave);
            }

            try {
                market = new MarketSystem();
            } catch (Exception e) {
                LogManager.get(this).error("Market init: " + e.getMessage());
            }

            battleEngine = new BattleEngine();
            sceneManager.getBattleScene().setBattle(battleEngine);
            sceneManager.getBattleScene().setBattleListener(new BattleScene.BattleListener() {
                @Override
                public void onBattleEnded(final boolean victory) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int stones = victory ? 250 + (data != null ? data.sectRealm * 50 : 0) : 25;
                            int exp = victory ? 20 + (data != null ? data.sectRealm * 5 : 0) : 5;
                            if (data != null) {
                                if (victory) data.earn(stones, 0, 0);
                                data.sectRealmExp += exp;
                                data.recalculateEconomy();
                            }
                            if (dialogManager != null) {
                                dialogManager.showBattleResult(victory, stones, exp, new DialogManager.BattleResultCallback() {
                                    @Override
                                    public void onReturn() {
                                        sceneManager.setState(GameState.SECT);
                                        try {
                                            AudioManager.getInstance(GameActivity.this).playBgm(AudioManager.THEME_SECT_PEACE);
                                        } catch (Exception ignored) {}
                                    }
                                });
                            } else {
                                sceneManager.setState(GameState.SECT);
                                try {
                                    AudioManager.getInstance(GameActivity.this).playBgm(AudioManager.THEME_SECT_PEACE);
                                } catch (Exception ignored) {}
                            }
                        }
                    });
                }
            });

            boolean loaded = false;
            try {
                LogManager.get(this).info("Loading save...");
                loaded = saveManager.load();
                LogManager.get(this).info("Load result: " + loaded);
            } catch (Exception e) {
                LogManager.get(this).error("Load failed: " + e.toString());
                e.printStackTrace();
            }

            if (!loaded) {
                try {
                    LogManager.get(this).warning("No save found. Init new game");
                    initNewGame();
                    LogManager.get(this).success("InitNewGame finished");
                } catch (Exception e) {
                    LogManager.get(this).error("InitNewGame CRASH: " + e.toString());
                    e.printStackTrace();
                }
            } else {
                LogManager.get(this).success("Save loaded. Go to SECT");
                if (sceneManager.getMenuScene() != null) {
                    sceneManager.getMenuScene().setHasSave(true);
                }
            }

            lastFrameTime = System.currentTimeMillis();
            startGameLoop();
            LogManager.get(this).success("onCreate FINISH. Game loop started");
        } catch (Exception e) {
            LogManager.get(this).error("onCreate FATAL: " + e.toString());
            e.printStackTrace();
            finish();
        }
    }

    public void initNewGame() {
        LogManager.get(this).info("initNewGame START");
        if (data == null) {
            LogManager.get(this).error("data is NULL! Abort");
            return;
        }

        data.reset();
        LogManager.get(this).info("data.reset done. Buildings size: " + data.buildings.size());

        for (int i = 0; i < STARTER_COUNT; i++) {
            Disciple d = generateRandomDisciple();
            switch (i) {
                case 0: d.currentTask = GameConfig.TASK_FARMING; break;
                case 1: d.currentTask = GameConfig.TASK_ALCHEMY; break;
                default: d.currentTask = GameConfig.TASK_GUARD; break;
            }
            d.updateEfficiency();
            data.addDisciple(d);
            LogManager.get(this).info("Added disciple: " + d.name);
        }
        LogManager.get(this).info("Total disciples: " + data.disciples.size());

        if (data.buildings != null && !data.buildings.isEmpty()) {
            data.buildings.get(0).build();
            LogManager.get(this).info("First building built");
        } else {
            LogManager.get(this).warning("buildings list is empty! Skip build");
        }

        data.recalculateEconomy();

        if (sceneManager.getSectScene() != null && sceneManager.getSectScene().getCamera() != null) {
            sceneManager.getSectScene().getCamera().pos.set(2000f, 2000f);
            LogManager.get(this).info("Camera centered to 2000,2000");
        }

        LogManager.get(this).success("initNewGame END");
    }

    private Disciple generateRandomDisciple() {
        Disciple d = new Disciple();
        d.name = RNG.pick(starterNames);
        d.isMale = RNG.chance(50);
        d.initStats(
            RNG.nextInt(10, 40), RNG.nextInt(10, 40), RNG.nextInt(10, 40),
            RNG.nextInt(5, 30), RNG.nextInt(10, 30), RNG.nextInt(10, 30), RNG.nextInt(5, 25)
        );
        d.realm = RNG.nextInt(2);
        d.realmExp = RNG.nextInt(500);
        d.element = RNG.nextInt(GameConfig.ELEM_COUNT);
        d.personality = RNG.nextInt(GameConfig.PER_COUNT);

        d.colorTint = GameConfig.getElementColor(d.element);

        try {
            TalentSystem.generateTalent(d);
        } catch (Exception e) {
            LogManager.get(this).error("Talent gen failed: " + e.getMessage());
            d.talentType = GameConfig.TAL_NONE;
        }

        try {
            HobbySystem.assignRandomHobby(d);
        } catch (Exception e) {
            LogManager.get(this).error("Hobby assign failed: " + e.getMessage());
            d.hobby = 0;
        }

        d.dailyWage = d.getExpectedWage();
        d.recalcCombat();
        d.updateEfficiency();

        d.position.set(2000f + RNG.nextFloat(-120, 120), 2000f + RNG.nextFloat(-120, 120));
        d.targetPos.set(d.position.x, d.position.y);

        return d;
    }

    public void startBattle() {
        if (battleEngine == null) {
            battleEngine = new BattleEngine();
        }
        ArrayList<Disciple> playerTeam = new ArrayList<Disciple>();
        if (data != null && data.disciples != null && !data.disciples.isEmpty()) {
            int count = Math.min(3, data.disciples.size());
            for (int i = 0; i < count; i++) {
                Disciple d = data.disciples.get(i);
                if (d != null && d.isAlive()) {
                    playerTeam.add(d);
                }
            }
        }
        if (playerTeam.isEmpty()) {
            Disciple defender = new Disciple("Sect Guardian");
            defender.initStats(30, 25, 25, 20, 25, 20, 15);
            defender.recalcCombat();
            playerTeam.add(defender);
        }

        ArrayList<Disciple> enemyTeam = new ArrayList<Disciple>();
        int realm = data != null ? data.sectRealm : 0;
        Disciple enemy1 = new Disciple("Corrupted Fiend");
        enemy1.initStats(20 + realm * 8, 18 + realm * 6, 15 + realm * 5, 10 + realm * 4, 15 + realm * 5, 15 + realm * 5, 10);
        enemy1.recalcCombat();
        enemyTeam.add(enemy1);

        Disciple enemy2 = new Disciple("Demonic Wolf");
        enemy2.initStats(16 + realm * 6, 22 + realm * 7, 12 + realm * 4, 10 + realm * 4, 12 + realm * 4, 12 + realm * 4, 8);
        enemy2.recalcCombat();
        enemyTeam.add(enemy2);

        battleEngine.startBattle(playerTeam, enemyTeam);
        if (sceneManager != null) {
            if (sceneManager.getBattleScene() != null) {
                sceneManager.getBattleScene().setBattle(battleEngine);
                sceneManager.getBattleScene().reset();
            }
            sceneManager.setState(GameState.BATTLE);
        }

        try {
            AudioManager.getInstance(this).playBgm(AudioManager.THEME_COMBAT_INTENSE);
            AudioManager.getInstance(this).playSfx(AudioManager.SFX_COMBAT_START);
        } catch (Exception ignored) {}
    }

    private void startGameLoop() {
        LogManager.get(this).info("Choreographer started");

        tickRunnable = new Runnable() {
            @Override
            public void run() {
                if (!paused && !destroyed) {
                    try {
                        processTick();
                    } catch (Exception e) {
                        LogManager.get(GameActivity.this).error("Tick: " + e.toString());
                    }
                }
                if (!destroyed && gameHandler != null) {
                    gameHandler.postDelayed(this, TICK_INTERVAL_MS);
                }
            }
        };
        gameHandler.post(tickRunnable);
    }

    private void processTick() {
        if (data == null) return;
        tickCounter++;
        if (data.time != null) data.time.tick();

        if (tickCounter >= GameConfig.DAY_TICKS) {
            tickCounter = 0;
            processDay();
        }

        if (market != null) {
            try {
                market.tick();
            } catch (Exception e) {
                LogManager.get(this).error("Market tick: " + e.getMessage());
            }
        }

        if (RNG.chance(5)) triggerRandomEvent();

        if (data.shouldAutoSave() && data.autoSave) {
            scheduleSave();
        }
    }

    private void processDay() {
        if (data.disciples == null) return;
        for (int i = data.disciples.size() - 1; i >= 0; i--) {
            try {
                Disciple d = data.disciples.get(i);
                if (d == null) {
                    data.disciples.remove(i);
                    continue;
                }
                d.dailyTick();
                try { AgeSystem.ageTick(d); } catch (Exception e) { Log.e(TAG, "Age tick: " + e.getMessage()); }

                if (!d.isAlive()) {
                    data.disciples.remove(i);
                    LogManager.get(this).event(d.name + " has passed away...");
                    data.markEconomyDirty();
                    continue;
                }
                data.earn(d.getTaskIncome(), 0, 0);
                data.spend(d.dailyWage, 0, 0);
                try { BehaviorEngine.updateBehavior(d, data.disciples); } catch (Exception e) { Log.e(TAG, "Behavior: " + e.getMessage()); }
                if (!d.hasFairy && RNG.chance(1)) { try { FairySystem.summonFairy(d); } catch (Exception e) { Log.e(TAG, "Fairy: " + e.getMessage()); } }
                if (RNG.chance(10)) { try { TalentSystem.applyGrowth(d); } catch (Exception e) { Log.e(TAG, "Growth: " + e.getMessage()); } }
            } catch (Exception e) {
                Log.e(TAG, "Disciple tick: " + e.getMessage());
            }
        }

        int cultCount = data.getActiveTaskCount(GameConfig.TASK_CULTIVATION);
        if (cultCount > 0) {
            data.sectRealmExp += cultCount * 2;
            if (data.sectRealmExp >= GameConfig.REALM_EXP_CAP) {
                data.sectRealmExp = 0;
                if (data.sectRealm < GameConfig.REALM_MAX - 1) data.sectRealm++;
            }
        }

        for (int i = 0; i < data.buildings.size(); i++) {
            Building b = data.buildings.get(i);
            if (b != null && b.isBuilt) {
                data.earn(b.getDailyIncome(), 0, 0);
            }
        }
        data.recalculateEconomy();

        if (market != null) {
            try { market.updatePrices(); } catch (Exception e) { Log.e(TAG, "Market prices: " + e.getMessage()); }
        }

        try {
            SecretManager.checkUnlock("ancient_tome");
            SecretManager.checkUnlock("dragon_vein");
        } catch (Exception e) {
            Log.e(TAG, "Secret: " + e.getMessage());
        }
        data.checkQuests(0, 1);
    }

    private void triggerRandomEvent() {
        LogManager.get(this).event(RNG.pick(randomEvents));
    }

    private void scheduleSave() {
        if (saveManager == null) return;
        gameHandler.removeCallbacks(saveRunnable);
        gameHandler.postDelayed(saveRunnable, SAVE_DELAY_MS);
    }

    private final Runnable saveRunnable = new Runnable() {
        @Override
        public void run() {
            if (!destroyed && saveManager != null) {
                try {
                    saveManager.save();
                } catch (Exception e) {
                    LogManager.get(GameActivity.this).error("Auto-save: " + e.getMessage());
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
        LogManager.get(this).info("onPause");
        try {
            com.sect.idle.gameplay.CoroutineGameLoop.get().pause();
        } catch (Throwable ignored) {}
        if (gameView != null) gameView.setPaused(true);
        if (data != null && data.autoSave && saveManager != null) {
            try { saveManager.save(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gameView != null) gameView.setPaused(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        destroyed = false;
        lastFrameTime = System.currentTimeMillis();
        LogManager.get(this).info("onResume");
        try {
            com.sect.idle.gameplay.CoroutineGameLoop.get().resume();
        } catch (Throwable ignored) {}
        if (gameView != null) gameView.setPaused(false);
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        com.sect.idle.utils.ExceptionManager.get().addBreadcrumb("Lifecycle", "GameActivity onDestroy");
        LogManager.get(this).warning("onDestroy");
        try {
            com.sect.idle.gameplay.CoroutineGameLoop.get().stop();
        } catch (Throwable ignored) {}
        super.onDestroy();
        if (gameHandler != null) {
            gameHandler.removeCallbacks(tickRunnable);
            gameHandler.removeCallbacks(saveRunnable);
        }
        if (data != null && data.autoSave && saveManager != null) {
            try { saveManager.save(); } catch (Exception e) { e.printStackTrace(); }
        }
        if (saveManager != null) { try { saveManager.shutdown(); } catch (Exception e) { e.printStackTrace(); } }
        if (gameView != null) { gameView.shutdown(); }
        if (dialogManager != null) { dialogManager.dismissAll(); }
        if (sceneManager != null && sceneManager.getSectScene() != null) {
            sceneManager.getSectScene().destroy();
        }
        com.sect.idle.utils.ExceptionManager.get().shutdown();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LogManager.get(this).warning("onLowMemory");
        if (gameView != null && gameView.getEffects() != null) {
            gameView.getEffects().clear();
        }
        System.gc();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_RUNNING_CRITICAL) {
            LogManager.get(this).warning("onTrimMemory: " + level);
            if (gameView != null && gameView.getEffects() != null) {
                gameView.getEffects().clear();
            }
            System.gc();
        }
    }

    public void onTouchDown(float x, float y) {
        if (sceneManager != null) sceneManager.onTouchDown(x, y);
    }

    public void onTouchUp(float x, float y) {
        if (sceneManager != null) sceneManager.onTouchUp(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            onTouchDown(event.getX(), event.getY());
        } else if (action == MotionEvent.ACTION_UP) {
            onTouchUp(event.getX(), event.getY());
        }
        return true;
    }

    private void screenToWorldSafe(float sx, float sy) {
        if (gameView != null && gameView.getCamera() != null) {
            activityTouchWorldPos.x = gameView.getCamera().screenToWorldX(sx);
            activityTouchWorldPos.y = gameView.getCamera().screenToWorldY(sy);
        } else {
            activityTouchWorldPos.x = sx;
            activityTouchWorldPos.y = sy;
        }
    }

    @Override
    public void onBackPressed() {
        com.sect.idle.utils.ExceptionManager.get().addBreadcrumb("Navigation", "onBackPressed triggered");
        if (com.sect.idle.ui.UINavigationManager.getInstance().canNavigateBack()) {
            com.sect.idle.ui.UINavigationManager.getInstance().handleBack();
            return;
        }
        if (dialogManager != null && dialogManager.hasActiveDialog()) {
            dialogManager.dismissTopDialog();
            return;
        }

        if (sceneManager != null) {
            GameState currentState = sceneManager.getState();
            if (currentState == GameState.BATTLE) {
                sceneManager.setState(GameState.SECT);
                return;
            } else if (currentState == GameState.SECT) {
                if (dialogManager != null) {
                    dialogManager.showConfirm("Return to Menu", "Save cultivation progress and return to the Title Menu?", new DialogManager.ConfirmCallback() {
                        @Override
                        public void onConfirm(boolean confirmed) {
                            if (confirmed) {
                                if (saveManager != null) {
                                    try { saveManager.save(); } catch (Exception ignored) {}
                                }
                                if (sceneManager != null) {
                                    sceneManager.setState(GameState.MENU);
                                }
                            }
                        }
                    });
                    return;
                }
            } else if (currentState == GameState.MENU) {
                if (dialogManager != null) {
                    dialogManager.showConfirm("Exit Game", "Depart from the Immortal Cultivation Realm?", new DialogManager.ConfirmCallback() {
                        @Override
                        public void onConfirm(boolean confirmed) {
                            if (confirmed) {
                                finish();
                            }
                        }
                    });
                    return;
                }
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public DialogManager getDialogManager() { return dialogManager; }
    public SectData getData() { return data; }
    public SaveManager getSaveManager() { return saveManager; }
}
