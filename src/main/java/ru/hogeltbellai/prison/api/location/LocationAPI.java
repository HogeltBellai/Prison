package ru.hogeltbellai.prison.api.location;

import org.bukkit.Location;
import ru.hogeltbellai.prison.Prison;

public class LocationAPI {
	
    public static String serializeLocation(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }
    
    public static String serializeLocationPlayer(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + location.getYaw() + "," + location.getPitch();
    }
    
    public static Location deserializeLocation(String serializedLocation) {
        if (serializedLocation != null) {
            String[] parts = serializedLocation.split(",");
            if (parts.length == 4) {
                return new Location(Prison.getInstance().getServer().getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
            }
        }
        return null;
    }

    public static Location deserializeLocationPlayer(String serializedLocation) {
        if (serializedLocation != null) {
            String[] parts = serializedLocation.split(",");
            if (parts.length == 6) {
                return new Location(Prison.getInstance().getServer().getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.valueOf(parts[4]), Float.valueOf(parts[5]));
            }
        }
        return null;
    }
}
