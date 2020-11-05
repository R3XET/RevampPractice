package eu.revamp.practice.command.impl.player;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.match.MatchRequest;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyState;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Menus;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.chat.color.CC;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@SuppressWarnings("Duplicates")
@AllArgsConstructor
public class DuelCommand {

    private final RevampPractice plugin;

    @Command(name = "duel", aliases = {"1v1"}, inGameOnly = true)
    public void duel(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <player>");
            return;
        }
        Player target = this.plugin.getServer().getPlayer(args.getArgs(0));
        if (target == null) {
            args.getSender().sendMessage(Messages.COULD_NOT_FIND_PLAYER.getMessage().replace("%player%", args.getArgs(0)));
            return;
        }
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.LOBBY && practiceProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.MUST_BE_IN_LOBBY.getMessage());
            return;
        }

        if (target == player) {
            args.getSender().sendMessage(Messages.CANT_DUEL_YOURSELF.getMessage());
            return;
        }
        PracticeProfile targetProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(target);

        if (targetProfile.getPlayerState() != PlayerState.LOBBY && targetProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.PARTY_NOT_IN_LOBBY.getMessage());
            return;
        }
        if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
            Party party = practiceProfile.getParty();

            if (party.getLeader() != player.getUniqueId()) {
                args.getSender().sendMessage(Messages.MUST_BE_PARTY_LEADER.getMessage());
                return;
            }

            if (targetProfile.getPlayerState() != PlayerState.PARTY) {
                args.getSender().sendMessage(Messages.PLAYER_NOT_IN_PARTY.getMessage().replace("%player%", target.getName()));
                return;
            }
            if (party.getMembers().contains(target.getUniqueId())) {
                args.getSender().sendMessage(Messages.CANT_DUEL_YOURSELF.getMessage());
                return;
            }
            Party targetParty = targetProfile.getParty();
            if (targetParty.getPartyState() != PartyState.LOBBY) {
                args.getSender().sendMessage(Messages.PLAYER_NOT_IN_LOBBY.getMessage().replace("%player%", target.getName()));
                return;
            }
            MatchRequest matchRequest = targetParty.getMatchRequestList().stream().filter(m -> m.isParty() && party.getLeader() == m.getRequester() && System.currentTimeMillis() - m.getTimestamp() <= 60000).findFirst().orElse(null);
            if (matchRequest != null) {
                args.getSender().sendMessage(Messages.DUEL_REQUEST_EXISTS.getMessage());
                return;
            }
            party.setDueling(targetParty);
            this.plugin.getManagerHandler().getPlayerManager().openPartyDuel(player);
            return;
        }
        if (targetProfile.getPlayerState() == PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.MUST_CREATE_PARTY_TO_DUEL.getMessage().replace("%player%", target.getName()));
            return;
        }
        if (targetProfile.getPlayerState() != PlayerState.LOBBY) {
            args.getSender().sendMessage(Messages.PLAYER_NOT_IN_LOBBY.getMessage().replace("%player%", target.getName()));
            return;
        }
        if (!targetProfile.isDuelRequests()) {
            args.getSender().sendMessage(Messages.PLAYER_DISABLED_DUELS.getMessage().replace("%player%", player.getName()));
            return;
        }
        MatchRequest matchRequest = targetProfile.getMatchRequestList().stream().filter(m -> !m.isParty() && player.getUniqueId() == m.getRequester() && System.currentTimeMillis() - m.getTimestamp() <= 60000).findFirst().orElse(null);
        if (matchRequest != null) {
            args.getSender().sendMessage(Messages.DUEL_REQUEST_EXISTS.getMessage());
            return;
        }
        practiceProfile.setDueling(target);
        player.openInventory(Menus.DUEL.getInventory());
    }
}
