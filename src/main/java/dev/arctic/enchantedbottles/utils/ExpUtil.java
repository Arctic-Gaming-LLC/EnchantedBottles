package dev.arctic.enchantedbottles.utils;

import org.bukkit.entity.Player;

public class ExpUtil {

    public static int getTotalExperienceLevel(Player player) {

        int high = calculateExpToLevel(player.getLevel());
        int low = calculateExpToLevel(player.getLevel() - 1);

        return high-low;
    }

    public static int getExperienceToNextLevel(Player player) {
        return calculateExpToLevel(player.getLevel() + 1) - calculateExpToLevel(player.getLevel());
    }

    public static int calculateExpToLevel(int level) {
        if (level <= 15) {
            return (level * level) + (6 * level);
        } else if (level <= 30) {
            return (int) (2.5 * (level * level) - 40.5 * level + 360);
        } else {
            return (int) (4.5 * (level * level) - 162.5 * level + 2220);
        }
    }

    public static int getTotalExperienceAtLevel(int level) {
        int totalExperience = 0;
        for (int i = 0; i < level; i++) {
            totalExperience += calculateExpToLevel(i);
        }
        return totalExperience;
    }

    public static void setPlayerExperience(Player player, int totalExp) {
        player.setLevel(0);
        player.setExp(0);
        player.giveExp(totalExp);
    }
}
