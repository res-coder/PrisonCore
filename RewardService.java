package com.seandoyle.prisoncore.reward;

import com.seandoyle.prisoncore.config.ConfigManager;
import com.seandoyle.prisoncore.data.PlayerDataService;
import com.seandoyle.prisoncore.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class RewardService {
    private final JavaPlugin plugin;
    private final ConfigManager configs;
    private final Messages messages;
    private final PlayerDataService playerData;
    private final Map<String, ArrayDeque<Long>> procWindows = new ConcurrentHashMap<>();

    public RewardService(JavaPlugin plugin, ConfigManager configs, Messages messages, PlayerDataService playerData) {
        this.plugin = plugin; this.configs = configs; this.messages = messages; this.playerData = playerData;
    }

    public void rollKeyFinder(Player player, int level) {
        if (level <= 0) return;
        double chance = clampProbability(configs.main().getDouble("rewards.key-finder.chance-per-level", 0.00025) * level);
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;
        int cap = Math.max(0, configs.main().getInt("rewards.key-finder.max-procs-per-minute", 8));
        if (!allow(player.getUniqueId(), "key", cap)) return;
        for (String template : configs.main().getStringList("rewards.key-finder.commands")) {
            String command = template.replace("<player>", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        player.sendMessage(messages.component(configs.main().getString("rewards.key-finder.message", "<light_purple>You found a key!")));
    }

    public void rollTokenFinder(Player player, int level) {
        if (level <= 0) return;
        double chance = clampProbability(configs.main().getDouble("rewards.token-finder.chance-per-level", 0.001) * level);
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;
        int cap = Math.max(0, configs.main().getInt("rewards.token-finder.max-procs-per-minute", 20));
        if (!allow(player.getUniqueId(), "tokens", cap)) return;
        long amount = Math.max(0L, configs.main().getLong("rewards.token-finder.tokens-per-proc", 25));
        playerData.addTokens(player.getUniqueId(), amount);
        String raw = configs.main().getString("rewards.token-finder.message", "<yellow>+<amount> tokens").replace("<amount>", Long.toString(amount));
        player.sendMessage(messages.component(raw));
    }

    private boolean allow(UUID uuid, String reward, int maxPerMinute) {
        if (maxPerMinute <= 0) return false;
        long now = Instant.now().toEpochMilli(); long cutoff = now - 60_000L;
        String key = uuid + ":" + reward;
        ArrayDeque<Long> q = procWindows.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && q.peekFirst() < cutoff) q.removeFirst();
            if (q.size() >= maxPerMinute) return false;
            q.addLast(now); return true;
        }
    }
    private double clampProbability(double value) { return Math.max(0.0, Math.min(1.0, value)); }
}
