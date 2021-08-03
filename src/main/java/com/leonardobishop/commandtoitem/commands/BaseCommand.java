package com.leonardobishop.commandtoitem.commands;

import com.leonardobishop.commandtoitem.CommandToItem;
import com.leonardobishop.commandtoitem.Item;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseCommand implements CommandExecutor, TabCompleter {

    private CommandToItem plugin;
    private ArrayList<String> nameCache;
    private final int maxAllowedItems = 2147483647;

    public BaseCommand(CommandToItem plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getLabel().equalsIgnoreCase("commandtoitem")) {
            if (args.length > 0) {
                if (args[0].equals("list")) {
                    List<String> ids = new ArrayList<>();
                    for (Item item : plugin.getItems()) {
                        ids.add(item.getId());
                    }
                    sender.sendMessage(ChatColor.GOLD + "Items: " + ChatColor.YELLOW + String.join(ChatColor.GRAY + ", " + ChatColor.YELLOW, ids));
                    return true;
                } else if (args[0].equals("reload")) {
                    plugin.reloadConfig();
                    refreshNameCache();
                    sender.sendMessage(ChatColor.GRAY + "CommandToItem has been reloaded");
                    return true;
                }


                Player target = null;

                if (args.length >= 2) {
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        if (!p.getName().equalsIgnoreCase(args[1]))
                            continue;
                        target = p;
                        break;
                    }
                }

                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "The specified player could not be found.");
                    return true;
                }

                // If a third argument is specified, validate it as an amount. Otherwise, use 1 as default
                int amount = 1;
                if (args.length > 2) {
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (NumberFormatException | NullPointerException e) {
                        amount = 0;
                    }
                }

                Item item = getItemByName(args[0]);
                if (item == null) {
                    sender.sendMessage(ChatColor.RED + "The item " + ChatColor.DARK_RED + args[0] + ChatColor.RED + " could not be found.");
                    return true;
                }
                // Limitations for item amount
                if (amount > maxAllowedItems || amount < 1) {
                    sender.sendMessage(ChatColor.RED + "Please enter an amount between " + ChatColor.DARK_RED + "1" + ChatColor.RED + " and " + ChatColor.DARK_RED + maxAllowedItems + ChatColor.RED + ".");
                    return true;
                }

                ItemStack itemStack = item.getItemStack(); // Get the item stack

                // If adding items would cause inventory overflow and lost items can't be dropped at feet, cancel adding items
                if (!plugin.getConfig().getBoolean("options.drop-if-full-inventory", false) && !canAddItems(amount, target, itemStack)) {
                    sender.sendMessage(plugin.getMessage(CommandToItem.Message.FULL_INV).replace("%player%", target.getName()));
                    return true;
                }

                // Add items one-by-one
                int addedItems = 0;
                for (int i = 0; i < amount; i++) {
                    if (!canAddItems(1, target, itemStack)) {
                        // If inventory is full, drop item
                        target.getWorld().dropItem(target.getLocation(), item.getItemStack());
                    } else {
                        // If inventory has space, add item
                        target.getInventory().addItem(itemStack);
                        addedItems++;
                    }
                }

                int lostItems = amount - addedItems;
                if (plugin.getConfig().getBoolean("options.show-receive-message", true)) {
                    if (lostItems > 0) {
                        // If some items were dropped in the process, notify user
                        target.sendMessage(plugin.getMessage(CommandToItem.Message.RECEIVE_ITEM_INVENTORY_FULL).replace("%item%", item.getItemStack().getItemMeta().getDisplayName()).replace("%given_amount%", Integer.toString(amount)).replace("%dropped_amount%", Integer.toString(lostItems)));
                    } else {
                        // If no items lost, send success message
                        target.sendMessage(plugin.getMessage(CommandToItem.Message.RECEIVE_ITEM).replace("%player%",
                                target.getName()).replace("%item%", item.getItemStack().getItemMeta().getDisplayName()).replace("%amount%", Integer.toString(amount)));
                    }
                }

                sender.sendMessage(plugin.getMessage(CommandToItem.Message.GIVE_ITEM).replace("%player%", target.getName()).replace("%item%", item.getItemStack().getItemMeta().getDisplayName()).replace("%amount%", Integer.toString(amount)));
                return true;
            }

            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Command To Item (ver " + plugin.getDescription().getVersion() + ")");
            sender.sendMessage(ChatColor.GRAY + "<> = required, [] = optional");
            sender.sendMessage(ChatColor.YELLOW + "/cti :" + ChatColor.GRAY + " view this menu");
            sender.sendMessage(ChatColor.YELLOW + "/cti <item> [player] :" + ChatColor.GRAY + " give <item> to [player] (or self if blank)");
            sender.sendMessage(ChatColor.YELLOW + "/cti list :" + ChatColor.GRAY + " list all items");
            sender.sendMessage(ChatColor.YELLOW + "/cti reload :" + ChatColor.GRAY + " reload the config");
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        if (nameCache == null) {
            refreshNameCache();
        }
        if (args.length < 2) {
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], nameCache, completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 3) {
            // Add common amounts that you specify do for this item
            Item item = getItemByName(args[0]); // Get item from arg name
            List<String> amounts = new ArrayList<>(); // List to store all numbers

            // If item doesn't exist, return blank array list
            if (item == null) return amounts;

            int maxStackSize = item.getItemStack().getMaxStackSize(); // Get max stack size of this item
            for (int i = 1; i <= maxStackSize; i++) {
                // Add numbers from 1 to max item size
                amounts.add(Integer.toString(i));
            }
            if (!amounts.contains("64")) amounts.add("64"); // If 64 not in list, add it

            // Match typed number to the list created
            List<String> completions = new ArrayList<>(); // List to store all matched numbers
            StringUtil.copyPartialMatches(args[2], amounts, completions);
            Collections.sort(completions);

            return completions;
        }
        return null;
    }

    public void refreshNameCache() {
        nameCache = new ArrayList<>();
        for (Item i : plugin.getItems()) {
            nameCache.add(i.getId());
        }
//        nameCache.add("list");
//        nameCache.add("reload");
    }

    private Item getItemByName(String name) {
        Item item = null;
        for (Item i : plugin.getItems()) {
            if (i.getId().equals(name)) {
                item = i;
                break;
            }
        }
        return item;
    }

    // Check if adding new items would cause inventory to overflow. If inventory is full and lost items aren't dropped at feet, cancel adding items
    private boolean canAddItems(int amount, Player target, ItemStack itemToAdd) {
        int amountAbleToAdd = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack usersItemStack = target.getInventory().getItem(i);
            if (usersItemStack == null) {
                // If slot is empty, it can be filled with an entire stack of new item
                amountAbleToAdd += itemToAdd.getMaxStackSize();
            } else if (usersItemStack.isSimilar(itemToAdd)) {
                // If slot is same as item, it can be filled until it reaches max stack size
                amountAbleToAdd += itemToAdd.getMaxStackSize() - usersItemStack.getAmount();
            }

            if (amountAbleToAdd >= amount) break; // Exit loop early if we're able to add sufficient items
        }

        return amountAbleToAdd >= amount;
    }
}
