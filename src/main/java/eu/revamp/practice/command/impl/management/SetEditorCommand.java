package eu.revamp.practice.command.impl.management;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.util.enums.Messages;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class SetEditorCommand {

    private final RevampPractice plugin;

    @Command(name = "seteditor", permission = "practice.command.seteditor", inGameOnly = true)
    public void setEditor(CommandArgs args) {
        Player player = args.getPlayer();

        this.plugin.getManagerHandler().getSettingsManager().setEditor(player.getLocation());
        args.getSender().sendMessage(Messages.EDITOR_SET.getMessage());
    }
}
