package eu.revamp.practice.command.impl.staff;
/*
import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.util.enums.Messages;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class NPCEditorCommand {

    private final RevampPractice plugin;

    @Command(name = "npceditor", aliases = {"npc"}, permission = "practice.command.npc", inGameOnly = true)
    public void npcEditor(CommandArgs args) {
        Player player = args.getPlayer();
        if (args.length() == 0) {
            player.sendMessage(Messages.NPC_HELP.getMessage());
        }
        else if (args.length() >= 1){
            eu.revamp.spigot.utils.reflection.NPC npc = new eu.revamp.spigot.utils.reflection.NPC("KitEditor", player.getLocation(), "Stimpay");
            switch (args.getArgs(1).toLowerCase()) {
                case "spawnEditor":
                    npc.spawn();
                    break;
                case "despawnEditor":
                    npc.destroy();
                    break;
                case "setLocation":
                    npc.setLocation(player.getLocation());
                    break;
                case "setName":
                    if (args.length() == 2) {
                        npc.setCustomName(args.getArgs(2));
                    } else {
                        player.sendMessage(Messages.NPC_HELP.getMessage());
                    }
                    break;
                case "setSkin":
                    if (args.length() == 2) {
                        npc.setSkin(args.getArgs(2));
                    } else {
                        player.sendMessage(Messages.NPC_HELP.getMessage());
                    }
                    break;
            }
        }
    }
}
*/