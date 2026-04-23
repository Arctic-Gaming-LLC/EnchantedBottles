package dev.arctic.enchantedbottles.commands;

import dev.arctic.enchantedbottles.utils.ExpUtil;
import dev.arctic.iceStorm.utils.MiniUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * /mend — uses the player's raw experience points to repair their equipped items
 * that carry the Mending enchantment, at the vanilla rate of 2 XP per durability point.
 *
 * <p>Items are repaired in priority order: held item first, then armour boots → helmet.
 * Repair stops when the player runs out of XP or all Mending items are full.
 */
public class MendCommand implements CommandExecutor {

    /** Vanilla Mending ratio: 2 XP restores 1 durability point. */
    private static final int XP_PER_DURABILITY = 2;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MiniUtil.resolve("<red>Only players can use this command."));
            return true;
        }

        var mendable = getMendableItems(player);
        if (mendable.isEmpty()) {
            player.sendMessage(MiniUtil.resolve(
                    "<icon_red_exclaim> <red>None of your equipped items have Mending."));
            return true;
        }

        int totalXp = ExpUtil.getTotalPlayerExp(player);
        if (totalXp <= 0) {
            player.sendMessage(MiniUtil.resolve(
                    "<icon_red_exclaim> <red>You have no experience to spend."));
            return true;
        }

        int xpBudget    = totalXp;
        int xpSpent     = 0;
        int repairCount = 0;

        for (var item : mendable) {
            if (xpBudget <= 0) break;

            var meta = item.getItemMeta();
            if (!(meta instanceof Damageable damageable)) continue;
            int damage = damageable.getDamage();
            if (damage <= 0) continue;

            int maxRepair    = xpBudget * XP_PER_DURABILITY;
            int actualRepair = Math.min(damage, maxRepair);
            int cost         = (int) Math.ceil((double) actualRepair / XP_PER_DURABILITY);

            damageable.setDamage(damage - actualRepair);
            item.setItemMeta((ItemMeta) damageable);

            xpBudget -= cost;
            xpSpent  += cost;
            repairCount++;
        }

        if (repairCount == 0) {
            player.sendMessage(MiniUtil.resolve(
                    "<icon_green_exclaim> <color:#4e9456>All your Mending items are already at full durability!"));
            return true;
        }

        ExpUtil.setPlayerExperience(player, totalXp - xpSpent);
        player.sendMessage(MiniUtil.resolve(
                "<icon_green_exclaim> <color:#4e9456>Repaired <color:#4ae252>" + repairCount
                + " <color:#4e9456>item(s), spending <color:#4ae252>" + xpSpent + " <color:#4e9456>exp."));
        return true;
    }

    /**
     * Returns the player's equipped Mending items in repair priority order:
     * main hand first, then armour from boots → helmet.
     */
    private List<ItemStack> getMendableItems(Player player) {
        var result = new ArrayList<ItemStack>();
        var inv    = player.getInventory();

        var held = inv.getItemInMainHand();
        if (isMendable(held)) result.add(held);

        var armor = inv.getArmorContents();
        if (armor != null) {
            for (var piece : armor) {
                if (isMendable(piece)) result.add(piece);
            }
        }
        return result;
    }

    private boolean isMendable(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!(item.getItemMeta() instanceof Damageable)) return false;
        return item.containsEnchantment(Enchantment.MENDING);
    }
}
