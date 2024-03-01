package ru.hogeltbellai.prison;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.storage.Database;
import ru.hogeltbellai.prison.storage.SQLFileReader;

/**
 * Programming by HogeltBellai
 * Site: hogeltbellai.ru
 */
@Getter
public final class Prison extends JavaPlugin {

    public Prison instance;
    public ConfigAPI configAPI;
    public Database database;

    @Override
    public void onEnable() {
        instance = this;
        configAPI = new ConfigAPI(this, "config");

        new SQLFileReader(this).saveFile("prison.sql");

        if(configAPI.getConfig().getBoolean("storage.enable")) {
            database = new Database(configAPI.getConfig().getString("storage.jdbcUrl"), configAPI.getConfig().getString("storage.username"), configAPI.getConfig().getString("storage.password"));
            String[] sqlCommands = new SQLFileReader(this).readerFile(getInstance().getDataFolder() + "/prison.sql");

            for (String sqlCommand : sqlCommands) {
                getDatabase().query(sqlCommand);
            }
        }
    }

    @Override
    public void onDisable() {
        if(configAPI.getConfig().getBoolean("storage.enable")) {
            getDatabase().disconnect();
        }
    }
}
