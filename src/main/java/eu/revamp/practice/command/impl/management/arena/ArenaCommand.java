package eu.revamp.practice.command.impl.management.arena;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.arena.Arena;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.kit.KitType;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.chat.color.CC;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@SuppressWarnings("Duplicates")
@AllArgsConstructor
public class ArenaCommand {

    private final RevampPractice plugin;

    @Command(name = "arena", permission = "practice.command.arena", inGameOnly = true)
    public void arena(CommandArgs args) {
        args.getSender().sendMessage(new String[]{
                CC.GRAY + "" + CC.STRIKE_THROUGH + "--------------------------------------------",
                CC.GOLD + "" + CC.BOLD + "Arena Commands:",
                CC.GRAY + " * " + CC.YELLOW + "/arena create <name> - Create an arena",
                CC.GRAY + " * " + CC.YELLOW + "/arena delete <name> - Delete an arena",
                CC.GRAY + " * " + CC.YELLOW + "/arena first <name> - Set first spawn of an arena",
                CC.GRAY + " * " + CC.YELLOW + "/arena second <name> - Set second spawn of an arena",
                CC.GRAY + " * " + CC.YELLOW + "/arena setType <name> <type> - Set type of an arena",
                CC.GRAY + " * " + CC.YELLOW + "/arena tp <name> - Teleport to an arena",
                CC.GRAY + "" + CC.STRIKE_THROUGH + "--------------------------------------------"
        });
    }

    @Command(name = "arena.create", permission = "practice.command.arena", inGameOnly = true)
    public void arenaCreate(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name>");
            return;
        }
        String name = args.getArgs(0);
        Arena arena = this.plugin.getManagerHandler().getArenaManager().getArena(name);

        if (arena != null) {
            args.getSender().sendMessage(Messages.ARENA_ALREADY_EXISTS.getMessage());
            return;
        }

        this.plugin.getManagerHandler().getArenaManager().addArena(new Arena(name));
        args.getSender().sendMessage(Messages.ARENA_CREATED.getMessage().replace("%arena%", name));
    }

    @Command(name = "arena.delete", permission = "practice.command.arena", inGameOnly = true)
    public void arenaDelete(CommandArgs args) {
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

        this.plugin.getManagerHandler().getArenaManager().removeArena(arena);
        args.getSender().sendMessage(Messages.ARENA_DELETED.getMessage().replace("%arena%", name));
    }

    @Command(name = "arena.first", permission = "practice.command.arena", inGameOnly = true)
    public void arenaFirst(CommandArgs args) {
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
        Player player = args.getPlayer();

        arena.setL1(player.getLocation());
        args.getSender().sendMessage(Messages.ARENA_FIRST_SET.getMessage().replace("%arena%", arena.getName()));
    }

    @Command(name = "arena.second", permission = "practice.command.arena", inGameOnly = true)
    public void arenaSecond(CommandArgs args) {
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
        Player player = args.getPlayer();

        arena.setL2(player.getLocation());
        args.getSender().sendMessage(Messages.ARENA_SECOND_SET.getMessage().replace("%arena%", arena.getName()));
    }

    @Command(name = "arena.settype", permission = "practice.command.arena", inGameOnly = true)
    public void arenaSetType(CommandArgs args) {
        if (args.length() != 2) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name> <type>");
            return;
        }
        String name = args.getArgs(0);
        Arena arena = this.plugin.getManagerHandler().getArenaManager().getArena(name);

        if (arena == null) {
            args.getSender().sendMessage(Messages.ARENA_DOESNT_EXIST.getMessage());
            return;
        }
        KitType kitType = KitType.getType(args.getArgs(1));
        if (kitType == null) {
            args.getSender().sendMessage(Messages.KIT_TYPE_DOESNT_EXIST.getMessage());
            return;
        }
        arena.setKitType(kitType);
        args.getSender().sendMessage(Messages.ARENA_TYPE_UPDATED.getMessage().replace("%arena%", arena.getName()).replace("%type%", kitType.toString()));
    }

    @Command(name = "arena.tp", permission = "practice.command.arena", inGameOnly = true)
    public void arenaTp(CommandArgs args) {
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
        Player player = args.getPlayer();
        player.teleport(arena.getL1());
    }
}
