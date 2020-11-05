package eu.revamp.practice.command.impl.staff;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.util.enums.Messages;
import lombok.AllArgsConstructor;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.player.PracticeProfile;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class SilentCommand {

    private final RevampPractice plugin;

    @Command(name = "silent", aliases = {"silentmode"}, permission = "practice.command.silent", inGameOnly = true)
    public void silent(CommandArgs args) {
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        practiceProfile.setSilentMode(!practiceProfile.isSilentMode());
        args.getSender().sendMessage(practiceProfile.isSilentMode() ? Messages.SILENT_ENABLED.getMessage() : Messages.SILENT_DISABLED.getMessage());
    }
}
