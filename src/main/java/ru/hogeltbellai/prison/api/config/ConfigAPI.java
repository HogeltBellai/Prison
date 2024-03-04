package ru.hogeltbellai.prison.api.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ru.hogeltbellai.prison.Prison;

import java.io.File;
import java.io.IOException;

@Getter
public class ConfigAPI {

    public File configFile;
    public FileConfiguration config;

    public ConfigAPI(String nameConfig) {
        configFile = new File(Prison.getInstance().getDataFolder(), nameConfig + ".yml");
        if (!configFile.exists()) {
            Prison.getInstance().saveResource(nameConfig + ".yml", false);
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
