package dev.arctic.enchantedbottles.listeners;

import dev.arctic.enchantedbottles.EnchantedBottles;
import dev.arctic.enchantedbottles.recipes.EnchantedBottleRecipe;
import dev.arctic.enchantedbottles.utils.BottleUtil;
import dev.arctic.enchantedbottles.utils.ExpUtil;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import static dev.arctic.enchantedbottles.EnchantedBottles.plugin;
import static dev.arctic.enchantedbottles.utils.BottleUtil.updateEnchantedBottle;

public class PlayerMoveEventListener implements Listener {

    private final Map<UUID, Long> playersInCauldrons = new HashMap<>();
    private final BottleUtil bottleUtil = new BottleUtil();

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
                    ItemStack item = player.getInventory().getItemInMainHand();
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && item.getType() != Material.AIR &&
                            meta.getPersistentDataContainer().has(EnchantedBottles.key, PersistentDataType.INTEGER)) {
                        if (item.getAmount() > 1) {
                            splitExtraBottles(player, item, player.isSneaking());
                        } else {
                            storePlayerExperience(player, player.isSneaking());
                        }
                    }
                });
            }
        }.runTaskTimer(EnchantedBottles.getPlugin(), 20L, 20L);
    }

    private void storePlayerExperience(Player player, boolean isSneaking) {
        int totalExp = ExpUtil.calculateExpToLevel(player.getLevel());
        if (totalExp <= 0) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();

        int bottleExp = meta.getPersistentDataContainer().get(EnchantedBottles.key, PersistentDataType.INTEGER);
        int maxStorage = EnchantedBottles.MAX_EXP;

        if (bottleExp >= maxStorage) return;

        int expToStore = 0;
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

        ExpUtil.setPlayerExperience(player, totalExp - expToStore);
        updateEnchantedBottle(player, item, bottleExp + expToStore);
    }

    private void splitExtraBottles(Player player, ItemStack itemInHand, boolean isSneaking) {
        int extraBottles = itemInHand.getAmount() - 1; // Calculate the extra bottles
        itemInHand.setAmount(1); // Reduce the stack in hand to 1

        ItemStack extraBottlesStack = EnchantedBottleRecipe.item.clone();
        extraBottlesStack.setAmount(extraBottles);

        int firstEmpty = player.getInventory().firstEmpty();

        if (firstEmpty != -1) {
            player.getInventory().setItem(firstEmpty, extraBottlesStack);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), extraBottlesStack);
        }

        storePlayerExperience(player, isSneaking); // Handle experience storing as needed
    }
}
