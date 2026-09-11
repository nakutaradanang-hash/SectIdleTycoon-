package com.sect.idle.gameplay;

import android.content.Context;
import com.sect.idle.models.Disciple;
import com.sect.idle.render.StateAnimationSystem;
import com.sect.idle.systems.AudioManager;

/**
 * GameplayFeedbackDispatcher - Centralized event dispatcher that synchronizes
 * game systems (WarSystem, TournamentSystem, TrainingSystem, BattleEngine)
 * with the UI StateAnimationSystem and AudioManager.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class GameplayFeedbackDispatcher {
    private static volatile GameplayFeedbackDispatcher instance;

    private Context context;
    private StateAnimationSystem animSystem;
    private AudioManager audioManager;

    public static GameplayFeedbackDispatcher getInstance() {
        if (instance == null) {
            synchronized (GameplayFeedbackDispatcher.class) {
                if (instance == null) {
                    instance = new GameplayFeedbackDispatcher();
                }
            }
        }
        return instance;
    }

    public void init(Context ctx) {
        this.context = ctx != null ? ctx.getApplicationContext() : null;
        this.animSystem = StateAnimationSystem.getInstance();
        if (ctx != null) {
            this.audioManager = AudioManager.getInstance(ctx);
        }
    }

    public void onCriticalStrike(float x, float y, String actor, String target, int damage, int repGain, boolean screenCenter) {
        if (animSystem != null) {
            animSystem.triggerCriticalStrike(x, y, actor, target, damage, repGain, screenCenter);
        }
        if (audioManager != null) {
            audioManager.playSfx(AudioManager.SFX_CRITICAL_STRIKE);
        }
    }

    public void onTournamentRound(float x, float y, Disciple d, String opponentName, boolean wonRound, int round, int prizeStones) {
        if (animSystem != null) {
            String discipleName = d != null ? d.name : "Disciple";
            animSystem.triggerTournamentClash(x, y, discipleName, opponentName, wonRound, round, prizeStones);
        }
        if (audioManager != null) {
            audioManager.playSfx(wonRound ? AudioManager.SFX_SWORD_SPAR : AudioManager.SFX_RING_BELL);
        }
    }

    public void onWarCampaignCompleted(WarSystem.WarResult result) {
        if (result == null) return;
        if (animSystem != null) {
            animSystem.triggerWarConquest(result.targetSectName, result.won, result.repChange, result.stoneReward, result.jadeReward);
        }
        if (audioManager != null) {
            if (result.won) {
                audioManager.playSfx(AudioManager.SFX_VICTORY);
                audioManager.playSfx(AudioManager.SFX_BARRIER_SHATTER);
            } else {
                audioManager.playSfx(AudioManager.SFX_DEFEAT);
            }
        }
    }

    public void onDiscipleBreakthrough(Disciple d, String newRealmName) {
        if (d == null) return;
        if (animSystem != null) {
            animSystem.triggerBreakthrough(d.position.x, d.position.y, d.name, newRealmName);
        }
        if (audioManager != null) {
            audioManager.playSfx(AudioManager.SFX_BREAKTHROUGH);
        }
    }

    public void onStatGain(float x, float y, String text, int color) {
        if (animSystem != null) {
            animSystem.triggerStatSurge(x, y, text, color);
        }
    }
}
