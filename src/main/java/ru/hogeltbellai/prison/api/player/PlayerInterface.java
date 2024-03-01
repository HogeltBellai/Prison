package ru.hogeltbellai.prison.api.player;

import org.bukkit.entity.Player;

import java.math.BigDecimal;

public interface PlayerInterface {

    public void setLevel(Player player, int level);

    public void setMoney(Player player, BigDecimal money);

    public void setBlock(Player player, int block);

    public void setFraction(Player player, String fraction);

    public int getLevel(Player player);

    public BigDecimal getMoney(Player player);

    public int getBlock(Player player);

    public String getFraction(Player player);
}
