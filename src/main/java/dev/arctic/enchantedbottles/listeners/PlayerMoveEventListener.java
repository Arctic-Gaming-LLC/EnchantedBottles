package dev.arctic.enchantedbottles.listeners;

import dev.arctic.enchantedbottles.EnchantedBottles;
import dev.arctic.enchantedbottles.recipes.EnchantedBottleRecipe;
import dev.arctic.enchantedbottles.utils.BottleUtil;
import dev.arctic.enchantedbottles.utils.ExpUtil;
import dev.arctic.iceStorm.items.PDC;
import dev.arctic.icestorm.libs.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;

public class PlayerMoveEventListener implements Listener {

    /** Bottles can hold up to 2 billion XP — huge but safe from int rollover. */
    static final long MAX_BOTTLE_EXP = 2_000_000_000L;

    private final Set<UUID> playersInCauldrons = new ObjectOpenHashSet<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only react on block-level position changes to avoid per-frame spam
        var from = event.getFrom();
        var to   = event.getTo();
        if (to == null) return;
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;

        var player = event.getPlayer();

        // ── Fast path: block-type check is cheap — do it before any PDC read ──────
        var loc        = player.getLocation();
        var block      = loc.getBlock();
        var belowBlock = loc.subtract(0, 1, 0).getBlock();
        boolean inCauldron = block.getType()      == Material.WATER_CAULDRON
                          || belowBlock.getType() == Material.WATER_CAULDRON;

        if (!inCauldron) {
            playersInCauldrons.remove(player.getUniqueId());
            return;
        }

        // ── Player is in/above a cauldron — now check for an enchanted bottle ─────
        var item = player.getInventory().getItemInMainHand();
        if (!PDC.has(item, EnchantedBottles.BOTTLE_KEY, PersistentDataType.LONG)) {
            playersInCauldrons.remove(player.getUniqueId());
            return;
        }

        playersInCauldrons.add(player.getUniqueId());
    }

    public void startExpDrainTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var playerId : Set.copyOf(playersInCauldrons)) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player == null || !player.isOnline()) {
                        playersInCauldrons.remove(playerId);
                        continue;
                    }

                    var item = player.getInventory().getItemInMainHand();
                    if (!PDC.has(item, EnchantedBottles.BOTTLE_KEY, PersistentDataType.LONG)) {
                        playersInCauldrons.remove(playerId);
                        continue;
                    }

                    if (item.getAmount() > 1) {
                        splitExtraBottles(player, item);
                    } else {
                        storePlayerExperience(player, player.isSneaking());
                    }
                }
            }
        }.runTaskTimer(EnchantedBottles.plugin, 20L, 20L);
    }

    private void storePlayerExperience(Player player, boolean isSneaking) {
        int playerTotalExp = ExpUtil.getTotalPlayerExp(player);
        if (playerTotalExp <= 0) return;

        var item       = player.getInventory().getItemInMainHand();
        long bottleExp = PDC.getOrDefault(item, EnchantedBottles.BOTTLE_KEY, PersistentDataType.LONG, 0L);

        if (bottleExp >= MAX_BOTTLE_EXP) return; // bottle is full — nothing to absorb

        int expToStore;
        if (isSneaking) {
            // Absorb everything in one tick
            expToStore = playerTotalExp;
        } else {
            // Absorb one level's worth per tick
            int oneLevelExp = ExpUtil.getOneLevelExp(player);
            if (oneLevelExp <= 0) return;
            expToStore = oneLevelExp;
        }

        long newBottleExp  = Math.min(bottleExp + expToStore, MAX_BOTTLE_EXP);
        int  actualStored  = (int) (newBottleExp - bottleExp); // capped portion actually taken

        ExpUtil.setPlayerExperience(player, playerTotalExp - actualStored);
        BottleUtil.updateEnchantedBottle(player, item, newBottleExp);
    }

    private void splitExtraBottles(Player player, ItemStack itemInHand) {
        int extras = itemInHand.getAmount() - 1;
        itemInHand.setAmount(1);

        var extraStack = EnchantedBottleRecipe.item.clone();
        extraStack.setAmount(extras);

        int slot = player.getInventory().firstEmpty();
        if (slot != -1) {
            player.getInventory().setItem(slot, extraStack);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), extraStack);
        }

        storePlayerExperience(player, player.isSneaking());
    }
}
