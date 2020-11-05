package eu.revamp.practice.command.impl.staff;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.match.MatchDeathReason;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.tournament.Tournament;
import eu.revamp.practice.util.enums.Menus;
import eu.revamp.practice.util.enums.Messages;
import lombok.AllArgsConstructor;
import eu.revamp.practice.tournament.TournamentState;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class TournamentCommand {

    private final RevampPractice plugin;

    @Command(name = "tournament", aliases = {"tourny"}, permission = "practice.command.tournament", inGameOnly = true)
    public void tournament(CommandArgs args) {
        args.getSender().sendMessage(Messages.TOURNAMENT_HELP.getMessage());
    }

    @Command(name = "tournament.start", aliases = {"tourny.start"}, permission = "practice.command.tournament", inGameOnly = true)
    public void tournamentStart(CommandArgs args) {
        if (this.plugin.getManagerHandler().getTournamentManager().getTournament() != null) {
            args.getSender().sendMessage(Messages.TOURNAMENT_ALREADY_STARTED.getMessage());
            return;
        }
        Player player = args.getPlayer();
        player.openInventory(Menus.TOURNAMENT.getInventory());
    }

    @Command(name = "tournament.stop", aliases = {"tourny.stop"}, permission = "practice.command.tournament", inGameOnly = true)
    public void tournamentStop(CommandArgs args) {
        Tournament tournament = this.plugin.getManagerHandler().getTournamentManager().getTournament();
        if (tournament == null) {
            args.getSender().sendMessage(Messages.NO_TOURNAMENT_STARTED.getMessage());
            return;
        }
        tournament.end(null);
    }

    @Command(name = "tournament.join", aliases = {"tourny.join"}, inGameOnly = true)
    public void tournamentJoin(CommandArgs args) {
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.LOBBY) {
            args.getSender().sendMessage(Messages.MUST_BE_IN_LOBBY.getMessage());
            return;
        }
        Tournament tournament = this.plugin.getManagerHandler().getTournamentManager().getTournament();
        if (tournament == null) {
            args.getSender().sendMessage(Messages.NO_TOURNAMENT_STARTED.getMessage());
            return;
        }
        if (tournament.getTournamentState() != TournamentState.STARTING) {
            args.getSender().sendMessage(Messages.TOURNAMENT_ALREADY_STARTED.getMessage());
            return;
        }
        if (tournament.getParticipants().size() >= this.plugin.getManagerHandler().getSettingsManager().getPlayersPerTournament() && !player.hasPermission("practice.tournament.bypass")) {
            args.getSender().sendMessage(Messages.TOURNAMENT_FULL.getMessage());
            return;
        }
        practiceProfile.setTournament(tournament);
        practiceProfile.setPlayerState(PlayerState.TOURNAMENT);
        tournament.getParticipants().add(player.getUniqueId());

        this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);

        tournament.broadcast(Messages.PLAYER_JOINED_TOURNAMENT.getMessage().replace("%player%", player.getName()).replace("%players%", String.valueOf(tournament.getParticipants().size())).replace("%max%", String.valueOf(this.plugin.getManagerHandler().getSettingsManager().getPlayersPerTournament())));
    }

    @Command(name = "tournament.status", aliases = {"tourny.status"}, inGameOnly = true)
    public void tournamentStatus(CommandArgs args) {
        Tournament tournament = this.plugin.getManagerHandler().getTournamentManager().getTournament();
        if (tournament == null) {
            args.getSender().sendMessage(Messages.NO_TOURNAMENT_STARTED.getMessage());
            return;
        }
        if (tournament.getTournamentState() != TournamentState.STARTED || tournament.getMatchList().size() == 0) {
            args.getSender().sendMessage(Messages.TOURNAMENT_NO_FIGHTS.getMessage());
            return;
        }
        args.getSender().sendMessage(Messages.TOURNAMENT_STATUS_HEADER.getMessage());
        tournament.getMatchList().forEach(m -> args.getSender().sendMessage(Messages.TOURNAMENT_STATUS_MATCH.getMessage().replace("%player1%", m.getT1l().getName()).replace("%player2%", m.getT2l().getName())));
        args.getSender().sendMessage(Messages.TOURNAMENT_STATUS_FOOTER.getMessage());
    }

    @Command(name = "tournament.leave", aliases = {"tourny.leave"}, inGameOnly = true)
    public void tournamentLeave(CommandArgs args) {
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.TOURNAMENT && practiceProfile.getPlayerState() != PlayerState.MATCH) {
            args.getSender().sendMessage(Messages.NOT_IN_TOURNAMENT.getMessage());
            return;
        }
        if (practiceProfile.getPlayerState() == PlayerState.MATCH) {
            Match match = practiceProfile.getMatch();

            if (!match.isTournament()) {
                args.getSender().sendMessage(Messages.NOT_IN_TOURNAMENT.getMessage());
                return;
            }
            match.addDeath(player, MatchDeathReason.LEFT, null);
            return;
        }
        Tournament tournament = this.plugin.getManagerHandler().getTournamentManager().getTournament();
        tournament.getParticipants().remove(player.getUniqueId());
        tournament.broadcast(Messages.PLAYER_LEFT_TOURNAMENT.getMessage().replace("%player%", player.getName()).replace("%players%", String.valueOf(tournament.getParticipants().size())).replace("%max%", String.valueOf(this.plugin.getManagerHandler().getSettingsManager().getPlayersPerTournament())));

        practiceProfile.setPlayerState(PlayerState.LOBBY);
        practiceProfile.setTournament(null);

        this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);

        args.getSender().sendMessage(Messages.LEFT_TOURNAMENT.getMessage());
    }
}
