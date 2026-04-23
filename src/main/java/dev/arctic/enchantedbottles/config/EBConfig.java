package dev.arctic.enchantedbottles.config;

import dev.arctic.iceStorm.configuration.PluginConfig;
import dev.arctic.iceStorm.configuration.annotation.*;
import dev.arctic.icestorm.libs.snakeyaml.engine.v2.nodes.MappingNode;
import net.kyori.adventure.text.format.TextColor;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * EnchantedBottles configuration, bound via IceStorm's annotation-driven {@link PluginConfig}.
 *
 * <p>On first run the file is written to disk with all defaults populated.
 * Missing keys are patched automatically on subsequent starts, so operators
 * never need to wipe the file after an update.
 */
@ConfigFile("config.yml")
public class EBConfig extends PluginConfig {

    // ── Behaviour ────────────────────────────────────────────────────────────────

    @Comment("Allow bottles to be used by players other than the one who filled them.")
    @BooleanKey(path = "share-bottles", defaultValue = "true")
    private boolean shareBottles;

    // ── Display colors ───────────────────────────────────────────────────────────

    @Comment("Hex color for the bottle's display name.")
    @StringKey(path = "colors.title-color", defaultValue = "#9455e0")
    private String titleColor;

    @Comment("Hex color for primary lore labels (e.g. 'Stored Exp' header).")
    @StringKey(path = "colors.primary-color", defaultValue = "#4e9456")
    private String primaryColor;

    @Comment("Hex color for secondary lore values (e.g. the XP number).")
    @StringKey(path = "colors.secondary-color", defaultValue = "#4ae252")
    private String secondaryColor;

    // ── Head texture ─────────────────────────────────────────────────────────────

    @Comment("Player-head texture URL. Must point to textures.minecraft.net.")
    @StringKey(path = "texture-url",
            defaultValue = "http://textures.minecraft.net/texture/348a7ea198ec4efd8b56bcda8aa4230039e04d1338ee98fa85897bd4f342d632")
    private String textureUrl;

    // ── Cauldron conversion ───────────────────────────────────────────────────────

    @Comment({"How many stored XP points equal one vanilla Exp Bottle when converting in a cauldron.",
              "Vanilla bottles give 3–11 XP each (avg ~7). Lower = more bottles per conversion."})
    @IntKey(path = "xp-per-vanilla-bottle", defaultValue = "7")
    private int xpPerVanillaBottle;

    // ─────────────────────────────────────────────────────────────────────────────

    // Parsed TextColor objects — computed after binding so we're not parsing hex on every lore update
    private TextColor titleTextColor;
    private TextColor primaryTextColor;
    private TextColor secondaryTextColor;

    public EBConfig(Path dataFolder, Logger logger) {
        super(dataFolder, logger);
    }

    @Override
    protected void postBind(MappingNode root) {
        titleTextColor   = parseColor(titleColor,   "#9455e0");
        primaryTextColor = parseColor(primaryColor,  "#4e9456");
        secondaryTextColor = parseColor(secondaryColor, "#4ae252");
    }

    private TextColor parseColor(String hex, String fallback) {
        TextColor c = TextColor.fromHexString(hex);
        return c != null ? c : TextColor.fromHexString(fallback);
    }

    // ── Accessors ────────────────────────────────────────────────────────────────

    public boolean isShareBottles()       { return shareBottles; }
    public String  getTextureUrl()        { return textureUrl; }
    public int     getXpPerVanillaBottle(){ return xpPerVanillaBottle; }

    public TextColor getTitleTextColor()    { return titleTextColor; }
    public TextColor getPrimaryTextColor()  { return primaryTextColor; }
    public TextColor getSecondaryTextColor(){ return secondaryTextColor; }

    /** Convenience: title color as a bare hex string for MiniMessage tags. */
    public String titleHex()    { return titleTextColor.asHexString(); }
    /** Convenience: primary color as a bare hex string for MiniMessage tags. */
    public String primaryHex()  { return primaryTextColor.asHexString(); }
    /** Convenience: secondary color as a bare hex string for MiniMessage tags. */
    public String secondaryHex(){ return secondaryTextColor.asHexString(); }
}
