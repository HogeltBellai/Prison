package ru.hogeltbellai.prison.api.player;

import org.bukkit.entity.Player;

import java.math.BigDecimal;

public interface PlayerInterface {

    public int getId(Player player);

    // users
    public void setLevel(Player player, String math, int level);

    public void setMoney(Player player, String math, BigDecimal money);

    public void setBlock(Player player, int block);

    public void setFraction(Player player, String fraction);

    public void setAutosell(Player player, int bool);

    public void setPet(Player player, String pet);

    public void setPodval(Player player, int bool);

    public int getLevel(Player player);

    public BigDecimal getMoney(Player player);

    public int getBlock(Player player);

    public String getFraction(Player player);

    public double getBooster(Player player);

    public boolean hasAutosell(Player player);

    public String getPet(Player player);

    public boolean hasPodval(Player player);

    // users_blocks
    public void setBlockData(int id, String type, int block);

    public int getDataBlock(int id, String blockType);

    void setLastTeleport(Player player);

    int getLastTeleport(Player player);
}
