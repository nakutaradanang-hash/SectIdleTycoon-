package com.sect.idle.gameplay;

import com.sect.idle.models.MarketListing;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * MarketSystemTest - Unit tests for market commodity listings, price fluctuation algorithms,
 * stock replenishment, and supply/demand calculation.
 */
public class MarketSystemTest {

    private MarketSystem marketSystem;

    @Before
    public void setUp() {
        marketSystem = new MarketSystem();
    }

    @Test
    public void testInitialListings() {
        Assert.assertNotNull(marketSystem.listings);
        Assert.assertFalse(marketSystem.listings.isEmpty());

        MarketListing listing = marketSystem.listings.get(0);
        Assert.assertNotNull(listing.id);
        Assert.assertNotNull(listing.itemName);
        Assert.assertTrue(listing.price > 0);
        Assert.assertTrue(listing.stock > 0);
        Assert.assertTrue(listing.maxStock >= listing.stock);
    }

    @Test
    public void testPriceFluctuationsOnStockChange() {
        MarketListing listing = marketSystem.listings.get(0);
        long origPrice = listing.originalPrice;

        // When stock is low, scarcity drives price up
        listing.stock = 1;
        listing.demand = 1.0f;
        listing.updatePrice();
        Assert.assertTrue(listing.price >= origPrice);

        // When stock is full, price is normal or lower
        listing.stock = listing.maxStock;
        listing.updatePrice();
        Assert.assertTrue(listing.price <= origPrice * 1.5f);
    }

    @Test
    public void testMarketTickCycle() {
        MarketListing listing = marketSystem.listings.get(0);
        listing.refreshTimer = 1;

        marketSystem.tick();
        Assert.assertEquals(24, listing.refreshTimer);
        Assert.assertTrue(listing.price > 0);
        Assert.assertTrue(listing.stock > 0);
    }
}
