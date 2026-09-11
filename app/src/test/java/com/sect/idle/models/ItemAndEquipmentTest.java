package com.sect.idle.models;

import com.sect.idle.core.GameConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * ItemAndEquipmentTest - Unit tests for Item, ItemTemplate caching, Equipment stats,
 * and Disciple loadouts.
 */
public class ItemAndEquipmentTest {

    @Test
    public void testEquipmentCreationAndModifiers() {
        Equipment sword = new Equipment("eq_sword_1", "Dragon Slayer Sword", 0, 3);
        sword.calcStats();
        Assert.assertEquals("eq_sword_1", sword.id);
        Assert.assertEquals("Dragon Slayer Sword", sword.name);
        Assert.assertEquals(0, sword.slot);
        Assert.assertEquals(3, sword.rarity);
        Assert.assertTrue(sword.atk > 0);
        Assert.assertTrue(sword.crit > 0);
    }

    @Test
    public void testDiscipleEquipAndStatRecalc() {
        Disciple d = new Disciple("Hero");
        d.initStats(20, 20, 20, 20, 20, 20, 20);
        int baseAtk = d.atk;
        int baseDef = d.def;

        Equipment sword = new Equipment("eq_sword", "Celestial Blade", 0, 2);
        sword.calcStats();
        Equipment armor = new Equipment("eq_armor", "Immortal Robe", 1, 2);
        armor.calcStats();

        d.weapon = sword;
        d.armor = armor;
        d.recalcCombat();

        Assert.assertTrue(d.atk > baseAtk);
        Assert.assertTrue(d.def > baseDef);
    }

    @Test
    public void testItemModelAndStacking() {
        Item item = new Item("spirit_herb", 5);
        Assert.assertEquals("spirit_herb", item.id);
        Assert.assertEquals("Spirit Herb", item.getName());
        Assert.assertEquals(Item.TYPE_MATERIAL, item.getType());
        Assert.assertEquals(5, item.count);

        // Stacking
        int remaining = item.add(10);
        Assert.assertEquals(0, remaining);
        Assert.assertEquals(15, item.count);

        // Removal
        int removed = item.remove(4);
        Assert.assertEquals(4, removed);
        Assert.assertEquals(11, item.count);
    }
}
