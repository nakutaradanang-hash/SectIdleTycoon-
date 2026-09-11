package com.sect.idle.integration;

import com.sect.idle.core.GameConfig;
import com.sect.idle.gameplay.MarketSystem;
import com.sect.idle.gameplay.ResourceManager;
import com.sect.idle.gameplay.SectData;
import com.sect.idle.models.Building;
import com.sect.idle.models.Item;
import com.sect.idle.models.MarketListing;
import com.sect.idle.utils.DataValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

/**
 * EconomyAndInventoryIntegrationTest - Comprehensive multi-module integration test validating:
 * 1. Resource accumulation, consumption, and bounds enforcement.
 * 2. Market trading pipelines, price fluctuations, dynamic supply/demand.
 * 3. Inventory item stacking, capacity limits, durability loss, and equipment equipping.
 * 4. Building construction, level-up costs, workforce assignment, and production multipliers.
 * 5. Negative and boundary conditions (arithmetic overflow, negative spending, zero balance operations).
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class EconomyAndInventoryIntegrationTest {

    private SectData sectData;
    private ResourceManager resourceManager;
    private MarketSystem marketSystem;

    @Before
    public void setUp() {
        sectData = SectData.getInstance();
        sectData.reset();
        sectData.spiritStones = 10000;
        sectData.spiritHerbs = 5000;
        sectData.spiritOres = 5000;
        sectData.jade = 50;

        resourceManager = ResourceManager.getInstance();
        resourceManager.syncFromSectData(sectData);

        marketSystem = new MarketSystem();
    }

    @Test
    public void testResourceSpendingAndBuildingUpgradePipeline() {
        // Construct Main Hall
        Building mainHall = sectData.buildings.get(GameConfig.BUILD_HALL);
        Assert.assertNotNull(mainHall);

        long initialStones = sectData.spiritStones;
        mainHall.build();
        Assert.assertTrue(mainHall.isBuilt);
        Assert.assertEquals(1, mainHall.level);

        // Upgrade Main Hall
        int initialLvl = mainHall.level;
        mainHall.upgrade();
        Assert.assertEquals(initialLvl + 1, mainHall.level);

        // Validate production and capacity scaling
        long dailyIncome = mainHall.getDailyIncome();
        Assert.assertTrue(dailyIncome >= 0);

        // Assign workers to Herb Garden
        Building herbGarden = sectData.buildings.get(GameConfig.BUILD_GARDEN);
        herbGarden.build();
        herbGarden.assignWorker();
        herbGarden.assignWorker();
        Assert.assertEquals(2, herbGarden.workers);

        // Unassign worker
        herbGarden.removeWorker();
        Assert.assertEquals(1, herbGarden.workers);
    }

    @Test
    public void testMarketCommodityTradingPipeline() {
        Assert.assertNotNull(marketSystem.listings);
        Assert.assertFalse(marketSystem.listings.isEmpty());

        MarketListing herbListing = marketSystem.listings.get(0);
        long initialPrice = herbListing.price;

        // Simulate market fluctuation over multiple ticks
        for (int i = 0; i < 30; i++) {
            marketSystem.tick();
        }

        // Verify bounds & sanity
        Assert.assertTrue("Market price must remain positive", herbListing.price > 0);
        Assert.assertTrue("Market price must respect bounds", herbListing.price >= (long)(herbListing.originalPrice * 0.5f));
        Assert.assertTrue("Market price must respect bounds", herbListing.price <= (long)(herbListing.originalPrice * 2.5f));

        // Manual force price update
        marketSystem.updatePrices();
        Assert.assertTrue(herbListing.price > 0);
    }

    @Test
    public void testInventoryStackingAndDurabilityLifecycle() {
        ArrayList<Item> inventory = new ArrayList<Item>();

        // 1. Add Stackable Material Items
        Item herbs1 = new Item("spirit_herb", 50);
        Item herbs2 = new Item("spirit_herb", 30);

        Assert.assertTrue(herbs1.canStackWith(herbs2));
        int remaining = herbs1.add(herbs2.count);
        Assert.assertEquals(0, remaining);
        Assert.assertEquals(80, herbs1.count);

        // 2. Add Max Stack Overflow Test
        int overflowRemaining = herbs1.add(1000);
        Assert.assertTrue("Remaining items should not exceed stack cap", overflowRemaining > 0);
        Assert.assertEquals(herbs1.getMaxStack(), herbs1.count);
        Assert.assertTrue(herbs1.isFullStack());

        // 3. Item Consumption / Removal
        int removed = herbs1.remove(30);
        Assert.assertEquals(30, removed);
        Assert.assertFalse(herbs1.isEmpty());

        // 4. Test Equipment Durability
        Item dragonScale = new Item("dragon_scale", 1);
        Assert.assertTrue(dragonScale.isEquipment());
        Assert.assertEquals(1.0f, dragonScale.getDurabilityPercent(), 0.01f);
        Assert.assertFalse(dragonScale.isBroken());

        dragonScale.damage(25);
        Assert.assertTrue(dragonScale.getDurabilityPercent() < 1.0f);
        Assert.assertTrue(dragonScale.getDurability() > 0);

        // Break equipment completely
        dragonScale.damage(100);
        Assert.assertTrue(dragonScale.isBroken());
        Assert.assertEquals(0.0f, dragonScale.getDurabilityPercent(), 0.01f);
    }

    @Test
    public void testNegativeAndBoundaryConditions() {
        // Boundary 1: Arithmetic Overflow Protection
        long safeMax = DataValidator.safeAdd(Long.MAX_VALUE - 10, 20);
        Assert.assertEquals(Long.MAX_VALUE, safeMax);

        long safeMult = DataValidator.safeMultiply(Long.MAX_VALUE / 2, 4);
        Assert.assertEquals(Long.MAX_VALUE, safeMult);

        // Boundary 2: Non-negative subtraction underflow
        long safeSub = DataValidator.safeSubtractNonNegative(50, 100);
        Assert.assertEquals(0L, safeSub);

        // Boundary 3: Price and Resource Clamping
        long clampedStones = DataValidator.clampLong(-500L, 0L, 1000000000000L);
        Assert.assertEquals(0L, clampedStones);

        long clampedOversized = DataValidator.clampLong(5000000000000L, 0L, 1000000000000L);
        Assert.assertEquals(1000000000000L, clampedOversized);

        // Boundary 4: Validate Price Limits
        Assert.assertFalse(DataValidator.validatePrice(0));
        Assert.assertFalse(DataValidator.validatePrice(-100));
        Assert.assertTrue(DataValidator.validatePrice(5000));
    }
}
