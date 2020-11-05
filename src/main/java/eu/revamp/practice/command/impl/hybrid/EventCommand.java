package eu.revamp.practice.command.impl.hybrid;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.hostedevent.EventState;
import eu.revamp.practice.hostedevent.HostedEvent;
import eu.revamp.practice.hostedevent.impl.SumoEvent;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.chat.color.CC;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("Duplicates")
@AllArgsConstructor
public class EventCommand {

    private final RevampPractice plugin;

    @Command(name = "event", inGameOnly = true)
    public void event(CommandArgs args) {
        args.getSender().sendMessage(Messages.EVENT_HELP.getMessage());
    }

    @Command(name = "event.start", permission = "practice.event.start", inGameOnly = true)
    public void eventStart(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <event>");
            return;
        }

        Player player = args.getPlayer();
        if (System.currentTimeMillis() - this.plugin.getManagerHandler().getEventManager().getLastEvent() <= this.plugin.getManagerHandler().getSettingsManager().getEventDelay() && !player.hasPermission("practice.event.bypass")) {
            args.getSender().sendMessage(Messages.EVENT_DELAY.getMessage());
            return;
        }
        String name = args.getArgs(0);

        if (this.plugin.getManagerHandler().getEventManager().getEvent(name) == null) {
            args.getSender().sendMessage(Messages.EVENT_DOESNT_EXIST.getMessage());
            return;
        }
        if (this.plugin.getManagerHandler().getEventManager().getStartedEvent(name) != null) {
            args.getSender().sendMessage(Messages.EVENT_ALREADY_STARTED.getMessage());
            return;
        }
        HostedEvent event = this.plugin.getManagerHandler().getEventManager().getEvent(name);
        this.plugin.getManagerHandler().getEventManager().getCurrentEvents().add(event);

        event.setDown(System.currentTimeMillis());
        new BukkitRunnable() {
            int i = 30;

            public void run() {

                if (!plugin.getManagerHandler().getEventManager().getCurrentEvents().contains(event)) {
                    this.cancel();
                    return;
                }

                if (i % 10 == 0 && i > 0) {
                    TextComponent clickable = new TextComponent(Messages.EVENT_STARTING.getMessage().replace("%player%", player.getName()).replace("%event%", event.getName()));
                    clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join " + event.getName()));
                    clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.EVENT_HOVER.getMessage().replace("%event%", event.getName())).create()));

