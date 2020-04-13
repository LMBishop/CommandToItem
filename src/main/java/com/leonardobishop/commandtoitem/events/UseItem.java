package com.leonardobishop.commandtoitem.events;

import com.leonardobishop.commandtoitem.CommandToItem;
import com.leonardobishop.commandtoitem.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class UseItem implements Listener {

    private CommandToItem plugin;

    public UseItem(CommandToItem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType() != Material.AIR) {
            ItemStack is = player.getItemInHand();

            Item item = null;
            for (Item i : plugin.getItems()) {
                if (i.compare(is)) {
                    item = i;
                    break;
                }
            }

            if (item != null) {
                event.setCancelled(true);

                if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                    return;
                }

                // cooldown
                if (item.isOnCooldown(event.getPlayer())) {
                    event.getPlayer().sendMessage(plugin.getMessage(CommandToItem.Message.COOLDOWN)
                            .replace("%cooldown%", String.valueOf(item.getCooldownFor(event.getPlayer()))));
                    return;
                }

                // condition: world
                if (item.getPermittedWorlds() != null && !item.getPermittedWorlds().isEmpty()) {
                    if (!item.getPermittedWorlds().contains(player.getWorld().getName())) {
                        event.getPlayer().sendMessage(plugin.getMessage(CommandToItem.Message.WORLD_NOT_PERMITTED));
                        return;
                    }
                }

                // condition: region
                if (item.getRegionCondition() != null) {
                    if (!item.getRegionCondition().validate(player.getLocation())) {
                        event.getPlayer().sendMessage(plugin.getMessage(CommandToItem.Message.REGION_NOT_PERMITTED));
                        return;
                    }
                }

                // condition: target player
                if (item.getRegionCondition() != null) {
                    if (!item.getRegionCondition().validate(player.getLocation())) {
                        event.getPlayer().sendMessage(plugin.getMessage(CommandToItem.Message.REGION_NOT_PERMITTED));
                        return;
                    }
                }


                // consumption
                if (item.isConsumed()) {
                    if (is.getAmount() == 1) {
                        player.setItemInHand(null);
                    } else {
                        int newAmount = is.getAmount() - 1;
                        is.setAmount(newAmount);
                        player.setItemInHand(is);
                    }
                    player.updateInventory();
                }

                item.executeUseActions(player);
            }
        }
    }

}
