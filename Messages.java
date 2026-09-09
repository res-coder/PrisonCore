package com.seandoyle.prisoncore.util;

import com.seandoyle.prisoncore.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class Messages {
    private final ConfigManager configs;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private String prefix;

    public Messages(ConfigManager configs) { this.configs = configs; reload(); }
    public void reload() { prefix = configs.main().getString("messages.prefix", "<gray>[PrisonCore] "); }

    public Component component(String raw) { return mm.deserialize(raw == null ? "" : raw); }
    public Component format(String raw, Map<String, ?> vars) {
        String value = raw == null ? "" : raw;
        for (var e : vars.entrySet()) value = value.replace("<" + e.getKey() + ">", String.valueOf(e.getValue()));
        return component(value);
    }
    public void send(CommandSender sender, String path) { send(sender, path, Map.of()); }
    public void send(CommandSender sender, String path, Map<String, ?> vars) {
        String raw = configs.main().getString("messages." + path, "<red>Missing message: " + path);
        sender.sendMessage(component(prefix).append(format(raw, vars)));
    }
    public void raw(CommandSender sender, String raw) { sender.sendMessage(component(prefix).append(component(raw))); }
}
