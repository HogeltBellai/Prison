package ru.hogeltbellai.prison.listener;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.hogeltbellai.prison.api.block.BlockAPI;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.message.MessageAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;
import ru.hogeltbellai.prison.event.BlockSellEvent;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class SellListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign) e.getClickedBlock().getState();
                if (sign.getLine(1).equals("§a[Продать]")) {
                    Bukkit.getServer().getPluginManager().callEvent(new BlockSellEvent(e.getPlayer()));
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).equalsIgnoreCase("%sell")) {
            event.setLine(0, "");
            event.setLine(1, "§a[Продать]");
            event.setLine(2, "§2Нажми что бы продать");
        }
    }

    @EventHandler
    public void onSellBlock(BlockSellEvent event) {
        Player player = event.getPlayer();
        sellBlocks(player);
    }

    public void sellBlocks(Player player) {
        DecimalFormat df = new DecimalFormat("0.00");
        Map<String, Double> blockPrices = new BlockAPI().getAllBlockPrices();
        Inventory inventory = player.getInventory();

        List<ItemStack> items = new ArrayList<>();
        items.addAll(Arrays.asList(inventory.getContents()));
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (offHandItem != null && offHandItem.getType() != Material.AIR) {
            items.add(offHandItem);
        }

        double totalPrice = items.stream()
                .filter(item -> item != null && blockPrices.containsKey(item.getType().name()))
                .mapToDouble(item -> {
                    double pricePerBlock = blockPrices.get(item.getType().name());
                    double amount = item.getAmount();
                    if (item.equals(offHandItem)) {
                        player.getInventory().setItemInOffHand(null);
                    } else {
                        inventory.removeItem(item);
                    }
                    return amount * pricePerBlock;
                })
                .sum();

        if (totalPrice > 0) {
            String formattedPrice = df.format(totalPrice).replace(",", ".");
            BigDecimal money = new BigDecimal(formattedPrice);
            double booster = new PlayerAPI().getBooster(player);
            BigDecimal totalMoney = money.multiply(BigDecimal.valueOf(booster));
            String formattedTotalMoney = df.format(totalMoney);

            new PlayerAPI().setMoney(player, "+", totalMoney);

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.sell.sell_block")
                    .replace("%money%", formattedTotalMoney)
                    .replace("%booster%", String.valueOf(booster))));
        }
    }
}
