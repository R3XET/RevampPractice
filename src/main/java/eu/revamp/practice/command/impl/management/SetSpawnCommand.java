package eu.revamp.practice.command.impl.management;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.util.enums.Messages;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class SetSpawnCommand {

    private final RevampPractice plugin;

    @Command(name = "setspawn", permission = "practice.command.setspawn", inGameOnly = true)
    public void setSpawn(CommandArgs args) {
        Player player = args.getPlayer();

        this.plugin.getManagerHandler().getSettingsManager().setSpawn(player.getLocation());
        args.getSender().sendMessage(Messages.SPAWN_SET.getMessage());
    }
}
