package eu.revamp.practice.command.impl.management.kit.hcf;

import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.serialize.BukkitSerilization;
import org.bukkit.entity.Player;

public class SetArcherKitCommand {

    @Command(name = "setarcherkit", permission = "practice.command.setarcherkit", inGameOnly = true)
    public void setArcherKit(CommandArgs args) {
        Player player = args.getPlayer();

        HCFKit.ARCHER.setInventory(BukkitSerilization.cloneInventory(player.getInventory()));
        HCFKit.ARCHER.setArmor(BukkitSerilization.cloneArmor(player.getInventory().getArmorContents()));

        args.getSender().sendMessage(Messages.ARCHER_KIT_SET.getMessage());
    }
}
