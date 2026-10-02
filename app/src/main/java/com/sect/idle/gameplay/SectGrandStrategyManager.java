package com.sect.idle.gameplay;

import com.sect.idle.core.FixedMath;
import com.sect.idle.core.WorldScaleEngine;
import com.sect.idle.models.Disciple;
import com.sect.idle.utils.ErrorCode;
import com.sect.idle.utils.ExceptionManager;

/**
 * SectGrandStrategyManager - Unified Master Orchestrator.
 * Combines Romance of the Three Kingdoms grand diplomacy & officer council,
 * Civilization 4X hex grid expansion & research trees, and The Sims life sim & needs.
 *
 * 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class SectGrandStrategyManager {

    private static volatile SectGrandStrategyManager instance;
    private static final Object LOCK = new Object();

    public final FactionDiplomacySystem diplomacy;
    public final OfficerCouncilSystem council;
    public final RealmHexGridMap hexMap;
    public final DaoTechTreeSystem techTree;
    public final SectPolicySystem policies;
    public final WonderMonumentSystem wonders;
    public final DiscipleLifeSimSystem lifeSim;
    public final ClimateSeasonSystem climate;
    public final WorldScaleEngine worldEngine;

    private int turnCount = 0;
    private int victoryStatus = WonderMonumentSystem.VICTORY_NONE;

    public static SectGrandStrategyManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new SectGrandStrategyManager();
                }
            }
        }
        return instance;
    }

    private SectGrandStrategyManager() {
        this.diplomacy = FactionDiplomacySystem.getInstance();
        this.council = OfficerCouncilSystem.getInstance();
        this.hexMap = RealmHexGridMap.getInstance();
        this.techTree = DaoTechTreeSystem.getInstance();
        this.policies = SectPolicySystem.getInstance();
        this.wonders = WonderMonumentSystem.getInstance();
        this.lifeSim = DiscipleLifeSimSystem.getInstance();
        this.climate = ClimateSeasonSystem.getInstance();
        this.worldEngine = WorldScaleEngine.getInstance();
    }

    /**
     * Executes a full grand turn progression (simulating 1 day in the Jianghu realm).
     */
    public String advanceTurn(final SectData sect) {
        if (sect == null) return "Error: SectData is null";

        turnCount++;
        final StringBuilder log = new StringBuilder();
        log.append("=== [DAY ").append(turnCount).append("] JIANHU REALM ADVANCEMENT ===\n");

        ExceptionManager.executeSafely(new ExceptionManager.SafeRunnable() {
            @Override
            public void run() throws Throwable {
                // 1. Advance Climate and Weather
                climate.advanceDay(sect);
                log.append("🌤️ Weather: ").append(climate.getWeatherName())
                   .append(" (").append(climate.getSeasonName()).append(", ").append((int) climate.ambientTemperatureCelsius).append("°C)\n");

                // 2. Ticks 4X Hex Grid Harvests
                hexMap.collectTurnHarvest(sect);
                log.append("🗺️ Hex Outposts: ").append(hexMap.getPlayerControlledTileCount())
                   .append(" territories harvested.\n");

                // 3. Ticks Dao Tech Research
                long researchYield = 20L + (sect.disciples != null ? sect.disciples.size() * 5L : 0L);
                if (policies.isPolicyActive("POL_HERMIT_DAO")) {
                    researchYield = (long)(researchYield * 1.4f);
                }
                boolean techFinished = techTree.addResearchProgress(researchYield);
                if (techFinished) {
                    log.append("💡 [TECH BREAKTHROUGH] Researched: ").append(techTree.getTech(techTree.currentResearchTechId).name).append("!\n");
                }

                // 4. Advance Wonder Construction
                boolean wonderFinished = wonders.advanceConstructionTurn();
                if (wonderFinished) {
                    log.append("🏛️ [WONDER ERECTED] Ancient Wonder completed!\n");
                }

                // 5. Ticks Three Kingdoms Faction Diplomacy & AI Sects
                diplomacy.advanceTurn(sect);

                // 6. Officer Council & Loyalty Morale Checks
                String loyaltyWarning = council.tickOfficerLoyalty(sect);
                if (loyaltyWarning != null) {
                    log.append(loyaltyWarning);
                }

                // 7. The Sims Needs Simulation & Moodlets
                lifeSim.tickDiscipleLifeNeeds(sect);

                // 8. Deterministic Physics & World Scale step
                worldEngine.stepSimulation();

                // 9. Evaluate 4X Victory Conditions
                victoryStatus = wonders.checkVictoryCondition(sect, diplomacy);
                if (victoryStatus != WonderMonumentSystem.VICTORY_NONE) {
                    log.append("🏆 [VICTORY ACHIEVED] ");
                    switch (victoryStatus) {
                        case WonderMonumentSystem.VICTORY_MARTIAL_CONQUEST:
                            log.append("Martial Hegemony - All rival sects subjugated!\n");
                            break;
                        case WonderMonumentSystem.VICTORY_CELESTIAL_ASCENSION:
                            log.append("Celestial Ascension - Built Tower of 9 Heavens and reached Godhood!\n");
                            break;
                        case WonderMonumentSystem.VICTORY_CULTURAL_ENLIGHTENMENT:
                            log.append("Cultural Enlightenment - Grand Lotus Wisdom achieved!\n");
                            break;
                        case WonderMonumentSystem.VICTORY_ECONOMIC_JADE:
                            log.append("Economic Jade Monopoly - Dominated all realm commerce!\n");
                            break;
                    }
                }
            }
        }, "GRAND_STRATEGY", "AdvanceTurn Day " + turnCount);

        return log.toString();
    }

    public int getTurnCount() { return turnCount; }
    public int getVictoryStatus() { return victoryStatus; }
}
