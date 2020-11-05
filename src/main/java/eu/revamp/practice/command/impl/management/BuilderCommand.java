package eu.revamp.practice.command.impl.management;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class BuilderCommand {

    private final RevampPractice plugin;

    @Command(name = "builder", aliases = {"building", "buildmode"}, permission = "practice.command.builder", inGameOnly = true)
    public void builder(CommandArgs args) {
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        practiceProfile.setBuildMode(!practiceProfile.isBuildMode());
        args.getSender().sendMessage(practiceProfile.isBuildMode() ? Messages.BUILDER_ENABLED.getMessage() : Messages.BUILDER_DISABLED.getMessage());
    }
}
