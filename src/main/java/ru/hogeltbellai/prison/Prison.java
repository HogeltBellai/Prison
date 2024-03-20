package ru.hogeltbellai.prison;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.menu.MenuAPI;
import ru.hogeltbellai.prison.api.mine.MineAPI;
import ru.hogeltbellai.prison.api.task.ItemTaskAPI;
import ru.hogeltbellai.prison.api.task.PlayerTaskAPI;
import ru.hogeltbellai.prison.commands.Help_Command;
import ru.hogeltbellai.prison.commands.LevelUP_Command;
import ru.hogeltbellai.prison.commands.Shop_Command;
import ru.hogeltbellai.prison.commands.Upgrade_Command;
import ru.hogeltbellai.prison.commands.admin.Admin_Command;
import ru.hogeltbellai.prison.listener.BlockListener;
import ru.hogeltbellai.prison.listener.PlayerListener;
import ru.hogeltbellai.prison.listener.SellListener;
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

    @Override
    public void onEnable() {
        instance = this;

        config = new ConfigAPI("config");
        new ConfigAPI("levels");
        new ConfigAPI("menus");
        new ConfigAPI("items");
        new ConfigAPI("mines");
        new ConfigAPI("blocks");
        new ConfigAPI("upgrades");

        new PlayerTaskAPI().loadConfig("levels");
        new ItemTaskAPI().loadConfig("upgrades");

        new SQLFileReader().saveFile("prison");

        initializeDatabase();

        new PrisonPlaceholder().register();
        new LevelUP_Command();
        new Upgrade_Command();
        new Shop_Command();
        new Help_Command();

        new Admin_Command();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new SellListener(), this);

        getServer().getPluginManager().registerEvents(new MenuAPI(), this);

        new MineAPI().new MineFillTask().runTaskTimer(this, 0, getConfig().getInt("prison.mine.time") * 1200L);
    }

    @Override
    public void onDisable() {
        if(config.getConfig().getBoolean("storage.enable")) {
            getDatabase().disconnect();
        }
    }

    public void initializeDatabase() {
        if (getConfig().getBoolean("storage.enable")) {
            database = new Database(
                    getConfig().getString("storage.jdbcUrl"),
                    getConfig().getString("storage.username"),
                    getConfig().getString("storage.password"),
                    getConfig().getInt("storage.poolMax")
            );
            readSQLFileReader();
        }
    }

    private void readSQLFileReader() {
        for (String sqlCommand : new SQLFileReader().readerFile("prison")) {
            getDatabase().query(sqlCommand);
        }
    }
}
