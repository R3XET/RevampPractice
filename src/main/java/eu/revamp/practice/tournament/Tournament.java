package eu.revamp.practice.tournament;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.ListUtil;
import eu.revamp.practice.util.enums.Messages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.*;

@Setter
@Getter
@RequiredArgsConstructor
public class Tournament {

    private final RevampPractice plugin;
    private final Kit kit;
    private TournamentState tournamentState = TournamentState.STARTING;
    private int round = 1;
    private List<Match> matchList = new ArrayList<>();
    private List<UUID> participants = new ArrayList<>();
    private Random random = new Random();
    private long down;
    private long startTime;

    public void startRound() {
        if (participants.size() == 0) {
            end(null);
            return;
        }
        if (participants.size() == 1) {
            end(this.plugin.getServer().getPlayer(participants.get(0)).getName());
            return;
        }
        UUID sitout = null;
        if (participants.size() % 2 != 0) {
            sitout = participants.get(random.nextInt(participants.size()));
        }
        List<UUID> l1 = new ArrayList<>(participants);
        List<UUID> l2 = new ArrayList<>();
        if (sitout != null) {
            l1.remove(sitout);

            Player player = this.plugin.getServer().getPlayer(sitout);
            player.sendMessage(Messages.TOURNAMENT_SITOUT.getMessage());
        }

        Collections.shuffle(l1);
        for (int i = 0; i < (l1.size() / 2); i++) {
            l2.add(l1.get(i));
            l1.remove(l1.get(i));
        }
        if (l2.size() != l1.size()) {
            end(null);
            return;
        }
        for (int i = 0; i < l1.size(); i++) {
            if (l1.get(i) != l2.get(i)) {
                Match match = new Match(this.plugin, kit, ListUtil.newList(l1.get(i)), ListUtil.newList(l2.get(i)), false, true);
                matchList.add(match);
                match.start();

                Player p1 = this.plugin.getServer().getPlayer(l1.get(i));
                PracticeProfile p1p = this.plugin.getManagerHandler().getProfileManager().getProfile(p1);

                p1p.setMatch(match);
                p1p.setPlayerState(PlayerState.MATCH);
                kit.getUnrankedMatch().add(p1.getUniqueId());

                Player p2 = this.plugin.getServer().getPlayer(l2.get(i));
                PracticeProfile p2p = this.plugin.getManagerHandler().getProfileManager().getProfile(p2);

                p2p.setMatch(match);
                p2p.setPlayerState(PlayerState.MATCH);
                kit.getUnrankedMatch().add(p2.getUniqueId());
            }
        }
    }

    public void end(String winner) {
        this.plugin.getServer().broadcastMessage(winner != null ? Messages.TOURNAMENT_WINNER.getMessage().replace("%player%", winner).replace("%kit%", kit.getName()) : Messages.TOURNAMENT_CANCELLED.getMessage().replace("%kit%", kit.getName()));

        participants.forEach(u -> {
            Player player = RevampPractice.getInstance().getServer().getPlayer(u);

            PracticeProfile practiceProfile = RevampPractice.getInstance().getManagerHandler().getProfileManager().getProfile(player);
            practiceProfile.setPlayerState(PlayerState.LOBBY);
            practiceProfile.setTournament(null);

            this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
            this.plugin.getManagerHandler().getPlayerManager().teleportSpawn(player);
        });

        this.plugin.getManagerHandler().getTournamentManager().setTournament(null);
    }

    public void broadcast(String message) {
        participants.forEach(u -> this.plugin.getServer().getPlayer(u).sendMessage(message));
    }
}
