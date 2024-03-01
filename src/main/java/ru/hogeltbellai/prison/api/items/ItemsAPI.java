package ru.hogeltbellai.prison.api.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.hogeltbellai.prison.api.chatcolor.ChatColorAPI;

public class ItemsAPI {
    private ItemStack item;

    public ItemsAPI() {
        item = new ItemStack(Material.STONE);
    }

    public static class Builder {
        private final ItemsAPI itemsAPI;

        public Builder() {
            itemsAPI = new ItemsAPI();
        }

        public Builder material(Material material) {
            itemsAPI.item.setType(material);
            return this;
        }

        public Builder displayName(String displayName) {
            ItemMeta meta = itemsAPI.item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(new ChatColorAPI().getColoredString(displayName));
            itemsAPI.item.setItemMeta(meta);
            return this;
        }

        public Builder lore(String... lore) {
            ItemMeta meta = itemsAPI.item.getItemMeta();
            assert meta != null;
            meta.setLore(new ChatColorAPI().getColoredStrings(lore));
            itemsAPI.item.setItemMeta(meta);
            return this;
        }

        public Builder hideFlags() {
            ItemMeta meta = itemsAPI.item.getItemMeta();
            assert meta != null;
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.setUnbreakable(true);
            itemsAPI.item.setItemMeta(meta);
            return this;
        }

        public ItemsAPI build() {
            return itemsAPI;
        }
    }
}
