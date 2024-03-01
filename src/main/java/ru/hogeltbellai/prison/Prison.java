package ru.hogeltbellai.prison;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
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
    public ConfigAPI configAPI;
    @Getter public Database database;

    @Override
    public void onEnable() {
        instance = this;
        configAPI = new ConfigAPI(this, "config");

        new SQLFileReader().saveFile("prison.sql");

        if(configAPI.getConfig().getBoolean("storage.enable")) {
            database = new Database(configAPI.getConfig().getString("storage.jdbcUrl"), configAPI.getConfig().getString("storage.username"), configAPI.getConfig().getString("storage.password"));
            String[] sqlCommands = new SQLFileReader().readerFile("prison.sql");

            for (String sqlCommand : sqlCommands) {
                getDatabase().query(sqlCommand);
            }
        }

        new PrisonPlaceholder().register();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        if(configAPI.getConfig().getBoolean("storage.enable")) {
            getDatabase().disconnect();
        }
    }
}
