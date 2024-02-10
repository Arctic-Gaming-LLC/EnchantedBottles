package dev.arctic.enchantedbottles;

import dev.arctic.enchantedbottles.listeners.PlayerInteractEventListener;
import dev.arctic.enchantedbottles.listeners.PlayerMoveEventListener;
import dev.arctic.enchantedbottles.recipes.EnchantedBottleRecipe;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public final class EnchantedBottles extends JavaPlugin implements Listener {

    public static EnchantedBottles plugin;
    public static NamespacedKey key = new NamespacedKey("enchanted_bottles", "stored_exp");

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        EnchantedBottleRecipe recipe = new EnchantedBottleRecipe();
        recipe.addRecipes();
        plugin.getLogger().log(Level.FINE, "[Enchanted Bottles] Recipes Loaded!");

        Bukkit.getPluginManager().registerEvents(this, this);
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerInteractEventListener(), this);

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
}
