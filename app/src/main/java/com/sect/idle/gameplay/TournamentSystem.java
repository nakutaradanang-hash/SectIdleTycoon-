package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

/**
 * TournamentSystem - Manages grand sect tournaments, disciple duels,
 * and arena ranking rewards.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class TournamentSystem {
    private static volatile TournamentSystem instance;

    public static TournamentSystem getInstance() {
        if (instance == null) {
            synchronized (TournamentSystem.class) {
                if (instance == null) {
                    instance = new TournamentSystem();
                }
            }
        }
        return instance;
    }

    public static class TournamentResult {
        public boolean won;
        public int roundsWon;
        public int prizeStones;
        public int prizeJade;
        public int prizeRep;
        public String opponentName;
        public String summary;
    }

    public TournamentResult enterTournament(Disciple d, SectData s, int tier) {
        TournamentResult result = new TournamentResult();
        if (d == null || s == null) {
            result.won = false;
            result.summary = "No disciple selected.";
            return result;
        }

        d.totalBattles++;
        int rounds = 3; // Best of 3 or 3 elimination stages
        int wonRounds = 0;
        int enemyBaseCombat = 100 + tier * 150 + RNG.nextInt(0, 100);

        for (int r = 1; r <= rounds; r++) {
            int disciplePower = (int) d.getPowerRating() + RNG.nextInt(-20, 30);
            int opponentPower = enemyBaseCombat + (r * 40);
            if (disciplePower >= opponentPower) {
                wonRounds++;
            }
        }

        result.roundsWon = wonRounds;
        result.won = (wonRounds >= 2);
        result.opponentName = "Champion of Rival Sect";

        // Check for Critical Strike during the duel
        boolean isCrit = RNG.nextFloat() < 0.45f;
        if (isCrit) {
            int critDmg = (int) (d.getPowerRating() * 2.2f + RNG.nextInt(50, 200));
            GameplayFeedbackDispatcher.getInstance().onCriticalStrike(d.position.x, d.position.y, d.name, result.opponentName, critDmg, 20 * (tier + 1), true);
        }

        if (result.won) {
            d.battlesWon++;
            d.totalBattlesWon++;
            int prizeStones = 300 * (tier + 1);
            int prizeJade = 10 * (tier + 1);
            int prizeRep = 25 * (tier + 1);

            result.prizeStones = prizeStones;
            result.prizeJade = prizeJade;
            result.prizeRep = prizeRep;

            d.reputation = Math.min(10000, d.reputation + prizeRep);
            d.addExperience(100 * (tier + 1));
            s.earn(prizeStones, 0, 0);
            s.jade += prizeJade;
            s.markEconomyDirty();

            result.summary = d.name + " emerged victorious in the Grand Tournament (Tier " + (tier + 1) + ")!";
            GameplayFeedbackDispatcher.getInstance().onTournamentRound(d.position.x, d.position.y, d, result.opponentName, true, wonRounds, prizeStones);
        } else {
            int consolationStones = 50 * (tier + 1);
            result.prizeStones = consolationStones;
            s.earn(consolationStones, 0, 0);
            d.addExperience(30 * (tier + 1));
            result.summary = d.name + " fought valiantly but was eliminated in round " + (wonRounds + 1) + ".";
            GameplayFeedbackDispatcher.getInstance().onTournamentRound(d.position.x, d.position.y, d, result.opponentName, false, wonRounds + 1, consolationStones);
        }

        return result;
    }
}
