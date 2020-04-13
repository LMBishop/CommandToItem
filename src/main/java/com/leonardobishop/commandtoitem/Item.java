package com.leonardobishop.commandtoitem;

import com.leonardobishop.commandtoitem.utils.GSound;
import com.leonardobishop.commandtoitem.utils.RegionCondition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Item {

    /*
    item reader for 1.14+ ONLY

    complete & working supporting:
     - name
     - material
     - lore
     - enchantments
     - itemflags
     - unbreakable
     - attribute modifier
     - custom model data
     */
    public static ItemStack getItemStack(String path, FileConfiguration config) {
        String cName = config.getString(path + ".name", path + ".name");
        String cType = config.getString(path + ".item", path + ".item");
        boolean hasCustomModelData = config.contains(path + ".custommodeldata");
        int customModelData = config.getInt(path + ".custommodeldata", 0);
        boolean unbreakable = config.getBoolean(path + ".unbreakable", false);
        List<String> cLore = config.getStringList(path + ".lore");
        List<String> cItemFlags = config.getStringList(path + ".itemflags");
        boolean hasAttributeModifiers = config.contains(path + ".attributemodifiers");
        List<Map<?, ?>> cAttributeModifiers = config.getMapList(path + ".attributemodifiers");

        String name;
        Material type = null;
        int data = 0;

        // lore
        List<String> lore = new ArrayList<>();
        if (cLore != null) {
            for (String s : cLore) {
                lore.add(ChatColor.translateAlternateColorCodes('&', s));
            }
        }

        // name
        name = ChatColor.translateAlternateColorCodes('&', cName);

        // material
        type = Material.matchMaterial(cType);
        if (type == null) {
            type = Material.STONE;
        }

        ItemStack is = new ItemStack(type, 1, (short) data);
        ItemMeta ism = is.getItemMeta();
        ism.setLore(lore);
        ism.setDisplayName(name);

        // custom model data
        if (hasCustomModelData) {
            ism.setCustomModelData(customModelData);
        }

        // attribute modifiers
        if (hasAttributeModifiers) {
            for (Map<?, ?> attr : cAttributeModifiers) {
                String cAttribute = (String) attr.get("attribute");
                Attribute attribute = null;
                for (Attribute enumattr : Attribute.values()) {
                    if (enumattr.toString().equals(cAttribute)) {
                        attribute = enumattr;
                        break;
                    }
                }

                if (attribute == null) continue;

                Map<?, ?> configurationSection = (Map<?, ?>) attr.get("modifier");

                String cUUID = (String) configurationSection.get("uuid");
                String cModifierName = (String) configurationSection.get("name");
                String cModifierOperation = (String) configurationSection.get("operation");
                double cAmount;
                try {
                    Object cAmountObj = configurationSection.get("amount");
                    if (cAmountObj instanceof Integer) {
                        cAmount = ((Integer) cAmountObj).doubleValue();
                    } else {
                        cAmount = (Double) cAmountObj;
                    }
                } catch (Exception e) {
                    cAmount = 1;
                }
                String cEquipmentSlot = (String) configurationSection.get("equipmentslot");

                UUID uuid = null;
                if (cUUID != null) {
                    try {
                        uuid = UUID.fromString(cUUID);
                    } catch (Exception ignored) {
                        // ignored
                    }
                }
                EquipmentSlot equipmentSlot = null;
                if (cEquipmentSlot != null) {
                    try {
                        equipmentSlot = EquipmentSlot.valueOf(cEquipmentSlot);
                    } catch (Exception ignored) {
                        // ignored
                    }
                }
                AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_NUMBER;
                try {
                    operation = AttributeModifier.Operation.valueOf(cModifierOperation);
                } catch (Exception ignored) {
                    // ignored
                }

                AttributeModifier modifier;
                if (uuid == null) {
                    modifier = new AttributeModifier(cModifierName, cAmount, operation);
                } else if (equipmentSlot == null) {
                    modifier = new AttributeModifier(uuid, cModifierName, cAmount, operation);
                } else {
                    modifier = new AttributeModifier(uuid, cModifierName, cAmount, operation, equipmentSlot);
                }

                ism.addAttributeModifier(attribute, modifier);
            }
        }

        // item flags
        if (config.isSet(path + ".itemflags")) {
            for (String flag : cItemFlags) {
                for (ItemFlag iflag : ItemFlag.values()) {
                    if (iflag.toString().equals(flag)) {
                        ism.addItemFlags(iflag);
                        break;
                    }
                }
            }
        }


        // unbreakable
        ism.setUnbreakable(unbreakable);

        // enchantments
        if (config.isSet(path + ".enchantments")) {
            for (String key : config.getStringList(path + ".enchantments")) {
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

        is.setItemMeta(ism);
        return is;
    }

    private Map<UUID, Long> cooldowns = new HashMap<>();

    private String id;
    private ItemStack itemStack;
    private List<String> commands;
    private List<String> messages;
    private boolean consumed;
    private int cooldown;
    private String sound;
    private List<String> permittedWorlds;
    private RegionCondition regionCondition;

    public Item(String id, ItemStack itemStack, List<String> commands, List<String> messages, boolean consumed, int cooldown,
                String sound, List<String> permittedWorlds, RegionCondition regionCondition) {
        this.id = id;
        this.itemStack = itemStack;
        this.commands = commands;
        this.messages = messages;
        this.consumed = consumed;
        this.cooldown = cooldown;
        this.sound = sound;
        this.permittedWorlds = permittedWorlds;
        this.regionCondition = regionCondition;
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

    public List<String> getPermittedWorlds() {
        return permittedWorlds;
    }

    public RegionCondition getRegionCondition() {
        return regionCondition;
    }

    @Override
    public String toString() {
        return id;
    }

}
