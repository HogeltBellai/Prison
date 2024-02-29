package ru.hogeltbellai.prison.api;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

@Getter
public class ConfigAPI {

    public File configFile;
    public FileConfiguration config;

    public ConfigAPI(Plugin pl, String nameConfig) {
        configFile = new File(pl.getDataFolder(), nameConfig + ".yml");
        if (!configFile.exists()) {
            pl.saveResource(nameConfig + ".yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
