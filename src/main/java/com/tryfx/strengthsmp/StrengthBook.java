package com.tryfx.strengthsmp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds and identifies the Strength Knowledge Book item.
 */
public final class StrengthBook {

    private final StrengthSMP plugin;
    private final NamespacedKey bookKey;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public StrengthBook(StrengthSMP plugin) {
        this.plugin = plugin;
        this.bookKey = new NamespacedKey(plugin, "strength_knowledge_book");
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();

        String nameStr = plugin.getConfig().getString("book.name", "<gold><bold>Strength Knowledge Book");
        meta.displayName(MM.deserialize(nameStr).decoration(TextDecoration.ITALIC, false));

        List<String> loreLines = plugin.getConfig().getStringList("book.lore");
        List<Component> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(MM.deserialize(line).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);

        meta.getPersistentDataContainer().set(bookKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isStrengthBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        Byte flag = meta.getPersistentDataContainer().get(bookKey, PersistentDataType.BYTE);
        return flag != null && flag == 1;
    }
}
