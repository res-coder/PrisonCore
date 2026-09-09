package com.seandoyle.prisoncore;

import com.seandoyle.prisoncore.command.PickaxeCommand;
import com.seandoyle.prisoncore.command.PrisonCoreCommand;
import com.seandoyle.prisoncore.command.TokensCommand;
import com.seandoyle.prisoncore.config.ConfigManager;
import com.seandoyle.prisoncore.data.PlayerDataService;
import com.seandoyle.prisoncore.enchant.EnchantRegistry;
import com.seandoyle.prisoncore.gui.EnchantGui;
import com.seandoyle.prisoncore.gui.TokenShopGui;
import com.seandoyle.prisoncore.item.PickaxeService;
import com.seandoyle.prisoncore.item.ScrollService;
import com.seandoyle.prisoncore.listener.InventoryListener;
import com.seandoyle.prisoncore.listener.MiningListener;
import com.seandoyle.prisoncore.listener.PlayerListener;
import com.seandoyle.prisoncore.reward.RewardService;
import com.seandoyle.prisoncore.util.Messages;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrisonCorePlugin extends JavaPlugin {
    private ConfigManager configs;
    private Messages messages;
    private PlayerDataService playerData;
    private EnchantRegistry enchantRegistry;
    private PickaxeService pickaxeService;
    private ScrollService scrollService;
    private RewardService rewardService;
    private EnchantGui enchantGui;
    private TokenShopGui tokenShopGui;

    @Override
    public void onEnable() {
        try {
            this.configs = new ConfigManager(this);
            configs.loadAll();
            this.messages = new Messages(configs);
            this.playerData = new PlayerDataService(this, configs);
            playerData.load();
            this.enchantRegistry = new EnchantRegistry(configs);
            this.pickaxeService = new PickaxeService(this, configs, messages, playerData, enchantRegistry);
            this.scrollService = new ScrollService(this, configs, messages, pickaxeService);
            this.rewardService = new RewardService(this, configs, messages, playerData);
            this.enchantGui = new EnchantGui(this, messages, playerData, pickaxeService, enchantRegistry);
            this.tokenShopGui = new TokenShopGui(this, configs, messages, playerData, scrollService);

            registerCommands();
            getServer().getPluginManager().registerEvents(new MiningListener(this, configs, messages, playerData, pickaxeService, rewardService), this);
            getServer().getPluginManager().registerEvents(new InventoryListener(messages, pickaxeService, scrollService, enchantGui, tokenShopGui), this);
            getServer().getPluginManager().registerEvents(new PlayerListener(configs, messages, pickaxeService), this);

            long autosaveTicks = Math.max(20L, configs.main().getLong("settings.autosave-seconds", 60L) * 20L);
            getServer().getScheduler().runTaskTimerAsynchronously(this, playerData::saveSnapshotSafe, autosaveTicks, autosaveTicks);
            getLogger().info("PrisonCore v" + getPluginMeta().getVersion() + " enabled successfully.");
        } catch (Exception ex) {
            getLogger().severe("PrisonCore failed to enable safely: " + ex.getMessage());
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void registerCommands() {
        PrisonCoreCommand pc = new PrisonCoreCommand(this, configs, messages, playerData, pickaxeService, scrollService);
        bind("prisoncore", pc);
        bind("pickaxe", new PickaxeCommand(messages, pickaxeService, enchantGui));
        bind("tokens", new TokensCommand(messages, playerData, tokenShopGui));
    }

    private void bind(String name, org.bukkit.command.TabExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) throw new IllegalStateException("Command missing from plugin.yml: " + name);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    public void reloadPrisonCore() {
        configs.loadAll();
        messages.reload();
        enchantRegistry.reload();
    }

    @Override
    public void onDisable() {
        if (playerData != null) playerData.saveNow();
    }
}
