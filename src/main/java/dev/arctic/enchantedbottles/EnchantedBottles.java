package dev.arctic.enchantedbottles;

import dev.arctic.enchantedbottles.listeners.BlockPlaceEventListener;
import dev.arctic.enchantedbottles.listeners.PlayerInteractEventListener;
import dev.arctic.enchantedbottles.listeners.PlayerMoveEventListener;
import dev.arctic.enchantedbottles.recipes.EnchantedBottleRecipe;
import dev.arctic.enchantedbottles.utils.ExpUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class EnchantedBottles extends JavaPlugin implements Listener {

    public static EnchantedBottles plugin;
    public static NamespacedKey key = new NamespacedKey("enchanted_bottles", "stored_exp");

    @Getter @Setter public static TextColor TITLE_COLOR;
    @Getter @Setter public static TextColor PRIMARY_COLOR;
    @Getter @Setter public static TextColor SECONDARY_COLOR;
    @Getter @Setter public static String TEXTURE_URL;
    @Getter @Setter public static boolean SHARING_ALLOWED;
    @Getter @Setter public static int MAX_EXP;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        //Making a change to test webhooks
        getConfig();
        saveDefaultConfig();

        TITLE_COLOR = TextColor.fromHexString(getConfig().getString("Colors.title color"));
        PRIMARY_COLOR = TextColor.fromHexString(getConfig().getString("Colors.primary color"));
        SECONDARY_COLOR = TextColor.fromHexString(getConfig().getString("Colors.secondary color"));
        TEXTURE_URL = getConfig().getString("texture url");
        SHARING_ALLOWED = getConfig().getBoolean("share bottles");
        MAX_EXP = ExpUtil.calculateExpToLevel(getConfig().getInt("level limit"));
        if (getMAX_EXP() == 0) {
            setMAX_EXP(2147483647);
        }

        //Add custom bottles to recipes
        EnchantedBottleRecipe recipe = new EnchantedBottleRecipe();
        recipe.addRecipes();
        plugin.getLogger().log(Level.WARNING, "[Enchanted Bottles] Recipes Loaded!");

        //Register Listeners
        Bukkit.getPluginManager().registerEvents(this, this);
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerInteractEventListener(), this);
        pm.registerEvents(new BlockPlaceEventListener(), this);

        PlayerMoveEventListener moveEventListener = new PlayerMoveEventListener();
        pm.registerEvents(moveEventListener, this);
        moveEventListener.startExpDrainTask();

        plugin.getLogger().log(Level.WARNING, "[Enchanted Bottles] Plugin Loaded!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public void reloadConfigDefaults() {
        TITLE_COLOR = TextColor.fromHexString(getConfig().getString("Colors.title color"));
        PRIMARY_COLOR = TextColor.fromHexString(getConfig().getString("Colors.primary color"));
        SECONDARY_COLOR = TextColor.fromHexString(getConfig().getString("Colors.secondary color"));
        TEXTURE_URL = getConfig().getString("texture url");
        SHARING_ALLOWED = getConfig().getBoolean("share bottles");
        MAX_EXP = ExpUtil.getTotalExperienceAtLevel(getConfig().getInt("level limit"));
    }
}
