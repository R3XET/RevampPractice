package eu.revamp.practice.command.impl.management.kit.hcf;

import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.serialize.BukkitSerilization;
import org.bukkit.entity.Player;

public class SetBardKitCommand {

    @Command(name = "setbardkit", permission = "practice.command.setbardkit", inGameOnly = true)
    public void setBardKit(CommandArgs args) {
        Player player = args.getPlayer();

        HCFKit.BARD.setInventory(BukkitSerilization.cloneInventory(player.getInventory()));
        HCFKit.BARD.setArmor(BukkitSerilization.cloneArmor(player.getInventory().getArmorContents()));

        args.getSender().sendMessage(Messages.BARD_KIT_SET.getMessage());
    }
}
