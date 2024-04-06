package ru.hogeltbellai.prison.api.pet;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PetAPI {

    private String name;
    private ItemStack headItem;
    private double speed;
    private List<PotionEffect> potionEffects;

    public PetAPI(String name, ItemStack headItem, double speed, List<PotionEffect> potionEffects) {
        this.name = name;
        this.headItem = headItem;
        this.speed = speed;
        this.potionEffects = potionEffects;
    }

    public PetAPI(ConfigurationSection config) {
        this.name = config.getString("name");
        this.headItem = loadHeadItem(config);
        this.speed = config.getDouble("walkSpeed", 0.2f);
        this.potionEffects = loadPotionEffects(config);
    }

    private ItemStack loadHeadItem(ConfigurationSection config) {
        ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) headItem.getItemMeta();

        meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.randomUUID()));
        String base64Texture = config.getString("headTexture");

        if (base64Texture != null && !base64Texture.isEmpty()) {
            try {
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                profile.getProperties().put("textures", new Property("textures", base64Texture));
                ItemMeta itemMeta = ((ItemMeta) meta);
                java.lang.reflect.Field profileField = null;
                profileField = itemMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        headItem.setItemMeta(meta);
        return headItem;
    }

    private List<PotionEffect> loadPotionEffects(ConfigurationSection config) {
        List<PotionEffect> effects = new ArrayList<>();
        if (config.contains("effects")) {
            for (String effectKey : config.getConfigurationSection("effects").getKeys(false)) {
                ConfigurationSection effectConfig = config.getConfigurationSection("effects." + effectKey);
                PotionEffectType type = PotionEffectType.getByName(effectConfig.getString("type"));
                if (type != null) {
                    int amplifier = effectConfig.getInt("amplifier", 0);
                    effects.add(new PotionEffect(type, Integer.MAX_VALUE, amplifier));
                }
            }
        }
        return effects;
    }

    public void applyEffects(Player player) {
        player.setWalkSpeed((float) speed);

        for (PotionEffect effect : potionEffects) {
            player.addPotionEffect(effect);
        }
    }
}
