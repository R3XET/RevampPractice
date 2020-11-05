package eu.revamp.practice.command.impl.management.kit.hcf;

import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.serialize.BukkitSerilization;
import org.bukkit.entity.Player;

public class SetRogueKitCommand {

    @Command(name = "setroguekit", permission = "practice.command.setroguekit", inGameOnly = true)
    public void setRogueKit(CommandArgs args) {
        Player player = args.getPlayer();

        HCFKit.ROGUE.setInventory(BukkitSerilization.cloneInventory(player.getInventory()));
        HCFKit.ROGUE.setArmor(BukkitSerilization.cloneArmor(player.getInventory().getArmorContents()));

        args.getSender().sendMessage(Messages.ROGUE_KIT_SET.getMessage());
    }
}
