package dev.arctic.enchantedbottles.listeners;

import dev.arctic.enchantedbottles.EnchantedBottles;
import dev.arctic.enchantedbottles.utils.ExpUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerMoveEventListener implements Listener {

    private final Map<UUID, Long> playersInCauldrons = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Block block = player.getLocation().getBlock();
        Block belowBlock = player.getLocation().subtract(0, 1, 0).getBlock();
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();

        boolean isInCauldron = (block.getType() == Material.CAULDRON || block.getType() == Material.WATER_CAULDRON ||
                belowBlock.getType() == Material.CAULDRON || belowBlock.getType() == Material.WATER_CAULDRON) &&
                meta != null && item.getType() != Material.AIR &&
                meta.getPersistentDataContainer().has(EnchantedBottles.key, PersistentDataType.INTEGER);

        if (isInCauldron) {
            playersInCauldrons.put(playerId, System.currentTimeMillis());
        } else {
            playersInCauldrons.remove(playerId);
        }
    }

    public void startExpDrainTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                playersInCauldrons.keySet().forEach(playerId -> {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player == null || !player.isOnline()) {
                        return;
                    }
                    if (!player.isSneaking() && player.getLevel() == 0 && player.getExp() == 0) {
                        // If the player has no experience, do not attempt to store more.
                        return;
                    }
                    ItemStack item = player.getInventory().getItemInMainHand();
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && item.getType() != Material.AIR &&
                            meta.getPersistentDataContainer().has(EnchantedBottles.key, PersistentDataType.INTEGER)) {
                        storePlayerExperience(player, player.isSneaking());
                    }
                });
            }
        }.runTaskTimer(EnchantedBottles.getPlugin(), 20L, 20L);
    }

    private void storePlayerExperience(Player player, boolean isSneaking) {
        //see how much exp the player has now, if that's 0, then just stop.
        int totalExp = ExpUtil.getTotalExperienceAll(player);
        if (totalExp <= 0) return;

        //Get the Bottle's information
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();

        //Initialize values!
        int bottleExp = meta.getPersistentDataContainer().get(EnchantedBottles.key, PersistentDataType.INTEGER);
        int maxStorage = EnchantedBottles.MAX_EXP;
        if (maxStorage == 0) {
            maxStorage = 2147483647; //set max int value so EXP cannot rollover
        }

        if (bottleExp >= maxStorage) return;

        int expToStore = 0;
        //BEGIN LOGIC! Start by resetting player's exp, so we know it will be added back correctly.
        if (isSneaking) {
            expToStore = totalExp;
            if (expToStore > maxStorage) {
                expToStore = maxStorage;
            }
        } else {
            expToStore = ExpUtil.getTotalExperienceLevel(player);
            if (expToStore + bottleExp > maxStorage) {
                expToStore = maxStorage - bottleExp;
            }
        }


        //We're finally done, so now we update the item and player's experience.
        player.setExp(0);
        player.setLevel(0); // always reset exp to 0 before giving experience
        player.giveExp(totalExp - expToStore);
        updateEnchantedBottle(player, item, meta, bottleExp + expToStore);
    }

    static void updateEnchantedBottle(Player player, ItemStack item, ItemMeta meta, int newStoredExp) {
        meta.getPersistentDataContainer().set(EnchantedBottles.key, PersistentDataType.INTEGER, newStoredExp);

        List<Component> newLore = new ArrayList<>();

        Component lore1 = Component.text("Stored Exp").decorate(TextDecoration.UNDERLINED).color(TextColor.color(EnchantedBottles.PRIMARY_COLOR));
        Component lore2 = Component.text(String.valueOf(newStoredExp)).color(EnchantedBottles.SECONDARY_COLOR);
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
