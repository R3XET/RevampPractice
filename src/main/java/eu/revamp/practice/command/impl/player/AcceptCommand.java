package eu.revamp.practice.command.impl.player;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.match.MatchRequest;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyState;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.ListUtil;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.chat.color.CC;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@SuppressWarnings("Duplicates")
@AllArgsConstructor
public class AcceptCommand {

    private final RevampPractice plugin;

    @Command(name = "accept", inGameOnly = true)
    public void accept(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <player>");
            return;
        }
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.LOBBY && practiceProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.MUST_BE_IN_LOBBY.getMessage());
            return;
        }
        Player target = this.plugin.getServer().getPlayer(args.getArgs(0));
        if (target == null) {
            args.getSender().sendMessage(Messages.COULD_NOT_FIND_PLAYER.getMessage().replace("%player%", args.getArgs(0)));
            return;
        }
        if (target == player) {
            args.getSender().sendMessage(Messages.CANT_DUEL_YOURSELF.getMessage());
            return;
        }
        PracticeProfile targetProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(target);
        if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
            Party party = practiceProfile.getParty();

            if (party.getLeader() != player.getUniqueId()) {
                args.getSender().sendMessage(Messages.MUST_BE_PARTY_LEADER.getMessage());
                return;
            }

            if (party.getPartyState() != PartyState.LOBBY) {
                args.getSender().sendMessage(Messages.MUST_BE_IN_LOBBY.getMessage());
                return;
            }

            if (targetProfile.getPlayerState() != PlayerState.PARTY) {
                args.getSender().sendMessage(Messages.PLAYER_NOT_IN_PARTY.getMessage().replace("%player%", target.getName()));
                return;
            }

            Party targetParty = targetProfile.getParty();
            if (targetParty.getPartyState() != PartyState.LOBBY) {
                args.getSender().sendMessage(Messages.PARTY_NOT_IN_LOBBY.getMessage());
                return;
            }
            MatchRequest matchRequest = party.getMatchRequestList().stream().filter(m -> m.isParty() && targetParty.getMembers().contains(m.getRequester()) && System.currentTimeMillis() - m.getTimestamp() <= 60000).findFirst().orElse(null);
            if (matchRequest == null) {
                args.getSender().sendMessage(Messages.DUEL_REQUEST_NOT_FOUND.getMessage());
                return;
            }
            party.getMatchRequestList().remove(matchRequest);

            Match match = new Match(this.plugin, matchRequest.getKit(), party.getMembers(), targetParty.getMembers(), false, false);
            match.start();

            party.setMatch(match);
            party.setPartyState(PartyState.MATCH);

            targetParty.setMatch(match);
            targetParty.setPartyState(PartyState.MATCH);
            return;
        }
        if (targetProfile.getPlayerState() != PlayerState.LOBBY) {
            args.getSender().sendMessage(Messages.PLAYER_NOT_IN_LOBBY.getMessage().replace("%player%", target.getName()));
            return;
        }
        MatchRequest matchRequest = practiceProfile.getMatchRequestList().stream().filter(m -> !m.isParty() && m.getRequester() == target.getUniqueId() && System.currentTimeMillis() - m.getTimestamp() <= 60000).findFirst().orElse(null);
        if (matchRequest == null) {
            args.getSender().sendMessage(Messages.DUEL_REQUEST_NOT_FOUND.getMessage());
            return;
        }
        practiceProfile.getMatchRequestList().remove(matchRequest);

        Match match = new Match(this.plugin, matchRequest.getKit(), ListUtil.newList(player.getUniqueId()), ListUtil.newList(target.getUniqueId()), false, false);
        match.start();

        practiceProfile.setMatch(match);
        practiceProfile.setPlayerState(PlayerState.MATCH);
        match.getKit().getUnrankedMatch().add(player.getUniqueId());

        targetProfile.setMatch(match);
        targetProfile.setPlayerState(PlayerState.MATCH);
        match.getKit().getUnrankedMatch().add(target.getUniqueId());
    }
}
