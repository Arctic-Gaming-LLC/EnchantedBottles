package dev.arctic.enchantedbottles.listeners;

import dev.arctic.enchantedbottles.EnchantedBottles;
import dev.arctic.iceStorm.items.PDC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;

public class BlockPlaceEventListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var item = event.getPlayer().getInventory().getItemInMainHand();
        if (PDC.has(item, EnchantedBottles.BOTTLE_KEY, PersistentDataType.LONG)) {
            event.setCancelled(true);
        }
    }
}
