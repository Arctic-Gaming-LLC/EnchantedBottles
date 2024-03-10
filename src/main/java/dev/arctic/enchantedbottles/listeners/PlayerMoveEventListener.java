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
        //see how much exp the player has now, if that's 0, then just stop.
        int totalExp = ExpUtil.calculateExpToLevel(player.getLevel());
        if (totalExp <= 0) return;

        //Get the Bottle's information
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();

        //Initialize values!
        int bottleExp = meta.getPersistentDataContainer().get(EnchantedBottles.key, PersistentDataType.INTEGER);
        int maxStorage = EnchantedBottles.MAX_EXP;

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

        ExpUtil.setPlayerExperience(player, totalExp - expToStore);
        updateEnchantedBottle(player, item, bottleExp + expToStore);
    }

    private void splitExtraBottles(Player player, ItemStack itemInHand, boolean isSneaking) {
        int extraBottles = itemInHand.getAmount() - 1;
        itemInHand.setAmount(1);

        ItemStack extraBottleItem = EnchantedBottleRecipe.item.clone();
        extraBottleItem.setAmount(extraBottles);

        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(extraBottleItem);
        player.getInventory().setItemInMainHand(itemInHand);

        overflow.forEach((k, v) -> player.getWorld().dropItem(player.getLocation(), v)); // Drop any that didn't fit

        storePlayerExperience(player, isSneaking);
    }

}
