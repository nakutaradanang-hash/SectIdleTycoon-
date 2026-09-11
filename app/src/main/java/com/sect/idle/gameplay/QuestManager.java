package com.sect.idle.gameplay;

import com.sect.idle.models.Quest;

import java.util.ArrayList;

/**
 * QuestManager - Single-responsibility manager for Sect quests, missions,
 * completion validation, and spiritual rewards.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class QuestManager {

    private static volatile QuestManager instance;
    private static final Object LOCK = new Object();

    private QuestManager() {}

    public static QuestManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new QuestManager();
                }
            }
        }
        return instance;
    }

    /**
     * Populates starter sect missions.
     */
    public ArrayList<Quest> generateStarterQuests() {
        ArrayList<Quest> quests = new ArrayList<Quest>();

        Quest q1 = new Quest("q_herbs", "Gather Spirit Herbs", "Harvest 50 Spirit Herbs from the Garden", 1, 50, 200, 20, 10);
        quests.add(q1);

        Quest q2 = new Quest("q_train", "Train Disciples", "Raise any disciple to higher mastery", 2, 3, 300, 30, 20);
        quests.add(q2);

        Quest q3 = new Quest("q_domain", "Expand Sect Domain", "Upgrade the Main Hall to Level 2", 0, 2, 500, 50, 30);
        quests.add(q3);

        return quests;
    }

    /**
     * Checks if a quest is complete and grants rewards.
     */
    public boolean claimReward(Quest quest, SectData data) {
        if (quest == null || data == null) return false;
        if (quest.completed && !quest.claimed) {
            quest.claimed = true;
            data.earn(quest.rewardSS, 0, 0);
            data.jade += quest.rewardJade;
            return true;
        }
        return false;
    }
}
