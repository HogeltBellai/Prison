package ru.hogeltbellai.prison.commands;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.chatcolor.ChatColorAPI;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.entity.CustomPet;
import ru.hogeltbellai.prison.api.items.ItemsAPI;
import ru.hogeltbellai.prison.api.menu.MenuAPI;
import ru.hogeltbellai.prison.api.message.MessageAPI;
import ru.hogeltbellai.prison.api.pet.PetAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class Pets_Command implements CommandExecutor, Listener {

    private ArrayList<Integer> petsSlots;
    private ArrayList<Integer> emptySlots;
    private HashMap<Player, ArrayList<String>> playerPets = new HashMap<>();

    public Pets_Command() {
        Prison.getInstance().getCommand("pets").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            FileConfiguration cfg = new ConfigAPI("pets").getConfig();
            int size = cfg.getInt("menu.size") * 9;
            MenuAPI.createMenu(player, "Питомцы", size);

            String emptyChar = cfg.getString("menu.empty_slots.symbol");
            String petsChar = cfg.getString("menu.pets_slots.symbol");
            String leftChar = cfg.getString("menu.left_slot.symbol");
            String rightChar = cfg.getString("menu.right_slot.symbol");
            String closeChar = cfg.getString("menu.close_slot.symbol");

            String emptyItem = cfg.getString("menu.empty_slots.item");
            //String leftItem = cfg.getString("menu.left_slot.item");
            //String rightItem = cfg.getString("menu.right_slot.item");
            //String closeItem = cfg.getString("menu.close_slot.item");

            petsSlots = new ArrayList<>();
            ArrayList<String> currentPets = new ArrayList<>();
            emptySlots = new ArrayList<>();
            int leftSlot = 0;
            int rightSlot = 0;
            int closeSlot = 0;
            ArrayList<String> pets = Prison.getInstance().getDatabase().getStringList("SELECT pet FROM users_pets WHERE player_id = ?", new PlayerAPI().getId(player));
            List<String> template = cfg.getStringList("menu.template");

            for (int j = 0; j < template.size(); j++) {
                String line = template.get(j);
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (String.valueOf(c).equals(petsChar)) {
                        petsSlots.add(9 * j + i);
                    } else if (String.valueOf(c).equals(emptyChar)) {
                        emptySlots.add(9 * j + i);
                    } else if (String.valueOf(c).equals(leftChar)) {
                        leftSlot = 9 * j + i;
                    } else if (String.valueOf(c).equals(rightChar)) {
                        rightSlot = 9 * j + i;
                    } else if (String.valueOf(c).equals(closeChar)) {
                        closeSlot = 9 * j + i;
                    }
                }
            }

            int max = Math.min(pets.size(), petsSlots.size());

            for (int i = 0; i < max; i++) {
                ItemStack head = new PetAPI(cfg.getConfigurationSection("pets." + pets.get(i))).getHeadItem();
                ItemMeta meta = head.getItemMeta();
                String name = new ChatColorAPI().getColoredString(new PetAPI(cfg.getConfigurationSection("pets." + pets.get(i))).getName()).replace("%player%", player.getName());
                meta.setDisplayName(name);
                ArrayList<String> lore = new ArrayList<>();


                if(new PlayerAPI().getPet(player) != null) {
                    if (new PlayerAPI().getPet(player).equals(pets.get(i))) {
                        lore.add("");
                        lore.add("§a§nВЫБРАНО");
                    } else {
                        lore.add("");
                        lore.add("§7§nНажмите, чтобы призвать");
                    }
                } else {
                    lore.add("");
                    lore.add("§7§nНажмите, чтобы призвать");
                }
                meta.setLore(lore);
                head.setItemMeta(meta);
                MenuAPI.setMenuItem(player, "Питомцы", petsSlots.get(i), head, () -> {
                    new PlayerAPI().setPet(player, playerPets.get(player).get(petsSlots.indexOf(MenuAPI.getClickedItem())));
                    CustomPet.removePetForPlayer(player);
                    PetAPI petAPI = Prison.getInstance().getPet().loadPetData(new PlayerAPI().getPet(player));
                    Prison.getInstance().getPet().spawnPet(player, petAPI);
                    player.closeInventory();
                    player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.pets.spawn").replace("%pet%", new ChatColorAPI().getColoredString(petAPI.getName())));
                });
                currentPets.add(pets.get(i));
            }
            playerPets.put(player, currentPets);

            for (int emptySlot : emptySlots) {
                MenuAPI.setMenuItem(player, "Питомцы", emptySlot, new ItemStack(Material.valueOf(emptyItem)), () -> { });
            }
            MenuAPI.setMenuItem(player, "Питомцы", leftSlot, new ItemStack(Material.valueOf(emptyItem)), () -> { });
            MenuAPI.setMenuItem(player, "Питомцы", rightSlot, new ItemStack(Material.valueOf(emptyItem)), () -> { });
            MenuAPI.setMenuItem(player, "Питомцы", closeSlot, new ItemsAPI.Builder().material(Material.BARRIER).displayName("&cУбрать питомца").lore("", "&7&nНажмите, что бы убрать").build().getItem(), () -> {
                CustomPet.removePetForPlayer(player);
                new PlayerAPI().setPet(player, null);
                player.setWalkSpeed(0.2f);
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.pets.remove"));
                player.closeInventory();
            });
        }
        return false;
    }
}