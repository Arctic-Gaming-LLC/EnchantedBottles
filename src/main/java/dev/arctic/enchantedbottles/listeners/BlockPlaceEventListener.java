package dev.arctic.enchantedbottles.listeners;

import dev.arctic.enchantedbottles.EnchantedBottles;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class BlockPlaceEventListener implements Listener {

    @EventHandler
    public static void blockPlaceEventListener(BlockPlaceEvent event){
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getItemMeta().getPersistentDataContainer().has(EnchantedBottles.key, PersistentDataType.INTEGER)){
            event.setCancelled(true);
        }
    }
}
