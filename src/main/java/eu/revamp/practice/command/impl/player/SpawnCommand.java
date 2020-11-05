package eu.revamp.practice.command.impl.player;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyState;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class SpawnCommand {

    private final RevampPractice plugin;

    @Command(name = "spawn", inGameOnly = true)
    public void spawn(CommandArgs args) {
        Player player = args.getPlayer();

        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.LOBBY && practiceProfile.getPlayerState() != PlayerState.QUEUE && practiceProfile.getPlayerState() != PlayerState.PARTY) {
            args.getSender().sendMessage(Messages.INVALID_STATE.getMessage());
            return;
        }
        if (practiceProfile.getPlayerState() == PlayerState.PARTY) {
            Party party = practiceProfile.getParty();

            if (party.getPartyState() != PartyState.LOBBY && party.getPartyState() != PartyState.QUEUE) {
                args.getSender().sendMessage(Messages.INVALID_STATE.getMessage());
                return;
            }
        }

        this.plugin.getManagerHandler().getPlayerManager().teleportSpawn(player);
        args.getSender().sendMessage(Messages.TELEPORTING_TO_SPAWN.getMessage());
    }
}
