package ru.hogeltbellai.prison.storage;

import ru.hogeltbellai.prison.Prison;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SQLFileReader {

    public String[] readerFile(String fileName) {
        List<String> sqlCommandsList = new LinkedList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(Prison.getInstance().getDataFolder() + "/" + fileName + ".sql"))) {
            String sqlCommands = bufferedReader.lines().collect(Collectors.joining("\n"));
            sqlCommandsList = Arrays.asList(sqlCommands.split(";"));
        } catch (IOException ex) {
            System.err.println("Ошибка: " + ex.getMessage());
        }

        return sqlCommandsList.toArray(new String[0]);
    }

    public void saveFile(String fileName) {
        File pluginFolder = Prison.getInstance().getDataFolder().getParentFile();
        File file = new File(pluginFolder, Prison.getInstance().getName() + "/" + fileName + ".sql");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
