package com.tryfx.strengthsmp;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class BookListener implements Listener {

    private final StrengthSMP plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public BookListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Only handle main-hand right-click to avoid double-firing (once per hand)
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return;
        if (!plugin.getStrengthBook().isStrengthBook(item)) return;

        event.setCancelled(true); // prevent normal enchanted book use (none exists, but be safe)

        Player player = event.getPlayer();
        StrengthManager sm = plugin.getStrengthManager();
        String prefix = plugin.getConfig().getString("messages.prefix", "");

        if (sm.isAtMax(player)) {
            String msg = plugin.getConfig().getString("messages.on-book-consume-capped", "")
                    .replace("%max%", String.valueOf(sm.getMax()));
            player.sendMessage(MM.deserialize(prefix + msg));
            return; // book is NOT consumed if it would have no effect
        }

        // Consume one book
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        sm.addStrength(player, 1);

        String msg = plugin.getConfig().getString("messages.on-book-consume", "")
                .replace("%strength%", formatStrength(sm.getStrength(player)));
        player.sendMessage(MM.deserialize(prefix + msg));
    }

    private String formatStrength(int value) {
        return (value > 0 ? "+" : "") + value;
    }
}
