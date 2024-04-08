package ru.hogeltbellai.prison.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.chatcolor.ChatColorAPI;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.entity.CustomPet;
import ru.hogeltbellai.prison.api.menu.MenuConfigAPI;
import ru.hogeltbellai.prison.api.pet.PetAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Pets_Command implements CommandExecutor, Listener {
    Inventory menu;
    ArrayList<Integer> pets_slots;
    ArrayList<Integer> empty_slots;
    HashMap<Player, ArrayList<String>> pets_cur = new HashMap<>();

    public Pets_Command() {
        Prison.getInstance().getCommand("pets").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) { return true; }

        Player player = (Player) sender;

        if(args.length == 0) {
            FileConfiguration cfg = new ConfigAPI("pets").getConfig();
            Integer size = cfg.getInt("menu.size")*9;
            menu = Bukkit.createInventory(player, size, "Питомцы");

            String empty_char = cfg.getString("menu.empty_slots.symbol");
            String pets_char = cfg.getString("menu.pets_slots.symbol");
            String left_char = cfg.getString("menu.left_slot.symbol");
            String right_char = cfg.getString("menu.right_slot.symbol");
            String close_char = cfg.getString("menu.close_slot.symbol");

            String empty_item = cfg.getString("menu.empty_slots.item");
            String left_item = cfg.getString("menu.left_slot.item");
            String right_item = cfg.getString("menu.right_slot.item");
            String close_item = cfg.getString("menu.close_slot.item");

            pets_slots = new ArrayList<>();
            ArrayList<String> curpets = new ArrayList<>();
            empty_slots = new ArrayList<>();
            Integer left_slot = 0;
            Integer right_slot = 0;
            Integer close_slot = 0;
            ArrayList<String> pets = Prison.getInstance().getDatabase().getStringList("SELECT pet FROM users_pets WHERE player_id = ?",  new PlayerAPI().getId(player));
            List<String> template = cfg.getStringList("menu.template");
            for (int j = 0; j < template.size(); j++) {
                String s = template.get(j);
                for (int i = 0; i < s.length(); i++) {
                    if (String.valueOf(s.charAt(i)).equals(pets_char)) { pets_slots.add(9*j+i); }
                    if (String.valueOf(s.charAt(i)).equals(empty_char)) { empty_slots.add(9*j+i); }
                    if (String.valueOf(s.charAt(i)).equals(left_char)) { left_slot = 9*j+i; }
                    if (String.valueOf(s.charAt(i)).equals(right_char)) { right_slot = 9*j+i; }
                    if (String.valueOf(s.charAt(i)).equals(close_char)) { close_slot = 9*j+i; }
                }
            }
            Integer max = 0;
            if (pets.size() > pets_slots.size()) { max = pets_slots.size(); } else { max = pets.size(); }

            for (int i = 0; i < max; i++) {
                ItemStack head = new PetAPI(cfg.getConfigurationSection("pets."+pets.get(i))).getHeadItem();
                ItemMeta meta = head.getItemMeta();
                String name = new ChatColorAPI().getColoredString(new PetAPI(cfg.getConfigurationSection("pets."+pets.get(i))).getName()).replace("%player%", player.getName());
                meta.setDisplayName(name);
                ArrayList<String> lore = new ArrayList<>();

                if (new PlayerAPI().getPet(player).equals(pets.get(i))) {
                    lore.add("Использовано");
                } else {
                    lore.add("Нажмите ПКМ чтобы надеть");
                }
                meta.setLore(lore);
                head.setItemMeta(meta);
                menu.setItem(pets_slots.get(i), head);
                curpets.add(pets.get(i));
            }
            pets_cur.put(player, curpets);
            for (int i = 0; i < empty_slots.size(); i++) {
                menu.setItem(empty_slots.get(i), new ItemStack(Material.valueOf(empty_item)));
            }
            menu.setItem(left_slot, new ItemStack(Material.valueOf(left_item)));
            menu.setItem(right_slot, new ItemStack(Material.valueOf(right_item)));
            menu.setItem(close_slot, new ItemStack(Material.valueOf(close_item)));
            player.openInventory(menu);
        }
        return false;
    }

    @EventHandler
    public void OnClickEvent(InventoryClickEvent e) {
        if (e.getView().getTitle() == "Питомцы") {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            if(!(empty_slots.contains(e.getSlot()) || e.getCurrentItem() == null)) {
                new PlayerAPI().setPet(player, pets_cur.get(player).get(pets_slots.indexOf(e.getSlot())));
                CustomPet.removePetForPlayer(player);
                PetAPI petAPI = Prison.getInstance().getPet().loadPetData(new PlayerAPI().getPet(player));
                Prison.getInstance().getPet().spawnPet(player, petAPI);
                player.closeInventory();
                player.sendMessage(new ChatColorAPI().getColoredString(petAPI.getName()+" был надет.").replace("%player%", player.getName()));
            }
        }
    }
}
