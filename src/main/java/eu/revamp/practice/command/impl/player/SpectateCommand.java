package eu.revamp.practice.command.impl.player;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyState;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Menus;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.chat.color.CC;
import lombok.AllArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@SuppressWarnings("Duplicates")
@AllArgsConstructor
public class SpectateCommand {

    private RevampPractice plugin;

    @Command(name = "spectate", aliases = {"spec", "sp"}, inGameOnly = true)
    public void spectate(CommandArgs args) {
        Player player = args.getPlayer();
        if (args.length() != 1) {
            //args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <player>");
            player.openInventory(Menus.MATCHES.getInventory());
            return;
        }

        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.LOBBY) {
            args.getSender().sendMessage(Messages.MUST_BE_IN_LOBBY.getMessage());
            return;
        }

        Player target = this.plugin.getServer().getPlayer(args.getArgs(0));
        if (target == null) {
            args.getSender().sendMessage(Messages.COULD_NOT_FIND_PLAYER.getMessage().replace("%player%", args.getArgs(0)));
            return;
        }

        PracticeProfile targetProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(target);
        if (targetProfile.getPlayerState() != PlayerState.MATCH && targetProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.PLAYER_NOT_IN_MATCH.getMessage().replace("%player%", target.getName()));
            return;
        }
        if (targetProfile.getPlayerState() == PlayerState.PARTY) {
            Party party = targetProfile.getParty();

            if (party.getPartyState() != PartyState.MATCH) {
                args.getSender().sendMessage(Messages.PLAYER_NOT_IN_MATCH.getMessage());
                return;
            }
            Match match = party.getMatch();
            practiceProfile.setPlayerState(PlayerState.SPECTATING);

            //Added fly
            player.setGameMode(GameMode.SPECTATOR);
            player.setAllowFlight(true);
            player.setFlying(true);

            practiceProfile.setSpectating(match);
            match.getSpectators().add(player.getUniqueId());

            player.teleport(target);
            this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);

            match.getTeamOne().forEach(u -> player.showPlayer(this.plugin.getServer().getPlayer(u)));
            match.getTeamTwo().forEach(u -> player.showPlayer(this.plugin.getServer().getPlayer(u)));

            if (!practiceProfile.isSilentMode())
                match.broadcast(Messages.PLAYER_NOW_SPECTATING.getMessage().replace("%player%", player.getName()));

            player.setGameMode(GameMode.SPECTATOR);
            player.setAllowFlight(true);
            player.setFlying(true);
            return;
        }

        Match match = targetProfile.getMatch();
        practiceProfile.setPlayerState(PlayerState.SPECTATING);

        practiceProfile.setSpectating(match);
        match.getSpectators().add(player.getUniqueId());

        player.teleport(target);
        this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
        this.plugin.getManagerHandler().getPlayerManager().hideAll(player);

        match.getTeamOne().forEach(u -> player.showPlayer(this.plugin.getServer().getPlayer(u)));
        match.getTeamOne().forEach(u -> this.plugin.getServer().getPlayer(u).hidePlayer(player));
        match.getTeamTwo().forEach(u -> player.showPlayer(this.plugin.getServer().getPlayer(u)));
        match.getTeamTwo().forEach(u -> this.plugin.getServer().getPlayer(u).hidePlayer(player));

        if (!practiceProfile.isSilentMode())
            match.broadcast(Messages.PLAYER_NOW_SPECTATING.getMessage().replace("%player%", player.getName()));

        //Added fly
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);
    }
}
