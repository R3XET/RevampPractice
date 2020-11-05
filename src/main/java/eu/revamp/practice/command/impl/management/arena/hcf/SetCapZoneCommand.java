package eu.revamp.practice.command.impl.management.arena.hcf;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.arena.Arena;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.kit.KitType;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.chat.color.CC;
import lombok.AllArgsConstructor;

@SuppressWarnings("Duplicates")
@AllArgsConstructor
public class SetCapZoneCommand {

    private final RevampPractice plugin;

    @Command(name = "setcapzone", permission = "practice.command.setcapzone", inGameOnly = true)
    public void setCapZone(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name>");
            return;
        }
        String name = args.getArgs(0);
        Arena arena = this.plugin.getManagerHandler().getArenaManager().getArena(name);
        if (arena == null) {
            args.getSender().sendMessage(Messages.ARENA_DOESNT_EXIST.getMessage());
            return;
        }
        if (arena.getKitType() != KitType.HCF) {
            args.getSender().sendMessage(Messages.NOT_HCF_ARENA.getMessage());
        }
        /* Will end up adding koths eventually */
    }
}
