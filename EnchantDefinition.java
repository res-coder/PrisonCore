package com.seandoyle.prisoncore.enchant;

import com.seandoyle.prisoncore.model.Progression;
import org.bukkit.Material;
import java.util.List;

public record EnchantDefinition(String id, String display, Material icon, int maxLevel, int requiredPickaxeLevel,
                                long baseCost, double costMultiplier, List<String> description) {
    public long costForCurrentLevel(int currentLevel) { return Progression.upgradeCost(baseCost, costMultiplier, currentLevel); }
}
