package com.seandoyle.prisoncore.gui;

import com.seandoyle.prisoncore.PrisonCorePlugin;
import com.seandoyle.prisoncore.data.PlayerDataService;
import com.seandoyle.prisoncore.enchant.EnchantDefinition;
import com.seandoyle.prisoncore.enchant.EnchantRegistry;
import com.seandoyle.prisoncore.item.PickaxeService;
import com.seandoyle.prisoncore.model.PickaxeData;
import com.seandoyle.prisoncore.util.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EnchantGui {
    public static final class Holder implements InventoryHolder {
        private Inventory inventory;
        @Override public Inventory getInventory() { return inventory; }
        void inventory(Inventory inventory) { this.inventory = inventory; }
    }

    private final Messages messages;
    private final PlayerDataService playerData;
    private final PickaxeService pickaxeService;
    private final EnchantRegistry registry;
    private final NamespacedKey enchantKey;

    public EnchantGui(PrisonCorePlugin plugin, Messages messages, PlayerDataService playerData, PickaxeService pickaxeService, EnchantRegistry registry) {
        this.messages = messages; this.playerData = playerData; this.pickaxeService = pickaxeService; this.registry = registry;
        this.enchantKey = new NamespacedKey(plugin, "gui_enchant");
    }

    public void open(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        PickaxeData data = pickaxeService.read(held);
        if (data == null) { messages.send(player, "invalid-pickaxe"); return; }
        Holder holder = new Holder();
        Inventory inv = Bukkit.createInventory(holder, 45, messages.component("<gradient:#00e5ff:#ff2da4><bold>Pickaxe Enchants</bold></gradient>"));
        holder.inventory(inv);
        fill(inv);
        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        int i=0;
        for (EnchantDefinition def : registry.all()) {
            if (i >= slots.length) break;
            int current = data.enchantLevel(def.id());
            long cost = def.costForCurrentLevel(current);
            ItemStack icon = new ItemStack(def.icon()); ItemMeta meta = icon.getItemMeta();
            meta.customName(messages.component(def.display()));
            List<Component> lore = new ArrayList<>();
            lore.add(messages.component("<gray>Level: <white>" + current + "/" + def.maxLevel()));
            lore.add(messages.component("<gray>Required Pickaxe Level: <aqua>" + def.requiredPickaxeLevel()));
            for (String line : def.description()) lore.add(messages.component(line));
            lore.add(Component.empty());
            lore.add(messages.component(current >= def.maxLevel() ? "<green><bold>MAX LEVEL" : "<gray>Upgrade Cost: <light_purple>" + cost + " tokens"));
            lore.add(messages.component("<dark_gray>Your tokens: " + playerData.tokens(player.getUniqueId())));
            if (current < def.maxLevel()) lore.add(messages.component("<yellow>Click to upgrade"));
            meta.lore(lore); meta.getPersistentDataContainer().set(enchantKey, PersistentDataType.STRING, def.id()); icon.setItemMeta(meta);
            inv.setItem(slots[i++], icon);
        }
        player.openInventory(inv);
    }

    public boolean handleClick(Player player, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) return true;
        String id = clicked.getItemMeta().getPersistentDataContainer().get(enchantKey, PersistentDataType.STRING);
        if (id == null) return true;
        ItemStack pickaxe = player.getInventory().getItemInMainHand(); PickaxeData data = pickaxeService.read(pickaxe); EnchantDefinition def = registry.get(id);
        if (data == null || def == null) { player.closeInventory(); messages.send(player, "invalid-pickaxe"); return true; }
        int current = data.enchantLevel(id);
        if (current >= def.maxLevel()) { messages.send(player, "enchant-maxed"); return true; }
        if (data.level() < def.requiredPickaxeLevel()) { messages.send(player, "enchant-level-required", Map.of("level", def.requiredPickaxeLevel())); return true; }
        long cost = def.costForCurrentLevel(current);
        if (!playerData.takeTokens(player.getUniqueId(), cost)) { messages.send(player, "insufficient-tokens", Map.of("amount", cost)); return true; }
        if (!pickaxeService.upgradeEnchant(pickaxe, player.getUniqueId(), id)) { playerData.addTokens(player.getUniqueId(), cost); messages.raw(player, "<red>Upgrade failed safely; your tokens were refunded."); return true; }
        messages.send(player, "enchant-purchased", Map.of("enchant", strip(def.display()), "level", current+1, "cost", cost));
        open(player); return true;
    }

    private void fill(Inventory inv) { ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE); ItemMeta m=pane.getItemMeta(); m.customName(Component.empty()); pane.setItemMeta(m); for(int i=0;i<inv.getSize();i++) inv.setItem(i,pane); }
    private String strip(String s) { return s.replaceAll("<[^>]+>", ""); }
}
