package dev.arctic.enchantedbottles.utils;

import dev.arctic.enchantedbottles.EnchantedBottles;
import dev.arctic.iceStorm.items.ItemBuilder;
import dev.arctic.iceStorm.items.PDC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class BottleUtil {

    private BottleUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Updates stored-exp and lore on an enchanted bottle, then writes it back into the
     * player's main hand.
     *
     * <p>On the first deposit (no creator recorded yet) the player's UUID is stored in
     * PDC so ownership checks don't rely on fragile lore parsing.
     */
    public static void updateEnchantedBottle(Player player, ItemStack item, long newStoredExp) {
        var cfg = EnchantedBottles.config;

        // Record creator on first use (before cloning — we read from the original)
        boolean needsCreator = !PDC.has(item, EnchantedBottles.CREATOR_KEY, PersistentDataType.STRING);

        var builder = ItemBuilder.from(item)
                .pdc(EnchantedBottles.BOTTLE_KEY, PersistentDataType.LONG, newStoredExp)
                .lore(List.of(
                        dev.arctic.iceStorm.utils.MiniUtil.resolve(
                                "<!italic><color:" + cfg.primaryHex() + "><underlined>Stored Exp"),
                        dev.arctic.iceStorm.utils.MiniUtil.resolve(
                                "<!italic><color:" + cfg.secondaryHex() + ">" + newStoredExp + " xp"),
                        dev.arctic.iceStorm.utils.MiniUtil.resolve(
                                "<!italic><dark_gray>────────────────"),
                        dev.arctic.iceStorm.utils.MiniUtil.resolve(
                                "<!italic><dark_gray>Left Click to claim all exp"),
                        dev.arctic.iceStorm.utils.MiniUtil.resolve(
                                "<!italic><dark_gray>Stand in cauldron to absorb gradually"),
                        dev.arctic.iceStorm.utils.MiniUtil.resolve(
                                "<!italic><dark_gray>Sneak in cauldron to absorb all at once"),
                        dev.arctic.iceStorm.utils.MiniUtil.resolve(
                                "<!italic><dark_gray>Drop into a water cauldron to convert to vanilla bottles")
                ));

        if (needsCreator) {
            builder.pdc(EnchantedBottles.CREATOR_KEY, PersistentDataType.STRING,
                    player.getUniqueId().toString());
        }

        player.getInventory().setItemInMainHand(builder.build());
    }
}
