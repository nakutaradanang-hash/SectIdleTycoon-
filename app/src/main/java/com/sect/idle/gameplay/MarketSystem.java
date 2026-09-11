package com.sect.idle.gameplay;

import com.sect.idle.models.MarketListing;
import java.util.ArrayList;

/**
 * MarketSystem - Dynamic marketplace with fluctuating commodities.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class MarketSystem {
    public final ArrayList<MarketListing> listings;

    public MarketSystem() {
        this.listings = new ArrayList<MarketListing>();
        initListings();
    }

    private void initListings() {
        listings.add(new MarketListing("item_herb_1", "Low Grade Spirit Grass", 1, 0, 10, 50));
        listings.add(new MarketListing("item_ore_1", "Raw Spirit Iron", 2, 0, 15, 30));
        listings.add(new MarketListing("item_pill_1", "Qi Condensation Pill", 0, 1, 50, 20));
        listings.add(new MarketListing("item_sword_1", "Iron Flying Sword", 4, 1, 200, 5));
    }

    public void tick() {
        for (int i = 0; i < listings.size(); i++) {
            MarketListing l = listings.get(i);
            if (l != null) {
                l.tick();
            }
        }
    }

    public void updatePrices() {
        for (int i = 0; i < listings.size(); i++) {
            MarketListing l = listings.get(i);
            if (l != null) {
                l.updatePrice();
            }
        }
    }
}
