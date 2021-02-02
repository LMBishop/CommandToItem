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
                if (item.isOnCooldown(event.getPlayer())) {
                    event.getPlayer().sendMessage(plugin.getMessage(CommandToItem.Message.COOLDOWN)
                            .replace("%cooldown%", String.valueOf(item.getCooldownFor(event.getPlayer()))));
                    return;
                }
                if (item.isPermissionRequired() && !event.getPlayer().hasPermission("commandtoitem.use." + item.getId())) {
                    event.getPlayer().sendMessage(plugin.getMessage(CommandToItem.Message.NO_PERMISSION));
                    return;
                }
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
