package com.leonardobishop.commandtoitem;

import com.google.common.io.ByteStreams;
import com.leonardobishop.commandtoitem.bstats.Metrics;
import com.leonardobishop.commandtoitem.commands.BaseCommand;
import com.leonardobishop.commandtoitem.events.UseItem;
import com.leonardobishop.commandtoitem.utils.itemgetter.ItemGetter;
import com.leonardobishop.commandtoitem.utils.itemgetter.ItemGetterLatest;
import com.leonardobishop.commandtoitem.utils.itemgetter.ItemGetter_1_13;
import com.leonardobishop.commandtoitem.utils.itemgetter.ItemGetter_Late_1_8;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CommandToItem extends JavaPlugin {

    private List<Item> items = new ArrayList<>();
    private Metrics metrics;
    private ItemGetter itemGetter;

    @Override
    public void onEnable() {
        File directory = new File(String.valueOf(this.getDataFolder()));
        if (!directory.exists() && !directory.isDirectory()) {
            directory.mkdir();
        }

        metrics = new Metrics(this);
        if (metrics.isEnabled()) {
            this.getLogger().log(Level.INFO, "Metrics started. This can be disabled at /plugins/bStats/config.yml.");
        }

        File config = new File(this.getDataFolder() + File.separator + "config.yml");
        if (!config.exists()) {
            try {
                config.createNewFile();
                try (InputStream in = CommandToItem.class.getClassLoader().getResourceAsStream("config.yml")) {
                    OutputStream out = new FileOutputStream(config);
                    ByteStreams.copy(in, out);
                } catch (IOException e) {
                    super.getLogger().severe("Failed to create config.");
                    e.printStackTrace();
                    super.getLogger().severe(ChatColor.RED + "...please delete the CommandToItem directory and try RESTARTING (not reloading).");
                }
            } catch (IOException e) {
                super.getLogger().severe("Failed to create config.");
                e.printStackTrace();
                super.getLogger().severe(ChatColor.RED + "...please delete the CommandToItem directory and try RESTARTING (not reloading).");
            }
        }


        super.getServer().getPluginCommand("commandtoitem").setExecutor(new BaseCommand(this));
        super.getServer().getPluginManager().registerEvents(new UseItem(this), this);

        executeVersionSpecificActions();

        this.reloadConfig();
    }

    public String getMessage(Message message) {
        return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages."
                + message.getId(), message.getDef()));
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        items.clear();
        for (String s : this.getConfig().getConfigurationSection("items").getKeys(false)) {
            ItemStack is = itemGetter.getItem("items." + s, this.getConfig(), this);

            // supports old config layout without "on-use"
            boolean consume = this.getConfig().getBoolean("items." + s + ".on-use.consume",
                    this.getConfig().getBoolean("items." + s + ".consume", true));

            List<String> commands;
            if (this.getConfig().contains("items." + s + ".on-use.commands")) commands = this.getConfig().getStringList("items." + s + ".on-use.commands");
            else commands = this.getConfig().getStringList("items." + s + ".commands");

            List<String> messages;
            if (this.getConfig().contains("items." + s + ".on-use.messages")) messages = this.getConfig().getStringList("items." + s + ".on-use.messages");
            else messages = this.getConfig().getStringList("items." + s + ".messages");

            int cooldown = this.getConfig().getInt("items." + s + ".on-use.cooldown",
                    this.getConfig().getInt("items." + s + ".cooldown", 0));

            String sound = this.getConfig().getString("items." + s + ".on-use.sound",
                    this.getConfig().getString("items." + s + ".sound", null));

            items.add(new Item(s.replace(" ", "_"), is, commands, messages, consume, cooldown, sound));
        }


    }

    public List<Item> getItems() {
        return items;
    }

    private void executeVersionSpecificActions() {
        String version;
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            getLogger().warning("Failed to resolve server version - some features will not work!");
            itemGetter = new ItemGetter_Late_1_8();
            return;
        }

        getLogger().info("Your server is running version " + version + ".");
        if (version.startsWith("v1_7") || version.startsWith("v1_8") || version.startsWith("v1_9")
                || version.startsWith("v1_10") || version.startsWith("v1_11") || version.startsWith("v1_12")) {
            itemGetter = new ItemGetter_Late_1_8();
        } else if (version.startsWith("v1_13")) {
            itemGetter = new ItemGetter_1_13();
        } else {
            itemGetter = new ItemGetterLatest();
        }
    }

    public enum Message {

        FULL_INV("full-inv", "&c%player%'s inventory is full!"),
        GIVE_ITEM("give-item", "&6Given &e%player% %item%&6."),
        RECEIVE_ITEM("receive-item", "&6You have been given %item%&6."),
        COOLDOWN("cooldown", "&cYou must wait &4%cooldown% &cseconds before using this item again.");

        private String id;
        private String def; // (default message if undefined)

        Message(String id, String def) {
            this.id = id;
            this.def = def;
        }

        public String getId() {
            return id;
        }

        public String getDef() {
            return def;
        }
    }

}
