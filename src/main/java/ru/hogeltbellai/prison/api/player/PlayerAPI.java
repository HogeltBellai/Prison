package ru.hogeltbellai.prison.api.player;

import org.bukkit.entity.Player;
import ru.hogeltbellai.prison.Prison;

import java.math.BigDecimal;
import java.sql.ResultSet;

public class PlayerAPI implements PlayerInterface {

    @Override
    public int getId(Player player) {
        return Prison.getInstance().getDatabase().getVaule("SELECT id FROM users WHERE name = ?", Integer.class, player.getName());
    }

    @Override
    public void setLevel(Player player, String math, int level) {
        Prison.getInstance().getDatabase().queryUpdate("UPDATE users SET level = level " + math + " ? WHERE name = ?", level, player.getName());
    }

    @Override
    public void setMoney(Player player, String math, BigDecimal money) {
        Prison.getInstance().getDatabase().query("UPDATE users SET money = money " + math + " ? WHERE name = ?", money, player.getName());
    }

    @Override
    public void setBlock(Player player, int block) {
        Prison.getInstance().getDatabase().queryUpdate("UPDATE users SET blocks = blocks + ? WHERE name = ?", block, player.getName());
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

    @Override
    public void setBlockData(int id, String type, int block) {
        int rowCount = Prison.getInstance().getDatabase().queryUpdate("UPDATE users_blocks SET amount = amount + ? WHERE player_id = ? AND block_type = ?", block, id, type);

        if (rowCount == 0) {
            Prison.getInstance().getDatabase().query("INSERT INTO users_blocks (player_id, block_type, amount) VALUES (?, ?, ?)", id, type, block);
        }
    }

    public int getDataBlock(int id, String blockType) {
        return Prison.getInstance().getDatabase().getVaule("SELECT amount FROM users_blocks WHERE player_id = ? AND block_type = ?", Integer.class, id, blockType);
    }
}
