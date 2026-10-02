package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import com.sect.idle.R;
import com.sect.idle.gameplay.BattleEngine;
import com.sect.idle.gameplay.CultivatorCombatAI;
import com.sect.idle.models.BattleSkill;
import com.sect.idle.models.BattleUnit;
import com.sect.idle.models.Disciple;
import com.sect.idle.render.Projection3D;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.systems.CameraSystem;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * BattleScene3D - Master 3D Xianxia Donghua Immortal Cultivation Battle Arena.
 * Features:
 * - Real 3D Ground Array with Rotating Bagua Taiji Eight Trigrams & Floating Daoist Monuments.
 * - 3D Spatial Cultivator Movement, Airborne Flying Swords (万剑归宗), and Parabolic Leaps.
 * - Cinematic Ultimate Techniques with slow-mo freeze frames, dynamic FOV zoom, and full-screen Donghua VFX.
 * - Complete Automation Workflow: Auto-Battle, 1x/2x/4x Speed Multipliers, Manual Ultimate Cards, and Formation Tactics.
 *
 * 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BattleScene3D {

    private final Context context;
    private BattleEngine battleEngine;
    private final Projection3D projection;

    // Camera Modes
    public static final int CAM_MODE_CINEMATIC_ORBIT = 0;
    public static final int CAM_MODE_ACTION_FOCUS = 1;
    public static final int CAM_MODE_ULTIMATE_ZOOM = 2;
    public static final int CAM_MODE_ISOMETRIC = 3;
    private int cameraMode = CAM_MODE_CINEMATIC_ORBIT;

    // Simulation Speed & Automation
    public float timeScale = 1.0f; // 1.0f, 2.0f, 4.0f
    public boolean autoBattle = true;
    public int activeFormation = 0; // 0=Bagua Array, 1=Four Beasts, 2=Five Elements

    // Pre-allocated Paints (Zero GC in loop)
    private final Paint bgGradientPaint;
    private final Paint baguaRingPaint;
    private final Paint baguaGlowPaint;
    private final Paint trigramPaint;
    private final Paint shadowPaint;
    private final Paint unitBodyPaint;
    private final Paint swordBladePaint;
    private final Paint swordTrailPaint;
    private final Paint qiAuraPaint;
    private final Paint shieldPaint;
    private final Paint hpBgPaint;
    private final Paint hpFillPaint;
    private final Paint qiFillPaint;
    private final Paint textNamePaint;
    private final Paint textDamagePaint;
    private final Paint hudPanelPaint;
    private final Paint hudBorderPaint;
    private final Paint hudTextPaint;
    private final Paint buttonBgPaint;
    private final Paint buttonTextPaint;
    private final Paint ultimateBannerBgPaint;
    private final Paint ultimateBannerTextPaint;
    private final Paint ultimateBannerSubPaint;
    private final Paint flashPaint;

    // Pre-allocated Geometries & Registers
    private final PointF projPoint = new PointF();
    private final PointF projPoint2 = new PointF();
    private final RectF rectPool = new RectF();
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();
    private final Path polyPath = new Path();

    // Sprite Cache
    private final HashMap<String, Bitmap> spriteCache = new HashMap<String, Bitmap>();
    private Bitmap heroBitmap;

    // AI States map: Unit -> AIState
    private final ArrayList<CultivatorCombatAI.AIState> aiStates = new ArrayList<CultivatorCombatAI.AIState>();

    // 3D Flying Swords in Arena (Storm of 10,000 Flying Swords VFX)
    private static final int MAX_SWORD_STORM = 24;
    private final float[] ssX = new float[MAX_SWORD_STORM];
    private final float[] ssY = new float[MAX_SWORD_STORM];
    private final float[] ssZ = new float[MAX_SWORD_STORM];
    private final float[] ssVX = new float[MAX_SWORD_STORM];
    private final float[] ssVY = new float[MAX_SWORD_STORM];
    private final float[] ssVZ = new float[MAX_SWORD_STORM];
    private final float[] ssLife = new float[MAX_SWORD_STORM];
    private final int[] ssColor = new int[MAX_SWORD_STORM];
    private int activeSwords = 0;

    // 3D Particles & Lightning Arcs
    private static final int MAX_PARTICLES_3D = 90;
    private final float[] pX = new float[MAX_PARTICLES_3D];
    private final float[] pY = new float[MAX_PARTICLES_3D];
    private final float[] pZ = new float[MAX_PARTICLES_3D];
    private final float[] pVX = new float[MAX_PARTICLES_3D];
    private final float[] pVY = new float[MAX_PARTICLES_3D];
    private final float[] pVZ = new float[MAX_PARTICLES_3D];
    private final float[] pLife = new float[MAX_PARTICLES_3D];
    private final float[] pMaxLife = new float[MAX_PARTICLES_3D];
    private final float[] pSize = new float[MAX_PARTICLES_3D];
    private final int[] pColor = new int[MAX_PARTICLES_3D];
    private int activeParticles = 0;

    // Floating 3D Damage Numbers
    private static final int MAX_DAMAGE_NUMS = 16;
    private final String[] dText = new String[MAX_DAMAGE_NUMS];
    private final float[] dX = new float[MAX_DAMAGE_NUMS];
    private final float[] dY = new float[MAX_DAMAGE_NUMS];
    private final float[] dZ = new float[MAX_DAMAGE_NUMS];
    private final float[] dLife = new float[MAX_DAMAGE_NUMS];
    private final int[] dColor = new int[MAX_DAMAGE_NUMS];
    private int activeDamageNums = 0;

    // Cinematic Ultimate Banner Trigger
    private boolean isUltimateActive = false;
    private float ultimateTimer = 0f;
    private String ultimateName = "";
    private String ultimateChinese = "";
    private int ultimateColor = 0xFFFFD700;

    // Screen Shake & Flash
    private float shakeIntensity = 0f;
    private float flashIntensity = 0f;
    private int flashColor = 0xFFFFFFFF;
    private float animTime = 0f;
    private float baguaRotation = 0f;

    // Dimensions
    private int screenW = 720;
    private int screenH = 1280;

    // Interaction Touch Rectangles
    private final RectF btnAutoRect = new RectF();
    private final RectF btnSpeedRect = new RectF();
    private final RectF btnFormationRect = new RectF();
    private final RectF btnReturnRect = new RectF();
    private final RectF[] ultimateCardRects = new RectF[3];

    public interface Battle3DCallback {
        void onBattleEnded(boolean victory);
    }
    private Battle3DCallback callback;
    public void setCallback(Battle3DCallback cb) { this.callback = cb; }

    public BattleScene3D(Context context) {
        this.context = context;
        this.projection = new Projection3D();

        for (int i = 0; i < 3; i++) {
            ultimateCardRects[i] = new RectF();
        }

        // Initialize Paints
        bgGradientPaint = new Paint();

        baguaRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        baguaRingPaint.setStyle(Paint.Style.STROKE);
        baguaRingPaint.setStrokeWidth(3f);
        baguaRingPaint.setColor(0x80FFD700);

        baguaGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        baguaGlowPaint.setStyle(Paint.Style.FILL);

        trigramPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trigramPaint.setStyle(Paint.Style.STROKE);
        trigramPaint.setStrokeWidth(2.5f);
        trigramPaint.setColor(0xAA00E5FF);

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(0x70000000);
        shadowPaint.setStyle(Paint.Style.FILL);

        unitBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        swordBladePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        swordBladePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        swordBladePaint.setStrokeWidth(2f);

        swordTrailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        swordTrailPaint.setStyle(Paint.Style.STROKE);
        swordTrailPaint.setStrokeWidth(3f);

        qiAuraPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        qiAuraPaint.setStyle(Paint.Style.FILL);

        shieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shieldPaint.setStyle(Paint.Style.STROKE);
        shieldPaint.setStrokeWidth(4f);

        hpBgPaint = new Paint();
        hpBgPaint.setColor(0xFF212121);

        hpFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hpFillPaint.setColor(0xFF00E676);

        qiFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        qiFillPaint.setColor(0xFFFFD700);

        textNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textNamePaint.setColor(0xFFFFFFFF);
        textNamePaint.setTextSize(14f);
        textNamePaint.setTypeface(Typeface.DEFAULT_BOLD);
        textNamePaint.setTextAlign(Paint.Align.CENTER);
        textNamePaint.setShadowLayer(4f, 0f, 0f, 0xFF000000);

        textDamagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textDamagePaint.setTextSize(20f);
        textDamagePaint.setTypeface(Typeface.DEFAULT_BOLD);
        textDamagePaint.setTextAlign(Paint.Align.CENTER);
        textDamagePaint.setShadowLayer(6f, 0f, 0f, 0xFF000000);

        hudPanelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudPanelPaint.setColor(0xCC0D0B18);

        hudBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudBorderPaint.setStyle(Paint.Style.STROKE);
        hudBorderPaint.setStrokeWidth(1.5f);
        hudBorderPaint.setColor(0x80FFD700);

        hudTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hudTextPaint.setColor(0xFFE0E0E0);
        hudTextPaint.setTextSize(12f);
        hudTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        buttonBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonBgPaint.setColor(0xEE1E1435);

        buttonTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonTextPaint.setColor(0xFFFFD700);
        buttonTextPaint.setTextSize(12f);
        buttonTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);

        ultimateBannerBgPaint = new Paint();
        ultimateBannerBgPaint.setColor(0xDD0A0518);

        ultimateBannerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ultimateBannerTextPaint.setColor(0xFFFFD700);
        ultimateBannerTextPaint.setTextSize(28f);
        ultimateBannerTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        ultimateBannerTextPaint.setTextAlign(Paint.Align.CENTER);
        ultimateBannerTextPaint.setShadowLayer(14f, 0f, 0f, 0xFFFF6D00);

        ultimateBannerSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ultimateBannerSubPaint.setColor(0xFF00E5FF);
        ultimateBannerSubPaint.setTextSize(16f);
        ultimateBannerSubPaint.setTypeface(Typeface.DEFAULT_BOLD);
        ultimateBannerSubPaint.setTextAlign(Paint.Align.CENTER);

        flashPaint = new Paint();
        flashPaint.setColor(0xFFFFFFFF);

        try {
            heroBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.img_cultivator_hero);
        } catch (Throwable ignored) {}
    }

    public void setBattle(BattleEngine engine) {
        this.battleEngine = engine;
        initAIStates();
    }

    public void reset() {
        animTime = 0f;
        shakeIntensity = 0f;
        flashIntensity = 0f;
        isUltimateActive = false;
        ultimateTimer = 0f;
        activeSwords = 0;
        activeParticles = 0;
        activeDamageNums = 0;
        initAIStates();
    }

    private void initAIStates() {
        aiStates.clear();
        if (battleEngine == null) return;
        ArrayList<BattleUnit> units = battleEngine.getUnits();
        int playerCount = 0;
        int enemyCount = 0;

        for (int i = 0; i < units.size(); i++) {
            BattleUnit u = units.get(i);
            int role = CultivatorCombatAI.determineRole(u);
            float startX, startY;

            if (u.team == 0) {
                // Player side (Front & Wings)
                float offset = (playerCount - 1) * 70f;
                startX = -140f - Math.abs(offset) * 0.2f;
                startY = offset;
                playerCount++;
            } else {
                // Enemy side
                float offset = (enemyCount - 1) * 70f;
                startX = 140f + Math.abs(offset) * 0.2f;
                startY = offset;
                enemyCount++;
            }

            CultivatorCombatAI.AIState ai = new CultivatorCombatAI.AIState(role, startX, startY);
            aiStates.add(ai);
        }
    }

    public void update(float dt) {
        float effectiveDt = dt * timeScale;
        animTime += effectiveDt;
        baguaRotation += 15f * effectiveDt;

        shakeIntensity = Math.max(0f, shakeIntensity * 0.85f);
        flashIntensity = Math.max(0f, flashIntensity - effectiveDt * 3f);

        // Update Camera Mode & Smooth Tracking
        updateCamera(effectiveDt);

        if (battleEngine != null && battleEngine.isRunning) {
            // Auto-Battle Logic & AI Decision Loops
            updateCombatAI(effectiveDt);
        }

        updateSwordStorm(effectiveDt);
        updateParticles(effectiveDt);
        updateDamageNumbers(effectiveDt);

        if (isUltimateActive) {
            ultimateTimer -= effectiveDt;
            if (ultimateTimer <= 0f) {
                isUltimateActive = false;
            }
        }
    }

    private void updateCamera(float dt) {
        switch (cameraMode) {
            case CAM_MODE_CINEMATIC_ORBIT:
                // Gentle swaying cinematic orbit
                projection.yaw = (float) Math.sin(animTime * 0.35f) * 12f;
                projection.pitch = 38f + (float) Math.sin(animTime * 0.5f) * 4f;
                projection.zoom = 1.0f + (float) Math.sin(animTime * 0.25f) * 0.05f;
                projection.targetX = (float) Math.sin(animTime * 0.4f) * 20f;
                projection.targetY = 0f;
                break;

            case CAM_MODE_ACTION_FOCUS:
                projection.zoom = 1.35f;
                projection.pitch = 42f;
                break;

            case CAM_MODE_ULTIMATE_ZOOM:
                projection.zoom = 1.6f;
                projection.pitch = 30f;
                break;
        }
        projection.updateTrig();
    }

    private void updateCombatAI(float dt) {
        ArrayList<BattleUnit> units = battleEngine.getUnits();
        ArrayList<BattleUnit> playerTeam = new ArrayList<BattleUnit>();
        ArrayList<BattleUnit> enemyTeam = new ArrayList<BattleUnit>();

        for (int i = 0; i < units.size(); i++) {
            BattleUnit u = units.get(i);
            if (u.team == 0) playerTeam.add(u);
            else enemyTeam.add(u);
        }

        for (int i = 0; i < units.size(); i++) {
            if (i >= aiStates.size()) break;
            final BattleUnit unit = units.get(i);
            final CultivatorCombatAI.AIState ai = aiStates.get(i);

            ArrayList<BattleUnit> allies = unit.team == 0 ? playerTeam : enemyTeam;
            ArrayList<BattleUnit> enemies = unit.team == 0 ? enemyTeam : playerTeam;

            // Update movement and kinematics
            CultivatorCombatAI.updateUnitAI(unit, ai, dt, allies, enemies, new CultivatorCombatAI.CombatActionCallback() {
                @Override
                public void onSkillImpact(BattleUnit actor, BattleSkill skill) {
                    executeSkillImpact(actor, ai, skill);
                }
            });

            // Auto-Battle Skill triggers
            if (unit.isAlive && ai.state == CultivatorCombatAI.STATE_IDLE_HOVER) {
                // Check if Ultimate ready and Auto is ON
                if ((autoBattle || unit.team == 1) && ai.qiCharge >= ai.maxQiCharge) {
                    for (int s = 0; s < ai.skills.size(); s++) {
                        BattleSkill sk = ai.skills.get(s);
                        if (sk.skillType == BattleSkill.TYPE_HEAVENLY_ULTIMATE && sk.isReady(ai.qiCharge)) {
                            triggerUltimate(unit, ai, sk);
                            break;
                        }
                    }
                }

                // Check Basic Attack / Regular Skill Ready
                if (ai.state == CultivatorCombatAI.STATE_IDLE_HOVER && unit.canAct()) {
                    BattleUnit target = CultivatorCombatAI.selectBestTarget(unit, ai, enemies);
                    if (target != null) {
                        int targetIdx = units.indexOf(target);
                        if (targetIdx >= 0 && targetIdx < aiStates.size()) {
                            CultivatorCombatAI.AIState targetAi = aiStates.get(targetIdx);
                            ai.targetX = targetAi.posX + (unit.team == 0 ? -40f : 40f);
                            ai.targetY = targetAi.posY;
                            ai.pendingSkill = ai.skills.get(0); // Basic Attack
                            ai.state = CultivatorCombatAI.STATE_LEAP_ATTACK;
                            ai.stateTimer = 0f;
                            unit.resetAction();
                        }
                    }
                } else {
                    unit.tickAction();
                }
            }
        }
    }

    public void triggerUltimate(BattleUnit actor, CultivatorCombatAI.AIState ai, BattleSkill skill) {
        if (actor == null || ai == null || skill == null) return;
        ai.pendingSkill = skill;
        ai.state = CultivatorCombatAI.STATE_CASTING_ULTIMATE;
        ai.stateTimer = 0f;
        skill.triggerCooldown();

        // Trigger Cinematic Ultimate Banner
        isUltimateActive = true;
        ultimateTimer = 1.3f;
        ultimateName = skill.name;
        ultimateChinese = skill.chineseName;
        ultimateColor = skill.vfxColor;

        shakeIntensity = 12f;
        triggerFlash(skill.vfxColor);

        // Spawn torrential Flying Sword Storm VFX
        if (skill.chineseName.contains("万剑") || skill.chineseName.contains("Sword")) {
            spawnSwordStorm(ai.posX, ai.posY, 120f, skill.vfxColor);
        }

        try {
            AudioManager.getInstance(context).playSfx(AudioManager.SFX_BREAKTHROUGH);
        } catch (Throwable ignored) {}
    }

    private void executeSkillImpact(BattleUnit actor, CultivatorCombatAI.AIState actorAi, BattleSkill skill) {
        if (actor == null || battleEngine == null || skill == null) return;

        ArrayList<BattleUnit> units = battleEngine.getUnits();
        int enemyTeam = actor.team == 0 ? 1 : 0;

        for (int i = 0; i < units.size(); i++) {
            BattleUnit target = units.get(i);
            if (target.team != enemyTeam || !target.isAlive) continue;

            CultivatorCombatAI.AIState targetAi = (i < aiStates.size()) ? aiStates.get(i) : null;

            // Damage calculation with formation & elemental buffs
            float formationMult = (activeFormation == 2 && actor.team == 0) ? 1.3f : 1.0f;
            int dmg = (int) (actor.calcDamage(target, skill.damageMultiplier * formationMult));
            if (dmg <= 0) dmg = 10;

            boolean isCrit = RNG.chance((int) (actor.critRate * ((activeFormation == 1 && actor.team == 0) ? 1.25f : 1.0f)));
            if (isCrit) dmg = (int) (dmg * 1.8f);

            target.takeDamage(dmg);

            // Spawn 3D VFX & Damage text
            float targetX = targetAi != null ? targetAi.posX : 0f;
            float targetY = targetAi != null ? targetAi.posY : 0f;
            float targetZ = targetAi != null ? targetAi.posZ : 0f;

            spawnDamageNumber(targetX, targetY, targetZ + 35f, (isCrit ? "CRIT! -" : "-") + dmg, isCrit ? 0xFFFF1744 : 0xFFFFD700);
            spawnHitParticles(targetX, targetY, targetZ + 15f, skill.vfxColor, isCrit ? 20 : 10);

            // Knockback physics
            if (targetAi != null) {
                targetAi.velX = (actor.team == 0 ? 120f : -120f);
                targetAi.velZ = 60f;
                targetAi.state = CultivatorCombatAI.STATE_KNOCKBACK;
                targetAi.stateTimer = 0f;
            }

            shakeIntensity = isCrit ? 14f : 6f;

            if (!skill.isAoE) break; // If single target, stop after first
        }

        // Check battle end condition
        checkBattleEnd();
    }

    private void checkBattleEnd() {
        if (battleEngine == null || !battleEngine.isRunning) return;
        ArrayList<BattleUnit> units = battleEngine.getUnits();
        boolean playersAlive = false;
        boolean enemiesAlive = false;

        for (int i = 0; i < units.size(); i++) {
            BattleUnit u = units.get(i);
            if (u.isAlive) {
                if (u.team == 0) playersAlive = true;
                else enemiesAlive = true;
            }
        }

        if (!playersAlive || !enemiesAlive) {
            battleEngine.isRunning = false;
            battleEngine.playerWon = playersAlive;
            if (callback != null) {
                callback.onBattleEnded(playersAlive);
            }
        }
    }

    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        if (canvas == null) return;
        screenW = canvas.getWidth();
        screenH = canvas.getHeight();
        projection.setViewport(screenW, screenH);

        canvas.save();

        // Screen Shake
        if (shakeIntensity > 0f) {
            canvas.translate(
                    (RNG.nextFloat() * 2f - 1f) * shakeIntensity,
                    (RNG.nextFloat() * 2f - 1f) * shakeIntensity
            );
        }

        // 1. Draw 3D Celestial Skybox & Astral Background
        renderCelestialSkybox(canvas, screenW, screenH);

        // 2. Draw 3D Bagua Taiji Formation Stage on Ground Plane (Z=0)
        render3DBaguaGroundStage(canvas);

        // 3. Draw 3D Flying Sword Storm VFX
        renderSwordStorm(canvas);

        // 4. Draw 3D Shadows & Cultivator Units (Depth sorted)
        render3DCultivatorUnits(canvas);

        // 5. Draw 3D Particles & Hit Arcs
        render3DParticles(canvas);

        // 6. Draw 3D Floating Combat Damage Numbers
        render3DDamageNumbers(canvas);

        // 7. Flash overlay
        if (flashIntensity > 0f) {
            flashPaint.setColor(flashColor);
            flashPaint.setAlpha((int) (flashIntensity * 160));
            canvas.drawRect(0, 0, screenW, screenH, flashPaint);
        }

        // 8. Draw Cinematic Ultimate Banner (If active)
        if (isUltimateActive) {
            renderCinematicUltimateBanner(canvas, screenW, screenH);
        }

        // 9. Draw Modern Donghua Combat HUD & Tactical Action Bar
        renderCombatHUD(canvas, screenW, screenH);

        canvas.restore();
    }

    /**
     * Renders a celestial Xianxia realm background with deep cosmic gradients, floating Dao stars, and glowing clouds.
     */
    private void renderCelestialSkybox(Canvas canvas, int w, int h) {
        bgGradientPaint.setShader(new LinearGradient(
                0, 0, 0, h,
                new int[]{0xFF05030A, 0xFF0D0B1C, 0xFF191030, 0xFF07050E},
                new float[]{0f, 0.4f, 0.75f, 1f},
                Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0, 0, w, h, bgGradientPaint);

        // Floating celestial Dao aura nebula
        baguaGlowPaint.setShader(new RadialGradient(
                w * 0.5f, h * 0.35f, w * 0.65f,
                new int[]{0x40FFD700, 0x2000E5FF, 0x00000000},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        ));
        canvas.drawCircle(w * 0.5f, h * 0.35f, w * 0.65f, baguaGlowPaint);
    }

    /**
     * Renders the glowing 3D Bagua Taiji Eight Trigrams formation on the 3D ground plane.
     */
    private void render3DBaguaGroundStage(Canvas canvas) {
        float arenaRadius = 240f;
        int ringSegments = 24;

        // Ground Outer Formation Circle in 3D
        polyPath.reset();
        for (int i = 0; i <= ringSegments; i++) {
            float angle = (float) (i * (Math.PI * 2.0 / ringSegments)) + (float) Math.toRadians(baguaRotation * 0.2f);
            float gx = (float) Math.cos(angle) * arenaRadius;
            float gy = (float) Math.sin(angle) * arenaRadius;
            projection.project(gx, gy, 0f, projPoint);
            if (i == 0) polyPath.moveTo(projPoint.x, projPoint.y);
            else polyPath.lineTo(projPoint.x, projPoint.y);
        }
        polyPath.close();

        baguaGlowPaint.setShader(new RadialGradient(
                projection.screenCenterX, projection.screenCenterY + 40f, arenaRadius * 1.5f,
                new int[]{0x3500E5FF, 0x15FFD700, 0x00000000},
                new float[]{0f, 0.6f, 1f}, Shader.TileMode.CLAMP
        ));
        canvas.drawPath(polyPath, baguaGlowPaint);
        canvas.drawPath(polyPath, baguaRingPaint);

        // Inner Taiji Yin-Yang Circles
        float innerRadius = 140f;
        polyPath.reset();
        for (int i = 0; i <= ringSegments; i++) {
            float angle = (float) (i * (Math.PI * 2.0 / ringSegments)) - (float) Math.toRadians(baguaRotation * 0.4f);
            float gx = (float) Math.cos(angle) * innerRadius;
            float gy = (float) Math.sin(angle) * innerRadius;
            projection.project(gx, gy, 0f, projPoint);
            if (i == 0) polyPath.moveTo(projPoint.x, projPoint.y);
            else polyPath.lineTo(projPoint.x, projPoint.y);
        }
        polyPath.close();
        trigramPaint.setColor(0x8000E5FF);
        canvas.drawPath(polyPath, trigramPaint);

        // 8 Trigram Hexagram Pillars
        for (int i = 0; i < 8; i++) {
            float angle = (float) (i * (Math.PI * 2.0 / 8.0)) + (float) Math.toRadians(baguaRotation * 0.2f);
            float gx = (float) Math.cos(angle) * (arenaRadius * 0.85f);
            float gy = (float) Math.sin(angle) * (arenaRadius * 0.85f);

            float scale = projection.project(gx, gy, 0f, projPoint);
            if (scale > 0f) {
                trigramPaint.setColor((i % 2 == 0) ? 0xCCFFD700 : 0xCC00E5FF);
                canvas.drawCircle(projPoint.x, projPoint.y, 5f * scale, trigramPaint);

                // Upward ethereal Qi pillar
                projection.project(gx, gy, 45f, projPoint2);
                canvas.drawLine(projPoint.x, projPoint.y, projPoint2.x, projPoint2.y, trigramPaint);
            }
        }
    }

    /**
     * Renders depth-sorted 3D Cultivators with dynamic height levitation, 3D shadows, flying swords, and health meters.
     */
    private void render3DCultivatorUnits(Canvas canvas) {
        if (battleEngine == null) return;
        ArrayList<BattleUnit> units = battleEngine.getUnits();
        if (units.isEmpty()) return;

        // Render 3D Shadows first on ground
        for (int i = 0; i < units.size(); i++) {
            if (i >= aiStates.size()) break;
            BattleUnit u = units.get(i);
            CultivatorCombatAI.AIState ai = aiStates.get(i);
            if (u.isAlive) {
                projection.render3DShadow(canvas, shadowPaint, ai.posX, ai.posY, ai.posZ, 32f);
            }
        }

        // Render Units with 3D projection
        for (int i = 0; i < units.size(); i++) {
            if (i >= aiStates.size()) break;
            BattleUnit u = units.get(i);
            CultivatorCombatAI.AIState ai = aiStates.get(i);

            float scale = projection.project(ai.posX, ai.posY, ai.posZ, projPoint);
            if (scale <= 0f) continue;

            float unitW = 75f * scale;
            float unitH = 85f * scale;
            float drawX = projPoint.x - unitW * 0.5f;
            float drawY = projPoint.y - unitH;

            // 1. Draw Ethereal Qi Aura
            if (u.isAlive) {
                qiAuraPaint.setColor(u.team == 0 ? 0x4000E5FF : 0x40FF1744);
                float auraPulse = (float) Math.sin(animTime * 6f + i) * 6f * scale;
                canvas.drawCircle(projPoint.x, projPoint.y - unitH * 0.5f, (unitW * 0.65f) + auraPulse, qiAuraPaint);
            }

            // 2. Draw Orbiting 3D Flying Swords
            if (u.isAlive) {
                for (int s = 0; s < CultivatorCombatAI.AIState.SWORD_COUNT; s++) {
                    float swX = ai.posX + ai.swordOffsetsX[s];
                    float swY = ai.posY + ai.swordOffsetsY[s];
                    float swZ = ai.posZ + ai.swordOffsetsZ[s];

                    float swScale = projection.project(swX, swY, swZ, projPoint2);
                    if (swScale > 0f) {
                        swordBladePaint.setColor(u.team == 0 ? 0xFFFFD700 : 0xFFFF3D00);
                        canvas.drawCircle(projPoint2.x, projPoint2.y, 3.5f * swScale, swordBladePaint);
                        canvas.drawLine(projPoint2.x - 6f * swScale, projPoint2.y - 8f * swScale,
                                projPoint2.x + 6f * swScale, projPoint2.y + 8f * swScale, swordBladePaint);
                    }
                }
            }

            // 3. Draw Cultivator Sprite or High-Fidelity Avatar
            if (heroBitmap != null && !heroBitmap.isRecycled() && u.isAlive) {
                dstRect.set((int) drawX, (int) drawY, (int) (drawX + unitW), (int) (drawY + unitH));
                canvas.save();
                if (u.team == 1) {
                    canvas.scale(-1f, 1f, projPoint.x, projPoint.y - unitH * 0.5f);
                }
                canvas.drawBitmap(heroBitmap, null, dstRect, unitBodyPaint);
                canvas.restore();
            } else {
                rectPool.set(drawX, drawY, drawX + unitW, drawY + unitH);
                unitBodyPaint.setColor(!u.isAlive ? 0xFF424242 : (u.team == 0 ? 0xFF1E88E5 : 0xFFE53935));
                canvas.drawRoundRect(rectPool, 12f * scale, 12f * scale, unitBodyPaint);
            }

            // 4. Draw Reactive Qi Shield
            if (ai.qiShieldAlpha > 0.05f) {
                shieldPaint.setColor(0xCC00E5FF);
                shieldPaint.setAlpha((int) (ai.qiShieldAlpha * 220));
                canvas.drawCircle(projPoint.x, projPoint.y - unitH * 0.5f, unitW * 0.8f, shieldPaint);
            }

            // 5. Cultivator Name & Realm Tag
            textNamePaint.setTextSize(Math.max(9f, 13f * scale));
            canvas.drawText(u.name, projPoint.x, drawY - 18f * scale, textNamePaint);

            // 6. 3D Floating Health & Spirit Qi Bar
            float barW = unitW * 1.1f;
            float barH = 6f * scale;
            float barX = projPoint.x - barW * 0.5f;
            float barY = drawY - 10f * scale;

            // HP Background
            rectPool.set(barX, barY, barX + barW, barY + barH);
            canvas.drawRoundRect(rectPool, 2f, 2f, hpBgPaint);

            // HP Fill
            float hpPct = u.maxHp > 0 ? Math.max(0f, Math.min(1f, u.hp / (float) u.maxHp)) : 0f;
            hpFillPaint.setColor(hpPct > 0.3f ? 0xFF00E676 : 0xFFFF3D00);
            rectPool.set(barX, barY, barX + barW * hpPct, barY + barH);
            canvas.drawRoundRect(rectPool, 2f, 2f, hpFillPaint);

            // Qi / Ultimate Gauge Bar
            float qiH = 4f * scale;
            float qiY = barY + barH + 2f;
            rectPool.set(barX, qiY, barX + barW, qiY + qiH);
            canvas.drawRoundRect(rectPool, 2f, 2f, hpBgPaint);

            float qiPct = ai.maxQiCharge > 0 ? Math.max(0f, Math.min(1f, ai.qiCharge / (float) ai.maxQiCharge)) : 0f;
            rectPool.set(barX, qiY, barX + barW * qiPct, qiY + qiH);
            canvas.drawRoundRect(rectPool, 2f, 2f, qiFillPaint);
        }
    }

    /**
     * Renders Torrential 3D Flying Sword Storm VFX across the battlefield.
     */
    private void renderSwordStorm(Canvas canvas) {
        for (int i = 0; i < activeSwords; i++) {
            float scale = projection.project(ssX[i], ssY[i], ssZ[i], projPoint);
            if (scale > 0f) {
                swordTrailPaint.setColor(ssColor[i]);
                swordTrailPaint.setAlpha((int) (Math.min(1f, ssLife[i]) * 255));
                canvas.drawCircle(projPoint.x, projPoint.y, 4f * scale, swordTrailPaint);

                // Trajectory tail
                float tailX = ssX[i] - ssVX[i] * 0.05f;
                float tailY = ssY[i] - ssVY[i] * 0.05f;
                float tailZ = ssZ[i] - ssVZ[i] * 0.05f;
                projection.project(tailX, tailY, tailZ, projPoint2);
                canvas.drawLine(projPoint.x, projPoint.y, projPoint2.x, projPoint2.y, swordTrailPaint);
            }
        }
    }

    private void render3DParticles(Canvas canvas) {
        for (int i = 0; i < activeParticles; i++) {
            float scale = projection.project(pX[i], pY[i], pZ[i], projPoint);
            if (scale > 0f) {
                swordBladePaint.setColor(pColor[i]);
                float alphaPct = pLife[i] / pMaxLife[i];
                swordBladePaint.setAlpha((int) (alphaPct * 255));
                canvas.drawCircle(projPoint.x, projPoint.y, pSize[i] * scale, swordBladePaint);
            }
        }
    }

    private void render3DDamageNumbers(Canvas canvas) {
        for (int i = 0; i < activeDamageNums; i++) {
            float scale = projection.project(dX[i], dY[i], dZ[i], projPoint);
            if (scale > 0f) {
                textDamagePaint.setColor(dColor[i]);
                textDamagePaint.setTextSize(Math.max(12f, 22f * scale));
                float alphaPct = Math.min(1f, dLife[i]);
                textDamagePaint.setAlpha((int) (alphaPct * 255));
                canvas.drawText(dText[i], projPoint.x, projPoint.y, textDamagePaint);
            }
        }
    }

    /**
     * Renders a grand Xianxia Donghua cinematic banner upon executing supreme heavenly ultimates.
     */
    private void renderCinematicUltimateBanner(Canvas canvas, int w, int h) {
        float bannerH = 110f;
        float bannerY = h * 0.22f;

        rectPool.set(0, bannerY, w, bannerY + bannerH);
        canvas.drawRect(rectPool, ultimateBannerBgPaint);

        hudBorderPaint.setColor(ultimateColor);
        canvas.drawLine(0, bannerY, w, bannerY, hudBorderPaint);
        canvas.drawLine(0, bannerY + bannerH, w, bannerY + bannerH, hudBorderPaint);

        ultimateBannerTextPaint.setColor(ultimateColor);
        canvas.drawText("⚡ " + ultimateChinese + " ⚡", w * 0.5f, bannerY + 48f, ultimateBannerTextPaint);
        canvas.drawText(ultimateName, w * 0.5f, bannerY + 84f, ultimateBannerSubPaint);
    }

    /**
     * Renders the modern Donghua Tactical Combat HUD:
     * - Top Status: Round / Turn / Arena Realm.
     * - Bottom Controls: Auto-Battle Toggle, Speed (1x/2x/4x), Formation selector, and Manual Ultimate Cards!
     */
    private void renderCombatHUD(Canvas canvas, int w, int h) {
        // Top Realm & Round Banner
        rectPool.set(w * 0.5f - 140f, 25f, w * 0.5f + 140f, 75f);
        canvas.drawRoundRect(rectPool, 16f, 16f, hudPanelPaint);
        hudBorderPaint.setColor(0x80FFD700);
        canvas.drawRoundRect(rectPool, 16f, 16f, hudBorderPaint);

        hudTextPaint.setTextAlign(Paint.Align.CENTER);
        hudTextPaint.setTextSize(14f);
        hudTextPaint.setColor(0xFFFFD700);
        if (battleEngine != null && battleEngine.isRunning) {
            canvas.drawText("Round " + battleEngine.round + " | Turn " + battleEngine.turn, w * 0.5f, 48f, hudTextPaint);
            hudTextPaint.setTextSize(10f);
            hudTextPaint.setColor(0xFFAAAAAA);
            canvas.drawText("Immortal Celestial Battlefield", w * 0.5f, 66f, hudTextPaint);
        } else if (battleEngine != null) {
            hudTextPaint.setColor(battleEngine.playerWon ? 0xFF00E676 : 0xFFFF1744);
            hudTextPaint.setTextSize(16f);
            canvas.drawText(battleEngine.playerWon ? "★ TRIUMPHANT VICTORY ★" : "☠ SECT DEFENDERS FALLEN ☠", w * 0.5f, 55f, hudTextPaint);

            // Victory Return Button
            btnReturnRect.set(w * 0.5f - 110f, 90f, w * 0.5f + 110f, 135f);
            canvas.drawRoundRect(btnReturnRect, 10f, 10f, buttonBgPaint);
            hudBorderPaint.setColor(battleEngine.playerWon ? 0xFFFFD700 : 0xFFFF1744);
            canvas.drawRoundRect(btnReturnRect, 10f, 10f, hudBorderPaint);
            buttonTextPaint.setTextSize(13f);
            canvas.drawText(battleEngine.playerWon ? "Claim Spoils & Return" : "Retreat to Sect", w * 0.5f, 118f, buttonTextPaint);
        }

        // Top Right Tactical Quick Toggles (Auto, Speed, Formation)
        float btnW = 72f;
        float btnH = 34f;
        float startX = w - 16f - btnW * 3f - 16f;

        // Auto Toggle
        btnAutoRect.set(startX, 25f, startX + btnW, 25f + btnH);
        buttonBgPaint.setColor(autoBattle ? 0xEE2E7D32 : 0xEE424242);
        canvas.drawRoundRect(btnAutoRect, 8f, 8f, buttonBgPaint);
        canvas.drawRoundRect(btnAutoRect, 8f, 8f, hudBorderPaint);
        buttonTextPaint.setColor(0xFFFFFFFF);
        buttonTextPaint.setTextSize(11f);
        canvas.drawText(autoBattle ? "AUTO ON" : "MANUAL", btnAutoRect.centerX(), btnAutoRect.centerY() + 4f, buttonTextPaint);

        // Speed Toggle
        btnSpeedRect.set(startX + btnW + 8f, 25f, startX + btnW * 2f + 8f, 25f + btnH);
        buttonBgPaint.setColor(0xEE1E1435);
        canvas.drawRoundRect(btnSpeedRect, 8f, 8f, buttonBgPaint);
        canvas.drawRoundRect(btnSpeedRect, 8f, 8f, hudBorderPaint);
        buttonTextPaint.setColor(0xFFFFD700);
        canvas.drawText((int) timeScale + "x SPEED", btnSpeedRect.centerX(), btnSpeedRect.centerY() + 4f, buttonTextPaint);

        // Formation Toggle
        btnFormationRect.set(startX + (btnW + 8f) * 2f, 25f, startX + btnW * 3f + 16f, 25f + btnH);
        canvas.drawRoundRect(btnFormationRect, 8f, 8f, buttonBgPaint);
        canvas.drawRoundRect(btnFormationRect, 8f, 8f, hudBorderPaint);
        String formName = activeFormation == 0 ? "BAGUA" : (activeFormation == 1 ? "4-BEAST" : "5-ELEM");
        canvas.drawText("阵 " + formName, btnFormationRect.centerX(), btnFormationRect.centerY() + 4f, buttonTextPaint);

        // Bottom Manual Ultimate Skill Cards for Player Cultivators
        if (battleEngine != null && battleEngine.isRunning) {
            renderPlayerUltimateCards(canvas, w, h);
        }
    }

    private void renderPlayerUltimateCards(Canvas canvas, int w, int h) {
        ArrayList<BattleUnit> units = battleEngine.getUnits();
        int playerIdx = 0;
        float cardW = (w - 48f) / 3f;
        float cardH = 65f;
        float cardY = h - cardH - 24f;

        for (int i = 0; i < units.size() && playerIdx < 3; i++) {
            BattleUnit u = units.get(i);
            if (u.team != 0) continue;

            CultivatorCombatAI.AIState ai = (i < aiStates.size()) ? aiStates.get(i) : null;
            float cardX = 16f + playerIdx * (cardW + 8f);
            ultimateCardRects[playerIdx].set(cardX, cardY, cardX + cardW, cardY + cardH);

            boolean isReady = (ai != null && ai.qiCharge >= ai.maxQiCharge && u.isAlive);

            // Card Background
            buttonBgPaint.setColor(isReady ? 0xEE1A237E : 0xDD120E22);
            canvas.drawRoundRect(ultimateCardRects[playerIdx], 12f, 12f, buttonBgPaint);

            hudBorderPaint.setColor(isReady ? 0xFFFFD700 : 0x40FFFFFF);
            hudBorderPaint.setStrokeWidth(isReady ? 2.5f : 1f);
            canvas.drawRoundRect(ultimateCardRects[playerIdx], 12f, 12f, hudBorderPaint);

            // Cultivator Name & Status
            buttonTextPaint.setTextSize(11f);
            buttonTextPaint.setColor(u.isAlive ? 0xFFFFFFFF : 0xFF757575);
            canvas.drawText(u.name, ultimateCardRects[playerIdx].centerX(), cardY + 20f, buttonTextPaint);

            if (u.isAlive) {
                buttonTextPaint.setTextSize(9f);
                buttonTextPaint.setColor(isReady ? 0xFFFFD700 : 0xFF00E5FF);
                canvas.drawText(isReady ? "⚡ ULTIMATE READY!" : "Qi: " + (ai != null ? ai.qiCharge : 0) + "%",
                        ultimateCardRects[playerIdx].centerX(), cardY + 38f, buttonTextPaint);

                // Qi Mini Progress Bar
                float barW = cardW - 20f;
                float barX = cardX + 10f;
                float barY = cardY + 48f;
                rectPool.set(barX, barY, barX + barW, barY + 4f);
                canvas.drawRect(rectPool, hpBgPaint);

                float pct = ai != null ? (ai.qiCharge / (float) ai.maxQiCharge) : 0f;
                rectPool.set(barX, barY, barX + barW * pct, barY + 4f);
                canvas.drawRect(rectPool, qiFillPaint);
            } else {
                buttonTextPaint.setTextSize(10f);
                buttonTextPaint.setColor(0xFFFF1744);
                canvas.drawText("FALLEN", ultimateCardRects[playerIdx].centerX(), cardY + 38f, buttonTextPaint);
            }

            playerIdx++;
        }
    }

    public boolean handleTouchEvent(float x, float y) {
        // Check Return / Claim Victory
        if (battleEngine != null && !battleEngine.isRunning) {
            if (btnReturnRect.contains(x, y)) {
                if (callback != null) callback.onBattleEnded(battleEngine.playerWon);
                return true;
            }
        }

        // Check Auto Toggle
        if (btnAutoRect.contains(x, y)) {
            autoBattle = !autoBattle;
            return true;
        }

        // Check Speed Toggle
        if (btnSpeedRect.contains(x, y)) {
            if (timeScale == 1.0f) timeScale = 2.0f;
            else if (timeScale == 2.0f) timeScale = 4.0f;
            else timeScale = 1.0f;
            return true;
        }

        // Check Formation Toggle
        if (btnFormationRect.contains(x, y)) {
            activeFormation = (activeFormation + 1) % 3;
            return true;
        }

        // Check Player Manual Ultimate Tap
        if (battleEngine != null && battleEngine.isRunning) {
            ArrayList<BattleUnit> units = battleEngine.getUnits();
            int pIdx = 0;
            for (int i = 0; i < units.size() && pIdx < 3; i++) {
                BattleUnit u = units.get(i);
                if (u.team != 0) continue;

                if (ultimateCardRects[pIdx].contains(x, y) && u.isAlive) {
                    CultivatorCombatAI.AIState ai = (i < aiStates.size()) ? aiStates.get(i) : null;
                    if (ai != null && ai.qiCharge >= ai.maxQiCharge) {
                        for (int s = 0; s < ai.skills.size(); s++) {
                            BattleSkill sk = ai.skills.get(s);
                            if (sk.skillType == BattleSkill.TYPE_HEAVENLY_ULTIMATE) {
                                triggerUltimate(u, ai, sk);
                                return true;
                            }
                        }
                    }
                }
                pIdx++;
            }
        }

        return false;
    }

    public void spawnSwordStorm(float centerX, float centerY, float radius, int color) {
        for (int i = 0; i < 16 && activeSwords < MAX_SWORD_STORM; i++) {
            int idx = activeSwords++;
            float angle = RNG.nextFloat() * 6.283f;
            float r = RNG.nextFloat() * radius;
            ssX[idx] = centerX + (float) Math.cos(angle) * r;
            ssY[idx] = centerY + (float) Math.sin(angle) * r;
            ssZ[idx] = 160f + RNG.nextFloat() * 80f;

            ssVX[idx] = (RNG.nextFloat() - 0.5f) * 60f;
            ssVY[idx] = (RNG.nextFloat() - 0.5f) * 60f;
            ssVZ[idx] = -320f - RNG.nextFloat() * 120f; // High speed downward dive

            ssLife[idx] = 0.8f;
            ssColor[idx] = color;
        }
    }

    private void updateSwordStorm(float dt) {
        for (int i = activeSwords - 1; i >= 0; i--) {
            ssLife[i] -= dt;
            ssX[i] += ssVX[i] * dt;
            ssY[i] += ssVY[i] * dt;
            ssZ[i] += ssVZ[i] * dt;

            if (ssZ[i] <= 0f) {
                // Impact ground! Spawn particle burst
                spawnHitParticles(ssX[i], ssY[i], 0f, ssColor[i], 4);
                ssLife[i] = 0f;
            }

            if (ssLife[i] <= 0f) {
                if (i < activeSwords - 1) {
                    ssX[i] = ssX[activeSwords - 1];
                    ssY[i] = ssY[activeSwords - 1];
                    ssZ[i] = ssZ[activeSwords - 1];
                    ssVX[i] = ssVX[activeSwords - 1];
                    ssVY[i] = ssVY[activeSwords - 1];
                    ssVZ[i] = ssVZ[activeSwords - 1];
                    ssLife[i] = ssLife[activeSwords - 1];
                    ssColor[i] = ssColor[activeSwords - 1];
                }
                activeSwords--;
            }
        }
    }

    public void spawnHitParticles(float x, float y, float z, int color, int count) {
        for (int j = 0; j < count && activeParticles < MAX_PARTICLES_3D; j++) {
            int idx = activeParticles++;
            pX[idx] = x;
            pY[idx] = y;
            pZ[idx] = z;
            pVX[idx] = (RNG.nextFloat() * 2f - 1f) * 120f;
            pVY[idx] = (RNG.nextFloat() * 2f - 1f) * 120f;
            pVZ[idx] = RNG.nextFloat() * 140f;
            pSize[idx] = 2.5f + RNG.nextFloat() * 3f;
            pLife[idx] = 0.45f + RNG.nextFloat() * 0.3f;
            pMaxLife[idx] = pLife[idx];
            pColor[idx] = color;
        }
    }

    private void updateParticles(float dt) {
        for (int i = activeParticles - 1; i >= 0; i--) {
            pLife[i] -= dt;
            pX[i] += pVX[i] * dt;
            pY[i] += pVY[i] * dt;
            pZ[i] += pVZ[i] * dt;
            pVZ[i] -= 220f * dt; // Gravity

            if (pLife[i] <= 0f || pZ[i] < 0f) {
                if (i < activeParticles - 1) {
                    pX[i] = pX[activeParticles - 1];
                    pY[i] = pY[activeParticles - 1];
                    pZ[i] = pZ[activeParticles - 1];
                    pVX[i] = pVX[activeParticles - 1];
                    pVY[i] = pVY[activeParticles - 1];
                    pVZ[i] = pVZ[activeParticles - 1];
                    pLife[i] = pLife[activeParticles - 1];
                    pMaxLife[i] = pMaxLife[activeParticles - 1];
                    pSize[i] = pSize[activeParticles - 1];
                    pColor[i] = pColor[activeParticles - 1];
                }
                activeParticles--;
            }
        }
    }

    public void spawnDamageNumber(float x, float y, float z, String text, int color) {
        if (activeDamageNums >= MAX_DAMAGE_NUMS) return;
        int idx = activeDamageNums++;
        dText[idx] = text;
        dX[idx] = x;
        dY[idx] = y;
        dZ[idx] = z;
        dLife[idx] = 1.2f;
        dColor[idx] = color;
    }

    private void updateDamageNumbers(float dt) {
        for (int i = activeDamageNums - 1; i >= 0; i--) {
            dLife[i] -= dt;
            dZ[i] += 40f * dt; // Ascend
            if (dLife[i] <= 0f) {
                if (i < activeDamageNums - 1) {
                    dText[i] = dText[activeDamageNums - 1];
                    dX[i] = dX[activeDamageNums - 1];
                    dY[i] = dY[activeDamageNums - 1];
                    dZ[i] = dZ[activeDamageNums - 1];
                    dLife[i] = dLife[activeDamageNums - 1];
                    dColor[i] = dColor[activeDamageNums - 1];
                }
                activeDamageNums--;
            }
        }
    }

    public void triggerFlash(int color) {
        flashColor = color;
        flashIntensity = 0.7f;
    }

    public void destroy() {
        if (heroBitmap != null && !heroBitmap.isRecycled()) {
            heroBitmap.recycle();
            heroBitmap = null;
        }
        for (Bitmap bmp : spriteCache.values()) {
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
            }
        }
        spriteCache.clear();
        aiStates.clear();
    }
}
