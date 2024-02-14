package dev.arctic.enchantedbottles.listeners;

import dev.arctic.enchantedbottles.EnchantedBottles;
import dev.arctic.enchantedbottles.utils.ExpUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.block.Action;

public class PlayerInteractEventListener implements Listener {

    private final Map<UUID, Long> lastUse = new HashMap<>();
    private static final long COOLDOWN_MS = 1000; // 1 second cooldown

    @EventHandler
    public void playerInteractEventListener(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        boolean sharingEnabled = EnchantedBottles.getPlugin().getConfig().getBoolean("share bottles");

        // Check cooldown
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

        Component message = Component.text().content("All stored experience has been returned!").color(TextColor.color(0x4e9456)).build();

        if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && meta != null && meta.getPersistentDataContainer().has(EnchantedBottles.key, PersistentDataType.INTEGER)) {
            event.setCancelled(true);
            int storedExp = meta.getPersistentDataContainer().get(EnchantedBottles.key, PersistentDataType.INTEGER);

            int currentExp = player.calculateTotalExperiencePoints();

            if (storedExp != 0) {
                setPlayerExperience(player,  storedExp + currentExp);
                updateEnchantedBottle(player);
                player.sendMessage(message);
                lastUse.put(playerId, System.currentTimeMillis()); // Update last use time
            }
        }
    }

    private void setPlayerExperience(Player player, int totalExp) {
        player.setLevel(0);
        player.setExp(0);
        player.giveExp(totalExp);
    }

    static void updateEnchantedBottle(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();

        meta.getPersistentDataContainer().set(EnchantedBottles.key, PersistentDataType.INTEGER, 0);

        List<Component> newLore = new ArrayList<>();
        Component lore1 = Component.text("Stored Exp").decorate(TextDecoration.UNDERLINED)
                .color(TextColor.color(EnchantedBottles.PRIMARY_COLOR));
        Component lore2 = Component.text(0).color(EnchantedBottles.SECONDARY_COLOR);
        Component lore3 = Component.text("Created By").decorate(TextDecoration.UNDERLINED).color(EnchantedBottles.PRIMARY_COLOR);
        Component lore4 = Component.text(player.getName()).color(EnchantedBottles.SECONDARY_COLOR);
        Component lore5 = Component.text("------------").color(TextColor.color(0x525252));
        Component lore6 = Component.text("Left Click to return all levels").color(TextColor.color(0x525252));
        Component lore7 = Component.text("Stand in a cauldron to store exp gradually").color(TextColor.color(0x525252));
        Component lore8 = Component.text("Sneak in a cauldron to store all exp").color(TextColor.color(0x525252));

        newLore.add(lore1);
        newLore.add(lore2);
        newLore.add(lore3);
        newLore.add(lore4);
        newLore.add(lore5);
        newLore.add(lore6);
        newLore.add(lore7);
        newLore.add(lore8);
        meta.lore(newLore);

        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
    }
}
