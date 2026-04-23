package dev.arctic.enchantedbottles.listeners;

import dev.arctic.enchantedbottles.EnchantedBottles;
import dev.arctic.iceStorm.items.PDC;
import dev.arctic.iceStorm.utils.MiniUtil;
import dev.arctic.icestorm.libs.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;

/**
 * Converts enchanted bottles into vanilla Exp Bottles when a player drops one
 * into a water cauldron.
 *
 * <h2>Flow</h2>
 * <ol>
 *   <li>{@link PlayerDropItemEvent} — if the dropped item is an enchanted bottle,
 *       add it to the watch set and start a short-lived polling task.</li>
 *   <li>Every 5 ticks the task checks whether the item entity's block is a
 *       {@code WATER_CAULDRON}. If so, it removes the item, spawns stacks of
 *       {@code EXPERIENCE_BOTTLE} equal to {@code storedExp / xp-per-vanilla-bottle},
 *       and notifies the original dropper (if still online).</li>
 *   <li>The task cancels itself after 30 seconds regardless, or when the item is
 *       picked up / despawns.</li>
 * </ol>
 */
public class BottleDropListener implements Listener {

    /** Item entity UUIDs currently being watched for cauldron entry. */
    private final Set<UUID> watchedItems = new ObjectOpenHashSet<>();

    // ── Drop detection ────────────────────────────────────────────────────────────

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        var dropped = event.getItemDrop();
        if (!PDC.has(dropped.getItemStack(), EnchantedBottles.BOTTLE_KEY, PersistentDataType.LONG)) return;

        watchedItems.add(dropped.getUniqueId());
        scheduleConversionCheck(event.getPlayer().getUniqueId(), dropped);
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────────

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        watchedItems.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        watchedItems.remove(event.getItem().getUniqueId());
    }

    // ── Polling task ──────────────────────────────────────────────────────────────

    private void scheduleConversionCheck(UUID dropperUUID, Item itemEntity) {
        new BukkitRunnable() {
            /** Max checks: 120 × 5 ticks = 600 ticks = 30 seconds. */
            int checks = 0;

            @Override
            public void run() {
                // Item gone or no longer watched (picked up / already converted)
                if (!itemEntity.isValid() || !watchedItems.contains(itemEntity.getUniqueId())) {
                    cancel();
                    return;
                }

                // Timeout
                if (++checks > 120) {
                    watchedItems.remove(itemEntity.getUniqueId());
                    cancel();
                    return;
                }

                // Only convert in a water cauldron (empty cauldron = intentional floor drop)
                if (itemEntity.getLocation().getBlock().getType() != Material.WATER_CAULDRON) return;

                watchedItems.remove(itemEntity.getUniqueId());
                convertToCauldron(dropperUUID, itemEntity);
                cancel();
            }
        }.runTaskTimer(EnchantedBottles.plugin, 2L, 5L);
    }

    // ── Conversion ────────────────────────────────────────────────────────────────

    private void convertToCauldron(UUID dropperUUID, Item itemEntity) {
        var stack      = itemEntity.getItemStack();
        long storedExp = PDC.getOrDefault(stack, EnchantedBottles.BOTTLE_KEY, PersistentDataType.LONG, 0L);
        var loc        = itemEntity.getLocation();
        var world      = loc.getWorld();
        itemEntity.remove();

        int  xpPerBottle = EnchantedBottles.config.getXpPerVanillaBottle();
        long bottleCount = storedExp / xpPerBottle;

        Player dropper = Bukkit.getPlayer(dropperUUID);

        if (bottleCount <= 0) {
            // Not enough exp stored — toss the original bottle back
            world.dropItemNaturally(loc, stack);
            if (dropper != null) {
                dropper.sendMessage(MiniUtil.resolve(
                        "<icon_red_exclaim> <red>Not enough stored exp to convert! "
                        + "Need at least <white>" + xpPerBottle + " XP<red>."));
            }
            return;
        }

        // Spawn vanilla exp bottle stacks (max 64 per entity)
        long remaining = bottleCount;
        while (remaining > 0) {
            int amt = (int) Math.min(remaining, 64);
            world.dropItemNaturally(loc, new ItemStack(Material.EXPERIENCE_BOTTLE, amt));
            remaining -= amt;
        }

        // Visual + audio feedback
        world.spawnParticle(Particle.ENCHANT, loc, 40, 0.4, 0.4, 0.4, 0.15);
        world.playSound(loc, Sound.ENTITY_SPLASH_POTION_BREAK, 1f, 0.9f);

        if (dropper != null) {
            dropper.sendMessage(MiniUtil.resolve(
                    "<icon_green_exclaim> <color:#4e9456>Converted <color:#4ae252>" + storedExp
                    + " XP <color:#4e9456>into <color:#4ae252>" + bottleCount
                    + " <color:#4e9456>exp bottle" + (bottleCount == 1L ? "" : "s") + "!"));
        }
    }
}
