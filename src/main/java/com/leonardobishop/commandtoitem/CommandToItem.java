package com.leonardobishop.commandtoitem;

import com.google.common.io.ByteStreams;
import com.leonardobishop.commandtoitem.commands.BaseCommand;
import com.leonardobishop.commandtoitem.events.UseItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CommandToItem extends JavaPlugin {

    private List<Item> items = new ArrayList<>();

    @Override
    public void onEnable() {
        File directory = new File(String.valueOf(this.getDataFolder()));
        if (!directory.exists() && !directory.isDirectory()) {
            directory.mkdir();
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
                }
            } catch (IOException e) {
                super.getLogger().severe("Failed to create config.");
                e.printStackTrace();
            }
        }
        this.reloadConfig();


        super.getServer().getPluginCommand("commandtoitem").setExecutor(new BaseCommand(this));
        super.getServer().getPluginManager().registerEvents(new UseItem(this), this);
    }

    public String getMessage(Message message) {
        return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages." + message.getId(), message.getDef()));
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        items.clear();
        for (String s : this.getConfig().getConfigurationSection("items").getKeys(false)) {
            ItemStack is = getItemStack("items." + s, this.getConfig());
            boolean consume = this.getConfig().getBoolean("items." + s + ".consume", true);
            List<String> commands = this.getConfig().getStringList("items." + s + ".commands");

            items.add(new Item(s.replace(" ", "_"), is, commands, consume));
        }
    }

    public List<Item> getItems() {
        return items;
    }

    public ItemStack getItemStack(String path, FileConfiguration config) {
        String cName = config.getString(path + ".name", path + ".name");
        String cType = config.getString(path + ".item", path + ".item");
        List<String> cLore = config.getStringList(path + ".lore");

        String name;
        Material type = null;
        int data = 0;
        List<String> lore = new ArrayList<>();
        if (cLore != null) {
            for (String s : cLore) {
                lore.add(ChatColor.translateAlternateColorCodes('&', s));
            }
        }
        name = ChatColor.translateAlternateColorCodes('&', cName);

        if (StringUtils.isNumeric(cType)) {
            type = Material.getMaterial(Integer.parseInt(cType));
        } else if (Material.getMaterial(cType) != null) {
            type = Material.getMaterial(cType);
        } else if (cType.contains(":")) {
            String[] parts = cType.split(":");
            if (parts.length > 1) {
                if (StringUtils.isNumeric(parts[0])) {
                    type = Material.getMaterial(Integer.parseInt(parts[0]));
                } else if (Material.getMaterial(parts[0]) != null) {
                    type = Material.getMaterial(parts[0]);
                }
                if (StringUtils.isNumeric(parts[1])) {
                    data = Integer.parseInt(parts[1]);
                }
            }
        }

        if (type == null) {
            type = Material.STONE;
        }


        ItemStack is = new ItemStack(type, 1, (short) data);
        ItemMeta ism = is.getItemMeta();
        ism.setLore(lore);
        ism.setDisplayName(name);
        is.setItemMeta(ism);

        if (config.isSet(path + ".enchantments")) {
            for (String key : getConfig().getStringList(path + ".enchantments")) {
                String[] split = key.split(":");
                String ench = split[0];
                String level = null;
                if (split.length == 2) {
                    level = split[1];
                } else {
                    level = "1";
                }

                if (Enchantment.getByName(ench) == null) continue;

                try {
                    Integer.parseInt(level);
                } catch (NumberFormatException e) {
                    level = "1";
                }

                is.addUnsafeEnchantment(Enchantment.getByName(ench), Integer.parseInt(level));
            }
        }

        return is;
    }

    public enum Message {

        FULL_INV("full-inv", "&c%player%'s inventory is full!"),
        GIVE_ITEM("give-item", "&6Given &e%player% %item%&6."),
        RECEIVE_ITEM("receive-item", "&6You have been given %item%&6.");

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
