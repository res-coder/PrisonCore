package com.seandoyle.prisoncore.data;

import com.seandoyle.prisoncore.config.ConfigManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataService {
    private final JavaPlugin plugin;
    private final ConfigManager configs;
    private final File file;
    private final ConcurrentHashMap<UUID, Long> tokens = new ConcurrentHashMap<>();

    public PlayerDataService(JavaPlugin plugin, ConfigManager configs) {
        this.plugin = plugin;
        this.configs = configs;
        this.file = new File(plugin.getDataFolder(), "player-data.yml");
    }

    public void load() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        var section = yaml.getConfigurationSection("players");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            try { tokens.put(UUID.fromString(key), Math.max(0L, section.getLong(key + ".tokens", 0L))); }
            catch (IllegalArgumentException ignored) { plugin.getLogger().warning("Ignoring invalid player UUID in player-data.yml: " + key); }
        }
    }

    public long tokens(UUID uuid) { return tokens.getOrDefault(uuid, 0L); }
    public void setTokens(UUID uuid, long amount) { tokens.put(uuid, Math.max(0L, amount)); }
    public void addTokens(UUID uuid, long amount) {
        if (amount == 0) return;
        tokens.compute(uuid, (k, old) -> Math.max(0L, safeAdd(old == null ? 0L : old, amount)));
    }
    public boolean takeTokens(UUID uuid, long amount) {
        if (amount < 0) return false;
        synchronized (tokens) {
            long current = tokens(uuid);
            if (current < amount) return false;
            tokens.put(uuid, current - amount);
            return true;
        }
    }

    private long safeAdd(long a, long b) {
        try { return Math.addExact(a, b); }
        catch (ArithmeticException ex) { return b > 0 ? Long.MAX_VALUE : 0L; }
    }

    public void saveSnapshotSafe() {
        try { saveSnapshot(new HashMap<>(tokens)); }
        catch (Exception ex) { plugin.getLogger().severe("Failed async player data save: " + ex.getMessage()); }
    }

    public void saveNow() {
        try { saveSnapshot(new HashMap<>(tokens)); }
        catch (Exception ex) { plugin.getLogger().severe("Failed final player data save: " + ex.getMessage()); }
    }

    private void saveSnapshot(Map<UUID, Long> snapshot) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        for (var e : snapshot.entrySet()) yaml.set("players." + e.getKey() + ".tokens", e.getValue());
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) throw new IOException("Could not create plugin data folder");
        yaml.save(file);
    }
}
