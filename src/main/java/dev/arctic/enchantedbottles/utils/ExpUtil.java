package dev.arctic.enchantedbottles.utils;

import org.bukkit.entity.Player;

public final class ExpUtil {

    private ExpUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns the total raw experience points the player currently has,
     * including partial progress within the current level.
     *
     * <p>This is the single source of truth for "how much XP does this player have".
     * {@code player.getTotalExperience()} can drift from reality after repeated
     * give/set calls, so we recompute from level + progress instead.
     */
    public static int getTotalPlayerExp(Player player) {
        int base    = getTotalExperienceAtLevel(player.getLevel());
        int partial = Math.round(player.getExp() * player.getExpToLevel());
        return base + partial;
    }

    /**
     * Returns the raw XP value of exactly one level at the player's current level
     * (i.e. the span from level N-1 to level N).
     */
    public static int getOneLevelExp(Player player) {
        return calculateExpToLevel(player.getLevel()) - calculateExpToLevel(player.getLevel() - 1);
    }

    /**
     * Returns the cumulative XP needed to reach {@code level} from 0.
     * Uses Minecraft's official formula for each tier.
     */
    public static int calculateExpToLevel(int level) {
        if (level <= 0)  return 0;
        if (level <= 15) return level * level + 6 * level;
        if (level <= 30) return (int) (2.5 * level * level - 40.5 * level + 360);
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

    /**
     * Returns the total XP a player accumulates by reaching {@code level},
     * summing every individual level's span from 0 → level.
     */
    public static int getTotalExperienceAtLevel(int level) {
        int total = 0;
        for (int i = 1; i <= level; i++) {
            total += calculateExpToLevel(i) - calculateExpToLevel(i - 1);
        }
        return total;
    }

    /**
     * Sets the player's total experience to {@code totalExp} raw XP points.
     * Resets level and progress first to avoid vanilla accumulation drift.
     */
    public static void setPlayerExperience(Player player, int totalExp) {
        player.setLevel(0);
        player.setExp(0);
        player.giveExp(Math.max(0, totalExp));
    }
}
