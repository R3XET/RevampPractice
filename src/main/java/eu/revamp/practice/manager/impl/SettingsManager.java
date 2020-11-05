package eu.revamp.practice.manager.impl;

import eu.revamp.practice.util.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import eu.revamp.practice.manager.Manager;
import eu.revamp.practice.manager.ManagerHandler;
import org.bukkit.Location;


@Getter
@Setter
public class SettingsManager extends Manager {

    private Location spawn;
    private Location editor;

    private int defaultElo;
    private int maxElo;
    private int minElo;
    private int maxPartyMembers;
    private boolean lightningEffect;
    private boolean deleteBottles;
    private long eventDelay;
    private int playersPerEvent;
    private int playersPerTournament;
    private boolean showPlayers;
    private boolean leaderboardsGUI;
    private boolean voidSpawn;
    private boolean hideInvisiblesInTab;
    private String partyChatFormat;

    public SettingsManager(ManagerHandler managerHandler) {
        super(managerHandler);

        fetch();
    }

    private void fetch() {
        if (managerHandler.getPlugin().getConfig().get("spawn") != null)
            spawn = LocationUtil.getLocationFromString(managerHandler.getPlugin().getConfig().getString("spawn"));
        if (managerHandler.getPlugin().getConfig().get("editor") != null)
            editor = LocationUtil.getLocationFromString(managerHandler.getPlugin().getConfig().getString("editor"));

        defaultElo = managerHandler.getPlugin().getConfig().getInt("settings.default-elo");
        maxElo = managerHandler.getPlugin().getConfig().getInt("settings.max-elo");
        minElo = managerHandler.getPlugin().getConfig().getInt("settings.min-elo");
        maxPartyMembers = managerHandler.getPlugin().getConfig().getInt("settings.max-party-members");
        lightningEffect = managerHandler.getPlugin().getConfig().getBoolean("settings.lightning-effect");
        deleteBottles = managerHandler.getPlugin().getConfig().getBoolean("settings.delete-bottles");
        eventDelay = managerHandler.getPlugin().getConfig().getInt("settings.event-delay") * 60000;
        playersPerEvent = managerHandler.getPlugin().getConfig().getInt("settings.players-per-event");
        playersPerTournament = managerHandler.getPlugin().getConfig().getInt("settings.players-per-tournament");
        showPlayers = managerHandler.getPlugin().getConfig().getBoolean("settings.show-players");
        leaderboardsGUI = managerHandler.getPlugin().getConfig().getBoolean("settings.leaderboards-gui");
        voidSpawn = managerHandler.getPlugin().getConfig().getBoolean("settings.void-spawn");
        hideInvisiblesInTab = managerHandler.getPlugin().getConfig().getBoolean("settings.hide-invisibles-in-tab");
        partyChatFormat = managerHandler.getPlugin().getConfig().getString("party-chat-format");
    }

    public void save() {
        if (spawn != null)
            managerHandler.getPlugin().getConfig().set("spawn", LocationUtil.getStringFromLocation(spawn));
        if (editor != null)
            managerHandler.getPlugin().getConfig().set("editor", LocationUtil.getStringFromLocation(editor));

        managerHandler.getPlugin().saveConfig();
    }
}
