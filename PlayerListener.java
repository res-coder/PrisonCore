package com.seandoyle.prisoncore.listener;

import com.seandoyle.prisoncore.config.ConfigManager;
import com.seandoyle.prisoncore.item.PickaxeService;
import com.seandoyle.prisoncore.util.Messages;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public final class PlayerListener implements Listener {
    private final ConfigManager configs; private final Messages messages; private final PickaxeService pickaxeService;
    public PlayerListener(ConfigManager configs,Messages messages,PickaxeService pickaxeService){this.configs=configs;this.messages=messages;this.pickaxeService=pickaxeService;}
    @EventHandler(priority=EventPriority.MONITOR) public void onJoin(PlayerJoinEvent e){
        if(!configs.main().getBoolean("settings.starter-pickaxe-on-first-join",true))return; Player p=e.getPlayer();
        boolean has=false; for(ItemStack item:p.getInventory().getContents())if(pickaxeService.isPrisonPickaxe(item)){has=true;break;}
        if(!has && !p.hasPlayedBefore()){p.getInventory().addItem(pickaxeService.createStarter(p.getUniqueId()));messages.raw(p,"<green>You received your Prison Pickaxe.");}
    }
    @EventHandler(ignoreCancelled=true) public void onDrop(PlayerDropItemEvent e){if(configs.main().getBoolean("settings.prevent-prison-pickaxe-drop",true)&&pickaxeService.isPrisonPickaxe(e.getItemDrop().getItemStack())){e.setCancelled(true);messages.raw(e.getPlayer(),"<red>Your Prison Pickaxe is protected from dropping.");}}
    @EventHandler public void onDespawn(ItemDespawnEvent e){if(configs.main().getBoolean("settings.prevent-prison-pickaxe-despawn",true)&&pickaxeService.isPrisonPickaxe(e.getEntity().getItemStack()))e.setCancelled(true);}
}
