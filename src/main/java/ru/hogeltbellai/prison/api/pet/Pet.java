package ru.hogeltbellai.prison.api.pet;

import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.entity.CustomPet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Pet implements Listener {

    CustomPet pet;
    private final Map<UUID, CustomPet> playerPets = new HashMap<>();
    private final ConfigAPI config = new ConfigAPI("pets");

    public void spawnPet(Player player, PetAPI petAPI) {
        Location petLocation = getPetLocation(player);
        pet = new CustomPet(player, petLocation, petAPI);
        playerPets.put(player.getUniqueId(), pet);
    }

    public void removePet(Player player) {
        CustomPet pet = playerPets.remove(player.getUniqueId());
        if (pet != null) {
            pet.removePet();
        }
    }

    private Location getPetLocation(Player player) {
        Location playerLocation = player.getEyeLocation();
        Vector playerDirection = playerLocation.getDirection().normalize();
        Vector rightDirection = playerDirection.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        Location petLocation = playerLocation.clone().add(rightDirection.multiply(2));
        petLocation.setY(petLocation.getY() - 1);
        return petLocation;
    }

    public PetAPI loadPetData(String petName) {
        if (config.getConfig().contains("pets." + petName)) {
            return new PetAPI(config.getConfig().getConfigurationSection("pets." + petName));
        } else {
            return null;
        }
    }
}
