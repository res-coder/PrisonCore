package com.seandoyle.prisoncore.enchant;

import com.seandoyle.prisoncore.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import java.util.*;

public final class EnchantRegistry {
    private final ConfigManager configs;
    private final LinkedHashMap<String, EnchantDefinition> definitions = new LinkedHashMap<>();
    public EnchantRegistry(ConfigManager configs) { this.configs = configs; reload(); }

    public void reload() {
        definitions.clear();
        ConfigurationSection root = configs.enchants().getConfigurationSection("enchants");
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            String base = "enchants." + id + ".";
            Material icon = Material.matchMaterial(configs.enchants().getString(base + "icon", "BOOK"));
            if (icon == null) icon = Material.BOOK;
            definitions.put(id.toLowerCase(Locale.ROOT), new EnchantDefinition(
                    id.toLowerCase(Locale.ROOT), configs.enchants().getString(base + "display", id), icon,
                    Math.max(1, configs.enchants().getInt(base + "max-level", 1)),
                    Math.max(1, configs.enchants().getInt(base + "required-pickaxe-level", 1)),
                    Math.max(0L, configs.enchants().getLong(base + "base-cost", 0L)),
                    Math.max(1.0, configs.enchants().getDouble(base + "cost-multiplier", 1.0)),
                    List.copyOf(configs.enchants().getStringList(base + "description"))));
        }
    }
    public EnchantDefinition get(String id) { return id == null ? null : definitions.get(id.toLowerCase(Locale.ROOT)); }
    public Collection<EnchantDefinition> all() { return Collections.unmodifiableCollection(definitions.values()); }
}
