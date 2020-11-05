package eu.revamp.practice.command.impl.player;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class ToggleDuelRequestsCommand {

    private final RevampPractice plugin;

    @Command(name = "toggleduelrequests", aliases = {"tdr", "toggledr"}, inGameOnly = true)
    public void toggleDuelRequests(CommandArgs args) {
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        practiceProfile.setDuelRequests(!practiceProfile.isDuelRequests());
        args.getSender().sendMessage(practiceProfile.isDuelRequests() ? Messages.DUEL_REQUEST_ENABLED.getMessage() : Messages.DUEL_REQUEST_DISABLED.getMessage());
    }
}
