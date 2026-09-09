package com.seandoyle.prisoncore.command;

import com.seandoyle.prisoncore.data.PlayerDataService;
import com.seandoyle.prisoncore.gui.TokenShopGui;
import com.seandoyle.prisoncore.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public final class TokensCommand implements TabExecutor {
    private final Messages messages;private final PlayerDataService data;private final TokenShopGui shop;
    public TokensCommand(Messages messages,PlayerDataService data,TokenShopGui shop){this.messages=messages;this.data=data;this.shop=shop;}
    @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
        if(args.length>0&&args[0].equalsIgnoreCase("shop")){if(sender instanceof Player p)shop.open(p);else messages.send(sender,"players-only");return true;}
        if(args.length==0){if(sender instanceof Player p)messages.raw(p,"<gray>Tokens: <light_purple>"+data.tokens(p.getUniqueId()));else messages.raw(sender,"<yellow>Usage: /tokens <player>");return true;}
        if(!sender.hasPermission("prisoncore.admin.tokens")){messages.send(sender,"no-permission");return true;} Player target=Bukkit.getPlayerExact(args[0]); if(target==null){messages.raw(sender,"<red>Player must be online.");return true;} messages.raw(sender,"<gray>"+target.getName()+" has <light_purple>"+data.tokens(target.getUniqueId())+" tokens<gray>.");return true;
    }
    @Override public List<String> onTabComplete(CommandSender s,Command c,String a,String[] args){if(args.length==1){java.util.ArrayList<String> out=new java.util.ArrayList<>();out.add("shop");if(s.hasPermission("prisoncore.admin.tokens"))Bukkit.getOnlinePlayers().forEach(p->out.add(p.getName()));return out.stream().filter(x->x.toLowerCase().startsWith(args[0].toLowerCase())).toList();}return List.of();}
}
