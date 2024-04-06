package ru.hogeltbellai.prison.api.entity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.pet.PetAPI;

import java.util.*;

@Setter
@Getter
public class CustomPet {

    private final Player owner;
    private Location previousLocation;
    private final PetAPI petAPI;
    private int entityId;
    private BukkitTask task;
    private PacketContainer spawnPacket;
    private PacketContainer equipmentPacket;
    private PacketContainer metadataPacket;
    private PacketContainer destroyPacket;
    private UUID uuid;
    public static final Map<Player, CustomPet> playerPets = new HashMap<>();
    private List<Player> playerNear = null;

    public CustomPet(Player owner, Location location, PetAPI petAPI) {
        this.previousLocation = location;
        this.owner = owner;
        this.petAPI = petAPI;
        this.playerNear = new ArrayList<>();

        this.uuid = UUID.randomUUID();

        this.spawnPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        this.equipmentPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        this.metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        this.destroyPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        createPet(location);
        startPetUpdate();
        playerPets.put(owner, this);
    }

    public void createPet(Location location) {
        try {
            entityId = (int) (Math.random() * Integer.MAX_VALUE);
            spawnPacket.getIntegers().write(0, entityId);
            spawnPacket.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
            spawnPacket.getUUIDs().write(0, uuid);
            spawnPacket.getDoubles().write(0, location.getX());
            spawnPacket.getDoubles().write(1, location.getY());
            spawnPacket.getDoubles().write(2, location.getZ());

            equipmentPacket.getIntegers().write(0, entityId);
            List<Pair<EnumWrappers.ItemSlot, ItemStack>> list = new ArrayList<>();
            list.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, petAPI.getHeadItem()));
            equipmentPacket.getSlotStackPairLists().write(0, list);

            metadataPacket.getModifier().writeDefaults();
            metadataPacket.getIntegers().write(0, entityId);

            WrappedDataWatcher dataWatcher = new WrappedDataWatcher();

            WrappedChatComponent displayName = WrappedChatComponent.fromText(ChatColor.translateAlternateColorCodes('&', petAPI.getName().replace("%player%", owner.getName())));
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), Optional.of(displayName));
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true);
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(14, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x01);
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20);

            metadataPacket.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

            ProtocolLibrary.getProtocolManager().broadcastServerPacket(spawnPacket);
            ProtocolLibrary.getProtocolManager().broadcastServerPacket(equipmentPacket);
            ProtocolLibrary.getProtocolManager().broadcastServerPacket(metadataPacket);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void removePet() {
        try {
            destroyPacket.getIntegerArrays().write(0, new int[]{this.entityId});
            ProtocolLibrary.getProtocolManager().broadcastServerPacket(destroyPacket);
            task.cancel();
            playerPets.remove(owner);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void movePet(Location location) {
        try {
            double distance = previousLocation.distance(location);

            if (distance < 20) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (location.distance(player.getLocation()) < 20) {
                        if (!this.playerNear.contains(player)) {
                            this.playerNear.add(player);
                            loadPetsForOnlinePlayer(owner, player);
                        }
                    } else {
                        if (this.playerNear.contains(player)) {
                            this.playerNear.remove(player);
                            removePetForPlayer(player, this);
                        }
                    }
                }
            }
            previousLocation = location;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void move(PacketContainer teleportPacket, Location location) {
        teleportPacket.getIntegers().write(0, this.entityId);

        teleportPacket.getDoubles().write(0, location.getX());
        teleportPacket.getDoubles().write(1, location.getY());
        teleportPacket.getDoubles().write(2, location.getZ());

        float playerYaw = owner.getLocation().getYaw();
        if (playerYaw < 0)
            playerYaw += 360;
        teleportPacket.getBytes().write(0, (byte) (playerYaw * 256 / 360));
    }

    public void startPetUpdate() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                petAPI.applyEffects(owner);
                Location newPetLocation = getPetLocation(owner);
                movePet(newPetLocation);

                PacketContainer teleportPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
                move(teleportPacket, newPetLocation);
                for(Player player : Bukkit.getOnlinePlayers()) {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, teleportPacket);
                }
            }
        }.runTaskTimer(Prison.getInstance(), 0, 1);
    }

    public Location getPetLocation(Player player) {
        Location playerLocation = player.getEyeLocation();
        Vector playerDirection = playerLocation.getDirection().normalize();
        Vector rightDirection = playerDirection.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        Location petLocation = playerLocation.clone().add(rightDirection.multiply(2));
        petLocation.setY(petLocation.getY() - 1);
        return petLocation;
    }

    public void loadAllPetsForOnlinePlayers() {
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(this.spawnPacket);
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(this.equipmentPacket);
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(this.metadataPacket);
    }

    public static void loadPetsForOnlinePlayer(Player player, Player target) {
        CustomPet pet = playerPets.get(player);
        if(pet != null) {
            pet.spawnPacket.getDoubles().write(0, player.getLocation().getX());
            pet.spawnPacket.getDoubles().write(1, player.getLocation().getY());
            pet.spawnPacket.getDoubles().write(2, player.getLocation().getZ());
            ProtocolLibrary.getProtocolManager().sendServerPacket(target, pet.spawnPacket);
            ProtocolLibrary.getProtocolManager().sendServerPacket(target, pet.equipmentPacket);
            ProtocolLibrary.getProtocolManager().sendServerPacket(target, pet.metadataPacket);
        }
    }

    public static void removePetForPlayer(Player player, Player target) {
        CustomPet pet = playerPets.get(player);
        if(pet != null) {
            PacketContainer destroyPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            destroyPacket.getIntegerArrays().write(0, new int[]{pet.entityId});

            ProtocolLibrary.getProtocolManager().sendServerPacket(target, pet.destroyPacket);
        }
    }

    public static void removePetForPlayer(Player player, CustomPet pet) {
        if (pet != null) {
            PacketContainer destroyPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            destroyPacket.getIntegerArrays().write(0, new int[]{pet.entityId});

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyPacket);
        }
    }

    public static void removePetForPlayer(Player player) {
        CustomPet pet = playerPets.get(player);
        if (pet != null) {
            pet.removePet();
            playerPets.remove(player);

            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }
    }
}