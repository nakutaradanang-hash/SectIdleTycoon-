package com.sect.idle.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.sect.idle.core.GameConfig;
import java.util.HashMap;
import java.util.Map;

/**
 * Item System v2.0 - Optimized data-driven item representation.
 */
public class Item {
    
    // ========== ITEM TYPE CONST ==========
    public static final int TYPE_CONSUMABLE = 0;
    public static final int TYPE_MATERIAL = 1;
    public static final int TYPE_QUEST = 2;
    public static final int TYPE_SECRET = 3;
    public static final int TYPE_EQUIPMENT = 4;
    public static final int TYPE_CRYSTAL = 5;

    // ========== INSTANCE DATA ==========
    public final String id;
    public int count;
    public int maxStack = 999;
    private int durability;
    
    // ========== TEMPLATE DATA ==========
    private static final Map<String, ItemTemplate> TEMPLATE_CACHE = new HashMap<>(64);
    static {
        register(new ItemTemplate("spirit_herb", "Spirit Herb", "Used in alchemy", TYPE_MATERIAL, 0, 99, 50, 0, 0, true, "icon_herb"));
        register(new ItemTemplate("iron_ore", "Iron Ore", "Used for forging", TYPE_MATERIAL, 0, 99, 30, 0, 0, true, "icon_ore"));
        register(new ItemTemplate("mystic_wood", "Mystic Wood", "Rare crafting material", TYPE_MATERIAL, 1, 50, 100, 0, 0, true, "icon_wood"));
        register(new ItemTemplate("heaven_pill", "Heaven Pill", "Restores 500 HP instantly", TYPE_CONSUMABLE, 2, 10, 2000, 1, 500, true, "icon_pill"));
        register(new ItemTemplate("dragon_scale", "Dragon Scale", "Armor +50", TYPE_EQUIPMENT, 3, 1, 5000, 2, 50, false, "icon_scale"));
        register(new ItemTemplate("ancient_scroll", "Ancient Scroll", "Quest item", TYPE_QUEST, 2, 1, 0, 0, 0, false, "icon_scroll"));
        register(new ItemTemplate("void_essence", "Void Essence", "Secret cultivation material", TYPE_SECRET, 4, 5, 10000, 0, 0, true, "icon_essence"));
        register(new ItemTemplate("fire_crystal", "Fire Crystal", "+10% Fire Dmg", TYPE_CRYSTAL, 1, 20, 200, 3, 10, true, "icon_crystal"));
    }

    private static void register(ItemTemplate t) {
        TEMPLATE_CACHE.put(t.id, t);
    }

    // ========== CONSTRUCTORS ==========
    public Item(@NonNull String id) {
        this(id, 1);
    }

    public Item(@NonNull String id, int count) {
        this.id = id;
        this.count = Math.max(1, count);
        ItemTemplate t = getTemplate();
        this.durability = (t.type == TYPE_EQUIPMENT) ? t.effectPower : -1;
    }

    public Item(@NonNull String id, @NonNull String name, int type, int rarity, int maxStack, int value) {
        this.id = id;
        this.count = 1;
        this.durability = -1;
        if (!TEMPLATE_CACHE.containsKey(id)) {
            register(new ItemTemplate(id, name, "", type, rarity, maxStack, value, 0, 0, true, "icon_default"));
        }
    }

    // ========== GETTERS ==========
    @NonNull
    public ItemTemplate getTemplate() {
        ItemTemplate t = TEMPLATE_CACHE.get(id);
        if (t == null) return TEMPLATE_CACHE.get("spirit_herb");
        return t;
    }

    @NonNull public String getName() { return getTemplate().name; }
    @NonNull public String getDesc() { return getTemplate().desc; }
    public int getType() { return getTemplate().type; }
    public int getRarity() { return getTemplate().rarity; }
    public int getMaxStack() { return getTemplate().maxStack; }
    public int getValue() { return getTemplate().baseValue; }
    public int getEffectType() { return getTemplate().effectType; }
    public int getEffectPower() { return getTemplate().effectPower; }
    public boolean isUsable() { return getTemplate().usable; }
    @NonNull public String getIconId() { return getTemplate().iconId; }

    public int getColor() {
        int r = getRarity();
        return GameConfig.RARITY_COLORS[Math.min(r, GameConfig.RARITY_COLORS.length - 1)];
    }

    // ========== LOGIC ==========
    public boolean canStackWith(@Nullable Item other) {
        return other != null && this.id.equals(other.id) && getMaxStack() > 1;
    }

    public int add(int amount) {
        int space = getMaxStack() - count;
        int toAdd = Math.min(space, amount);
        count += toAdd;
        return amount - toAdd;
    }

    public int remove(int amount) {
        int toRemove = Math.min(count, amount);
        count -= toRemove;
        return toRemove;
    }

    public boolean isFullStack() { return count >= getMaxStack(); }
    public boolean isEmpty() { return count <= 0; }
    public boolean isEquipment() { return getType() == TYPE_EQUIPMENT; }

    public int getDurability() { return durability; }
    public void damage(int amount) { if (durability > 0) durability = Math.max(0, durability - amount); }
    public boolean isBroken() { return isEquipment() && durability <= 0; }
    public float getDurabilityPercent() { 
        if (!isEquipment() || durability < 0) return 1f;
        return (float)durability / getTemplate().effectPower; 
    }

    @NonNull
    @Override
    public String toString() {
        return getName() + " x" + count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return id.equals(item.id) && count == item.count;
    }

    @Override
    public int hashCode() {
        return id.hashCode() * 31 + count;
    }

    public static final class ItemTemplate {
        public final String id;
        public final String name;
        public final String desc;
        public final int type;
        public final int rarity;
        public final int maxStack;
        public final int baseValue;
        public final int effectType;
        public final int effectPower;
        public final boolean usable;
        public final String iconId;

        ItemTemplate(String id, String name, String desc, int type, int rarity, int maxStack, int baseValue, int effectType, int effectPower, boolean usable, String iconId) {
            this.id = id; this.name = name; this.desc = desc; this.type = type;
            this.rarity = rarity; this.maxStack = maxStack; this.baseValue = baseValue;
            this.effectType = effectType; this.effectPower = effectPower; this.usable = usable; this.iconId = iconId;
        }
    }
}
