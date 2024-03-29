package ru.hogeltbellai.prison.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.hogeltbellai.prison.api.items.ItemsConfigAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

public class PodvalManager {

    public void getPodval(Player player) {
        if (player.getItemInHand() != null) {
            ItemStack heldItem = player.getItemInHand();
            String heldItemName = ItemsConfigAPI.getItemNameByMaterial(heldItem);
            if (heldItemName != null && heldItemName.equalsIgnoreCase("podval")) {
                if(!new PlayerAPI().hasPodval(player)) {
                    new PlayerAPI().setPodval(player, 1);
                        heldItem.setAmount(heldItem.getAmount() - 1);
                        if (heldItem.getAmount() <= 0) {
                            player.getInventory().removeItem(heldItem);
                        } else {
                            player.getInventory().setItemInMainHand(heldItem);
                        }
                } else {
                    player.sendMessage("У вас уже есть подвал");
                }
            }
        }
    }
}
