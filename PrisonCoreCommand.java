package com.seandoyle.prisoncore.command;

import com.seandoyle.prisoncore.PrisonCorePlugin;
import com.seandoyle.prisoncore.config.ConfigManager;
import com.seandoyle.prisoncore.data.PlayerDataService;
import com.seandoyle.prisoncore.item.PickaxeService;
import com.seandoyle.prisoncore.item.ScrollService;
import com.seandoyle.prisoncore.model.PickaxeData;
import com.seandoyle.prisoncore.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PrisonCoreCommand implements TabExecutor {
    private final PrisonCorePlugin plugin;
    private final ConfigManager configs;
    private final Messages messages;
    private final PlayerDataService data;
    private final PickaxeService pickaxes;
    private final ScrollService scrolls;

    public PrisonCoreCommand(PrisonCorePlugin plugin, ConfigManager configs, Messages messages, PlayerDataService data,
                             PickaxeService pickaxes, ScrollService scrolls) {
        this.plugin=plugin; this.configs=configs; this.messages=messages; this.data=data; this.pickaxes=pickaxes; this.scrolls=scrolls;
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("prisoncore.admin")) { messages.send(sender,"no-permission"); return true; }
        if (args.length==0 || args[0].equalsIgnoreCase("help")) { help(sender); return true; }
        String sub=args[0].toLowerCase(Locale.ROOT);
        switch(sub) {
            case "reload" -> { if(!sender.hasPermission("prisoncore.admin.reload")){messages.send(sender,"no-permission");return true;} plugin.reloadPrisonCore(); messages.send(sender,"reload"); }
            case "givepickaxe" -> givePickaxe(sender,args);
            case "givescroll" -> giveScroll(sender,args);
            case "tokens" -> tokens(sender,args);
            case "setlevel" -> setLevel(sender,args);
            case "addxp" -> addXp(sender,args);
            case "inspect" -> inspect(sender,args);
            case "repair" -> repair(sender,args);
            default -> help(sender);
        }
        return true;
    }

    private void givePickaxe(CommandSender sender,String[] args){
        if(!sender.hasPermission("prisoncore.admin.give")){messages.send(sender,"no-permission");return;}
        Player p=target(args,1,sender);if(p==null)return;
        Map<Integer,ItemStack> left=p.getInventory().addItem(pickaxes.createStarter(p.getUniqueId()));
        if(!left.isEmpty()){messages.raw(sender,"<red>Target inventory is full; pickaxe not given.");return;}
        messages.raw(sender,"<green>Gave a Prison Pickaxe to "+p.getName()+".");
    }

    private void giveScroll(CommandSender sender,String[] args){
        if(!sender.hasPermission("prisoncore.admin.give")){messages.send(sender,"no-permission");return;}
        if(args.length<3){messages.raw(sender,"<yellow>Usage: /pc givescroll <player> <rename|lore|transmog> [material] [amount]");return;}
        Player p=target(args,1,sender);if(p==null)return; String type=args[2].toLowerCase(Locale.ROOT); Material material=null; int amount=1;
        if(type.equals("transmog")){if(args.length<4){messages.raw(sender,"<yellow>Transmog requires a pickaxe material, e.g. NETHERITE_PICKAXE.");return;} material=Material.matchMaterial(args[3]);if(material==null||!material.name().endsWith("_PICKAXE")){messages.raw(sender,"<red>Invalid pickaxe material.");return;} if(args.length>=5)amount=parsePositiveInt(args[4],1);} else if(args.length>=4) amount=parsePositiveInt(args[3],1);
        if(!List.of("rename","lore","transmog").contains(type)){messages.raw(sender,"<red>Unknown scroll type.");return;}
        ItemStack item=scrolls.create(type,amount,material);Map<Integer,ItemStack> left=p.getInventory().addItem(item);if(!left.isEmpty()){left.values().forEach(i->p.getWorld().dropItemNaturally(p.getLocation(),i));}
        messages.raw(sender,"<green>Gave "+amount+" "+type+" scroll(s) to "+p.getName()+".");
    }

    private void tokens(CommandSender sender,String[] args){
        if(!sender.hasPermission("prisoncore.admin.tokens")){messages.send(sender,"no-permission");return;}
        if(args.length<4){messages.raw(sender,"<yellow>Usage: /pc tokens <set|add|take> <player> <amount>");return;}
        Player p=target(args,2,sender);if(p==null)return; long amount=parsePositiveLong(args[3],-1);if(amount<0){messages.raw(sender,"<red>Amount must be a non-negative whole number.");return;}
        switch(args[1].toLowerCase(Locale.ROOT)){case "set"->data.setTokens(p.getUniqueId(),amount);case "add"->data.addTokens(p.getUniqueId(),amount);case "take"->{if(!data.takeTokens(p.getUniqueId(),amount)){messages.raw(sender,"<red>Player does not have enough tokens.");return;}}default->{messages.raw(sender,"<red>Use set, add, or take.");return;}}
        messages.raw(sender,"<green>Updated "+p.getName()+". New balance: "+data.tokens(p.getUniqueId())+" tokens.");pickaxes.refresh(p.getInventory().getItemInMainHand(),p.getUniqueId());
    }

    private void setLevel(CommandSender sender,String[] args){
        if(args.length<3){messages.raw(sender,"<yellow>Usage: /pc setlevel <player> <level>");return;} Player p=target(args,1,sender);if(p==null)return;int level=parsePositiveInt(args[2],-1);int cap=configs.main().getInt("pickaxe.level-cap",1000);if(level<1||level>cap){messages.raw(sender,"<red>Level must be 1-"+cap+".");return;}ItemStack item=p.getInventory().getItemInMainHand();PickaxeData d=pickaxes.read(item);if(d==null){messages.raw(sender,"<red>Target must hold a Prison Pickaxe.");return;}d.level(level);d.xp(0);pickaxes.write(item,d,p.getUniqueId());messages.raw(sender,"<green>Set "+p.getName()+"'s pickaxe to level "+level+".");
    }

    private void addXp(CommandSender sender,String[] args){
        if(args.length<3){messages.raw(sender,"<yellow>Usage: /pc addxp <player> <amount>");return;}Player p=target(args,1,sender);if(p==null)return;double amount;try{amount=Double.parseDouble(args[2]);}catch(NumberFormatException ex){amount=-1;}if(!Double.isFinite(amount)||amount<=0){messages.raw(sender,"<red>XP must be a positive number.");return;}ItemStack item=p.getInventory().getItemInMainHand();if(!pickaxes.isPrisonPickaxe(item)){messages.raw(sender,"<red>Target must hold a Prison Pickaxe.");return;}int levels=pickaxes.addXp(item,p.getUniqueId(),amount);messages.raw(sender,"<green>Added "+amount+" XP to "+p.getName()+"'s pickaxe"+(levels>0?" ("+levels+" level(s) gained).":"."));
    }

    private void inspect(CommandSender sender,String[] args){Player p=args.length>=2?target(args,1,sender):(sender instanceof Player pl?pl:null);if(p==null)return;PickaxeData d=pickaxes.read(p.getInventory().getItemInMainHand());if(d==null){messages.raw(sender,"<red>Target must hold a Prison Pickaxe.");return;}messages.raw(sender,"<aqua>Item ID: <white>"+d.itemId());messages.raw(sender,"<aqua>Schema: <white>"+d.schema()+" <aqua>Level: <white>"+d.level()+" <aqua>XP: <white>"+String.format(Locale.US,"%.2f",d.xp()));messages.raw(sender,"<aqua>Enchants: <white>"+d.enchants());messages.raw(sender,"<aqua>Rename credits: <white>"+d.renameCredits()+" <aqua>Lore credits: <white>"+d.loreCredits());}
    private void repair(CommandSender sender,String[] args){if(!sender.hasPermission("prisoncore.admin.repair")){messages.send(sender,"no-permission");return;}Player p=args.length>=2?target(args,1,sender):(sender instanceof Player pl?pl:null);if(p==null)return;ItemStack item=p.getInventory().getItemInMainHand();if(!pickaxes.isPrisonPickaxe(item)){messages.raw(sender,"<red>Target must hold a Prison Pickaxe.");return;}pickaxes.refresh(item,p.getUniqueId());messages.raw(sender,"<green>Pickaxe validated and presentation rebuilt.");}

    private Player target(String[] args,int index,CommandSender sender){if(args.length<=index){messages.raw(sender,"<red>Missing player name.");return null;}Player p=Bukkit.getPlayerExact(args[index]);if(p==null)messages.raw(sender,"<red>Player must be online.");return p;}
    private int parsePositiveInt(String raw,int fallback){try{return Integer.parseInt(raw);}catch(NumberFormatException ex){return fallback;}}
    private long parsePositiveLong(String raw,long fallback){try{return Long.parseLong(raw);}catch(NumberFormatException ex){return fallback;}}
    private void help(CommandSender s){messages.raw(s,"<gradient:#00e5ff:#ff2da4><bold>PrisonCore Admin</bold></gradient>");messages.raw(s,"<gray>/pc givepickaxe <player>");messages.raw(s,"<gray>/pc givescroll <player> <rename|lore|transmog> [material] [amount]");messages.raw(s,"<gray>/pc tokens <set|add|take> <player> <amount>");messages.raw(s,"<gray>/pc setlevel <player> <level>");messages.raw(s,"<gray>/pc addxp <player> <amount>");messages.raw(s,"<gray>/pc inspect [player]");messages.raw(s,"<gray>/pc repair [player]");messages.raw(s,"<gray>/pc reload");}

    @Override public List<String> onTabComplete(CommandSender sender,Command command,String alias,String[] args){
        if(args.length==1)return filter(List.of("help","reload","givepickaxe","givescroll","tokens","setlevel","addxp","inspect","repair"),args[0]);
        if(args.length==2&&List.of("givepickaxe","givescroll","setlevel","addxp","inspect","repair").contains(args[0].toLowerCase(Locale.ROOT)))return online(args[1]);
        if(args.length==2&&args[0].equalsIgnoreCase("tokens"))return filter(List.of("set","add","take"),args[1]);
        if(args.length==3&&args[0].equalsIgnoreCase("tokens"))return online(args[2]);
        if(args.length==3&&args[0].equalsIgnoreCase("givescroll"))return filter(List.of("rename","lore","transmog"),args[2]);
        if(args.length==4&&args[0].equalsIgnoreCase("givescroll")&&args[2].equalsIgnoreCase("transmog"))return filter(List.of("WOODEN_PICKAXE","STONE_PICKAXE","IRON_PICKAXE","GOLDEN_PICKAXE","DIAMOND_PICKAXE","NETHERITE_PICKAXE"),args[3]);
        return List.of();
    }
    private List<String> online(String prefix){return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n->n.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))).toList();}
    private List<String> filter(List<String> values,String prefix){return values.stream().filter(v->v.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))).toList();}
}
