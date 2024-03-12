package ru.hogeltbellai.prison.api.mine;

import lombok.Getter;
import org.bukkit.Location;

import java.util.List;

@Getter
public class Mine {

    private final String name;
    private final Location pos1;
    private final Location pos2;
    private final List<String> blockChances;

    public Mine(String name, Location pos1, Location pos2, List<String> blockChances) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.blockChances = blockChances;
    }
}
