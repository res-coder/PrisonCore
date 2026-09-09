package com.seandoyle.prisoncore.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PickaxeData {
    public static final int CURRENT_SCHEMA = 2;
    private final UUID itemId;
    private int schema;
    private int level;
    private double xp;
    private int renameCredits;
    private int loreCredits;
    private final Map<String, Integer> enchants;

    public PickaxeData(UUID itemId, int schema, int level, double xp, int renameCredits, int loreCredits, Map<String, Integer> enchants) {
        this.itemId = itemId;
        this.schema = schema;
        this.level = Math.max(1, level);
        this.xp = Math.max(0, xp);
        this.renameCredits = Math.max(0, renameCredits);
        this.loreCredits = Math.max(0, loreCredits);
        this.enchants = new HashMap<>(enchants);
    }
    public static PickaxeData fresh() { return new PickaxeData(UUID.randomUUID(), CURRENT_SCHEMA, 1, 0, 0, 0, Map.of()); }
    public UUID itemId() { return itemId; }
    public int schema() { return schema; }
    public void schema(int schema) { this.schema = schema; }
    public int level() { return level; }
    public void level(int value) { level = Math.max(1, value); }
    public double xp() { return xp; }
    public void xp(double value) { xp = Math.max(0, value); }
    public int renameCredits() { return renameCredits; }
    public void renameCredits(int value) { renameCredits = Math.max(0, value); }
    public int loreCredits() { return loreCredits; }
    public void loreCredits(int value) { loreCredits = Math.max(0, value); }
    public int enchantLevel(String id) { return enchants.getOrDefault(id.toLowerCase(), 0); }
    public void enchantLevel(String id, int level) { if (level <= 0) enchants.remove(id.toLowerCase()); else enchants.put(id.toLowerCase(), level); }
    public Map<String,Integer> enchants() { return Collections.unmodifiableMap(enchants); }
}
