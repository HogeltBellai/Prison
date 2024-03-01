# Prison

# Плагин + API для LootFarmig.fun
Игровой режим Prison на базе ядра Spigot 1.16.5
Содержит удобный API для внешнего использования

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
