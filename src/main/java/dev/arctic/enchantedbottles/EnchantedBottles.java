package dev.arctic.enchantedbottles;

import dev.arctic.enchantedbottles.commands.MendCommand;
import dev.arctic.enchantedbottles.config.EBConfig;
import dev.arctic.enchantedbottles.listeners.BlockPlaceEventListener;
import dev.arctic.enchantedbottles.listeners.BottleDropListener;
import dev.arctic.enchantedbottles.listeners.PlayerInteractEventListener;
import dev.arctic.enchantedbottles.listeners.PlayerMoveEventListener;
import dev.arctic.enchantedbottles.recipes.EnchantedBottleRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class EnchantedBottles extends JavaPlugin {

    public static EnchantedBottles plugin;

    public static EBConfig config;

    /** PDC key for the stored exp value on a bottle. Namespace kept as "enchanted_bottles"
     *  so bottles crafted in earlier versions remain compatible. */
    public static NamespacedKey BOTTLE_KEY;

    /** PDC key for the UUID string of whoever first deposited into the bottle. */
    public static NamespacedKey CREATOR_KEY;

    @Override
    public void onEnable() {
        plugin = this;

        BOTTLE_KEY  = new NamespacedKey("enchanted_bottles", "stored_exp");
        CREATOR_KEY = new NamespacedKey("enchanted_bottles", "creator");

        config = new EBConfig(getDataFolder().toPath(), getLogger());
        if (!config.isValid()) {
            getLogger().severe("Config failed to load — disabling EnchantedBottles.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new EnchantedBottleRecipe().addRecipes();
        getLogger().info("Recipes loaded.");

        var pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerInteractEventListener(), this);
        pm.registerEvents(new BlockPlaceEventListener(), this);
        pm.registerEvents(new BottleDropListener(), this);

        var moveListener = new PlayerMoveEventListener();
        pm.registerEvents(moveListener, this);
        moveListener.startExpDrainTask();

        var mend = getCommand("mend");
        if (mend != null) mend.setExecutor(new MendCommand());

        getLogger().info("EnchantedBottles v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("EnchantedBottles disabled.");
    }
}
