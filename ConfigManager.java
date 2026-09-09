package com.seandoyle.prisoncore.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration main;
    private FileConfiguration enchants;
    private FileConfiguration shop;

    public ConfigManager(JavaPlugin plugin) { this.plugin = plugin; }

    public synchronized void loadAll() {
        plugin.saveDefaultConfig();
        saveIfMissing("enchants.yml");
        saveIfMissing("shop.yml");
        plugin.reloadConfig();
        main = plugin.getConfig();
        enchants = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "enchants.yml"));
        shop = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "shop.yml"));
        validate();
    }

    private void saveIfMissing(String name) {
        File f = new File(plugin.getDataFolder(), name);
        if (!f.exists()) plugin.saveResource(name, false);
    }

    private void validate() {
        if (main.getDouble("pickaxe.xp-formula.base", 0) <= 0) throw new IllegalStateException("pickaxe.xp-formula.base must be > 0");
        if (main.getDouble("pickaxe.xp-formula.exponent", 0) <= 0) throw new IllegalStateException("pickaxe.xp-formula.exponent must be > 0");
        if (main.getInt("pickaxe.level-cap", 0) < 1) throw new IllegalStateException("pickaxe.level-cap must be >= 1");
        if (enchants.getConfigurationSection("enchants") == null) throw new IllegalStateException("enchants.yml has no enchants section");
    }

    public FileConfiguration main() { return main; }
    public FileConfiguration enchants() { return enchants; }
    public FileConfiguration shop() { return shop; }
    public JavaPlugin plugin() { return plugin; }

    public boolean worldEnabled(String world) {
        List<String> worlds = main.getStringList("settings.enabled-worlds");
        return worlds.isEmpty() || worlds.stream().anyMatch(w -> w.equalsIgnoreCase(world));
    }
}
