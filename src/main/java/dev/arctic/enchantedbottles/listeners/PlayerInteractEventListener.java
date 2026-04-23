package dev.arctic.enchantedbottles.listeners;

import dev.arctic.enchantedbottles.EnchantedBottles;
import dev.arctic.enchantedbottles.recipes.EnchantedBottleRecipe;
import dev.arctic.enchantedbottles.utils.ExpUtil;
import dev.arctic.iceStorm.items.PDC;
import dev.arctic.iceStorm.utils.MiniUtil;
import dev.arctic.icestorm.libs.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class PlayerInteractEventListener implements Listener {

    private static final long COOLDOWN_MS = 500;
    private final Object2LongOpenHashMap<UUID> lastUse = new Object2LongOpenHashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        var action = event.getAction();
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) return;

        var player = event.getPlayer();
        var item   = player.getInventory().getItemInMainHand();

        if (!PDC.has(item, EnchantedBottles.BOTTLE_KEY, PersistentDataType.LONG)) return;

        event.setCancelled(true);

        // Cooldown
        long now      = System.currentTimeMillis();
        long lastTime = lastUse.getLong(player.getUniqueId()); // returns 0L if absent
        if (now - lastTime < COOLDOWN_MS) return;

        // Ownership check (PDC-based — no lore parsing)
        if (!EnchantedBottles.config.isShareBottles()) {
            String creator = PDC.get(item, EnchantedBottles.CREATOR_KEY, PersistentDataType.STRING);
            if (creator != null && !player.getUniqueId().toString().equals(creator)) {
                player.sendMessage(MiniUtil.resolve(
                        "<icon_red_exclaim> <red>This bottle belongs to someone else!"));
                return;
            }
        }

        long storedExp = PDC.getOrDefault(item, EnchantedBottles.BOTTLE_KEY, PersistentDataType.LONG, 0L);
        if (storedExp == 0L) {
            player.sendMessage(MiniUtil.resolve("<icon_red_exclaim> <red>This bottle is empty!"));
            return;
        }

        // Return all stored exp on top of whatever the player already has, clamped to int range
        long combined = (long) ExpUtil.getTotalPlayerExp(player) + storedExp;
        ExpUtil.setPlayerExperience(player, (int) Math.min(combined, Integer.MAX_VALUE));

        // Reset the bottle to the clean empty template
        player.getInventory().setItemInMainHand(EnchantedBottleRecipe.item.clone());
        lastUse.put(player.getUniqueId(), now);

        player.sendMessage(MiniUtil.resolve(
                "<icon_green_exclaim> <color:#4e9456>All stored experience has been returned!"));
    }
}
