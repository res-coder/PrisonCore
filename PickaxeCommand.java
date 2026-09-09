package com.seandoyle.prisoncore.command;

import com.seandoyle.prisoncore.gui.EnchantGui;
import com.seandoyle.prisoncore.item.PickaxeService;
import com.seandoyle.prisoncore.model.PickaxeData;
import com.seandoyle.prisoncore.util.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class PickaxeCommand implements TabExecutor {
    private final Messages messages; private final PickaxeService pickaxeService; private final EnchantGui gui;
    public PickaxeCommand(Messages messages,PickaxeService pickaxeService,EnchantGui gui){this.messages=messages;this.pickaxeService=pickaxeService;this.gui=gui;}
    @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
        if(!(sender instanceof Player p)){messages.send(sender,"players-only");return true;} if(!p.hasPermission("prisoncore.use")){messages.send(p,"no-permission");return true;}
        ItemStack item=p.getInventory().getItemInMainHand(); PickaxeData data=pickaxeService.read(item);
        if(args.length==0){gui.open(p);return true;} if(data==null){messages.send(p,"invalid-pickaxe");return true;}
        String sub=args[0].toLowerCase();
        if(sub.equals("rename")){if(args.length<2){messages.raw(p,"<yellow>Usage: /pickaxe rename <name>");return true;} String name=String.join(" ",java.util.Arrays.copyOfRange(args,1,args.length)); if(!pickaxeService.consumeRename(item,p.getUniqueId(),messages.component(name))){messages.send(p,"rename-credit-required");}else messages.raw(p,"<green>Pickaxe renamed.");return true;}
        if(sub.equals("lore")){if(args.length<2){messages.raw(p,"<yellow>Usage: /pickaxe lore <text>");return true;} String line=String.join(" ",java.util.Arrays.copyOfRange(args,1,args.length)); if(!pickaxeService.consumeLore(item,p.getUniqueId(),messages.component(line))){messages.send(p,"lore-credit-required");}else messages.raw(p,"<green>Custom lore added.");return true;}
        if(sub.equals("repair")){pickaxeService.refresh(item,p.getUniqueId());messages.raw(p,"<green>Pickaxe metadata and lore refreshed.");return true;}
        messages.raw(p,"<yellow>Usage: /pickaxe [rename|lore|repair]");return true;
    }
    @Override public List<String> onTabComplete(CommandSender s,Command c,String a,String[] args){if(args.length==1)return List.of("rename","lore","repair").stream().filter(x->x.startsWith(args[0].toLowerCase())).toList();return List.of();}
}
