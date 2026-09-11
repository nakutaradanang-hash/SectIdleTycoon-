package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;

import com.sect.idle.R;
import com.sect.idle.core.GameConfig;
import com.sect.idle.gameplay.CoroutineGameLoop;
import com.sect.idle.gameplay.GameEngine;
import com.sect.idle.gameplay.SectData;
import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;
import com.sect.idle.render.Fake3D;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.systems.CameraSystem;
import com.sect.idle.systems.NumberFormatter;
import com.sect.idle.systems.RNG;

import java.util.HashMap;

/**
 * SectScene v6.0 - Completely Overhauled Isometric 3D Xianxia Sect Realm.
 *
 * Visual Highlights:
 *  - 5 Majestic Floating Islands (Archipelago of Mount Shu) instead of repeating tiled wallpaper.
 *  - High-DPI density-responsive Xianxia UI with rich golden Dao borders and legible text.
 *  - Clean circular-framed cultivating disciples with elemental aura halos (Zero black box artifacts).
 *  - Atmospheric celestial skybox with drifting clouds, parallax mist, and ascending Qi particles.
 *  - Responsive bottom navigation cards and top treasury capsule status bar.
 *  - Zero-allocation hot rendering loop optimized for 1GB RAM devices (Itel A70).
 */
public final class SectScene {
    private static final String TAG = "SectScene";

    private final Context appContext;
    private CameraSystem camera;
    private final Fake3D fake3D;
    private final float density;
    private final float scaledDensity;

    // World & Map Center
    public static final float MAP_CENTER_X = 1000f;
    public static final float MAP_CENTER_Y = 1000f;
    private static final float WORLD_BOUND_MIN = 200f;
    private static final float WORLD_BOUND_MAX = 1800f;

    // 5 Celestial Peaks Layout (Centered around Map Center)
    public static final int PEAK_MAIN_HALL = 0;   // Central Apex
    public static final int PEAK_SWORD = 1;       // North-East
    public static final int PEAK_HERB_GARDEN = 2; // South-West
    public static final int PEAK_ALCHEMY = 3;     // South-East
    public static final int PEAK_ORE_MINE = 4;    // North-West
    public static final int PEAK_SPIRIT_POOL = 5; // South Center
    public static final int PEAK_COUNT = 6;

    private final float[] peakX = new float[PEAK_COUNT];
    private final float[] peakY = new float[PEAK_COUNT];
    private final float[] peakRadiusX = new float[PEAK_COUNT];
    private final float[] peakRadiusY = new float[PEAK_COUNT];
    private final int[] peakBuildingTypes = new int[PEAK_COUNT];
    private final String[] peakNames = new String[PEAK_COUNT];
    private final String[] peakSubNames = new String[PEAK_COUNT];

    // Paints
    private final Paint bgPaint, skyGradientPaint, cloudPaint;
    private final Paint islandBasePaint, islandTopPaint, islandRimPaint, islandShadowPaint;
    private final Paint bridgePaint, runeCirclePaint;
    private final Paint buildingGlowPaint, buildingBorderPaint, buildingTagPaint, buildingTagBgPaint;
    private final Paint discipleShadowPaint, discipleAuraPaint, discipleAvatarFramePaint;
    private final Paint hpBgPaint, hpBarPaint, mpBarPaint;
    private final Paint uiBgPaint, uiBorderPaint, uiTextBold, uiTextRegular, uiResourcePillPaint;
    private final Paint lightPaint, particlePaint, floatingTextPaint;
    private final Paint minimapBgPaint, minimapBorderPaint, minimapDotPaint;
    private final Paint dayNightOverlayPaint, topBarGradientPaint, botBarGradientPaint;
    private final Paint tapDebugPaint;

    // Reusable Geometry to eliminate Garbage Collection pauses
    private final RectF r1, r2, r3, r4;
    private final Rect srcRect, dstRect;
    private final Path path1, path2;
    private final StringBuilder sb1, sb2;

    // 3D Bitmaps Cache
    private Bitmap parallaxBg;
    private Bitmap openWorldBg;
    private final Bitmap[] building3DCache = new Bitmap[16];
    private final HashMap<String, Bitmap> disciple3DCache = new HashMap<>();
    private Bitmap cultivatorHeroBitmap;
    private Bitmap lightBakedBitmap;

    // Interactive State
    private String selectedDiscipleId = null;
    private int selectedDisciple = -1;
    private int selectedBuilding = -1;
    private float animTime = 0f;
    private boolean initialCameraCentered = false;

    // Particle Systems
    private static final int MAX_PARTICLES = 40;
    private final float[] ptX = new float[MAX_PARTICLES], ptY = new float[MAX_PARTICLES];
    private final float[] ptVX = new float[MAX_PARTICLES], ptVY = new float[MAX_PARTICLES];
    private final float[] ptLife = new float[MAX_PARTICLES], ptMaxLife = new float[MAX_PARTICLES];
    private final float[] ptSize = new float[MAX_PARTICLES];
    private final int[] ptColor = new int[MAX_PARTICLES];
    private int activeParticles = 0;

    // Floating Text FX
    private static final int MAX_FLOATING = 16;
    private final String[] ftText = new String[MAX_FLOATING];
    private final float[] ftX = new float[MAX_FLOATING], ftY = new float[MAX_FLOATING];
    private final float[] ftLife = new float[MAX_FLOATING], ftMaxLife = new float[MAX_FLOATING];
    private final int[] ftColor = new int[MAX_FLOATING];
    private int activeFloating = 0;

    // Drifting Celestial Clouds
    private static final int CLOUD_COUNT = 6;
    private final float[] cloudX = new float[CLOUD_COUNT], cloudY = new float[CLOUD_COUNT];
    private final float[] cloudSpeed = new float[CLOUD_COUNT], cloudScale = new float[CLOUD_COUNT];
    private final int[] cloudAlpha = new int[CLOUD_COUNT];

    // Hardware & Quality Settings
    private final boolean lowEnd;
    private final boolean enableParticles;
    private final boolean enableLighting;
    private final boolean enableShadows;

    // Dynamic UI Measurements (Scaled to DPI)
    private float uiTopBarHeight;
    private float uiBotBarHeight;

    // Speed Multiplier
    private int speedMultiplierIdx = 0; // 0=1x, 1=2x, 2=5x
    private static final float[] SPEED_VALUES = {1.0f, 2.0f, 5.0f};

    // Day/Night & Weather
    private static final int SKY_NIGHT = 0xFF0D0C1D;
    private static final int SKY_DAWN  = 0xFF1C1330;
    private static final int SKY_DAY   = 0xFF152238;
    private static final int SKY_DUSK  = 0xFF24142B;
    private float cachedDayT = 0f;

    private float lastTapWorldX = Float.NaN, lastTapWorldY = Float.NaN;
    private float tapMarkerLife = 0f;

    public interface OnSelectionListener {
        void onDiscipleSelected(Disciple d);
        void onBuildingTapped(Building b, boolean isBuilt);
        void onBattleRequested();
        void onTournamentRequested();
        void onWarRequested();
        void onRecruitRequested();
        void onMarketRequested();
        void onMenuRequested();
        void onSectHubRequested(String initialTab);
    }
    private OnSelectionListener selectionListener;
    private final SlashMiniGame slashMiniGame;

    public SectScene(Context context) {
        this.appContext = context;
        this.fake3D = new Fake3D();

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        this.density = dm.density > 0f ? dm.density : 1.0f;
        this.scaledDensity = dm.scaledDensity > 0f ? dm.scaledDensity : 1.0f;

        this.uiTopBarHeight = dp(76f);
        this.uiBotBarHeight = dp(66f);

        // Hardware profile check
        boolean isLow = false;
        try {
            android.app.ActivityManager am = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                isLow = am.isLowRamDevice() || am.getMemoryClass() <= 128;
            }
        } catch (Throwable ignored) {}
        this.lowEnd = isLow;
        this.enableParticles = !lowEnd;
        this.enableLighting = !lowEnd;
        this.enableShadows = true;

        int aaFlag = lowEnd ? 0 : Paint.ANTI_ALIAS_FLAG;

        // Background & Sky
        bgPaint = new Paint(); bgPaint.setColor(SKY_NIGHT);
        skyGradientPaint = new Paint(aaFlag);
        cloudPaint = new Paint(aaFlag); cloudPaint.setColor(0x22F0F0F0);

