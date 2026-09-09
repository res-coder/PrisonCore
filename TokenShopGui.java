package com.seandoyle.prisoncore.gui;

import com.seandoyle.prisoncore.PrisonCorePlugin;
import com.seandoyle.prisoncore.config.ConfigManager;
import com.seandoyle.prisoncore.data.PlayerDataService;
import com.seandoyle.prisoncore.item.ScrollService;
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

public final class TokenShopGui {
    public static final class Holder implements InventoryHolder { private Inventory inv; @Override public Inventory getInventory(){return inv;} void inventory(Inventory i){inv=i;} }
    private final Messages messages; private final PlayerDataService playerData; private final ScrollService scrollService; private final NamespacedKey shopKey;
    public TokenShopGui(PrisonCorePlugin plugin, ConfigManager configs, Messages messages, PlayerDataService playerData, ScrollService scrollService) {
        this.messages=messages; this.playerData=playerData; this.scrollService=scrollService; this.shopKey=new NamespacedKey(plugin,"shop_item");
    }
    public void open(Player p) {
        Holder h=new Holder(); Inventory inv=Bukkit.createInventory(h,27,messages.component("<gradient:#00e5ff:#ff2da4><bold>Token Shop</bold></gradient>")); h.inventory(inv); fill(inv);
        put(inv,11,"rename",scrollService.create("rename",1,null),p); put(inv,13,"lore",scrollService.create("lore",1,null),p);
        ItemStack info=new ItemStack(Material.NETHER_STAR); ItemMeta m=info.getItemMeta(); m.customName(messages.component("<aqua>Your Balance")); m.lore(List.of(messages.component("<light_purple>"+playerData.tokens(p.getUniqueId())+" tokens"))); info.setItemMeta(m); inv.setItem(15,info);
        p.openInventory(inv);
    }
    private void put(Inventory inv,int slot,String id,ItemStack item,Player p){ItemMeta m=item.getItemMeta(); List<Component> lore=m.lore()==null?new ArrayList<>():new ArrayList<>(m.lore()); lore.add(Component.empty()); lore.add(messages.component("<gray>Cost: <light_purple>"+scrollService.cost(id)+" tokens")); lore.add(messages.component("<yellow>Click to purchase")); m.lore(lore); m.getPersistentDataContainer().set(shopKey,PersistentDataType.STRING,id); item.setItemMeta(m); inv.setItem(slot,item);}
    public boolean handleClick(Player p,ItemStack clicked){if(clicked==null||!clicked.hasItemMeta())return true; String id=clicked.getItemMeta().getPersistentDataContainer().get(shopKey,PersistentDataType.STRING); if(id==null)return true; long cost=scrollService.cost(id); if(!playerData.takeTokens(p.getUniqueId(),cost)){messages.send(p,"insufficient-tokens",Map.of("amount",cost));return true;} ItemStack item=scrollService.create(id,1,null); Map<Integer,ItemStack> overflow=p.getInventory().addItem(item); if(!overflow.isEmpty()){playerData.addTokens(p.getUniqueId(),cost); messages.raw(p,"<red>Your inventory is full. Purchase canceled and refunded."); return true;} messages.raw(p,"<green>Purchased a "+id+" scroll for "+cost+" tokens."); open(p); return true;}
    private void fill(Inventory inv){ItemStack pane=new ItemStack(Material.GRAY_STAINED_GLASS_PANE); ItemMeta m=pane.getItemMeta();m.customName(Component.empty());pane.setItemMeta(m);for(int i=0;i<inv.getSize();i++)inv.setItem(i,pane);}
}
