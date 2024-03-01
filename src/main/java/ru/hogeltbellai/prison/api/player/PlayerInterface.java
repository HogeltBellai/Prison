package ru.hogeltbellai.prison.api.player;

import org.bukkit.entity.Player;

import java.math.BigDecimal;

public interface PlayerInterface {

    public BigDecimal getMoney(Player player);

    public int getBlock(Player player);
}
