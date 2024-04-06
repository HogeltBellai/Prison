package ru.hogeltbellai.prison;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.entity.CustomPet;
import ru.hogeltbellai.prison.api.menu.MenuAPI;
import ru.hogeltbellai.prison.api.mine.MineAPI;
import ru.hogeltbellai.prison.api.pet.Pet;
import ru.hogeltbellai.prison.api.pet.PetAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;
import ru.hogeltbellai.prison.api.task.ItemTaskAPI;
import ru.hogeltbellai.prison.api.task.PlayerTaskAPI;
import ru.hogeltbellai.prison.commands.*;
import ru.hogeltbellai.prison.commands.admin.Admin_Command;
import ru.hogeltbellai.prison.listener.BlockListener;
import ru.hogeltbellai.prison.listener.PetListener;
import ru.hogeltbellai.prison.listener.PlayerListener;
import ru.hogeltbellai.prison.listener.SellListener;
import ru.hogeltbellai.prison.placeholder.PrisonPlaceholder;
import ru.hogeltbellai.prison.storage.Database;
import ru.hogeltbellai.prison.storage.SQLFileReader;
import ru.hogeltbellai.prison.utils.CaseManager;

/**
 * Programming by HogeltBellai
 * Site: hogeltbellai.ru
 */
public class Prison extends JavaPlugin {

    @Getter public static Prison instance;
    @Getter public Database database;
    @Getter public LuckPerms luckPermsAPI;
    @Getter public CaseManager caseManager;
    @Getter public Pet pet;
    @Getter public ProtocolManager protocolManager;

    ConfigAPI config;

    @Override
    public void onEnable() {
        instance = this;

        config = new ConfigAPI("config");
        caseManager = new CaseManager();
        pet = new Pet();
        protocolManager = ProtocolLibrary.getProtocolManager();
        MineAPI mineAPI = new MineAPI();

        new ConfigAPI("levels");
        new ConfigAPI("menus");
        new ConfigAPI("items");
        new ConfigAPI("mines");
        new ConfigAPI("blocks");
        new ConfigAPI("upgrades");
        new ConfigAPI("pets");

        new PlayerTaskAPI().loadConfig("levels");
        new ItemTaskAPI().loadConfig("upgrades");

        new SQLFileReader().saveFile("prison");

        getLuckPerms();
        initializeDatabase();

        new PrisonPlaceholder().register();

        new Pay_Command();
        new Menu_Command();
        new Mine_Command();
        new LevelUP_Command();
        new Upgrade_Command();
        new Shop_Command();
        new Help_Command();
        new Fraction_Command();
        new Spawn_Command();
        new Admin_Command();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new SellListener(), this);
        getServer().getPluginManager().registerEvents(new PetListener(), this);

        getServer().getPluginManager().registerEvents(new MenuAPI(), this);

        MineAPI.MineFillTask mineFillTask = new MineAPI.MineFillTask(mineAPI);
        mineFillTask.runTaskTimerAsynchronously(Prison.getInstance(), 0, Prison.getInstance().getConfig().getInt("prison.mine.time") * 1200L);
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

    public void getLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPermsAPI = provider.getProvider();
        }
    }
}
