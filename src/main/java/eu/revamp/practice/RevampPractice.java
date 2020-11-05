package eu.revamp.practice;

import eu.revamp.practice.command.CommandFramework;
import eu.revamp.practice.command.impl.hybrid.EventCommand;
import eu.revamp.practice.command.impl.management.BuilderCommand;
import eu.revamp.practice.command.impl.management.SetEditorCommand;
import eu.revamp.practice.command.impl.management.SetSpawnCommand;
import eu.revamp.practice.command.impl.management.arena.ArenaCommand;
import eu.revamp.practice.command.impl.management.kit.KitCommand;
import eu.revamp.practice.command.impl.management.kit.hcf.SetArcherKitCommand;
import eu.revamp.practice.command.impl.management.kit.hcf.SetBardKitCommand;
import eu.revamp.practice.command.impl.management.kit.hcf.SetDiamondKitCommand;
import eu.revamp.practice.command.impl.management.kit.hcf.SetRogueKitCommand;
import eu.revamp.practice.command.impl.player.*;
import eu.revamp.practice.command.impl.staff.SetEloCommand;
import eu.revamp.practice.command.impl.staff.SilentCommand;
import eu.revamp.practice.command.impl.staff.TournamentCommand;
import eu.revamp.practice.hostedevent.impl.SumoEvent;
import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.listener.PacketListener;
import eu.revamp.practice.listener.PlayerListener;
import eu.revamp.practice.listener.hcf.ArcherListener;
import eu.revamp.practice.listener.hcf.BardListener;
import eu.revamp.practice.listener.hcf.RogueListener;
import eu.revamp.practice.manager.ManagerHandler;
import eu.revamp.practice.task.*;
import eu.revamp.practice.task.player.ArcherMarkTask;
import eu.revamp.practice.task.player.RogueBackstabTask;
import eu.revamp.practice.util.misc.BlankObject;
import eu.revamp.practice.util.misc.EntityHider;
import eu.revamp.practice.util.misc.Logger;
import eu.revamp.spigot.utils.generic.Tasks;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

@Getter
public class RevampPractice extends JavaPlugin {

    @Getter
    private static RevampPractice instance;

    private EntityHider entityHider;
    private ManagerHandler managerHandler;


    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveConfig();

        managerHandler = new ManagerHandler(this);
        managerHandler.register();

        registerCommands(
                new SetSpawnCommand(this),
                new SetEditorCommand(this),
                new KitCommand(this),
                new ArenaCommand(this),
                new SpawnCommand(this),
                new InventoryCommand(this),
                new PartyCommand(this),
                new DuelCommand(this),
                new LeaderboardsCommand(this),
                new AcceptCommand(this),
                new SetDiamondKitCommand(),
                new SetArcherKitCommand(),
                new SetBardKitCommand(),
                new SetRogueKitCommand(),
                new EventCommand(this),
                //new BuildWorldDelete(this),

                //ADDED
                new SumoEvent(this),
                //ADDED


                (this.getConfig().getBoolean("scoreboard.enabled") ? new ToggleScoreboardCommand(this) : new BlankObject()), // Disable /togglescoreboard when scoreboard option is disabled
                new TournamentCommand(this),
                new SpectateCommand(this),
                new SilentCommand(this),
                new BuilderCommand(this),
                new ToggleDuelRequestsCommand(this),
                new SetEloCommand(this),
                (this.getConfig().getBoolean("settings.settings-gui") ? new SettingsCommand(this) : new BlankObject())
        );

        registerListeners(
                new PlayerListener(this),
                new PacketListener(this),
                new BardListener(this),
                new ArcherListener(this),
                new RogueListener(this)


                //new BetterDeathListener(this)


        );
        entityHider = new EntityHider(this, EntityHider.Policy.BLACKLIST);

        registerTasks(
                new InventoryTask(this),
                new ScoreboardTask(this),
                new WorldTask(this),
                new ArcherMarkTask(this),
                new RogueBackstabTask(this)
        );
        new EnderpearlTask(this).runTaskTimerAsynchronously(this, 1L, 1L); // registered separately because of different timing
        new MongoTask(this).runTaskTimerAsynchronously(this, 2400, 2400); // save player data every 2 mins

        this.getServer().getWorlds().forEach(w -> w.setTime(6000L));

        disableGameRules(
                "doDaylightCycle",
                "doMobSpawning",
                "doFireTick");

        Tasks.runLater(this, () -> {
            Kit kit = this.getManagerHandler().getKitManager().getKit("Combo");
            kit.setDamageTicks(1);
            Logger.success("Combo Knockback set to 1 tick");
        }, 100L);
    }

    @Override
    public void onDisable() {
        this.getServer().getOnlinePlayers().forEach(p -> p.kickPlayer("Server restarting."));
        managerHandler.getSettingsManager().save();
        managerHandler.getKitManager().save();
        managerHandler.getArenaManager().save();
        managerHandler.getEventManager().save();

        this.getServer().getWorlds().forEach(w -> w.getEntities().stream().filter(e -> e.getType() == EntityType.DROPPED_ITEM).forEach(Entity::remove));
    }

    private void registerCommands(Object... objects) {
        CommandFramework commandFramework = new CommandFramework(this);
        Arrays.stream(objects).forEach(commandFramework::registerCommands);
    }

    private void registerListeners(Listener... listeners) {
        Arrays.stream(listeners).forEach(l -> getServer().getPluginManager().registerEvents(l, this));
    }

    private void registerTasks(BukkitRunnable... bukkitRunnables) {
        Arrays.stream(bukkitRunnables).forEach(br -> br.runTaskTimerAsynchronously(this, 0L, 0L));
    }

    private void disableGameRules(String... gameRules) {
        Arrays.stream(gameRules).forEach(gr -> this.getServer().getWorlds().forEach(w -> w.setGameRuleValue(gr, "false")));
    }
}
