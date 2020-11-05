package eu.revamp.practice.command.impl.player;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class ToggleScoreboardCommand {

    private final RevampPractice plugin;

    @Command(name = "togglescoreboard", aliases = {"togglesidebar", "tsb", "togglesb"}, inGameOnly = true)
    public void toggleScoreboard(CommandArgs args) {
        Player player = args.getPlayer();

        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
        practiceProfile.setScoreboard(!practiceProfile.isScoreboard());

        args.getSender().sendMessage(practiceProfile.isScoreboard() ? Messages.SCOREBOARD_ENABLED.getMessage() : Messages.SCOREBOARD_DISABLED.getMessage());
    }
}
