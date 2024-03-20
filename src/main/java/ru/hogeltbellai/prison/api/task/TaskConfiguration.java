package ru.hogeltbellai.prison.api.task;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Setter
@Getter
public class TaskConfiguration extends TaskAbstract {

    public String itemName;
    public Map<String, Integer> blocks;
    public BigDecimal money;
    public Map<String, Object> enchantments;
    public String material;
    public String displayName;

    public TaskConfiguration(String itemName, Map<String, Integer> blocks, BigDecimal money) {
        this.itemName = itemName;
        this.blocks = blocks;
        this.money = money;
    }
}
