package ru.hogeltbellai.prison.api.player;

import org.bukkit.entity.Player;
import ru.hogeltbellai.prison.Prison;

import java.math.BigDecimal;

public class PlayerAPI implements PlayerInterface {

    @Override
    public void setLevel(Player player, int level) {
        Prison.getInstance().getDatabase().query("UPDATE users SET level = level + ? WHERE name = ?", level, player.getName());
    }

    @Override
    public void setMoney(Player player, BigDecimal money) {
        Prison.getInstance().getDatabase().query("UPDATE users SET money = money + ? WHERE name = ?", money, player.getName());
    }

    @Override
    public void setBlock(Player player, int block) {
        Prison.getInstance().getDatabase().query("UPDATE users SET blocks = blocks + ? WHERE name = ?", block, player.getName());
    }

    @Override
    public void setFraction(Player player, String fraction) {
        Prison.getInstance().getDatabase().query("UPDATE users SET fraction = ? WHERE name = ?", fraction, player.getName());
    }

    @Override
    public int getLevel(Player player) {
        return Prison.getInstance().getDatabase().getVaule("SELECT level FROM users WHERE name = ?", Integer.class, player.getName());
    }

    @Override
    public BigDecimal getMoney(Player player) {
        return Prison.getInstance().getDatabase().getVaule("SELECT money FROM users WHERE name = ?", BigDecimal.class, player.getName());
    }

    @Override
    public int getBlock(Player player) {
        return Prison.getInstance().getDatabase().getVaule("SELECT blocks FROM users WHERE name = ?", Integer.class, player.getName());
    }

    @Override
    public String getFraction(Player player) {
        return Prison.getInstance().getDatabase().getVaule("SELECT fraction FROM users WHERE name = ?", String.class, player.getName());
    }
}
