package ru.hogeltbellai.prison.api.mine;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.location.LocationAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MineAPI {

    ConfigAPI config = new ConfigAPI("mines");

    public boolean createMine(Player player, String name, Location pos1, Location pos2, List<String> blockChances) {
        if (config.getConfig().contains(name)) {
            return false;
        }

        ConfigurationSection mineSection = config.getConfig().createSection(name);
        mineSection.set("displayName", "Тестовая шахта");
        mineSection.set("pos1", LocationAPI.serializeLocation(pos1));
        mineSection.set("pos2", LocationAPI.serializeLocation(pos2));
        mineSection.set("level", 1);
        mineSection.set("teleport", LocationAPI.serializeLocationPlayer(player.getLocation()));
        mineSection.set("blockChances", blockChances);
        config.saveConfig();
        fillMine(name);
        return true;
    }

    public Location getTeleportLocation(String name) {
        ConfigurationSection mineSection = config.getConfig().getConfigurationSection(name);
        String teleportLocation = mineSection.getString("teleport");
        return LocationAPI.deserializeLocationPlayer(teleportLocation);
    }

    public String getName(String name) {
        ConfigurationSection mineSection = config.getConfig().getConfigurationSection(name);
        return mineSection.getString("displayName").replace("&", "§");
    }

    public int getLevel(String name) {
        ConfigurationSection mineSection = config.getConfig().getConfigurationSection(name);
        return mineSection.getInt("level");
    }

    public List<Mine> getAllMines() {
        List<Mine> mines = new ArrayList<>();
        for (String name : config.getConfig().getKeys(false)) {
            ConfigurationSection mineSection = config.getConfig().getConfigurationSection(name);
            Location pos1 = LocationAPI.deserializeLocation(mineSection.getString("pos1"));
            Location pos2 = LocationAPI.deserializeLocation(mineSection.getString("pos2"));
            List<String> blockChances = mineSection.getStringList("blockChances");
            mines.add(new Mine(name, pos1, pos2, blockChances));
        }
        return mines;
    }

    public void fillMine(String name) {
        ConfigurationSection mineSection = config.getConfig().getConfigurationSection(name);
        if (mineSection == null) {
            return;
        }
        Location pos1 = LocationAPI.deserializeLocation(mineSection.getString("pos1"));
        Location pos2 = LocationAPI.deserializeLocation(mineSection.getString("pos2"));

        if (pos1 == null || pos2 == null || pos1.getWorld() == null) {
            return;
        }

        World world = pos1.getWorld();
        List<String> blockChances = mineSection.getStringList("blockChances");

        Bukkit.getScheduler().runTask(Prison.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != null && inMinePlayer(player.getLocation(), pos1, pos2)) {
                    int highestY = Math.max(pos1.getBlockY(), pos2.getBlockY());
                    Location playerLocation = player.getLocation();
                    if (playerLocation.getY() < highestY) {
                        player.teleport(new Location(pos1.getWorld(), playerLocation.getX(), highestY + 1, playerLocation.getZ(), playerLocation.getYaw(), playerLocation.getPitch()));
                    }
                }
            }

            // После телепортации игроков запускаем асинхронную задачу для установки блоков
            new BukkitRunnable() {
                @Override
                public void run() {
                    Random random = new Random();
                    for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()); x <= Math.max(pos1.getBlockX(), pos2.getBlockX()); x++) {
                        for (int y = Math.min(pos1.getBlockY(), pos2.getBlockY()); y <= Math.max(pos1.getBlockY(), pos2.getBlockY()); y++) {
                            for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()); z <= Math.max(pos1.getBlockZ(), pos2.getBlockZ()); z++) {
                                if (world.isChunkLoaded(x >> 4, z >> 4)) {
                                    String randomBlock = getRandomBlock(blockChances, random);
                                    if (randomBlock != null) {
                                        try {
                                            Material material = Material.valueOf(randomBlock);
                                            setBlockInNativeWorld(world, x, y, z, material);
                                        } catch (IllegalArgumentException e) {
                                            Bukkit.getLogger().warning("Некорректное имя материала: " + randomBlock);
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }.runTaskAsynchronously(Prison.getInstance());
        });
    }

    private String getRandomBlock(List<String> blockChances, Random random) {
        if (blockChances.isEmpty()) {
            return null;
        }

        List<WeightedBlock> weightedBlocks = new ArrayList<>();
        int totalWeight = 0;

        for (String blockChance : blockChances) {
            String[] parts = blockChance.split(":");
            if (parts.length != 2) {
                continue;
            }
            String blockName = parts[0];
            int weight;
            try {
                weight = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                continue;
            }
            weightedBlocks.add(new WeightedBlock(blockName, weight));
            totalWeight += weight;
        }

        if (totalWeight == 0) {
            return null;
        }

        int randomNumber = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (WeightedBlock weightedBlock : weightedBlocks) {
            cumulativeWeight += weightedBlock.weight;
            if (randomNumber < cumulativeWeight) {
                return weightedBlock.blockName;
            }
        }

        return null;
    }

    public void setBlockInNativeWorld(World world, int x, int y, int z, Material material) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                net.minecraft.server.v1_16_R3.World nmsWorld = ((CraftWorld) world).getHandle();
                BlockPosition bp = new BlockPosition(x, y, z);
                IBlockData ibd = CraftMagicNumbers.getBlock(material).getBlockData();
                nmsWorld.setTypeAndData(bp, ibd, 3);
            }
        };

        runnable.runTask(Prison.getInstance());
    }

    public boolean inMinePlayer(Location location, Location pos1, Location pos2) {
        World world = pos1.getWorld();
        if (world == null || !location.getWorld().equals(world)) {
            return false;
        }

        double minX = Math.min(pos1.getX(), pos2.getX()) - 1;
        double maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ()) - 1;
        double maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;

        double playerX = location.getX();
        double playerY = location.getY();
        double playerZ = location.getZ();

        return playerX >= minX && playerX <= maxX &&
                playerY >= minY && playerY <= maxY &&
                playerZ >= minZ && playerZ <= maxZ;
    }

    public boolean inMineBlock(Block b, String name) {
        ConfigurationSection mineSection = config.getConfig().getConfigurationSection(name);
        Location pos1 = LocationAPI.deserializeLocation(mineSection.getString("pos1"));
        Location pos2 = LocationAPI.deserializeLocation(mineSection.getString("pos2"));
        Location playerLocation = b.getLocation();

        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        if (playerLocation.getWorld().equals(pos1.getWorld()) &&
                playerLocation.getX() >= minX && playerLocation.getX() <= maxX &&
                playerLocation.getY() >= minY && playerLocation.getY() <= maxY &&
                playerLocation.getZ() >= minZ && playerLocation.getZ() <= maxZ) {
            return true;
        }
        return false;
    }

    private class WeightedBlock {
        String blockName;
        int weight;

        public WeightedBlock(String blockName, int weight) {
            this.blockName = blockName;
            this.weight = weight;
        }
    }

    public static class MineFillTask extends BukkitRunnable {

        private final MineAPI mineAPI;

        public MineFillTask(MineAPI mineAPI) {
            this.mineAPI = mineAPI;
        }

        @Override
        public void run() {
            List<Mine> mines = mineAPI.getAllMines();
            mines.forEach(mine -> mineAPI.fillMine(mine.getName()));
        }
    }
}
