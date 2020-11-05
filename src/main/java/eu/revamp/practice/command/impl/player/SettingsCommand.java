package eu.revamp.practice.command.impl.player;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.util.enums.Menus;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

/**
 * @author 7x6
 * @since 20/09/2019
 */
@AllArgsConstructor
public class SettingsCommand {

    private final RevampPractice plugin;

    @Command(name = "practicesettings", aliases = "psettings", inGameOnly = true)
    public void settings(CommandArgs args) {
        Player player = args.getPlayer();
        player.openInventory(Menus.SETTINGS.getInventory());
    }
}
