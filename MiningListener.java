package com.seandoyle.prisoncore.listener;

import com.seandoyle.prisoncore.PrisonCorePlugin;
import com.seandoyle.prisoncore.config.ConfigManager;
import com.seandoyle.prisoncore.data.PlayerDataService;
import com.seandoyle.prisoncore.item.PickaxeService;
import com.seandoyle.prisoncore.model.PickaxeData;
import com.seandoyle.prisoncore.reward.RewardService;
import com.seandoyle.prisoncore.util.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class MiningListener implements Listener {
    private final ConfigManager configs; private final Messages messages; private final PlayerDataService playerData; private final PickaxeService pickaxeService; private final RewardService rewards;
    public MiningListener(PrisonCorePlugin plugin, ConfigManager configs, Messages messages, PlayerDataService playerData, PickaxeService pickaxeService, RewardService rewards){this.configs=configs;this.messages=messages;this.playerData=playerData;this.pickaxeService=pickaxeService;this.rewards=rewards;}

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onMine(BlockBreakEvent event){
        Player p=event.getPlayer(); if(!configs.worldEnabled(p.getWorld().getName()))return;
        ItemStack item=p.getInventory().getItemInMainHand(); PickaxeData data=pickaxeService.read(item); if(data==null)return;
        long baseTokens=Math.max(0L,configs.main().getLong("settings.tokens-per-block",1)); playerData.addTokens(p.getUniqueId(),baseTokens);
        double baseXp=Math.max(0.0,configs.main().getDouble("pickaxe.base-xp-per-block",1.0));
        double multiplier=1.0+(data.enchantLevel("xp_hoarder")*0.15); int gained=pickaxeService.addXp(item,p.getUniqueId(),baseXp*multiplier);
        if(gained>0){PickaxeData updated=pickaxeService.read(item);messages.send(p,"level-up",Map.of("level",updated.level()));}
        PickaxeData updated=pickaxeService.read(item); rewards.rollKeyFinder(p,updated.enchantLevel("key_finder")); rewards.rollTokenFinder(p,updated.enchantLevel("token_finder"));
        pickaxeService.refresh(item,p.getUniqueId());
    }
}
