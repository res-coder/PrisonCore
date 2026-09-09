package com.seandoyle.prisoncore.listener;

import com.seandoyle.prisoncore.gui.EnchantGui;
import com.seandoyle.prisoncore.gui.TokenShopGui;
import com.seandoyle.prisoncore.item.PickaxeService;
import com.seandoyle.prisoncore.item.ScrollService;
import com.seandoyle.prisoncore.util.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public final class InventoryListener implements Listener {
    private final Messages messages; private final PickaxeService pickaxeService; private final ScrollService scrollService; private final EnchantGui enchantGui; private final TokenShopGui tokenShopGui;
    public InventoryListener(Messages messages,PickaxeService pickaxeService,ScrollService scrollService,EnchantGui enchantGui,TokenShopGui tokenShopGui){this.messages=messages;this.pickaxeService=pickaxeService;this.scrollService=scrollService;this.enchantGui=enchantGui;this.tokenShopGui=tokenShopGui;}
    @EventHandler public void onClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player p))return;
        if(e.getInventory().getHolder() instanceof EnchantGui.Holder){e.setCancelled(true); if(e.getClickedInventory()==e.getInventory())enchantGui.handleClick(p,e.getCurrentItem()); return;}
        if(e.getInventory().getHolder() instanceof TokenShopGui.Holder){e.setCancelled(true); if(e.getClickedInventory()==e.getInventory())tokenShopGui.handleClick(p,e.getCurrentItem()); return;}
        ItemStack cursor=e.getCursor(); ItemStack target=e.getCurrentItem();
        if(scrollService.type(cursor)!=null && pickaxeService.isPrisonPickaxe(target)){
            e.setCancelled(true);
            if(scrollService.apply(cursor,target,p.getUniqueId())){
                int left=cursor.getAmount()-1; if(left<=0)e.setCursor(null); else {cursor.setAmount(left);e.setCursor(cursor);} e.setCurrentItem(target);
                messages.raw(p,"<green>Scroll applied successfully.");
            } else messages.raw(p,"<red>That scroll cannot be applied to this pickaxe.");
        }
    }
}
