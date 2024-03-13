package ru.hogeltbellai.prison.api.newtask;

import java.math.BigDecimal;
import java.util.Map;

public abstract class TaskAbstract {
    abstract Map<String, Integer> getBlocks();
    abstract BigDecimal getMoney();
}
