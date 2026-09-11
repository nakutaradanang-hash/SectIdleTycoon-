package com.sect.idle.gameplay;

import com.sect.idle.models.BattleUnit;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

/**
 * BattleEngine - Turn-based ATB battle simulation.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BattleEngine {
    public boolean isRunning = false;
    public boolean playerWon = false;
    public int round = 1;
    public int turn = 0;

    private final ArrayList<BattleUnit> units;
    private final ArrayList<String> log;
    private static final int MAX_LOG_SIZE = 50;

    public BattleEngine() {
        this.units = new ArrayList<BattleUnit>();
        this.log = new ArrayList<String>();
    }

    public void startBattle(ArrayList<Disciple> playerTeam, ArrayList<Disciple> enemyTeam) {
        units.clear();
        log.clear();
        round = 1;
        turn = 0;
        isRunning = true;
        playerWon = false;

        if (playerTeam != null) {
            for (int i = 0; i < playerTeam.size(); i++) {
                Disciple d = playerTeam.get(i);
                if (d != null && d.isAlive()) {
                    units.add(new BattleUnit(d, 0));
                }
            }
        }

        if (enemyTeam != null) {
            for (int i = 0; i < enemyTeam.size(); i++) {
                Disciple d = enemyTeam.get(i);
                if (d != null && d.isAlive()) {
                    units.add(new BattleUnit(d, 1));
                }
            }
        }

        addLog("Battle begins! Round " + round);
    }

    public void tick() {
        if (!isRunning) return;

        if (units.isEmpty()) {
            isRunning = false;
            return;
        }

        turn++;
        for (int i = 0; i < units.size(); i++) {
            BattleUnit u = units.get(i);
            if (u == null || !u.isAlive) continue;

            u.tickAction();
            u.tickCooldowns();

            if (u.canAct()) {
                performTurn(u);
                u.resetAction();
                checkBattleEnd();
                if (!isRunning) break;
            }
        }
    }

    private void performTurn(BattleUnit actor) {
        if (actor == null || !actor.isAlive) return;
        actor.tickRegen();

        BattleUnit target = findTarget(actor.team == 0 ? 1 : 0);
        if (target == null) return;

        int dmg = actor.calcDamage(target, 1.0f);
        if (dmg <= 0) {
            addLog(actor.name + " attacked " + target.name + ", but missed!");
        } else {
            target.takeDamage(dmg);
            if (dmg > actor.atk * 1.5f) {
                addLog(actor.name + " landed a critical hit on " + target.name + " for " + dmg + " dmg!");
            } else {
                addLog(actor.name + " attacked " + target.name + " for " + dmg + " dmg.");
            }

            if (!target.isAlive) {
                actor.kills++;
                addLog(target.name + " has fallen in combat!");
            }
        }
    }

    private BattleUnit findTarget(int team) {
        int count = 0;
        for (int i = 0; i < units.size(); i++) {
            BattleUnit u = units.get(i);
            if (u != null && u.isAlive && u.team == team) {
                count++;
            }
        }
        if (count == 0) return null;
        int targetIndex = RNG.nextInt(count);
        int current = 0;
        for (int i = 0; i < units.size(); i++) {
            BattleUnit u = units.get(i);
            if (u != null && u.isAlive && u.team == team) {
                if (current == targetIndex) {
                    return u;
                }
                current++;
            }
        }
        return null;
    }

    private void checkBattleEnd() {
        boolean playerAlive = false;
        boolean enemyAlive = false;

        for (int i = 0; i < units.size(); i++) {
            BattleUnit u = units.get(i);
            if (u != null && u.isAlive) {
                if (u.team == 0) playerAlive = true;
                else enemyAlive = true;
            }
        }

        if (!playerAlive || !enemyAlive) {
            isRunning = false;
            playerWon = playerAlive;
            if (playerWon) {
                addLog("VICTORY! The disciples triumphed!");
            } else {
                addLog("DEFEAT! The sect fell back.");
            }
        }
    }

    public void addLog(String msg) {
        if (msg == null) return;
        log.add(msg);
        if (log.size() > MAX_LOG_SIZE) {
            log.remove(0);
        }
    }

    public ArrayList<BattleUnit> getUnits() { return units; }
    public ArrayList<String> getLog() { return log; }
}
