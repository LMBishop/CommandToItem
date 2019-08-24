package com.leonardobishop.commandtoitem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Item {

    private String id;
    private ItemStack itemStack;
    private List<String> commands;
    private boolean consumed;

    public Item(String id, ItemStack itemStack, List<String> commands, boolean consumed) {
        this.id = id;
        this.itemStack = itemStack;
        this.commands = commands;
        this.consumed = consumed;
    }

    public void executeCommands(Player player) {
        for (String command : commands) {
            command = command.replace("%player%", player.getName());
            if (command.contains("executeas:player")) {
                command = command.replace("executeas:player ", "").replace("executeas:player", "");
                Bukkit.getServer().dispatchCommand(player, command);
            } else if (command.contains("executeas:console")) {
                command = command.replace("executeas:console ", "").replace("executeas:console", "");
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
            }
        }
    }

    public boolean compare(ItemStack to) {
        return itemStack.isSimilar(to);
    }

    public String getId() {
        return id;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<String> getCommands() {
        return commands;
    }

    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public String toString() {
        return id;
    }
}
