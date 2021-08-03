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

                if (args.length == 2) {
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        if (!p.getName().equalsIgnoreCase(args[1]))
                            continue;
                        target = p;
                        break;
                    }
                } else if (sender instanceof Player) {
                    target = ((Player) sender);
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
                ItemStack itemStack = item.getItemStack(); // Get the item stack
                if (item == null) {
                    sender.sendMessage(ChatColor.RED + "The item " + ChatColor.DARK_RED + args[0] + ChatColor.RED + " could not be found.");
                    return true;
                }
                // Limitations for item amount
                if (amount > maxAllowedItems || amount < 1) {
                    sender.sendMessage(ChatColor.RED + "Please enter an amount between " + ChatColor.DARK_RED + "1" + ChatColor.RED + " and " + ChatColor.DARK_RED + maxAllowedItems + ChatColor.RED + ".");
                    return true;
                }

                        sender.sendMessage(plugin.getMessage(CommandToItem.Message.GIVE_ITEM).replace("%player%", target.getName()).replace("%item%", item.getItemStack().getItemMeta().getDisplayName()));
                    } else {
                        sender.sendMessage(plugin.getMessage(CommandToItem.Message.FULL_INV).replace("%player%", target.getName()));
                    }
                    return true;
                }

                target.getInventory().addItem(item.getItemStack());

                sender.sendMessage(plugin.getMessage(CommandToItem.Message.GIVE_ITEM).replace("%player%",
                        target.getName()).replace("%item%", item.getItemStack().getItemMeta().getDisplayName()));
                if (plugin.getConfig().getBoolean("options.show-receive-message", true)) {
                    target.sendMessage(plugin.getMessage(CommandToItem.Message.RECEIVE_ITEM).replace("%player%",
                            target.getName()).replace("%item%", item.getItemStack().getItemMeta().getDisplayName()));
                }
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
}
