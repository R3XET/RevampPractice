package eu.revamp.practice.command.impl.player;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.practice.util.misc.InventorySnapshot;
import eu.revamp.spigot.utils.chat.color.CC;
import lombok.AllArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class InventoryCommand {

    private final RevampPractice plugin;

    @Command(name = "inventory", aliases = {"inv"}, inGameOnly = true)
    public void inventory(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <player>");
            return;
        }
        OfflinePlayer target = this.plugin.getServer().getOfflinePlayer(args.getArgs(0));

        if (InventorySnapshot.getByUUID(target.getUniqueId()) == null) {
            args.getSender().sendMessage(Messages.INVENTORY_NOT_FOUND.getMessage());
            return;
        }
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        player.openInventory(InventorySnapshot.getByUUID(target.getUniqueId()).getInventory());
        practiceProfile.setViewingPlayerInv(true);
    }
}
