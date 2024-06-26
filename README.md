# Prison

# Плагин + API для LootFarmig.fun
Игровой режим Prison на базе ядра Spigot 1.16.5
Содержит удобный API для внешнего использования

# PlaceholderAPI
Все возможные плейсхолдеры игрока
```
%prison_level% - Уровень игрока
%prison_block% - Блоки игрока
%prison_money% - Денег у игрока
%prison_fraction% - Фракция игрока
%prison_block_level% - Задания для повышения уровня (Блоки)
%prison_money_level% - Задания для повышения уровня (Деньги)
%prison_block_upgrade% - Задания для повышения уровня (Блоки)
%prison_money_upgrade% - Задания для повышения уровня (Деньги)
```

# API use
Получение главного класса
```
Prison.getInstance()
```

Получение класса Database
```
Prison.getInstance().getDatabase()
```

# Создание предмета через ItemsAPI
Получение ItemsAPI и его билдера
```
new ItemsAPI.Builder()
```

Так же имеются разные настройки кастомного предмета
```
.material(Material)
.displayName(String)
.lore(String...)
.hideFlags()
```

Для того что бы забилдить предмет
```
.build()
```

Наглядный пример тестового предмета и его настройка
```
new ItemsAPI.Builder().material(Material.STICK).displayName("&eМой предмет").lore("&cОписание 1", "&eОписание 2").hideFlags().build();
```
