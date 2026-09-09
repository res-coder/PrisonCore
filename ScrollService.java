package com.seandoyle.prisoncore.item;

import com.seandoyle.prisoncore.config.ConfigManager;
import com.seandoyle.prisoncore.util.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class ScrollService {
    private final ConfigManager configs;
    private final Messages messages;
    private final PickaxeService pickaxeService;
    private final NamespacedKey scrollTypeKey;
    private final NamespacedKey transmogMaterialKey;

    public ScrollService(JavaPlugin plugin, ConfigManager configs, Messages messages, PickaxeService pickaxeService) {
        this.configs = configs;
        this.messages = messages;
        this.pickaxeService = pickaxeService;
        this.scrollTypeKey = new NamespacedKey(plugin, "scroll_type");
        this.transmogMaterialKey = new NamespacedKey(plugin, "transmog_material");
    }

    public ItemStack create(String type, int amount, Material transmogMaterial) {
        String id = type.toLowerCase(Locale.ROOT);
        String base = "scrolls." + id + ".";
        Material material = Material.matchMaterial(configs.shop().getString(base + "material", "PAPER"));
        if (material == null) material = Material.PAPER;
        ItemStack item = new ItemStack(material, Math.max(1, Math.min(64, amount)));
        ItemMeta meta = item.getItemMeta();
        meta.customName(messages.component(configs.shop().getString(base + "display", "<white>" + id + " Scroll")));
        List<Component> lore = new ArrayList<>();
        for (String line : configs.shop().getStringList(base + "lore")) lore.add(messages.component(line));
        if (id.equals("transmog") && transmogMaterial != null) lore.add(messages.component("<gray>Target: <aqua>" + transmogMaterial.name()));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(scrollTypeKey, PersistentDataType.STRING, id);
        if (id.equals("transmog") && transmogMaterial != null) meta.getPersistentDataContainer().set(transmogMaterialKey, PersistentDataType.STRING, transmogMaterial.name());
        item.setItemMeta(meta);
        return item;
    }

    public String type(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(scrollTypeKey, PersistentDataType.STRING);
    }

    public boolean apply(ItemStack scroll, ItemStack pickaxe, UUID viewer) {
        String type = type(scroll);
        if (type == null || !pickaxeService.isPrisonPickaxe(pickaxe)) return false;
        return switch (type) {
            case "rename" -> pickaxeService.addRenameCredit(pickaxe, viewer);
            case "lore" -> pickaxeService.addLoreCredit(pickaxe, viewer);
            case "transmog" -> {
                String raw = scroll.getItemMeta().getPersistentDataContainer().get(transmogMaterialKey, PersistentDataType.STRING);
                Material material = raw == null ? null : Material.matchMaterial(raw);
                yield pickaxeService.transmog(pickaxe, viewer, material);
            }
            default -> false;
        };
    }

    public long cost(String type) { return Math.max(0L, configs.shop().getLong("scrolls." + type.toLowerCase(Locale.ROOT) + ".cost", 0L)); }
}
