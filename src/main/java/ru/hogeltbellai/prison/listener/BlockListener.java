package ru.hogeltbellai.prison.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ru.hogeltbellai.prison.api.mine.MineAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        new MineAPI().getAllMines().forEach(mine -> {
            if(new MineAPI().inMineBlock(event.getBlock(), mine.getName())) {
                if (player.getGameMode() != GameMode.CREATIVE) {
                    new PlayerAPI().setBlock(player, 1);

                    player.getInventory().addItem(new ItemStack(event.getBlock().getType()));
                    event.setDropItems(false);
                    event.setExpToDrop(0);
                }
            }
        });
    }
}
