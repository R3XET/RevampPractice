package eu.revamp.practice.manager.impl;

import eu.revamp.practice.manager.Manager;
import eu.revamp.practice.manager.ManagerHandler;
import eu.revamp.practice.tournament.Tournament;
import eu.revamp.practice.util.enums.Messages;
import lombok.Getter;
import lombok.Setter;
import eu.revamp.practice.tournament.TournamentState;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.scheduler.BukkitRunnable;

@Setter
@Getter
public class TournamentManager extends Manager {

    private Tournament tournament;

    public TournamentManager(ManagerHandler managerHandler) {
        super(managerHandler);
    }

    public void next() {
        try {
            tournament.setDown(System.currentTimeMillis());
            new BukkitRunnable() {
                int i = tournament.getRound() > 1 ? 30 : 60;

                public void run() {
                    if (tournament == null || (tournament.getTournamentState() != TournamentState.STARTING && tournament.getTournamentState() != TournamentState.WAITING)) {
                        this.cancel();
                        return;
                    }
                    if (i % 10 == 0 && tournament.getTournamentState() == TournamentState.STARTING) {
                        TextComponent clickable = new TextComponent(Messages.TOURNAMENT_STARTING.getMessage().replace("%kit%", tournament.getKit().getName()));
                        clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tournament join"));
                        clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.TOURNAMENT_HOVER.getMessage().replace("%kit%", tournament.getKit().getName())).create()));

                        managerHandler.getPlugin().getServer().getOnlinePlayers().forEach(p -> p.spigot().sendMessage(clickable));
                    }
                    i -= 1;
                    if (i == 0) {
                        tournament.setStartTime(System.currentTimeMillis());
                        tournament.startRound();

                        tournament.setTournamentState(TournamentState.STARTED);
                        this.cancel();
                    }
                }
            }.runTaskTimer(managerHandler.getPlugin(), 0L, 20L);
        } catch (Exception ex) {
            // get rid of errors ;3
        }
    }
}
