package dev.arctic.enchantedbottles.utils;

import dev.arctic.enchantedbottles.EnchantedBottles;
import dev.arctic.enchantedbottles.recipes.EnchantedBottleRecipe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BottleUtil {

    public static void updateEnchantedBottle(Player player, ItemStack item, int newStoredExp) {
        ItemMeta meta = item.getItemMeta();
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
        Component lore9 = Component.text(UUID.randomUUID().toString()).color(TextColor.color(0x292929));

        newLore.add(lore1);
        newLore.add(lore2);
        newLore.add(lore3);
        newLore.add(lore4);
        newLore.add(lore5);
        newLore.add(lore6);
        newLore.add(lore7);
        newLore.add(lore8);
        newLore.add(lore9);
        meta.lore(newLore);
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
    }
}
