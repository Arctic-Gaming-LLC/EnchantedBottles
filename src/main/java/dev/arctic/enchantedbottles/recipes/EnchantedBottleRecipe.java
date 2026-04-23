package dev.arctic.enchantedbottles.recipes;

import dev.arctic.enchantedbottles.EnchantedBottles;
import dev.arctic.iceStorm.items.ItemBuilder;
import dev.arctic.iceStorm.utils.MiniUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.UUID;

public class EnchantedBottleRecipe {

    /** Empty-bottle template — clone this whenever the recipe is crafted or a bottle is claimed. */
    public static ItemStack item;

    public void addRecipes() {
        rebuildItem();

        var recipeKey = new NamespacedKey(EnchantedBottles.plugin, "enchanted_bottle");
        var recipe    = new ShapedRecipe(recipeKey, item);
        recipe.shape(" A ", "ABA", " A ");
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('B', Material.GLASS_BOTTLE);
        Bukkit.addRecipe(recipe);
    }

    /**
     * Builds (or rebuilds) the empty-bottle template from current config values.
     * Call after a config reload so colors and texture are up-to-date.
     *
     * <p>Custom-textured player heads still require SkullMeta — there is no ItemFactory
     * shortcut for this. We use ItemBuilder's typed {@code meta(Class, Consumer)} to keep
     * the rest of the setup fluent.
     */
    public static void rebuildItem() {
        var cfg = EnchantedBottles.config;

        item = ItemBuilder.of(Material.PLAYER_HEAD)
                .meta(SkullMeta.class, meta -> {
                    PlayerProfile profile  = Bukkit.createPlayerProfile(UUID.randomUUID());
                    PlayerTextures textures = profile.getTextures();
                    try {
                        textures.setSkin(URI.create(cfg.getTextureUrl()).toURL());
                    } catch (MalformedURLException e) {
                        EnchantedBottles.plugin.getLogger().warning(
                                "Invalid texture URL in config: " + cfg.getTextureUrl());
                    }
                    profile.setTextures(textures);
                    meta.setPlayerProfile((com.destroystokyo.paper.profile.PlayerProfile) profile);
                })
                .name(MiniUtil.resolve(
                        "<!italic><color:" + cfg.titleHex() + ">Enchanted Bottle"))
                .enchant(Enchantment.UNBREAKING, 1)
                .flags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
                .pdc(EnchantedBottles.BOTTLE_KEY, PersistentDataType.LONG, 0L)
                .build();
    }
}
