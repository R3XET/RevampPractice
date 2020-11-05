package eu.revamp.practice.manager;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.manager.impl.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ManagerHandler {

    private final RevampPractice plugin;

    private MongoManager mongoManager;
    private SettingsManager settingsManager;
    private ConfigurationManager configurationManager;
    private ItemsManager itemsManager;
    private PlayerManager playerManager;
    private ProfileManager profileManager;
    private KitManager kitManager;
    private MenuManager menuManager;
    private ScoreboardManager scoreboardManager;
    private ArenaManager arenaManager;
    private EventManager eventManager;
    private TournamentManager tournamentManager;

    public void register() {
        mongoManager = new MongoManager(this);
        settingsManager = new SettingsManager(this);
        configurationManager = new ConfigurationManager(this);
        itemsManager = new ItemsManager(this);
        playerManager = new PlayerManager(this);
        profileManager = new ProfileManager(this);
        kitManager = new KitManager(this);
        menuManager = new MenuManager(this);
        scoreboardManager = new ScoreboardManager(this);
        arenaManager = new ArenaManager(this);
        eventManager = new EventManager(this);
        tournamentManager = new TournamentManager(this);
    }
}