        // Floating Islands & Terrain (Isometric Xianxia Palette)
        islandBasePaint = new Paint(aaFlag); islandBasePaint.setColor(0xFF1E2838); // Dark mountain crag
        islandTopPaint = new Paint(aaFlag); islandTopPaint.setColor(0xFF2A4742);  // Jade mountain plateau
        islandRimPaint = new Paint(aaFlag); islandRimPaint.setColor(0xFFD4AF37);  // Golden Qi boundary
        islandRimPaint.setStyle(Paint.Style.STROKE); islandRimPaint.setStrokeWidth(dp(1.5f));
        islandShadowPaint = new Paint(aaFlag); islandShadowPaint.setColor(0x55000000);

        bridgePaint = new Paint(aaFlag); bridgePaint.setColor(0x6600E5FF);
        bridgePaint.setStyle(Paint.Style.STROKE); bridgePaint.setStrokeWidth(dp(3f));

        runeCirclePaint = new Paint(aaFlag); runeCirclePaint.setColor(0x44D4AF37);
        runeCirclePaint.setStyle(Paint.Style.STROKE); runeCirclePaint.setStrokeWidth(dp(1f));

        // Building Overlays & Tags
        buildingGlowPaint = new Paint(aaFlag);
        buildingBorderPaint = new Paint(aaFlag); buildingBorderPaint.setColor(0xFFD4AF37);
        buildingBorderPaint.setStyle(Paint.Style.STROKE); buildingBorderPaint.setStrokeWidth(dp(2f));

        buildingTagBgPaint = new Paint(aaFlag); buildingTagBgPaint.setColor(0xDD120E22);
        buildingTagPaint = new Paint(aaFlag); buildingTagPaint.setColor(0xFFFFFFFF);
        buildingTagPaint.setTypeface(Typeface.DEFAULT_BOLD);
        buildingTagPaint.setTextSize(sp(11f));
        buildingTagPaint.setTextAlign(Paint.Align.CENTER);

        // Disciples
        discipleShadowPaint = new Paint(aaFlag); discipleShadowPaint.setColor(0x66000000);
        discipleAuraPaint = new Paint(aaFlag); discipleAuraPaint.setStyle(Paint.Style.STROKE);
        discipleAvatarFramePaint = new Paint(aaFlag);

        hpBgPaint = new Paint(); hpBgPaint.setColor(0xAA111111);
        hpBarPaint = new Paint(); hpBarPaint.setColor(0xFF00E5FF); // Cyan Qi
        mpBarPaint = new Paint(); mpBarPaint.setColor(0xFFD4AF37); // Golden Dao

        // UI System
        uiBgPaint = new Paint(aaFlag);
        uiBorderPaint = new Paint(aaFlag); uiBorderPaint.setStyle(Paint.Style.STROKE);
        uiResourcePillPaint = new Paint(aaFlag); uiResourcePillPaint.setColor(0xCC1A1A2E);

        uiTextBold = new Paint(aaFlag);
        uiTextBold.setTypeface(Typeface.DEFAULT_BOLD);
        uiTextBold.setColor(0xFFF0F0F0);
        uiTextBold.setTextSize(sp(13f));

        uiTextRegular = new Paint(aaFlag);
        uiTextRegular.setTypeface(Typeface.DEFAULT);
        uiTextRegular.setColor(0xFFD0D0D0);
        uiTextRegular.setTextSize(sp(11f));

        // Particles & FX
        lightPaint = new Paint(aaFlag);
        particlePaint = new Paint(aaFlag);
        floatingTextPaint = new Paint(aaFlag);
        floatingTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        floatingTextPaint.setTextAlign(Paint.Align.CENTER);
        floatingTextPaint.setShadowLayer(dp(3f), dp(1f), dp(1f), 0xFF000000);

        // Minimap & Day/Night
        minimapBgPaint = new Paint(aaFlag); minimapBgPaint.setColor(0xEE120E24);
        minimapBorderPaint = new Paint(aaFlag); minimapBorderPaint.setColor(0xFFD4AF37);
        minimapBorderPaint.setStyle(Paint.Style.STROKE); minimapBorderPaint.setStrokeWidth(dp(1.2f));
        minimapDotPaint = new Paint(aaFlag);

        dayNightOverlayPaint = new Paint();
        topBarGradientPaint = new Paint(aaFlag);
        botBarGradientPaint = new Paint(aaFlag);

        tapDebugPaint = new Paint(aaFlag); tapDebugPaint.setColor(0xFF00E5FF);
        tapDebugPaint.setStyle(Paint.Style.STROKE); tapDebugPaint.setStrokeWidth(dp(2.5f));

        // Geometric Buffers
        r1 = new RectF(); r2 = new RectF(); r3 = new RectF(); r4 = new RectF();
        srcRect = new Rect(); dstRect = new Rect();
        path1 = new Path(); path2 = new Path();
        sb1 = new StringBuilder(64); sb2 = new StringBuilder(64);

        initPeakPositions();
        initDriftingClouds();

        if (enableLighting) {
            lightBakedBitmap = bakeRadialLightTexture(128);
        }

