package ru.hogeltbellai.prison.listener;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ru.hogeltbellai.prison.api.mine.MineAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;
import ru.hogeltbellai.prison.utils.CaseManager;

public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        new MineAPI().getAllMines().forEach(mine -> {
            if(new MineAPI().inMineBlock(event.getBlock(), mine.getName())) {
                if (player.getGameMode() != GameMode.CREATIVE) {
                    new PlayerAPI().setBlock(player, 1);
                    new PlayerAPI().setBlockData(new PlayerAPI().getId(player), event.getBlock().getType().toString(), 1);

                    player.getInventory().addItem(new ItemStack(event.getBlock().getType()));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    event.setDropItems(false);
                    event.setExpToDrop(0);

                    CaseManager.dropKey(player);

                    if(new PlayerAPI().hasAutosell(player)) {
                        new SellListener().sellBlocks(player);
                    }
                }
            }
        });
    }
}
