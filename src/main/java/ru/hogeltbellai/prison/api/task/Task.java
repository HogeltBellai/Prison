package ru.hogeltbellai.prison.api.task;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class Task {
    private final Map<String, Integer> blocks;
    private final BigDecimal money;

    public Task(Map<String, Integer> blocks, BigDecimal money) {
        this.blocks = blocks;
        this.money = money;
    }
}
