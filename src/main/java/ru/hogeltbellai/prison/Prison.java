package ru.hogeltbellai.prison;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.config.menu.MenuAPI;
import ru.hogeltbellai.prison.commands.LevelUP_Command;
import ru.hogeltbellai.prison.listener.PlayerListener;
import ru.hogeltbellai.prison.placeholder.PrisonPlaceholder;
import ru.hogeltbellai.prison.storage.Database;
import ru.hogeltbellai.prison.storage.SQLFileReader;

/**
 * Programming by HogeltBellai
 * Site: hogeltbellai.ru
 */
public class Prison extends JavaPlugin {

    @Getter public static Prison instance;
    @Getter public Database database;

    ConfigAPI config;
    ConfigAPI configLevel;

    @Override
    public void onEnable() {
        instance = this;

        config = new ConfigAPI("config");
        configLevel = new ConfigAPI("levels");

        new SQLFileReader().saveFile("prison.sql");

        if(config.getConfig().getBoolean("storage.enable")) {
            database = new Database(config.getConfig().getString("storage.jdbcUrl"), config.getConfig().getString("storage.username"), config.getConfig().getString("storage.password"));
            String[] sqlCommands = new SQLFileReader().readerFile("prison.sql");

            for (String sqlCommand : sqlCommands) {
                getDatabase().query(sqlCommand);
            }
        }

        new PrisonPlaceholder().register();

        new LevelUP_Command();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new MenuAPI(), this);
    }

    @Override
    public void onDisable() {
        if(config.getConfig().getBoolean("storage.enable")) {
            getDatabase().disconnect();
        }
    }
}