        loadAssets();
        this.slashMiniGame = new SlashMiniGame();
    }

    private float dp(float val) { return val * density; }
    private float sp(float val) { return val * scaledDensity; }

    /**
     * Initializes the 5+1 Celestial Peaks on the Mount Shu Archipelago.
     */
    private void initPeakPositions() {
        // 0. Central Apex Peak - Grand Palace Hall
        peakX[PEAK_MAIN_HALL] = MAP_CENTER_X;
        peakY[PEAK_MAIN_HALL] = MAP_CENTER_Y - 20f;
        peakRadiusX[PEAK_MAIN_HALL] = 210f;
        peakRadiusY[PEAK_MAIN_HALL] = 135f;
        peakBuildingTypes[PEAK_MAIN_HALL] = GameConfig.BUILD_HALL;
        peakNames[PEAK_MAIN_HALL] = "凌霄宝殿";
        peakSubNames[PEAK_MAIN_HALL] = "Grand Palace";

        // 1. North-East Peak - Scripture & Sword Pavilion
        peakX[PEAK_SWORD] = MAP_CENTER_X + 340f;
        peakY[PEAK_SWORD] = MAP_CENTER_Y - 240f;
        peakRadiusX[PEAK_SWORD] = 160f;
        peakRadiusY[PEAK_SWORD] = 105f;
        peakBuildingTypes[PEAK_SWORD] = GameConfig.BUILD_LIBRARY;
        peakNames[PEAK_SWORD] = "万剑仙峰";
        peakSubNames[PEAK_SWORD] = "Sword Peak";

        // 2. South-West Peak - Spirit Herb Garden
        peakX[PEAK_HERB_GARDEN] = MAP_CENTER_X - 340f;
        peakY[PEAK_HERB_GARDEN] = MAP_CENTER_Y + 200f;
        peakRadiusX[PEAK_HERB_GARDEN] = 175f;
        peakRadiusY[PEAK_HERB_GARDEN] = 115f;
        peakBuildingTypes[PEAK_HERB_GARDEN] = GameConfig.BUILD_GARDEN;
        peakNames[PEAK_HERB_GARDEN] = "药王仙谷";
        peakSubNames[PEAK_HERB_GARDEN] = "Herb Valley";

        // 3. South-East Peak - Nine-Turn Alchemy Pavilion
        peakX[PEAK_ALCHEMY] = MAP_CENTER_X + 340f;
        peakY[PEAK_ALCHEMY] = MAP_CENTER_Y + 200f;
        peakRadiusX[PEAK_ALCHEMY] = 165f;
        peakRadiusY[PEAK_ALCHEMY] = 110f;
        peakBuildingTypes[PEAK_ALCHEMY] = GameConfig.BUILD_ALCHEMY;
        peakNames[PEAK_ALCHEMY] = "九转丹阁";
        peakSubNames[PEAK_ALCHEMY] = "Alchemy Tower";

        // 4. North-West Peak - Spirit Ore Vein
        peakX[PEAK_ORE_MINE] = MAP_CENTER_X - 340f;
        peakY[PEAK_ORE_MINE] = MAP_CENTER_Y - 240f;
        peakRadiusX[PEAK_ORE_MINE] = 160f;
        peakRadiusY[PEAK_ORE_MINE] = 105f;
        peakBuildingTypes[PEAK_ORE_MINE] = GameConfig.BUILD_MINE;
        peakNames[PEAK_ORE_MINE] = "玄晶灵脉";
        peakSubNames[PEAK_ORE_MINE] = "Spirit Ore Mine";

        // 5. South Center - Spirit Pool
        peakX[PEAK_SPIRIT_POOL] = MAP_CENTER_X;
        peakY[PEAK_SPIRIT_POOL] = MAP_CENTER_Y + 360f;
        peakRadiusX[PEAK_SPIRIT_POOL] = 150f;
        peakRadiusY[PEAK_SPIRIT_POOL] = 95f;
        peakBuildingTypes[PEAK_SPIRIT_POOL] = GameConfig.BUILD_SPIRIT_POOL;
        peakNames[PEAK_SPIRIT_POOL] = "灵泉圣池";
        peakSubNames[PEAK_SPIRIT_POOL] = "Spirit Pool";
    }

    private void initDriftingClouds() {
        for (int i = 0; i < CLOUD_COUNT; i++) {
            cloudX[i] = RNG.nextFloat(WORLD_BOUND_MIN, WORLD_BOUND_MAX);
            cloudY[i] = RNG.nextFloat(WORLD_BOUND_MIN, WORLD_BOUND_MAX);
            cloudSpeed[i] = RNG.nextFloat(8f, 22f);
            cloudScale[i] = RNG.nextFloat(140f, 280f);
            cloudAlpha[i] = RNG.nextInt(25, 65);
        }
    }

    private void loadAssets() {
        try {
            parallaxBg = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bg_sect_floating_islands);
        } catch (Throwable ignored) {}

        try {
            cultivatorHeroBitmap = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.img_cultivator_hero);
        } catch (Throwable ignored) {}

        // Preload Building 3D models
        get3DBuildingModel(GameConfig.BUILD_HALL);
        get3DBuildingModel(GameConfig.BUILD_LIBRARY);
        get3DBuildingModel(GameConfig.BUILD_GARDEN);
        get3DBuildingModel(GameConfig.BUILD_ALCHEMY);
        get3DBuildingModel(GameConfig.BUILD_MINE);
        get3DBuildingModel(GameConfig.BUILD_SPIRIT_POOL);
    }

    public void setAtlas(Bitmap atlas, Bitmap tileset, Bitmap parallax, Bitmap walkSheet) {
        if (parallax != null && !parallax.isRecycled()) {
            this.parallaxBg = parallax;
        }
    }

    private Bitmap get3DBuildingModel(int type) {
        int idx = GameConfig.clamp(type, 0, building3DCache.length - 1);
        if (building3DCache[idx] == null || building3DCache[idx].isRecycled()) {
            try {
                int resId;
                switch (type) {
                    case GameConfig.BUILD_HALL:
                        resId = R.drawable.obj_3d_main_hall;
                        break;
                    case GameConfig.BUILD_ALCHEMY:
                        resId = R.drawable.obj_3d_alchemy;
                        break;
                    case GameConfig.BUILD_FORGE:
                        resId = R.drawable.obj_3d_forge;
                        break;
                    case GameConfig.BUILD_SPIRIT_POOL:
                        resId = R.drawable.obj_3d_spirit_pool;
                        break;
                    case GameConfig.BUILD_LIBRARY:
                    case GameConfig.BUILD_TOWER:
                        resId = R.drawable.obj_3d_scripture;
                        break;
                    case GameConfig.BUILD_GARDEN:
                    case GameConfig.BUILD_MINE:
                    default:
                        resId = R.drawable.obj_3d_herb_garden;
                        break;
                }
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                building3DCache[idx] = BitmapFactory.decodeResource(appContext.getResources(), resId, opts);
            } catch (Throwable e) {
                return null;
            }
        }
        return building3DCache[idx];
    }

    private static Bitmap bakeRadialLightTexture(int size) {
        try {
            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            float r = size * 0.5f;
            p.setShader(new RadialGradient(r, r, r, 0xAAFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
            c.drawCircle(r, r, r, p);
            return bmp;
        } catch (Throwable oom) {
            return null;
        }
    }

    private void drawLightGlow(Canvas canvas, float cx, float cy, float radius, int color, int intensityAlpha) {
        if (lightBakedBitmap == null || lightBakedBitmap.isRecycled()) return;
        lightPaint.setColor(color);
        lightPaint.setAlpha(intensityAlpha);
        r1.set(cx - radius, cy - radius, cx + radius, cy + radius);
        canvas.drawBitmap(lightBakedBitmap, null, r1, lightPaint);
    }

    // =========================================================================
    // UPDATE TICK
    // =========================================================================

    public void update(float dt) {
        animTime += dt;
        if (tapMarkerLife > 0f) tapMarkerLife -= dt;

        if (slashMiniGame != null && slashMiniGame.isActive()) {
            slashMiniGame.update(dt);
            return;
        }

        // Update Drifting Celestial Clouds
        for (int i = 0; i < CLOUD_COUNT; i++) {
            cloudX[i] += cloudSpeed[i] * dt;
            if (cloudX[i] > WORLD_BOUND_MAX + 200f) {
                cloudX[i] = WORLD_BOUND_MIN - 200f;
                cloudY[i] = RNG.nextFloat(WORLD_BOUND_MIN, WORLD_BOUND_MAX);
            }
        }

        SectData data = SectData.getInstance();
        if (data == null) return;

        // Update Disciples Movement around the peaks
        if (data.disciples != null) {
            int n = data.disciples.size();
            for (int i = 0; i < n; i++) {
                Disciple d = data.disciples.get(i);
                if (d == null) continue;

                // If disciple has no target or has reached target, assign random peak waypoint
                if (d.position.distSq(d.targetPos) < 16f) {
                    d.isMoving = false;
                    if (RNG.chance(15)) {
                        int targetPeak = RNG.nextInt(PEAK_COUNT);
                        float offsetX = RNG.nextFloat(-peakRadiusX[targetPeak] * 0.5f, peakRadiusX[targetPeak] * 0.5f);
                        float offsetY = RNG.nextFloat(-peakRadiusY[targetPeak] * 0.4f, peakRadiusY[targetPeak] * 0.4f);
                        d.targetPos.set(peakX[targetPeak] + offsetX, peakY[targetPeak] + offsetY);
                    }
                } else {
                    d.moveTo(d.targetPos.x, d.targetPos.y, dt * 45f);
                    d.isMoving = true;
                    d.facing = d.targetPos.x > d.position.x ? 1 : -1;
                }
            }
        }

        if (enableParticles) updateParticles(dt);
        updateFloatingTexts(dt);
    }

    private void updateParticles(float dt) {
        for (int i = activeParticles - 1; i >= 0; i--) {
            ptLife[i] -= dt;
            if (ptLife[i] <= 0) {
                int last = activeParticles - 1;
                if (i < last) {
                    ptX[i] = ptX[last]; ptY[i] = ptY[last];
                    ptVX[i] = ptVX[last]; ptVY[i] = ptVY[last];
                    ptLife[i] = ptLife[last]; ptMaxLife[i] = ptMaxLife[last];
                    ptSize[i] = ptSize[last]; ptColor[i] = ptColor[last];
                }
                activeParticles--;
                continue;
            }
            ptX[i] += ptVX[i] * dt; ptY[i] += ptVY[i] * dt; ptVY[i] -= 4f * dt; // Ascending Qi
        }

        // Spawn Spiritual Qi Motes
        if (activeParticles < MAX_PARTICLES && RNG.chance(40)) {
            int peakIdx = RNG.nextInt(PEAK_COUNT);
            float px = peakX[peakIdx] + RNG.nextFloat(-peakRadiusX[peakIdx], peakRadiusX[peakIdx]);
            float py = peakY[peakIdx] + RNG.nextFloat(-peakRadiusY[peakIdx], peakRadiusY[peakIdx]);
            int color = peakIdx == PEAK_ORE_MINE ? 0xFF00E5FF : (peakIdx == PEAK_ALCHEMY ? 0xFFFF9100 : 0xFFD4AF37);
            spawnParticle(px, py, RNG.nextFloat(-8f, 8f), RNG.nextFloat(-22f, -8f), RNG.nextFloat(2.5f, 5f), color, RNG.nextFloat(1.5f, 3.5f));
        }
    }

    private void updateFloatingTexts(float dt) {
        for (int i = activeFloating - 1; i >= 0; i--) {
            ftLife[i] -= dt; ftY[i] -= 28f * dt;
            if (ftLife[i] <= 0) {
                int last = activeFloating - 1;
                if (i < last) {
                    ftText[i] = ftText[last]; ftX[i] = ftX[last];
                    ftY[i] = ftY[last]; ftLife[i] = ftLife[last];
                    ftMaxLife[i] = ftMaxLife[last]; ftColor[i] = ftColor[last];
                }
                activeFloating--;
            }
        }
    }

    private void spawnParticle(float x, float y, float vx, float vy, float size, int color, float life) {
        if (activeParticles >= MAX_PARTICLES) return;
        int i = activeParticles++;
        ptX[i] = x; ptY[i] = y; ptVX[i] = vx; ptVY[i] = vy;
        ptSize[i] = size; ptColor[i] = color; ptLife[i] = life; ptMaxLife[i] = life;
    }

    public void showFloatingText(String text, float x, float y, int color) {
        if (activeFloating >= MAX_FLOATING) return;
        int i = activeFloating++;
        ftText[i] = text; ftX[i] = x; ftY[i] = y;
        ftColor[i] = color; ftLife[i] = 2.2f; ftMaxLife[i] = 2.2f;
    }

    public void spawnExplosion(float x, float y, int color, int count) {
        if (!enableParticles) return;
        for (int i = 0; i < count && activeParticles < MAX_PARTICLES; i++) {
            float angle = RNG.nextFloat(0f, 6.283f);
            float speed = RNG.nextFloat(25f, 90f);
            spawnParticle(x, y, (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed,
                    RNG.nextFloat(2f, 6f), color, RNG.nextFloat(0.6f, 1.6f));
        }
    }

    // =========================================================================
    // RENDER LOOP
    // =========================================================================

    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        if (canvas == null || cam == null) return;
        SectData data = SectData.getInstance();
        if (data == null) return;
        final int W = canvas.getWidth(), H = canvas.getHeight();
        if (W <= 0 || H <= 0) return;

        this.camera = cam;

        // Auto-center camera comfortably on initial startup so user sees the 5 peaks immediately
        if (!initialCameraCentered) {
            cam.pos.set(MAP_CENTER_X, MAP_CENTER_Y);
            cam.targetPos.set(MAP_CENTER_X, MAP_CENTER_Y);
            cam.zoom = 0.75f;
            cam.targetZoom = 0.75f;
            initialCameraCentered = true;
        }

        if (slashMiniGame != null && slashMiniGame.isActive()) {
            slashMiniGame.render(canvas, W, H);
            return;
        }

        // 1. Celestial Skybox & Parallax Layer (No Repeating Wallpaper)
        renderCelestialSkybox(canvas, cam, W, H, data);

        // 2. Distant Parallax Clouds
        renderDriftingClouds(canvas, cam);

        // 3. Celestial Spirit Bridges Connecting Peaks
        renderCelestialBridges(canvas, cam);

        // 4. 5 Majestic Floating Islands (Archipelago of Mount Shu)
        renderFloatingIslands(canvas, cam, data);

        // 5. 3D Buildings & Facility Halos
        render3DBuildings(canvas, cam, data);

        // 6. Cultivator Disciples
        renderDisciples(canvas, cam, data);

        // 7. Ascending Spiritual Particles & Floating Yield Texts
        if (enableParticles) renderParticles(canvas, cam);
        renderFloatingTexts(canvas, cam);

        // 8. Dynamic Radial Lighting
        if (enableLighting) renderRadialLighting(canvas, cam, data, W, H);

        // 9. Day/Night Atmospheric Shader
        renderDayNightOverlay(canvas, W, H);

        // 10. Modern High-DPI Xianxia UI Suite
        renderTopTreasuryBar(canvas, data, W, H);
        renderBottomNavigationDeck(canvas, W, H);
        renderQuickActionSidebar(canvas, W, H);
        renderMinimap(canvas, data, W, H);

        if (GameConfig.DEBUG) renderTapDebugMarker(canvas, cam);
    }

    public void renderLayered(Canvas[] layers, Paint paint, CameraSystem cam, int W, int H) {
        if (layers != null && layers.length > 0 && layers[0] != null) {
            render(layers[0], paint, cam);
        }
    }

    // =========================================================================
    // SCENE GRAPH & WORLD RENDERING
    // =========================================================================

    private void renderCelestialSkybox(Canvas canvas, CameraSystem cam, int W, int H, SectData data) {
        int skyColor = computeSkyColor(data.time != null ? data.time.hour : 12);
        bgPaint.setColor(skyColor);
        canvas.drawRect(0, 0, W, H, bgPaint);

        // Celestial Horizon Gradient
        skyGradientPaint.setShader(new LinearGradient(
                0, 0, 0, H,
                skyColor, 0xFF080612, Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0, 0, W, H, skyGradientPaint);

        // Draw Parallax Landscape seamlessly scaled across screen
        if (parallaxBg != null && !parallaxBg.isRecycled()) {
            int pw = parallaxBg.getWidth(), ph = parallaxBg.getHeight();
            float parallaxX = (cam.pos.x - MAP_CENTER_X) * 0.05f;
            float parallaxY = (cam.pos.y - MAP_CENTER_Y) * 0.05f;

            float scale = Math.max((float) W / pw, (float) H / ph) * 1.15f;
            int drawW = (int) (pw * scale);
            int drawH = (int) (ph * scale);
            int drawX = (W - drawW) / 2 - (int) parallaxX;
            int drawY = (H - drawH) / 2 - (int) parallaxY;

            dstRect.set(drawX, drawY, drawX + drawW, drawY + drawH);
            srcRect.set(0, 0, pw, ph);

            islandBasePaint.setAlpha(120);
            canvas.drawBitmap(parallaxBg, srcRect, dstRect, islandBasePaint);
            islandBasePaint.setAlpha(255);
        }
    }

    private void renderDriftingClouds(Canvas canvas, CameraSystem cam) {
        for (int i = 0; i < CLOUD_COUNT; i++) {
            float sx = cam.worldToScreenX(cloudX[i]);
            float sy = cam.worldToScreenY(cloudY[i]);
            float scale = cloudScale[i] * cam.zoom;

            cloudPaint.setAlpha(cloudAlpha[i]);
            r1.set(sx - scale, sy - scale * 0.45f, sx + scale, sy + scale * 0.45f);
            canvas.drawOval(r1, cloudPaint);
        }
    }

    private void renderCelestialBridges(Canvas canvas, CameraSystem cam) {
        float centerX = cam.worldToScreenX(peakX[PEAK_MAIN_HALL]);
        float centerY = cam.worldToScreenY(peakY[PEAK_MAIN_HALL] + 20f);

        for (int i = 1; i < PEAK_COUNT; i++) {
            float px = cam.worldToScreenX(peakX[i]);
            float py = cam.worldToScreenY(peakY[i] + 20f);

            // Glowing cyan celestial bridge beam
            bridgePaint.setAlpha(70 + (int) (Math.sin(animTime * 2.5f + i) * 30));
            bridgePaint.setStrokeWidth(dp(3f) * cam.zoom);
            canvas.drawLine(centerX, centerY, px, py, bridgePaint);

            // Outer golden boundary line
            runeCirclePaint.setAlpha(50);
            runeCirclePaint.setStrokeWidth(dp(1f) * cam.zoom);
            canvas.drawLine(centerX, centerY, px, py, runeCirclePaint);
        }
    }

    /**
     * Renders the 5+1 Floating Islands of the Mount Shu Archipelago with isometric depth.
     */
    private void renderFloatingIslands(Canvas canvas, CameraSystem cam, SectData data) {
        float zoom = cam.zoom;

        for (int i = 0; i < PEAK_COUNT; i++) {
            float worldX = peakX[i];
            float worldY = peakY[i];

            // Gentle floating breathing animation
            float floatBob = (float) Math.sin(animTime * 1.8f + i * 1.2f) * 6f;
            float drawWorldY = worldY + floatBob;

            float sx = cam.worldToScreenX(worldX);
            float sy = cam.worldToScreenY(drawWorldY);
            float rx = peakRadiusX[i] * zoom;
            float ry = peakRadiusY[i] * zoom;

            // 1. Floating Island Atmospheric Shadow (Projected on ground below)
            if (enableShadows) {
                float shadowSy = cam.worldToScreenY(worldY + 70f);
                r1.set(sx - rx * 0.9f, shadowSy - ry * 0.4f, sx + rx * 0.9f, shadowSy + ry * 0.4f);
                canvas.drawOval(r1, islandShadowPaint);
            }

            // 2. Floating Mountain Island Base (Cliff body)
            path1.reset();
            path1.moveTo(sx - rx, sy);
            path1.cubicTo(
                    sx - rx * 0.7f, sy + ry * 1.6f,
                    sx, sy + ry * 2.2f,
                    sx, sy + ry * 2.4f
            );
            path1.cubicTo(
                    sx, sy + ry * 2.2f,
                    sx + rx * 0.7f, sy + ry * 1.6f,
                    sx + rx, sy
            );
            path1.close();

            islandBasePaint.setColor(i == PEAK_MAIN_HALL ? 0xFF1C192E : 0xFF1A2230);
            canvas.drawPath(path1, islandBasePaint);

            // 3. Floating Island Top Terrace (Jade Plateau)
            r2.set(sx - rx, sy - ry, sx + rx, sy + ry);
            islandTopPaint.setColor(i == PEAK_HERB_GARDEN ? 0xFF1E4838 : (i == PEAK_ORE_MINE ? 0xFF183344 : 0xFF243B3D));
            canvas.drawOval(r2, islandTopPaint);

            // 4. Golden Dao Perimeter Ring (Protective Qi Boundary)
            islandRimPaint.setAlpha(160 + (int) (Math.sin(animTime * 3f + i) * 60));
            canvas.drawOval(r2, islandRimPaint);

            // 5. Ancient Eight-Trigram Formation Runes
            if (zoom > 0.4f) {
                float runeR = rx * 0.65f;
                runeCirclePaint.setAlpha(80 + (int) (Math.sin(animTime * 2f + i) * 40));
                r3.set(sx - runeR, sy - runeR * 0.6f, sx + runeR, sy + runeR * 0.6f);
                canvas.drawOval(r3, runeCirclePaint);
            }
        }
    }

    /**
     * Renders 3D Building Models on their dedicated peaks with badges, tags, and yield rates.
     */
    private void render3DBuildings(Canvas canvas, CameraSystem cam, SectData data) {
        float zoom = cam.zoom;
        float bldgSize = 130f * zoom;
        float halfBldg = bldgSize * 0.5f;

        for (int i = 0; i < PEAK_COUNT; i++) {
            float worldX = peakX[i];
            float floatBob = (float) Math.sin(animTime * 1.8f + i * 1.2f) * 6f;
            float worldY = peakY[i] + floatBob;

            float sx = cam.worldToScreenX(worldX);
            float sy = cam.worldToScreenY(worldY);

            int bldgType = peakBuildingTypes[i];
            Building b = null;
            if (data.buildings != null) {
                for (int j = 0; j < data.buildings.size(); j++) {
                    Building candidate = data.buildings.get(j);
                    if (candidate != null && candidate.type == bldgType) {
                        b = candidate;
                        break;
                    }
                }
            }

            int bldgLevel = b != null ? Math.max(1, b.level) : 1;
            boolean isBuilt = b == null || b.isBuilt;

            // 1. Building Halo Glow
            int glowColor = getBuildingGlowColor(bldgType);
            if (enableLighting && zoom > 0.4f) {
                drawLightGlow(canvas, sx, sy - halfBldg * 0.4f, 100f * zoom, glowColor, 120);
            }

            // 2. Selected Highlight Ring
            if (i == selectedBuilding) {
                float pulse = 1f + (float) Math.sin(animTime * 5f) * 0.12f;
                buildingBorderPaint.setColor(0xFFFFD700);
                buildingBorderPaint.setAlpha(240);
                r1.set(sx - halfBldg * 1.2f * pulse, sy - bldgSize * 0.9f * pulse,
                       sx + halfBldg * 1.2f * pulse, sy + halfBldg * 0.4f * pulse);
                canvas.drawRoundRect(r1, dp(12f), dp(12f), buildingBorderPaint);
            }

            // 3. Render 3D Building Drawable Model
            Bitmap model = get3DBuildingModel(bldgType);
            if (model != null && !model.isRecycled()) {
                dstRect.set((int) (sx - halfBldg), (int) (sy - bldgSize), (int) (sx + halfBldg), (int) sy);
                canvas.drawBitmap(model, null, dstRect, islandTopPaint);
            }

            // 4. Facility Title & Level Pill
            float tagW = dp(96f) * Math.min(1.2f, Math.max(0.7f, zoom));
            float tagH = dp(24f) * Math.min(1.2f, Math.max(0.7f, zoom));
            float tagY = sy + dp(4f) * zoom;

            r1.set(sx - tagW * 0.5f, tagY, sx + tagW * 0.5f, tagY + tagH);
            canvas.drawRoundRect(r1, dp(6f), dp(6f), buildingTagBgPaint);

            buildingBorderPaint.setColor(glowColor);
            buildingBorderPaint.setAlpha(180);
            buildingBorderPaint.setStrokeWidth(dp(1.2f));
            canvas.drawRoundRect(r1, dp(6f), dp(6f), buildingBorderPaint);

            sb1.setLength(0);
            sb1.append(peakNames[i]).append(" Lv.").append(bldgLevel);
            buildingTagPaint.setColor(0xFFF0F0F0);
            buildingTagPaint.setTextSize(sp(10f) * Math.min(1.2f, Math.max(0.8f, zoom)));
            canvas.drawText(sb1, 0, sb1.length(), sx, tagY + tagH * 0.68f, buildingTagPaint);

            // 5. Production Rate Floating Indicator
            if (zoom > 0.55f && isBuilt) {
                sb2.setLength(0);
                switch (bldgType) {
                    case GameConfig.BUILD_HALL: sb2.append("+").append(bldgLevel * 15).append(" 🪙/s"); break;
                    case GameConfig.BUILD_GARDEN: sb2.append("+").append(bldgLevel * 8).append(" 🌿/s"); break;
                    case GameConfig.BUILD_MINE: sb2.append("+").append(bldgLevel * 6).append(" 💎/s"); break;
                    case GameConfig.BUILD_ALCHEMY: sb2.append("+").append(bldgLevel * 2).append(" 💊/m"); break;
                    case GameConfig.BUILD_LIBRARY: sb2.append("+").append(bldgLevel * 12).append(" Atk"); break;
                }
                if (sb2.length() > 0) {
                    buildingTagPaint.setColor(glowColor);
                    buildingTagPaint.setTextSize(sp(9f) * zoom);
                    canvas.drawText(sb2, 0, sb2.length(), sx, tagY - dp(6f) * zoom, buildingTagPaint);
                }
            }
        }
    }

    /**
     * Renders Cultivator Disciples cleanly with circular celestial frames & element aura rings.
     */
    private void renderDisciples(Canvas canvas, CameraSystem cam, SectData data) {
        if (data.disciples == null || data.disciples.isEmpty()) return;

        float zoom = cam.zoom;
        float avatarRadius = dp(16f) * zoom;
        int n = data.disciples.size();

        for (int i = 0; i < n; i++) {
            Disciple d = data.disciples.get(i);
            if (d == null) continue;

            float px = cam.worldToScreenX(d.position.x);
            float py = cam.worldToScreenY(d.position.y);

            // Disciple Shadow
            if (enableShadows) {
                r1.set(px - avatarRadius * 0.9f, py - avatarRadius * 0.3f, px + avatarRadius * 0.9f, py + avatarRadius * 0.3f);
                canvas.drawOval(r1, discipleShadowPaint);
            }

            float bob = d.isMoving ? (float) Math.abs(Math.sin(animTime * 10f + i)) * dp(4f) * zoom : (float) Math.sin(animTime * 3f + i) * dp(2f) * zoom;
            float drawPy = py - avatarRadius - bob;

            int elemColor = GameConfig.getElementColor(d.element);

            // 1. Celestial Cultivation Aura Halo
            discipleAuraPaint.setColor(elemColor);
            discipleAuraPaint.setAlpha(120 + (int) (Math.sin(animTime * 4f + i) * 60));
            discipleAuraPaint.setStrokeWidth(dp(2.2f) * zoom);
            canvas.drawCircle(px, drawPy, avatarRadius * 1.25f, discipleAuraPaint);

            // 2. Selection Ring
            if (i == selectedDisciple) {
                float pulse = 1f + (float) Math.sin(animTime * 6f) * 0.15f;
                buildingBorderPaint.setColor(0xFFFFD700);
                buildingBorderPaint.setAlpha(255);
                canvas.drawCircle(px, drawPy, avatarRadius * 1.5f * pulse, buildingBorderPaint);
            }

            // 3. Circular Cultivator Portrait / Silhouette Frame (Eliminates raw black box)
            discipleAvatarFramePaint.setColor(0xFF1F1D36);
            canvas.drawCircle(px, drawPy, avatarRadius, discipleAvatarFramePaint);

            if (cultivatorHeroBitmap != null && !cultivatorHeroBitmap.isRecycled()) {
                canvas.save();
                path2.reset();
                path2.addCircle(px, drawPy, avatarRadius * 0.92f, Path.Direction.CCW);
                canvas.clipPath(path2);

                dstRect.set((int) (px - avatarRadius), (int) (drawPy - avatarRadius), (int) (px + avatarRadius), (int) (drawPy + avatarRadius));
                if (d.facing < 0) {
                    canvas.scale(-1f, 1f, px, drawPy);
                }
                canvas.drawBitmap(cultivatorHeroBitmap, null, dstRect, discipleAvatarFramePaint);
                canvas.restore();
            }

            // Golden Avatar Border Rim
            buildingBorderPaint.setColor(0xFFD4AF37);
            buildingBorderPaint.setAlpha(200);
            buildingBorderPaint.setStrokeWidth(dp(1.2f) * zoom);
            canvas.drawCircle(px, drawPy, avatarRadius, buildingBorderPaint);

            // 4. Disciple Name & Realm Badge
            if (zoom > 0.45f) {
                sb1.setLength(0);
                sb1.append(d.name != null ? d.name : "Cultivator");
                uiTextBold.setColor(0xFFFFFFFF);
                uiTextBold.setTextSize(sp(10f) * Math.min(1.2f, Math.max(0.7f, zoom)));
                uiTextBold.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(sb1, 0, sb1.length(), px, drawPy - avatarRadius - dp(4f) * zoom, uiTextBold);
                uiTextBold.setTextAlign(Paint.Align.LEFT);

                // 5. Mini HP & MP Qi Bars
                float barW = dp(28f) * zoom;
                float barH = dp(3.5f) * zoom;
                float barY = drawPy + avatarRadius + dp(3f) * zoom;

                r1.set(px - barW * 0.5f, barY, px + barW * 0.5f, barY + barH);
                canvas.drawRect(r1, hpBgPaint);

                float hpRatio = Math.max(0f, Math.min(1f, d.maxHp > 0 ? d.hp / (float) d.maxHp : 1f));
                r2.set(r1.left, r1.top, r1.left + barW * hpRatio, r1.bottom);
                canvas.drawRect(r2, hpBarPaint);

                // MP Bar
                if (d.maxMp > 0) {
                    float mpRatio = Math.max(0f, Math.min(1f, d.mp / (float) d.maxMp));
                    r3.set(px - barW * 0.5f, barY + barH + dp(1f) * zoom, px + barW * 0.5f, barY + barH * 2f + dp(1f) * zoom);
                    canvas.drawRect(r3, hpBgPaint);
                    r4.set(r3.left, r3.top, r3.left + barW * mpRatio, r3.bottom);
                    canvas.drawRect(r4, mpBarPaint);
                }
            }
        }
    }

    private void renderParticles(Canvas canvas, CameraSystem cam) {
        for (int i = 0; i < activeParticles; i++) {
            float px = cam.worldToScreenX(ptX[i]), py = cam.worldToScreenY(ptY[i]);
            float lifeRatio = ptLife[i] / ptMaxLife[i];
            particlePaint.setColor(ptColor[i]);
            particlePaint.setAlpha((int) (255 * lifeRatio));
            canvas.drawCircle(px, py, ptSize[i] * cam.zoom * lifeRatio, particlePaint);
        }
    }

    private void renderFloatingTexts(Canvas canvas, CameraSystem cam) {
        for (int i = 0; i < activeFloating; i++) {
            float px = cam.worldToScreenX(ftX[i]), py = cam.worldToScreenY(ftY[i]);
            float lifeRatio = ftLife[i] / ftMaxLife[i];
            floatingTextPaint.setColor(ftColor[i]);
            floatingTextPaint.setAlpha((int) (255 * lifeRatio));
            floatingTextPaint.setTextSize(sp(14f) * cam.zoom);
            canvas.drawText(ftText[i], px, py, floatingTextPaint);
        }
    }

    private void renderRadialLighting(Canvas canvas, CameraSystem cam, SectData data, int W, int H) {
        float radius = 160f * cam.zoom;
        for (int i = 0; i < PEAK_COUNT; i++) {
            float px = cam.worldToScreenX(peakX[i]), py = cam.worldToScreenY(peakY[i]);
            if (px < -radius || px > W + radius || py < -radius || py > H + radius) continue;
            drawLightGlow(canvas, px, py, radius, getBuildingGlowColor(peakBuildingTypes[i]), 90);
        }
    }

    private void renderDayNightOverlay(Canvas canvas, int W, int H) {
        int tint = cachedDayT > 0.5f ? SKY_DUSK : SKY_NIGHT;
        int alpha = cachedDayT > 0.5f ? (int) (25 * (1f - cachedDayT) * 2f) : (int) (70 * (1f - cachedDayT * 2f));
        if (alpha <= 0) return;
        dayNightOverlayPaint.setColor(tint);
        dayNightOverlayPaint.setAlpha(Math.max(0, Math.min(85, alpha)));
        canvas.drawRect(0, 0, W, H, dayNightOverlayPaint);
    }

    // =========================================================================
    // MODERN HIGH-DPI XIANXIA UI SUITE
    // =========================================================================

    /**
     * Renders Top Treasury Bar with resource capsules, sect badge, speed dial, and time.
     */
    private void renderTopTreasuryBar(Canvas canvas, SectData data, int W, int H) {
        float barH = uiTopBarHeight;

        // Gradient Glass Background
        uiBgPaint.setColor(0xEE120E22);
        canvas.drawRect(0, 0, W, barH, uiBgPaint);

        topBarGradientPaint.setShader(new LinearGradient(
                0, 0, 0, barH, 0xEE1A1430, 0x001A1430, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, W, barH, topBarGradientPaint);

        // Golden Trim Divider
        uiBorderPaint.setColor(0xFFD4AF37);
        uiBorderPaint.setStrokeWidth(dp(1.5f));
        canvas.drawLine(0, barH, W, barH, uiBorderPaint);

        // 1. Sect Name & Realm Header Badge
        float padX = dp(12f);
        float padY = dp(10f);

        sb1.setLength(0);
        sb1.append(data.sectName != null ? data.sectName : "Mount Shu Sect");
        sb1.append(" • ").append(GameConfig.getRealmName(data.sectRealm));

        uiTextBold.setColor(0xFFFFD700);
        uiTextBold.setTextSize(sp(13f));
        canvas.drawText(sb1, 0, sb1.length(), padX, padY + dp(14f), uiTextBold);

        // 2. Resource Pills (Row 2 of Top Bar)
        float pillTop = padY + dp(24f);
        float pillH = dp(28f);
        float totalPills = 4f;
        float pillGap = dp(6f);
        float speedBtnW = dp(46f);
        float availableW = W - padX * 2 - speedBtnW - pillGap;
        float pillW = (availableW - (totalPills - 1) * pillGap) / totalPills;

        // Spirit Stones
        renderResourcePill(canvas, padX + 0 * (pillW + pillGap), pillTop, pillW, pillH, "🪙", data.spiritStones, 0xFFFFD700);
        // Spirit Herbs
        renderResourcePill(canvas, padX + 1 * (pillW + pillGap), pillTop, pillW, pillH, "🌿", data.spiritHerbs, 0xFF00E676);
        // Spirit Ores
        renderResourcePill(canvas, padX + 2 * (pillW + pillGap), pillTop, pillW, pillH, "💎", data.spiritOres, 0xFF00E5FF);
        // Spirit Pills
        renderResourcePill(canvas, padX + 3 * (pillW + pillGap), pillTop, pillW, pillH, "💊", data.spiritPills, 0xFFFF9100);

        // 3. Time Dilation Speed Toggle Button (1x / 2x / 5x)
        float speedX = W - padX - speedBtnW;
        r1.set(speedX, pillTop, speedX + speedBtnW, pillTop + pillH);
        uiResourcePillPaint.setColor(0xEE2A1E4A);
        canvas.drawRoundRect(r1, dp(6f), dp(6f), uiResourcePillPaint);

        uiBorderPaint.setColor(0xFF00E5FF);
        uiBorderPaint.setStrokeWidth(dp(1.2f));
        canvas.drawRoundRect(r1, dp(6f), dp(6f), uiBorderPaint);

        uiTextBold.setColor(0xFF00E5FF);
        uiTextBold.setTextSize(sp(11f));
        uiTextBold.setTextAlign(Paint.Align.CENTER);
        sb1.setLength(0);
        sb1.append((int) SPEED_VALUES[speedMultiplierIdx]).append("x ⚡");
        canvas.drawText(sb1, 0, sb1.length(), speedX + speedBtnW * 0.5f, pillTop + pillH * 0.65f, uiTextBold);
        uiTextBold.setTextAlign(Paint.Align.LEFT);

        // 4. Time Display
        String timeDisplay = data.time != null ? data.time.getDisplay() : "Year 1 • Month 1";
        uiTextRegular.setColor(0xFFAAAAAA);
        uiTextRegular.setTextSize(sp(10f));
        uiTextRegular.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(timeDisplay, W - padX, padY + dp(14f), uiTextRegular);
        uiTextRegular.setTextAlign(Paint.Align.LEFT);
    }

    private void renderResourcePill(Canvas canvas, float left, float top, float width, float height, String icon, long amount, int accentColor) {
        r1.set(left, top, left + width, top + height);
        uiResourcePillPaint.setColor(0xEE181428);
        canvas.drawRoundRect(r1, dp(6f), dp(6f), uiResourcePillPaint);

        uiBorderPaint.setColor(accentColor);
        uiBorderPaint.setAlpha(120);
        uiBorderPaint.setStrokeWidth(dp(1f));
        canvas.drawRoundRect(r1, dp(6f), dp(6f), uiBorderPaint);

        // Icon
        uiTextBold.setColor(0xFFFFFFFF);
        uiTextBold.setTextSize(sp(11f));
        canvas.drawText(icon, left + dp(4f), top + height * 0.65f, uiTextBold);

        // Amount
        sb2.setLength(0);
        sb2.append(NumberFormatter.format(amount, sb1));
        uiTextBold.setColor(accentColor);
        uiTextBold.setTextSize(sp(10.5f));
        canvas.drawText(sb2, 0, sb2.length(), left + dp(20f), top + height * 0.65f, uiTextBold);
    }

    /**
     * Renders Bottom Navigation Deck (5 Core Xianxia Action Cards).
     */
    private void renderBottomNavigationDeck(Canvas canvas, int W, int H) {
        float barH = uiBotBarHeight;
        float barTop = H - barH;

        uiBgPaint.setColor(0xEE120E22);
        canvas.drawRect(0, barTop, W, H, uiBgPaint);

        botBarGradientPaint.setShader(new LinearGradient(
                0, barTop, 0, H, 0x001A1430, 0xEE1A1430, Shader.TileMode.CLAMP));
        canvas.drawRect(0, barTop, W, H, botBarGradientPaint);

        uiBorderPaint.setColor(0xFFD4AF37);
        uiBorderPaint.setStrokeWidth(dp(1.5f));
        canvas.drawLine(0, barTop, W, barTop, uiBorderPaint);

        float totalButtons = 6f;
        float margin = dp(4f);
        float totalMargin = (totalButtons + 1) * margin;
        float btnWidth = (W - totalMargin) / totalButtons;
        float btnTop = barTop + dp(6f);
        float btnBottom = H - dp(6f);

        renderNavButton(canvas, margin + 0 * (btnWidth + margin), btnTop, btnWidth, btnBottom - btnTop, "⚔️", "War", 0xFFFF5252);
        renderNavButton(canvas, margin + 1 * (btnWidth + margin), btnTop, btnWidth, btnBottom - btnTop, "🏆", "Arena", 0xFFFFD700);
        renderNavButton(canvas, margin + 2 * (btnWidth + margin), btnTop, btnWidth, btnBottom - btnTop, "💥", "Battle", 0xFFFF6E40);
        renderNavButton(canvas, margin + 3 * (btnWidth + margin), btnTop, btnWidth, btnBottom - btnTop, "👥", "Recruit", 0xFF00E676);
        renderNavButton(canvas, margin + 4 * (btnWidth + margin), btnTop, btnWidth, btnBottom - btnTop, "🏛️", "Sect Hub", 0xFF00E5FF);
        renderNavButton(canvas, margin + 5 * (btnWidth + margin), btnTop, btnWidth, btnBottom - btnTop, "⚙️", "Menu", 0xFFAB47BC);
    }

    private void renderNavButton(Canvas canvas, float left, float top, float width, float height, String icon, String label, int accentColor) {
        r1.set(left, top, left + width, top + height);
        uiBgPaint.setColor(0xEE201836);
        canvas.drawRoundRect(r1, dp(8f), dp(8f), uiBgPaint);

        uiBorderPaint.setColor(accentColor);
        uiBorderPaint.setAlpha(160);
        uiBorderPaint.setStrokeWidth(dp(1.2f));
        canvas.drawRoundRect(r1, dp(8f), dp(8f), uiBorderPaint);

        uiTextBold.setTextAlign(Paint.Align.CENTER);

        // Icon
        uiTextBold.setColor(0xFFFFFFFF);
        uiTextBold.setTextSize(sp(13f));
        canvas.drawText(icon, left + width * 0.5f, top + height * 0.44f, uiTextBold);

        // Label
        uiTextBold.setColor(accentColor);
        uiTextBold.setTextSize(sp(9.5f));
        canvas.drawText(label, left + width * 0.5f, top + height * 0.82f, uiTextBold);

        uiTextBold.setTextAlign(Paint.Align.LEFT);
    }

    private void renderQuickActionSidebar(Canvas canvas, int W, int H) {
        // Center Camera Quick Button
        float btnSize = dp(38f);
        float btnX = dp(12f);
        float btnY = uiTopBarHeight + dp(16f);

        r1.set(btnX, btnY, btnX + btnSize, btnY + btnSize);
        uiBgPaint.setColor(0xDD1A1430);
        canvas.drawRoundRect(r1, dp(19f), dp(19f), uiBgPaint);

        uiBorderPaint.setColor(0xFFD4AF37);
        uiBorderPaint.setStrokeWidth(dp(1.5f));
        canvas.drawRoundRect(r1, dp(19f), dp(19f), uiBorderPaint);

        uiTextBold.setTextAlign(Paint.Align.CENTER);
        uiTextBold.setColor(0xFFFFD700);
        uiTextBold.setTextSize(sp(14f));
        canvas.drawText("🏰", btnX + btnSize * 0.5f, btnY + btnSize * 0.65f, uiTextBold);
        uiTextBold.setTextAlign(Paint.Align.LEFT);
    }

    private void renderMinimap(Canvas canvas, SectData data, int W, int H) {
        float mmSize = dp(68f);
        float mmX = W - mmSize - dp(12f);
        float mmY = uiTopBarHeight + dp(16f);

        r1.set(mmX, mmY, mmX + mmSize, mmY + mmSize);
        canvas.drawRoundRect(r1, dp(8f), dp(8f), minimapBgPaint);
        canvas.drawRoundRect(r1, dp(8f), dp(8f), minimapBorderPaint);

        float scale = mmSize / (WORLD_BOUND_MAX - WORLD_BOUND_MIN);
        float offsetX = WORLD_BOUND_MIN;
        float offsetY = WORLD_BOUND_MIN;

        // Draw Peak Dots
        for (int i = 0; i < PEAK_COUNT; i++) {
            minimapDotPaint.setColor(getBuildingGlowColor(peakBuildingTypes[i]));
            canvas.drawCircle(mmX + (peakX[i] - offsetX) * scale, mmY + (peakY[i] - offsetY) * scale, dp(2.5f), minimapDotPaint);
        }

        // Draw Disciples Dots
        if (data.disciples != null) {
            for (int i = 0; i < data.disciples.size(); i++) {
                Disciple d = data.disciples.get(i);
                if (d == null) continue;
                minimapDotPaint.setColor(GameConfig.getElementColor(d.element));
                canvas.drawCircle(mmX + (d.position.x - offsetX) * scale, mmY + (d.position.y - offsetY) * scale, dp(1.8f), minimapDotPaint);
            }
        }
    }

    private void renderTapDebugMarker(Canvas canvas, CameraSystem cam) {
        if (tapMarkerLife <= 0f || Float.isNaN(lastTapWorldX)) return;
        float sx = cam.worldToScreenX(lastTapWorldX), sy = cam.worldToScreenY(lastTapWorldY);
        tapDebugPaint.setAlpha((int) (255 * Math.min(1f, tapMarkerLife)));
        canvas.drawCircle(sx, sy, dp(24f) * cam.zoom, tapDebugPaint);
    }

    // =========================================================================
    // TOUCH DISPATCHER & HIT TESTING
    // =========================================================================

    public void onTouchDown(float x, float y) {
        if (slashMiniGame != null && slashMiniGame.isActive()) {
            slashMiniGame.onTouchDown(x, y);
            return;
        }

        SectData data = SectData.getInstance();
        if (data == null || camera == null) return;

        int W = camera.viewportW > 0 ? camera.viewportW : 720;
        int H = camera.viewportH > 0 ? camera.viewportH : 1280;

        // 1. Check Top Bar Clicks (Speed Toggle & Sect Hub)
        if (y <= uiTopBarHeight) {
            float padX = dp(12f);
            float speedBtnW = dp(46f);
            float speedX = W - padX - speedBtnW;

            // Speed multiplier toggle button
            if (x >= speedX && x <= speedX + speedBtnW) {
                speedMultiplierIdx = (speedMultiplierIdx + 1) % SPEED_VALUES.length;
                float speed = SPEED_VALUES[speedMultiplierIdx];
                GameEngine.get().setSpeedMultiplier(speed);
                try {
                    AudioManager.getInstance(appContext).playSfx(AudioManager.SFX_CLICK);
                } catch (Throwable ignored) {}
                return;
            }

            // Sect Header -> Open Sect Hub
            if (x < speedX) {
                try {
                    AudioManager.getInstance(appContext).playSfx(AudioManager.SFX_CLICK);
                } catch (Throwable ignored) {}
                if (selectionListener != null) {
                    selectionListener.onSectHubRequested(null);
                }
                return;
            }
        }

        // 2. Check Bottom Action Bar Navigation Deck
        if (y >= H - uiBotBarHeight) {
            float totalButtons = 6f;
            float margin = dp(4f);
            float totalMargin = (totalButtons + 1) * margin;
            float btnWidth = (W - totalMargin) / totalButtons;

            for (int b = 0; b < 6; b++) {
                float bLeft = margin + b * (btnWidth + margin);
                float bRight = bLeft + btnWidth;
                if (x >= bLeft && x <= bRight) {
                    try {
                        AudioManager.getInstance(appContext).playSfx(AudioManager.SFX_CLICK);
                    } catch (Throwable ignored) {}
                    if (selectionListener != null) {
                        switch (b) {
                            case 0: selectionListener.onWarRequested(); break;
                            case 1: selectionListener.onTournamentRequested(); break;
                            case 2: selectionListener.onBattleRequested(); break;
                            case 3: selectionListener.onRecruitRequested(); break;
                            case 4: selectionListener.onSectHubRequested(null); break;
                            case 5: selectionListener.onMenuRequested(); break;
                        }
                    }
                    return;
                }
            }
        }

        // 3. Quick Action Button: Center Camera
        float btnSize = dp(38f);
        float centerBtnX = dp(12f);
        float centerBtnY = uiTopBarHeight + dp(16f);
        if (x >= centerBtnX && x <= centerBtnX + btnSize && y >= centerBtnY && y <= centerBtnY + btnSize) {
            camera.targetPos.set(MAP_CENTER_X, MAP_CENTER_Y);
            camera.targetZoom = 0.75f;
            try {
                AudioManager.getInstance(appContext).playSfx(AudioManager.SFX_CLICK);
            } catch (Throwable ignored) {}
            return;
        }

        lastTapWorldX = camera.screenToWorldX(x);
        lastTapWorldY = camera.screenToWorldY(y);
        tapMarkerLife = 1f;

        // 4. Hit Test Disciples
        if (data.disciples != null) {
            float touchRadius = dp(32f);
            for (int i = 0; i < data.disciples.size(); i++) {
                Disciple d = data.disciples.get(i);
                if (d == null) continue;
                float px = camera.worldToScreenX(d.position.x);
                float py = camera.worldToScreenY(d.position.y);
                if (Math.abs(x - px) <= touchRadius && Math.abs(y - py) <= touchRadius) {
                    selectedDiscipleId = d.id;
                    selectedDisciple = i;
                    selectedBuilding = -1;
                    spawnExplosion(d.position.x, d.position.y, 0xFFFFD700, 10);
                    try {
                        AudioManager.getInstance(appContext).playSfx(AudioManager.SFX_CLICK);
                    } catch (Throwable ignored) {}
                    if (selectionListener != null) selectionListener.onDiscipleSelected(d);
                    return;
                }
            }
        }

        // 5. Hit Test Peaks / Buildings
        for (int i = 0; i < PEAK_COUNT; i++) {
            float px = camera.worldToScreenX(peakX[i]);
            float py = camera.worldToScreenY(peakY[i]);
            float rx = peakRadiusX[i] * camera.zoom * 1.1f;
            float ry = peakRadiusY[i] * camera.zoom * 1.1f;

            if (Math.abs(x - px) <= rx && Math.abs(y - py) <= ry) {
                selectedBuilding = i;
                selectedDiscipleId = null;
                selectedDisciple = -1;

                int bType = peakBuildingTypes[i];
                Building b = null;
                if (data.buildings != null) {
                    for (int j = 0; j < data.buildings.size(); j++) {
                        Building candidate = data.buildings.get(j);
                        if (candidate != null && candidate.type == bType) {
                            b = candidate;
                            break;
                        }
                    }
                }

                try {
                    AudioManager.getInstance(appContext).playSfx(AudioManager.SFX_CLICK);
                } catch (Throwable ignored) {}

                if (b != null && selectionListener != null) {
                    selectionListener.onBuildingTapped(b, b.isBuilt);
                }
                return;
            }
        }
    }

    public void onTouchMove(float x, float y) {
        if (slashMiniGame != null && slashMiniGame.isActive()) {
            slashMiniGame.onTouchMove(x, y);
        }
    }

    public void onTouchUp(float x, float y) {
        if (slashMiniGame != null && slashMiniGame.isActive()) {
            slashMiniGame.onTouchUp();
        }
    }

    // =========================================================================
    // HELPERS & LIFECYCLE
    // =========================================================================

    private int getBuildingGlowColor(int type) {
        switch (type) {
            case GameConfig.BUILD_HALL: return 0xFFFFD700;       // Gold
            case GameConfig.BUILD_LIBRARY: return 0xFF00E5FF;    // Cyan
            case GameConfig.BUILD_GARDEN: return 0xFF00E676;     // Jade Green
            case GameConfig.BUILD_ALCHEMY: return 0xFFFF9100;    // Amber Flame
            case GameConfig.BUILD_MINE: return 0xFF00B0FF;       // Spirit Crystal
            case GameConfig.BUILD_SPIRIT_POOL: return 0xFF1DE9B6;// Turquoise
            default: return 0xFFFFD700;
        }
    }

    private int computeSkyColor(float hour) {
        hour = hour % 24f; if (hour < 0f) hour += 24f;
        if (hour < 6f)  { cachedDayT = hour / 6f * 0.5f; return lerpColor(SKY_NIGHT, SKY_DAWN, hour / 6f); }
        if (hour < 12f) { cachedDayT = 0.5f + (hour - 6f) / 6f * 0.5f; return lerpColor(SKY_DAWN, SKY_DAY, (hour - 6f) / 6f); }
        if (hour < 18f) { cachedDayT = 1f - (hour - 12f) / 6f * 0.5f; return lerpColor(SKY_DAY, SKY_DUSK, (hour - 12f) / 6f); }
        cachedDayT = 0.5f - (hour - 18f) / 6f * 0.5f;
        return lerpColor(SKY_DUSK, SKY_NIGHT, (hour - 18f) / 6f);
    }

    private static int lerpColor(int c1, int c2, float t) {
        t = t < 0f ? 0f : (t > 1f ? 1f : t);
        int a1=(c1>>24)&0xFF, r1=(c1>>16)&0xFF, g1=(c1>>8)&0xFF, b1=c1&0xFF;
        int a2=(c2>>24)&0xFF, r2=(c2>>16)&0xFF, g2=(c2>>8)&0xFF, b2=c2&0xFF;
        int a=(int)(a1+(a2-a1)*t), r=(int)(r1+(r2-r1)*t), g=(int)(g1+(g2-g1)*t), b=(int)(b1+(b2-b1)*t);
        return (a<<24)|(r<<16)|(g<<8)|b;
    }

    public void setSelectionListener(OnSelectionListener listener) { this.selectionListener = listener; }
    public SlashMiniGame getSlashMiniGame() { return slashMiniGame; }
    public void setCamera(CameraSystem cam) { this.camera = cam; }
    public CameraSystem getCamera() { return camera; }

    public void setSelectedDisciple(int index) {
        SectData data = SectData.getInstance();
        if (data != null && data.disciples != null && index >= 0 && index < data.disciples.size()) {
            Disciple d = data.disciples.get(index);
            selectedDiscipleId = d != null ? d.id : null;
            selectedDisciple = d != null ? index : -1;
        } else {
            selectedDiscipleId = null;
            selectedDisciple = -1;
        }
    }
    public void setSelectedBuilding(int index) { selectedBuilding = index; }
    public int getSelectedDisciple() { return selectedDisciple; }
    public void resetSelection() {
        selectedDiscipleId = null;
        selectedDisciple = -1;
        selectedBuilding = -1;
    }

    public void spawnLevelUpEffect(float x, float y) {
        showFloatingText("BREAKTHROUGH!", x, y - dp(40f), 0xFFFFD700);
        spawnExplosion(x, y, 0xFFFFD700, 20);
    }

    public void destroy() {
        if (parallaxBg != null && !parallaxBg.isRecycled()) {
            parallaxBg.recycle();
            parallaxBg = null;
        }
        if (cultivatorHeroBitmap != null && !cultivatorHeroBitmap.isRecycled()) {
            cultivatorHeroBitmap.recycle();
            cultivatorHeroBitmap = null;
        }
        if (lightBakedBitmap != null && !lightBakedBitmap.isRecycled()) {
            lightBakedBitmap.recycle();
            lightBakedBitmap = null;
        }
        for (int i = 0; i < building3DCache.length; i++) {
            if (building3DCache[i] != null && !building3DCache[i].isRecycled()) {
                building3DCache[i].recycle();
            }
            building3DCache[i] = null;
        }
        disciple3DCache.clear();
    }
}
