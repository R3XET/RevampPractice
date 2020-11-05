package eu.revamp.practice.manager.impl;

import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.manager.Manager;
import eu.revamp.practice.manager.ManagerHandler;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileManager extends Manager {

    private Map<UUID, PracticeProfile> practiceProfileMap;

    public ProfileManager(ManagerHandler managerHandler) {
        super(managerHandler);
        practiceProfileMap = new HashMap<>();
    }

    public void addPlayer(Player player) {
        practiceProfileMap.put(player.getUniqueId(), new PracticeProfile());
    }

    public void removePlayer(Player player) {
        practiceProfileMap.remove(player.getUniqueId());
    }

    public PracticeProfile getProfile(Player player) {
        return practiceProfileMap.get(player.getUniqueId());
    }

    public PracticeProfile getProfile(UUID uuid) {
        return practiceProfileMap.get(uuid);
    }

    public boolean hasProfile(Player player) {
        return practiceProfileMap.containsKey(player.getUniqueId());
    }
}
