package ru.hogeltbellai.prison.api.newtask;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
public class TaskConfiguration extends TaskAbstract {

    private final Map<String, Integer> blocks;
    private final BigDecimal money;
    private final String configName;

    public TaskConfiguration(String configName, Map<String, Integer> blocks, BigDecimal money) {
        this.configName = configName;
        this.blocks = blocks;
        this.money = money;
    }
}
