package com.seandoyle.prisoncore.item;

import com.seandoyle.prisoncore.config.ConfigManager;
import com.seandoyle.prisoncore.data.PlayerDataService;
import com.seandoyle.prisoncore.enchant.EnchantDefinition;
import com.seandoyle.prisoncore.enchant.EnchantRegistry;
import com.seandoyle.prisoncore.model.PickaxeData;
import com.seandoyle.prisoncore.model.Progression;
import com.seandoyle.prisoncore.util.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class PickaxeService {
    private final ConfigManager configs;
    private final Messages messages;
    private final PlayerDataService playerData;
    private final EnchantRegistry registry;
    private final NamespacedKey typeKey, idKey, schemaKey, levelKey, xpKey, enchantsKey, renameCreditsKey, loreCreditsKey;

    public PickaxeService(JavaPlugin plugin, ConfigManager configs, Messages messages, PlayerDataService playerData, EnchantRegistry registry) {
        this.configs = configs; this.messages = messages; this.playerData = playerData; this.registry = registry;
        typeKey = new NamespacedKey(plugin, "item_type"); idKey = new NamespacedKey(plugin, "item_id"); schemaKey = new NamespacedKey(plugin, "schema");
        levelKey = new NamespacedKey(plugin, "level"); xpKey = new NamespacedKey(plugin, "xp"); enchantsKey = new NamespacedKey(plugin, "enchants");
        renameCreditsKey = new NamespacedKey(plugin, "rename_credits"); loreCreditsKey = new NamespacedKey(plugin, "lore_credits");
    }

    public ItemStack createStarter(UUID owner) {
        Material mat = Material.matchMaterial(configs.main().getString("pickaxe.material", "DIAMOND_PICKAXE"));
        if (mat == null || !mat.name().endsWith("_PICKAXE")) mat = Material.DIAMOND_PICKAXE;
        ItemStack item = new ItemStack(mat);
        write(item, PickaxeData.fresh(), owner);
        return item;
    }

    public boolean isPrisonPickaxe(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        return "pickaxe".equals(item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING));
    }

    public PickaxeData read(ItemStack item) {
        if (!isPrisonPickaxe(item)) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        UUID id;
        try { id = UUID.fromString(Objects.requireNonNullElse(pdc.get(idKey, PersistentDataType.STRING), UUID.randomUUID().toString())); }
        catch (IllegalArgumentException ex) { id = UUID.randomUUID(); }
        int schema = Objects.requireNonNullElse(pdc.get(schemaKey, PersistentDataType.INTEGER), 1);
        int level = Objects.requireNonNullElse(pdc.get(levelKey, PersistentDataType.INTEGER), 1);
        double xp = Objects.requireNonNullElse(pdc.get(xpKey, PersistentDataType.DOUBLE), 0.0);
        int rename = Objects.requireNonNullElse(pdc.get(renameCreditsKey, PersistentDataType.INTEGER), 0);
        int lore = Objects.requireNonNullElse(pdc.get(loreCreditsKey, PersistentDataType.INTEGER), 0);
        return new PickaxeData(id, schema, level, xp, rename, lore, decodeEnchants(pdc.get(enchantsKey, PersistentDataType.STRING)));
    }

    public void write(ItemStack item, PickaxeData data, UUID viewer) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(typeKey, PersistentDataType.STRING, "pickaxe"); pdc.set(idKey, PersistentDataType.STRING, data.itemId().toString());
        pdc.set(schemaKey, PersistentDataType.INTEGER, PickaxeData.CURRENT_SCHEMA); pdc.set(levelKey, PersistentDataType.INTEGER, data.level());
        pdc.set(xpKey, PersistentDataType.DOUBLE, data.xp()); pdc.set(enchantsKey, PersistentDataType.STRING, encodeEnchants(data.enchants()));
        pdc.set(renameCreditsKey, PersistentDataType.INTEGER, data.renameCredits()); pdc.set(loreCreditsKey, PersistentDataType.INTEGER, data.loreCredits());
        meta.setUnbreakable(true); meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        applyVanillaEnchants(meta, data);
        if (!meta.hasCustomName()) meta.customName(messages.component(configs.main().getString("pickaxe.name", "<aqua>Prison Pickaxe")));
        meta.lore(buildLore(data, viewer));
        item.setItemMeta(meta);
    }

    private void applyVanillaEnchants(ItemMeta meta, PickaxeData data) {
        int eff = data.enchantLevel("efficiency"); int fortune = data.enchantLevel("fortune");
        if (eff > 0) meta.addEnchant(Enchantment.EFFICIENCY, eff, true); else meta.removeEnchant(Enchantment.EFFICIENCY);
        if (fortune > 0) meta.addEnchant(Enchantment.FORTUNE, fortune, true); else meta.removeEnchant(Enchantment.FORTUNE);
    }

    private List<Component> buildLore(PickaxeData data, UUID viewer) {
        List<Component> result = new ArrayList<>();
        double required = xpRequired(data.level());
        long percent = required <= 0 ? 100 : Math.min(100, Math.round((data.xp() / required) * 100));
        List<String> enchantLines = new ArrayList<>();
        for (EnchantDefinition def : registry.all()) {
            int lvl = data.enchantLevel(def.id());
            if (lvl > 0) enchantLines.add(def.display() + " <gray>" + lvl + "/" + def.maxLevel());
        }
        if (enchantLines.isEmpty()) enchantLines.add("<dark_gray>None yet");
        for (String line : configs.main().getStringList("pickaxe.lore")) {
            if (line.equals("<enchants>")) { for (String ench : enchantLines) result.add(messages.component(ench)); continue; }
            String rendered = line.replace("<level>", Integer.toString(data.level()))
                    .replace("<xp>", String.format(Locale.US, "%.0f", data.xp()))
                    .replace("<xp_required>", String.format(Locale.US, "%.0f", required))
                    .replace("<progress>", Long.toString(percent))
                    .replace("<tokens>", Long.toString(playerData.tokens(viewer)));
            result.add(messages.component(rendered));
        }
        return result;
    }

    public int addXp(ItemStack item, UUID viewer, double amount) {
        PickaxeData data = read(item); if (data == null || amount <= 0) return 0;
        int oldLevel = data.level(); int cap = configs.main().getInt("pickaxe.level-cap", 1000);
        data.xp(data.xp() + amount);
        while (data.level() < cap) {
            double needed = xpRequired(data.level());
            if (data.xp() < needed) break;
            data.xp(data.xp() - needed); data.level(data.level() + 1);
        }
        if (data.level() >= cap) data.xp(Math.min(data.xp(), xpRequired(cap)));
        write(item, data, viewer);
        return data.level() - oldLevel;
    }

    public double xpRequired(int level) { return Progression.xpRequired(level, configs.main().getDouble("pickaxe.xp-formula.base", 100), configs.main().getDouble("pickaxe.xp-formula.exponent", 1.35)); }

    public boolean upgradeEnchant(ItemStack item, UUID viewer, String id) {
        PickaxeData data = read(item); EnchantDefinition def = registry.get(id); if (data == null || def == null) return false;
        int current = data.enchantLevel(id);
        if (current >= def.maxLevel()) return false;
        data.enchantLevel(id, current + 1); write(item, data, viewer); return true;
    }

    public void refresh(ItemStack item, UUID viewer) { PickaxeData data = read(item); if (data != null) write(item, data, viewer); }

    public boolean addRenameCredit(ItemStack item, UUID viewer) { PickaxeData d = read(item); if (d == null) return false; d.renameCredits(d.renameCredits()+1); write(item,d,viewer); return true; }
    public boolean addLoreCredit(ItemStack item, UUID viewer) { PickaxeData d = read(item); if (d == null) return false; d.loreCredits(d.loreCredits()+1); write(item,d,viewer); return true; }
    public boolean consumeRename(ItemStack item, UUID viewer, Component name) { PickaxeData d=read(item); if(d==null||d.renameCredits()<1)return false; d.renameCredits(d.renameCredits()-1); ItemMeta m=item.getItemMeta(); m.customName(name); item.setItemMeta(m); write(item,d,viewer); return true; }
    public boolean consumeLore(ItemStack item, UUID viewer, Component line) { PickaxeData d=read(item); if(d==null||d.loreCredits()<1)return false; d.loreCredits(d.loreCredits()-1); write(item,d,viewer); ItemMeta m=item.getItemMeta(); List<Component> lore=m.lore()==null?new ArrayList<>():new ArrayList<>(m.lore()); lore.add(Math.max(0,lore.size()-1), line); m.lore(lore); item.setItemMeta(m); return true; }
    public boolean transmog(ItemStack item, UUID viewer, Material material) { if(!isPrisonPickaxe(item)||material==null||!material.name().endsWith("_PICKAXE"))return false; PickaxeData d=read(item); item.setType(material); write(item,d,viewer); return true; }

    private Map<String,Integer> decodeEnchants(String raw) {
        Map<String,Integer> out=new HashMap<>(); if(raw==null||raw.isBlank())return out;
        for(String token:raw.split(";")){String[] p=token.split("=",2); if(p.length!=2)continue; try{int v=Integer.parseInt(p[1]); if(v>0)out.put(p[0].toLowerCase(Locale.ROOT),v);}catch(NumberFormatException ignored){}}
        return out;
    }
    private String encodeEnchants(Map<String,Integer> enchants) { return enchants.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e->e.getKey()+"="+e.getValue()).reduce((a,b)->a+";"+b).orElse(""); }
}
