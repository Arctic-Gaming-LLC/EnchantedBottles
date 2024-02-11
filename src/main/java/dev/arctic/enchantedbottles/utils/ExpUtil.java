package dev.arctic.enchantedbottles.utils;

import org.bukkit.entity.Player;

public class ExpUtil {

    public static int calculateExpToNextLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    public static int getTotalExperienceLevel(Player player) {

        int high = calculateExpToLevel(player.getLevel());
        int low = calculateExpToLevel(player.getLevel() - 1);

        return high-low;
    }

    public static int getTotalExperienceAll(Player player) {
        int level = player.getLevel();
        float progress = player.getExp();
        int totalExperience = calculateExpToLevel(level);

        // Add the progress towards the next level
        totalExperience += (int) progress;
        return totalExperience;
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
}
