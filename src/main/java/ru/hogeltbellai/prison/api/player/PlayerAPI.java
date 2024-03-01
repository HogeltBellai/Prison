package ru.hogeltbellai.prison.api.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;

import java.math.BigDecimal;

public class PlayerAPI implements PlayerInterface {

    @Override
    public BigDecimal getMoney(Player player) {
        return Prison.getInstance().getDatabase().getVaule("SELECT money FROM users WHERE name = ?", BigDecimal.class, player.getName());
    }

    @Override
    public int getBlock(Player player) {
        return Prison.getInstance().getDatabase().getVaule("SELECT blocks FROM users WHERE name = ?", Integer.class, player.getName());
    }
}
