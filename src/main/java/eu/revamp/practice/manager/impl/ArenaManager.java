package eu.revamp.practice.manager.impl;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.arena.Arena;
import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.kit.KitType;
import eu.revamp.practice.util.LocationUtil;
import eu.revamp.practice.manager.Manager;
import eu.revamp.practice.manager.ManagerHandler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import sun.security.provider.ConfigFile;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ArenaManager extends Manager {

    private List<Arena> arenasList;

    public ArenaManager(ManagerHandler managerHandler) {
        super(managerHandler);

        arenasList = new ArrayList<>();
        fetch();
    }

    private void fetch() {
        if (managerHandler.getConfigurationManager().getArenasFile().getConfigurationSection("arenas") == null) {
            return;
        }

        managerHandler.getConfigurationManager().getArenasFile().getConfigurationSection("arenas").getKeys(false).forEach(a -> {
            Arena arena = new Arena(a);
            arena.setL1(LocationUtil.getLocationFromString(managerHandler.getConfigurationManager().getArenasFile().getString("arenas." + a + ".l1")));
            arena.setL2(LocationUtil.getLocationFromString(managerHandler.getConfigurationManager().getArenasFile().getString("arenas." + a + ".l2")));
            arena.setKitType(KitType.getType(managerHandler.getConfigurationManager().getArenasFile().getString("arenas." + a + ".type")));

            addArena(arena);
        });
    }

    public Arena getArena(String name) {
        return arenasList.stream().filter(a -> a.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void addArena(Arena arena) {
        arenasList.add(arena);
    }

    public void removeArena(Arena arena) {
        arenasList.remove(arena);
    }

    public Arena randomArena(Kit kit) {
        // Cleaned up to Java 8 Standards
        List<Arena> compatible = arenasList.stream().filter(a -> a.getKitType() == kit.getKitType()).collect(Collectors.toList());
        return compatible.get(new Random().nextInt(compatible.size()));
    }

    public Arena randomArena(KitType kitType) {
        List<Arena> compatible = arenasList.stream().filter(a -> a.getKitType() == kitType).collect(Collectors.toList());
        return compatible.get(new Random().nextInt(compatible.size()));
    }

    public void save() {
        try {
            managerHandler.getConfigurationManager().getArenasFile().set("arenas", null);
            arenasList.forEach(a -> {
                if (a.getL1() != null)
                    managerHandler.getConfigurationManager().getArenasFile().set("arenas." + a.getName() + ".l1", LocationUtil.getStringFromLocation(a.getL1()));
                if (a.getL2() != null)
                    managerHandler.getConfigurationManager().getArenasFile().set("arenas." + a.getName() + ".l2", LocationUtil.getStringFromLocation(a.getL2()));
                managerHandler.getConfigurationManager().getArenasFile().set("arenas." + a.getName() + ".type", a.getKitType().toString());
            });
            managerHandler.getConfigurationManager().saveArenasFile();
        } catch (Exception ignored) {
        }
    }



    private final RevampPractice plugin = RevampPractice.getInstance();

    @Getter
    private final Map<String, Arena> arenas = new HashMap<>();

    @Getter
    private final Map<Arena, UUID> arenaMatchUUIDs = new HashMap<>();

    @Getter
    @Setter
    private int generatingArenaRunnables;

}
