package ru.hogeltbellai.prison.api.block;

import org.bukkit.Material;
import ru.hogeltbellai.prison.api.config.ConfigAPI;

import java.util.HashMap;
import java.util.Map;

public class BlockAPI {

    ConfigAPI config = new ConfigAPI("blocks");

    public Map<String, Double> getAllBlockPrices() {
        Map<String, Double> blockPrices = new HashMap<>();
        for (String key : config.getConfig().getKeys(false)) {
            blockPrices.put(key, config.getConfig().getDouble(key));
        }
        return blockPrices;
    }

    public int getBlockPrice(String blockName) {
        return config.getConfig().getInt(blockName);
    }
}
