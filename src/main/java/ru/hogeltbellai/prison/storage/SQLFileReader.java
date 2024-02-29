package ru.hogeltbellai.prison.storage;

import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SQLFileReader {

    private Plugin pl;

    public SQLFileReader(Plugin pl) {
        this.pl = pl;
    }

    public String[] readerFile(String path) {
        List<String> sqlCommandsList = new LinkedList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            String sqlCommands = bufferedReader.lines().collect(Collectors.joining("\n"));
            sqlCommandsList = Arrays.asList(sqlCommands.split(";"));
        } catch (IOException ex) {
            System.err.println("Ошибка: " + ex.getMessage());
        }

        return sqlCommandsList.toArray(new String[0]);
    }

    public void saveFile(String fileName) {
        File pluginFolder = pl.getDataFolder().getParentFile();
        File file = new File(pluginFolder, pl.getName() + "/" + fileName);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
