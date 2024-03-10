package dev.arctic.enchantedbottles.listeners;

import dev.arctic.enchantedbottles.EnchantedBottles;
import dev.arctic.enchantedbottles.recipes.EnchantedBottleRecipe;
import dev.arctic.enchantedbottles.utils.BottleUtil;
import dev.arctic.enchantedbottles.utils.ExpUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.block.Action;

public class PlayerInteractEventListener implements Listener {

    private final Map<UUID, Long> lastUse = new HashMap<>();
    private static final long COOLDOWN_MS = 500;

    @EventHandler
    public void playerInteractEventListener(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        boolean sharingEnabled = EnchantedBottles.getPlugin().getConfig().getBoolean("share bottles");

        if (lastUse.containsKey(playerId) && System.currentTimeMillis() - lastUse.get(playerId) < COOLDOWN_MS) {
            player.sendMessage("Please wait before using this again.");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();

        if (!sharingEnabled) {
            List<Component> lore = meta.lore();
            if (lore != null && lore.size() >= 4) {
                Component playerNameComponent = Component.text(player.getName()).color(EnchantedBottles.SECONDARY_COLOR);
                Component fourthLoreComponent = lore.get(3);
                if (!fourthLoreComponent.equals(playerNameComponent)) {
                    player.sendMessage("You cannot use this!");
                    return;
                }
            }
        }

        if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && meta != null && meta.getPersistentDataContainer().has(EnchantedBottles.key, PersistentDataType.INTEGER)) {
            event.setCancelled(true);
            int storedExp = meta.getPersistentDataContainer().get(EnchantedBottles.key, PersistentDataType.INTEGER);
            if (storedExp == 0) {
                player.sendMessage(Component.text("No experience stored!").color(TextColor.color(0x94564e)));
                return;
            }
            ExpUtil.setPlayerExperience(player, (int) (storedExp + (ExpUtil.getTotalExperienceLevel(player) + player.getExp())));
            player.sendMessage(Component.text("All stored experience has been returned!").color(TextColor.color(0x4e9456)));
            player.getInventory().setItemInMainHand(EnchantedBottleRecipe.item);
            lastUse.put(playerId, System.currentTimeMillis());
        }
    }
}
