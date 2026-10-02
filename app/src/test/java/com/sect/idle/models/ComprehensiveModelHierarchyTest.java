package com.sect.idle.models;

import com.sect.idle.core.GameConfig;
import org.junit.Assert;
import org.junit.Test;

public class ComprehensiveModelHierarchyTest {

    @Test
    public void testSkillCooldownAndPower() {
        Skill skill = new Skill("sk_1", "Dragon Palm", "Strikes enemy with dragon qi", 1, GameConfig.ELEM_FIRE, 5, 3, 20, 1.5f, 0, 1, GameConfig.RARITY_EPIC);
        Assert.assertEquals("Dragon Palm", skill.name);
        Assert.assertTrue(skill.isReady());
        Assert.assertEquals("Epic", skill.getRarityName());

        skill.resetCd();
        Assert.assertEquals(3, skill.currentCooldown);
        Assert.assertFalse(skill.isReady());

        skill.use();
        Assert.assertEquals(2, skill.currentCooldown);
        skill.use();
        skill.use();
        Assert.assertTrue(skill.isReady());

        skill.level = 2;
        Assert.assertTrue(skill.getPower() > 1.5f);
    }

    @Test
    public void testTalentAttributesAndFormatting() {
        Talent talent = new Talent("T1", "Heavenly Fire Body", "Flames of Rebirth", "Grants pure fire affinity",
                Talent.CATEGORY_BODY, Talent.GRADE_DIVINE, GameConfig.ELEM_FIRE, 0xFFFF5722);

        talent.expMultiplier = 1.35f;
        talent.breakthroughRateBonus = 15;
        talent.atkMultiplier = 1.25f;
        talent.critRateBonus = 10;
        talent.hpRegenPct = 0.05f;

        Assert.assertEquals("Divine Grade [Legend]", talent.getGradeLabel());
        String summary = talent.getFormattedSummary();
        Assert.assertTrue(summary.contains("Heavenly Fire Body"));
        Assert.assertTrue(summary.contains("Cultivation EXP: +35%"));
        Assert.assertTrue(summary.contains("Combat ATK: +25%"));
    }

    @Test
    public void testFairyFeedingAndInteraction() {
        Fairy fairy = new Fairy("Lumi", "Wind");
        Assert.assertEquals("Lumi", fairy.name);
        Assert.assertEquals(1, fairy.level);

        fairy.feed(60);
        Assert.assertEquals(2, fairy.level);
        Assert.assertTrue(fairy.efficiencyBonus > 5);

        for (int i = 0; i < 20; i++) {
            fairy.interact();
        }
        Assert.assertTrue(fairy.maxBond > 100);
    }

    @Test
    public void testMarketListingDynamicPricing() {
        MarketListing listing = new MarketListing("item_herb", "Spirit Grass", 0, GameConfig.RARITY_COMMON, 100L, 50);
        Assert.assertEquals(100L, listing.price);

        // When stock is depleted, price should increase
        listing.stock = 5;
        listing.demand = 1.5f;
        listing.updatePrice();
        Assert.assertTrue(listing.price > 100L);

        // When stock is abundant and demand is low, price should drop towards floor
        listing.stock = 50;
        listing.demand = 0.5f;
        listing.updatePrice();
        Assert.assertTrue(listing.price <= 100L);
    }

    @Test
    public void testStatusEffectInstantiation() {
        StatusEffect burn = new StatusEffect("Ignite", 2, 3, 25, 0);
        Assert.assertEquals("Ignite", burn.name);
        Assert.assertEquals(2, burn.type);
        Assert.assertEquals(3, burn.duration);
        Assert.assertEquals(25, burn.power);
    }
}
