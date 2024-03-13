package ru.hogeltbellai.prison.api.items;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.hogeltbellai.prison.api.chatcolor.ChatColorAPI;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ItemsAPI {

    private final ItemStack item;

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

        public Builder lore(List<String> lore) {
            ItemMeta meta = itemsAPI.item.getItemMeta();
            List<String> coloredLore = new ChatColorAPI().getColoredStrings(lore.toArray(new String[0]));
            assert meta != null;
            meta.setLore(coloredLore);
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
