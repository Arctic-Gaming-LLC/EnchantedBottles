package dev.arctic.enchantedbottles.recipes;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.arctic.enchantedbottles.EnchantedBottles;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;


public class EnchantedBottleRecipe {

    public void addRecipes() {
        addEnchantedBottleRecipe();
    }

    private void addEnchantedBottleRecipe(){

        Plugin plugin = EnchantedBottles.getPlugin();
        NamespacedKey key = new NamespacedKey(plugin, "enchanted_bottles");
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        NamespacedKey keyInteger = new NamespacedKey("enchanted_bottles", "stored_exp");

        SkullMeta headMeta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        String textureURLString = "http://textures.minecraft.net/texture/348a7ea198ec4efd8b56bcda8aa4230039e04d1338ee98fa85897bd4f342d632";
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL(textureURLString));
            profile.setTextures(textures);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        headMeta.setPlayerProfile(profile);
        item.setItemMeta(headMeta);

        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(keyInteger, PersistentDataType.INTEGER, 0);

        Component name = Component.text().content("Enchanted Bottle").color(TextColor.color(0x9455e0)).build();
        meta.displayName(name);

        item.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape(" A ", "ABA", " A ");
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('B', Material.GLASS_BOTTLE);
        Bukkit.addRecipe(recipe);
    }
}
