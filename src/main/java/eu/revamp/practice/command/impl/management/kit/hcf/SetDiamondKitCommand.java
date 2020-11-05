package eu.revamp.practice.command.impl.management.kit.hcf;

import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.serialize.BukkitSerilization;
import org.bukkit.entity.Player;

public class SetDiamondKitCommand {

    @Command(name = "setdiamondkit", permission = "practice.command.setdiamondkit", inGameOnly = true)
    public void setDiamondKit(CommandArgs args) {
        Player player = args.getPlayer();

        HCFKit.DIAMOND.setInventory(BukkitSerilization.cloneInventory(player.getInventory()));
        HCFKit.DIAMOND.setArmor(BukkitSerilization.cloneArmor(player.getInventory().getArmorContents()));

        args.getSender().sendMessage(Messages.DIAMOND_KIT_SET.getMessage());
    }
}
