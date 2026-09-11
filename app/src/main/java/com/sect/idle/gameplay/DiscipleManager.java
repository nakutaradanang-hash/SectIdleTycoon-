package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;

import java.util.ArrayList;

/**
 * DiscipleManager - Single-responsibility manager for Disciple lifecycle, recruitment,
 * stat growth, realm breakthroughs, and task assignment.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class DiscipleManager {

    private static final String[] MALE_NAMES = {
            "Li Yun", "Zhang Wei", "Wang Fang", "Chen Ming", "Liu Hua", "Zhao Kai", "Sun Mei", "Wu Jian",
            "Xiao Yan", "Lin Dong", "Han Li", "Meng Hao", "Wang Lin", "Bai Xiaochun", "Ji Ning", "Qin Yu"
    };

    private static final String[] FEMALE_NAMES = {
            "Su Ling", "Ye Qing", "Liu Meng", "Zhao Wan", "Lin Yue", "Mu Chen", "Tang Wan", "Chu Yu",
            "Nalan Yan", "Yun Yun", "Xun Er", "Zi Yan", "Feng Qing", "Luo Li", "Jiang Chen", "Lu Xue"
    };

    private static volatile DiscipleManager instance;
    private static final Object LOCK = new Object();

    private DiscipleManager() {}

    public static DiscipleManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new DiscipleManager();
                }
            }
        }
        return instance;
    }

    /**
     * Creates a newly generated disciple with randomized attributes, elemental roots, personality, talent, and hobby.
     */
    public Disciple createRandomDisciple(int minStat, int maxStat, int minRealm, int maxRealm) {
        Disciple d = new Disciple();
        d.isMale = RNG.chance(50);
        d.name = d.isMale ? RNG.pick(MALE_NAMES) : RNG.pick(FEMALE_NAMES);

        d.initStats(
                RNG.nextInt(minStat, maxStat),
                RNG.nextInt(minStat, maxStat),
                RNG.nextInt(minStat, maxStat),
                RNG.nextInt(Math.max(5, minStat - 10), maxStat - 5),
                RNG.nextInt(Math.max(5, minStat - 10), maxStat - 5),
                RNG.nextInt(Math.max(5, minStat - 10), maxStat - 5),
                RNG.nextInt(5, 30)
        );

        d.realm = RNG.nextInt(minRealm, maxRealm);
        d.realmExp = RNG.nextInt(100, 600);
        d.element = RNG.nextInt(GameConfig.ELEM_COUNT);
        d.personality = RNG.nextInt(GameConfig.PER_COUNT);

        try {
            TalentSystem.generateTalent(d);
        } catch (Exception ignored) {
            d.talentType = GameConfig.TAL_NONE;
        }

        try {
            HobbySystem.assignRandomHobby(d);
        } catch (Exception ignored) {
            d.hobby = 0;
        }

        d.dailyWage = d.getExpectedWage();
        d.recalcCombat();
        d.updateEfficiency();
        return d;
    }

    /**
     * Seeds default initial disciples for new sect creation.
     */
    public ArrayList<Disciple> generateInitialDisciples(int count) {
        ArrayList<Disciple> list = new ArrayList<Disciple>();
        for (int i = 0; i < count; i++) {
            Disciple d = createRandomDisciple(15, 45, 0, 1);
            if (i == 0) d.currentTask = GameConfig.TASK_FARMING;
            else if (i == 1) d.currentTask = GameConfig.TASK_ALCHEMY;
            else d.currentTask = GameConfig.TASK_GUARD;
            d.updateEfficiency();
            list.add(d);
        }
        return list;
    }

    /**
     * Assigns a disciple to a specific sect duty and recalculates efficiency.
     */
    public boolean assignTask(Disciple disciple, int task) {
        if (disciple == null) return false;
        disciple.currentTask = task;
        disciple.updateEfficiency();
        SectData.getInstance().recalculateEconomy();
        return true;
    }

    /**
     * Attempts a realm breakthrough for a disciple.
     */
    public boolean attemptBreakthrough(Disciple disciple) {
        if (disciple == null) return false;
        int expNeeded = 1000 + (disciple.realm * 1500);
        if (disciple.realmExp < expNeeded) return false;

        // Base 70% success rate modulated by wisdom
        int chance = 70 + (disciple.wis / 5);
        if (chance > 95) chance = 95;

        if (RNG.chance(chance)) {
            disciple.realmExp -= expNeeded;
            disciple.realm++;
            disciple.str += 5;
            disciple.vit += 5;
            disciple.intel += 5;
            disciple.wis += 5;
            disciple.recalculateStats();
            disciple.hp = disciple.maxHp;
            disciple.recalcCombat();
            disciple.updateEfficiency();
            return true;
        } else {
            disciple.realmExp = (int)(disciple.realmExp * 0.7f); // loss on failure
            return false;
        }
    }
}
