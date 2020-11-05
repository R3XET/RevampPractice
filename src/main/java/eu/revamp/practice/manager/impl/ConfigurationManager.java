package eu.revamp.practice.manager.impl;

import eu.revamp.practice.manager.Manager;
import eu.revamp.practice.manager.ManagerHandler;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.practice.util.misc.Logger;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

@Getter
public class ConfigurationManager extends Manager {

    private YamlConfiguration messagesFile;
    private YamlConfiguration kitsFile;
    private YamlConfiguration arenasFile;
    private YamlConfiguration eventsFile;

    public ConfigurationManager(ManagerHandler managerHandler) {
        super(managerHandler);

        createFiles();
    }

    private void createFiles() {
        try {
            File mFile = new File(managerHandler.getPlugin().getDataFolder(), "messages.yml");
            if (!mFile.exists()) {
                mFile.createNewFile();
                Logger.success("Created messages.yml");
            }
            messagesFile = YamlConfiguration.loadConfiguration(mFile);
            Arrays.stream(Messages.values()).filter(messages -> messagesFile.get(messages.getPath()) == null).forEach(messages -> messagesFile.set(messages.getPath(), messages.getDefaultMessage()));
            saveMessagesFile();

            File kFile = new File(managerHandler.getPlugin().getDataFolder(), "kits.yml");
            if (!kFile.exists()) {
                kFile.createNewFile();
                Logger.success("Created kits.yml");
            }
            kitsFile = YamlConfiguration.loadConfiguration(kFile);

            File aFile = new File(managerHandler.getPlugin().getDataFolder(), "arenas.yml");
            if (!aFile.exists()) {
                aFile.createNewFile();
                Logger.success("Created arenas.yml");
            }
            arenasFile = YamlConfiguration.loadConfiguration(aFile);

            File eFile = new File(managerHandler.getPlugin().getDataFolder(), "events.yml");
            if (!eFile.exists()) {
                eFile.createNewFile();
                Logger.success("Created events.yml");
            }
            eventsFile = YamlConfiguration.loadConfiguration(eFile);
        } catch (Exception ex) {
            Logger.error("Error loading files.");
        }
    }

    public void saveMessagesFile() {
        try {
            File mFile = new File(managerHandler.getPlugin().getDataFolder(), "messages.yml");
            messagesFile.save(mFile);
        } catch (Exception ex) {
            Logger.error("Could not save messages.yml");
        }
    }

    public void saveKitsFile() {
        try {
            File kFile = new File(managerHandler.getPlugin().getDataFolder(), "kits.yml");
            kitsFile.save(kFile);
        } catch (Exception ex) {
            Logger.error("Could not save kits.yml");
        }
    }

    public void saveArenasFile() {
        try {
            File aFile = new File(managerHandler.getPlugin().getDataFolder(), "arenas.yml");
            arenasFile.save(aFile);
        } catch (Exception ex) {
            Logger.error("Could not save arenas.yml");
        }
    }

    public void saveEventsFile() {
        try {
            File eFile = new File(managerHandler.getPlugin().getDataFolder(), "events.yml");
            eventsFile.save(eFile);
        } catch (Exception ex) {
            Logger.error("Could not save event.yml");
        }
    }
}