                    plugin.getServer().getOnlinePlayers().forEach(p -> p.spigot().sendMessage(clickable));
                }
                i -= 1;
                if (i == 0) {
                    event.setStartTime(System.currentTimeMillis());
                    event.startRound();

                    event.setEventState(EventState.STARTED);
                    this.cancel();
                }
            }
        }.runTaskTimer(this.plugin, 0L, 20L);
    }

    @Command(name = "event.stop", permission = "practice.command.event", inGameOnly = true)
    public void eventStop(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <event>");
            return;
        }

        Player player = args.getPlayer();
        if (System.currentTimeMillis() - this.plugin.getManagerHandler().getEventManager().getLastEvent() <= this.plugin.getManagerHandler().getSettingsManager().getEventDelay() && !player.hasPermission("practice.event.bypass")) {
            args.getSender().sendMessage(Messages.EVENT_DELAY.getMessage());
            return;
        }
        String name = args.getArgs(0);

        if (this.plugin.getManagerHandler().getEventManager().getEvent(name) == null) {
            args.getSender().sendMessage(Messages.EVENT_DOESNT_EXIST.getMessage());
            return;
        }
        if (this.plugin.getManagerHandler().getEventManager().getStartedEvent(name) == null) {
            args.getSender().sendMessage(Messages.NO_EVENT_STARTED.getMessage());
            return;
        }
        HostedEvent hostedEvent = this.plugin.getManagerHandler().getEventManager().getStartedEvent(name);
        hostedEvent.end(hostedEvent.getName(), null);
    }

    @Command(name = "event.join", inGameOnly = true)
    public void eventJoin(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <event>");
            return;
        }
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.LOBBY) {
            args.getSender().sendMessage(Messages.MUST_BE_IN_LOBBY.getMessage());
            return;
        }
        String name = args.getArgs(0);
        HostedEvent event = this.plugin.getManagerHandler().getEventManager().getEvent(name);
        if (event == null) {
            args.getSender().sendMessage(Messages.EVENT_DOESNT_EXIST.getMessage());
            return;
        }
        HostedEvent currentEvent = this.plugin.getManagerHandler().getEventManager().getStartedEvent(name);
        if (currentEvent == null) {
            args.getSender().sendMessage(Messages.NO_EVENT_STARTED.getMessage());
            return;
        }
        if (currentEvent.getEventState() != EventState.STARTING) {
            args.getSender().sendMessage(Messages.EVENT_ALREADY_STARTED.getMessage());
            return;
        }
        if (event.getPlayers().size() >= this.plugin.getManagerHandler().getSettingsManager().getPlayersPerEvent() && !player.hasPermission("practice.event.bypass")) {
            args.getSender().sendMessage(Messages.EVENT_FULL.getMessage());
            return;
        }
        currentEvent.getPlayers().add(player.getUniqueId());
        practiceProfile.setPlayerState(PlayerState.EVENT);

        currentEvent.broadcast(Messages.PLAYER_JOINED_EVENT.getMessage().replace("%player%", player.getName()));

        practiceProfile.setHostedEvent(currentEvent);
        this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);

        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            currentEvent.getPlayers().forEach(u -> {
                Player players = this.plugin.getServer().getPlayer(u);

                players.showPlayer(player);
                player.showPlayer(players);
            });
        }, 1L);

        player.teleport(event.getLobby());
    }

    @Command(name = "event.spectate", aliases = {"event.spec", "event.sp"}, inGameOnly = true)
    public void eventSpectate(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <event>");
            return;
        }
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.LOBBY) {
            args.getSender().sendMessage(Messages.MUST_BE_IN_LOBBY.getMessage());
            return;
        }
        String name = args.getArgs(0);
        HostedEvent event = this.plugin.getManagerHandler().getEventManager().getEvent(name);
        if (event == null) {
            args.getSender().sendMessage(Messages.EVENT_DOESNT_EXIST.getMessage());
            return;
        }
        HostedEvent currentEvent = this.plugin.getManagerHandler().getEventManager().getStartedEvent(name);
        if (currentEvent == null) {
            args.getSender().sendMessage(Messages.NO_EVENT_STARTED.getMessage());
            return;
        }
        event.getSpectators().add(player.getUniqueId());
        practiceProfile.setHostedEvent(event);
        practiceProfile.setPlayerState(PlayerState.SPECTATING_EVENT);
        player.setAllowFlight(true);
        player.setFlying(true);

        player.teleport(event.getLobby());
        this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
        if (!practiceProfile.isSilentMode())
            event.broadcast(Messages.PLAYER_NOW_SPECTATING.getMessage().replace("%player%", player.getName()));

        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);

        event.getPlayers().forEach(u -> player.showPlayer(this.plugin.getServer().getPlayer(u)));
    }

    @Command(name = "event.leave", inGameOnly = true)
    public void eventLeave(CommandArgs args) {
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.EVENT && practiceProfile.getPlayerState() != PlayerState.SPECTATING_EVENT) {
            args.getSender().sendMessage(Messages.NOT_IN_EVENT.getMessage());
            return;
        }
        HostedEvent hostedEvent = practiceProfile.getHostedEvent();

        if (hostedEvent instanceof SumoEvent) {
            SumoEvent sumoEvent = (SumoEvent) hostedEvent;

            if (sumoEvent.getP1() == player || sumoEvent.getP2() == player) {
                sumoEvent.eliminatePlayer(player);
            }
        }

        practiceProfile.setPlayerState(PlayerState.LOBBY);
        if (hostedEvent.getPlayers().contains(player.getUniqueId())) {
            hostedEvent.getPlayers().remove(player.getUniqueId());

            hostedEvent.broadcast(Messages.PLAYER_LEFT_EVENT.getMessage().replace("%player%", player.getName()));
            args.getSender().sendMessage(Messages.LEFT_EVENT.getMessage());
        }
        if (hostedEvent.getSpectators().contains(player.getUniqueId())) {
            hostedEvent.getSpectators().remove(player.getUniqueId());

            if (!practiceProfile.isSilentMode())
                hostedEvent.broadcast(Messages.PLAYER_NO_LONGER_SPECTATING.getMessage().replace("%player%", player.getName()));
            args.getSender().sendMessage(Messages.NO_LONGER_SPECTATING.getMessage());
        }

        this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
        this.plugin.getManagerHandler().getPlayerManager().teleportSpawn(player);
    }

    @Command(name = "event.lobby", permission = "practice.command.event", inGameOnly = true)
    public void eventLobby(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <event>");
            return;
        }
        String name = args.getArgs(0);
        HostedEvent event = this.plugin.getManagerHandler().getEventManager().getEvent(name);
        if (event == null) {
            args.getSender().sendMessage(Messages.EVENT_DOESNT_EXIST.getMessage());
            return;
        }
        Player player = args.getPlayer();
        event.setLobby(player.getLocation());

        args.getSender().sendMessage(Messages.EVENT_LOBBY_SET.getMessage().replace("%event%", event.getName()));
    }

    @Command(name = "event.first", permission = "practice.command.event", inGameOnly = true)
    public void eventFirst(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <event>");
            return;
        }
        String name = args.getArgs(0);
        HostedEvent event = this.plugin.getManagerHandler().getEventManager().getEvent(name);
        if (event == null) {
            args.getSender().sendMessage(Messages.EVENT_DOESNT_EXIST.getMessage());
            return;
        }
        Player player = args.getPlayer();
        event.setFirst(player.getLocation());

        args.getSender().sendMessage(Messages.EVENT_FIRST_SET.getMessage().replace("%event%", event.getName()));
    }

    @Command(name = "event.second", permission = "practice.command.event", inGameOnly = true)
    public void eventSecond(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <event>");
            return;
        }
        String name = args.getArgs(0);
        HostedEvent event = this.plugin.getManagerHandler().getEventManager().getEvent(name);
        if (event == null) {
            args.getSender().sendMessage(Messages.EVENT_DOESNT_EXIST.getMessage());
            return;
        }
        Player player = args.getPlayer();
        event.setSecond(player.getLocation());

        args.getSender().sendMessage(Messages.EVENT_SECOND_SET.getMessage().replace("%event%", event.getName()));
    }
}
