package ru.hogeltbellai.prison.api.menu;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.items.ItemsAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MenuAPI implements Listener {

    private static final Map<UUID, CustomMenu> customMenuMap = new HashMap<>();

    public static void createMenu(Player player, String title, int size) {
        CustomMenu customMenu = customMenuMap.get(player.getUniqueId());
        Inventory inventory;
        if (customMenu == null || !customMenu.getTitle().equals(title)) {
            inventory = Bukkit.createInventory(player, size, title);
            customMenu = new CustomMenu(title, inventory);
            customMenuMap.put(player.getUniqueId(), customMenu);
        } else {
            inventory = customMenu.getInventory();
        }
        player.openInventory(inventory);
    }

    public static void setMenuItem(Player player, String title, int slot, ItemStack itemStack, Runnable runnable) {
        CustomMenu customMenu = customMenuMap.get(player.getUniqueId());
        if(customMenu != null && customMenu.getTitle().equals(title)) {
            customMenu.setItem(slot, itemStack, runnable);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if(clickedInventory != null && clickedInventory.getHolder() instanceof Player) {
            String inventoryTitle = player.getOpenInventory().getTitle();
            CustomMenu customMenu = customMenuMap.get(player.getUniqueId());

            if(customMenu != null && customMenu.getTitle().equals(inventoryTitle)) {
                event.setCancelled(true);

                int slot = event.getRawSlot();
                customMenu.handleClick(slot);
            }
        }
    }

    @Setter
    @Getter
    private static class CustomMenu {

        private String title;
        private Inventory inventory;
        private Map<Integer, Runnable> integerRunnableMap;

        public CustomMenu(String title, Inventory inventory) {
            this.title = title;
            this.inventory = inventory;
            this.integerRunnableMap = new HashMap<>();
        }

        public void setItem(int slot, ItemStack item, Runnable action) {
            inventory.setItem(slot, item);
            integerRunnableMap.put(slot, action);
        }

        public void handleClick(int slot) {
            Runnable action = integerRunnableMap.get(slot);
            if(action != null) {
                action.run();
            }
        }
    }
}
