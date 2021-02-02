package com.leonardobishop.commandtoitem;

import com.leonardobishop.commandtoitem.utils.GSound;
import com.leonardobishop.commandtoitem.utils.itemgetter.ItemGetter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Item {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private final String id;
    private final ItemStack itemStack;
    private final List<String> commands;
    private final List<String> messages;
    private final boolean consumed;
    private final int cooldown;
    private final String sound;
    private final boolean permissionRequired;

    public Item(String id, ItemStack itemStack, List<String> commands, List<String> messages, boolean consumed, int cooldown, String sound, boolean permissionRequired) {
        this.id = id;
        this.itemStack = itemStack;
        this.commands = commands;
        this.messages = messages;
        this.consumed = consumed;
        this.cooldown = cooldown;
        this.sound = sound;
        this.permissionRequired = permissionRequired;
    }

    /**
     * Execute all actions to do with using an item (that is executing the commands,
     * sending messages and applying cooldowns).
     *
     * @param player the player who is using the item
     */
    public void executeUseActions(Player player) {
        playSounds(player);
        executeCommands(player);
        putOnCooldown(player);
        sendMessages(player);
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

    public void playSounds(Player player) {
        if (sound == null || sound.equals("")) {
            return;
        }
        try {
            player.playSound(player.getLocation(), GSound.match(this.sound).parseSound(), 1, 1);
        } catch (Exception e) {
            // doesnt exist
        }

    }

    public void sendMessages(Player player) {
        for (String message : messages) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message
                    .replace("%player%", player.getName())
                    .replace("%item%", itemStack.getItemMeta().getDisplayName())
                    .replace("%cooldown%", String.valueOf(getCooldownFor(player)))));
        }
    }

    public boolean isOnCooldown(Player player) {
        if (cooldown > 0 && cooldowns.containsKey(player.getUniqueId())) {
            long timeNow = System.currentTimeMillis();
            long timeExpire = cooldowns.get(player.getUniqueId());

            return timeExpire > timeNow;
        } else return false;
    }

    public long getCooldownFor(Player player) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeNow = System.currentTimeMillis();
            long timeExpire = cooldowns.get(player.getUniqueId());
            long diff = timeExpire - timeNow;

            return TimeUnit.SECONDS.convert(diff, TimeUnit.MILLISECONDS);
        } else return 0;
    }

    public void putOnCooldown(Player player) {
        if (cooldown <= 0) return;

        long timeExpire = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(cooldown, TimeUnit.SECONDS);
        cooldowns.put(player.getUniqueId(), timeExpire);
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

    public int getCooldown() {
        return cooldown;
    }

    public boolean isPermissionRequired() {
        return permissionRequired;
    }

    @Override
    public String toString() {
        return id;
    }

}
