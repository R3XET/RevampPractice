package eu.revamp.practice.command.impl.management.kit;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.command.Command;
import eu.revamp.practice.command.CommandArgs;
import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.kit.KitType;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import eu.revamp.spigot.utils.chat.color.CC;
import eu.revamp.spigot.utils.serialize.BukkitSerilization;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@SuppressWarnings("Duplicates")
@AllArgsConstructor
public class KitCommand {

    private final RevampPractice plugin;

    @Command(name = "kit", permission = "practice.command.kit", inGameOnly = true)
    public void kit(CommandArgs args) {
        args.getSender().sendMessage(new String[]{
                CC.GRAY + "" + CC.STRIKE_THROUGH + "--------------------------------------------",
                CC.GOLD + "" + CC.BOLD + "Kit Commands:",
                CC.GRAY + " * " + CC.YELLOW + "/kit create <name> - Create a kit",
                CC.GRAY + " * " + CC.YELLOW + "/kit delete <name> - Delete a kit",
                CC.GRAY + " * " + CC.YELLOW + "/kit setDisplay <name> - Set display icon of a kit",
                CC.GRAY + " * " + CC.YELLOW + "/kit setKit <name> - Set inventory of a kit",
                CC.GRAY + " * " + CC.YELLOW + "/kit setEditChest <name> <editChest> - Set edit chest value of a kit",
                CC.GRAY + " * " + CC.YELLOW + "/kit setEditInv <name> - Set edit inventory a kit",
                CC.GRAY + " * " + CC.YELLOW + "/kit setType <name> <type> - Set type of a kit",
                CC.GRAY + " * " + CC.YELLOW + "/kit setDamageTicks <name> <ticks> - Set damage ticks of a kit",
                CC.GRAY + " * " + CC.YELLOW + "/kit setRanked <name> <ranked> - Set ranked value of a kit",
                CC.GRAY + " * " + CC.YELLOW + "/kit setUnranked <name> <unranked> - Set unranked value of a kit",
                CC.GRAY + " * " + CC.YELLOW + "/kit setEditable <name> <editable> - Set editable value of a kit",
                CC.GRAY + " * " + CC.YELLOW + "/kit loadKit <name> - Load the inventory of a kit",
                CC.GRAY + "" + CC.STRIKE_THROUGH + "--------------------------------------------"
        });
    }

    @Command(name = "kit.create", permission = "practice.command.kit", inGameOnly = true)
    public void kitCreate(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name>");
            return;
        }
        String name = args.getArgs(0);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(name);

        if (name.equalsIgnoreCase("hcf")) {
            args.getSender().sendMessage(CC.RED + "This kit name is reserved.");
            return;
        }

        if (kit != null) {
            args.getSender().sendMessage(Messages.KIT_ALREADY_EXISTS.getMessage());
            return;
        }
        this.plugin.getManagerHandler().getKitManager().addKit(new Kit(name));
        this.plugin.getManagerHandler().getMongoManager().getMongoDatabase().createCollection(name);
        args.getSender().sendMessage(Messages.KIT_CREATED.getMessage().replace("%kit%", name));
    }

    @Command(name = "kit.delete", permission = "practice.command.kit", inGameOnly = true)
    public void kitDelete(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name>");
            return;
        }
        String name = args.getArgs(0);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(name);

        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }
        this.plugin.getManagerHandler().getKitManager().removeKit(kit);
        this.plugin.getManagerHandler().getMongoManager().getMongoDatabase().getCollection(kit.getName()).drop();
        args.getSender().sendMessage(Messages.KIT_DELETED.getMessage().replace("%kit%", kit.getName()));
    }

    @Command(name = "kit.setdisplay", permission = "practice.command.kit", inGameOnly = true)
    public void kitSetDisplay(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name>");
            return;
        }
        String name = args.getArgs(0);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(name);

        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }

        Player player = args.getPlayer();
        if (player.getItemInHand().getType() == Material.AIR) {
            args.getSender().sendMessage(Messages.HAND_CANT_BE_AIR.getMessage());
            return;
        }
        kit.setDisplay(player.getItemInHand());
        args.getSender().sendMessage(Messages.KIT_DISPLAY_SET.getMessage().replace("%kit%", kit.getName()));
    }

    @Command(name = "kit.setkit", permission = "practice.command.kit", inGameOnly = true)
    public void kitSetKit(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name>");
            return;
        }
        String name = args.getArgs(0);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(name);

        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }
        Player player = args.getPlayer();

        kit.setInventory(BukkitSerilization.cloneInventory(player.getInventory()));
        kit.setArmor(BukkitSerilization.cloneArmor(player.getInventory().getArmorContents()));

        args.getSender().sendMessage(Messages.KIT_INVENTORY_SET.getMessage().replace("%kit%", kit.getName()));
    }

    @Command(name = "kit.seteditchest", permission = "practice.command.kit", inGameOnly = true)
    public void kitSetEditChest(CommandArgs args) {
        if (args.length() != 2) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name> <true:false>");
            return;
        }
        String name = args.getArgs(0);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(name);

        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }
        try {
            boolean value = Boolean.parseBoolean(args.getArgs(1));

            kit.setEditChest(value);
            args.getSender().sendMessage(Messages.KIT_EDITCHEST_UPDATED.getMessage().replace("%kit%", kit.getName()).replace("%value%", String.valueOf(value)));
        } catch (Exception ex) {
            args.getSender().sendMessage(CC.RED + "Invalid value.");
        }
    }

    @Command(name = "kit.seteditinv", permission = "practice.command.kit", inGameOnly = true)
    public void kitSetEditInv(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name>");
            return;
        }
        String name = args.getArgs(0);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(name);

        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }
        Player player = args.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        practiceProfile.setEditInv(kit);
        if (kit.getEditInventory() == null)
            kit.setEditInventory(this.plugin.getServer().createInventory(null, 54, kit.getName()));
        player.openInventory(kit.getEditInventory());
    }

    @Command(name = "kit.settype", permission = "practice.command.kit", inGameOnly = true)
    public void kitSetType(CommandArgs args) {
        if (args.length() != 2) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name> <type>");
            return;
        }
        String name = args.getArgs(0);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(name);

        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }
        KitType kitType = KitType.getType(args.getArgs(1));
        if (kitType == null) {
            args.getSender().sendMessage(Messages.KIT_TYPE_DOESNT_EXIST.getMessage());
            return;
        }
        if (kitType == KitType.HCF) {
            args.getSender().sendMessage(CC.RED + "This type of kit is reserved.");
            return;
        }
        kit.setKitType(kitType);
        args.getSender().sendMessage(Messages.KIT_TYPE_UPDATED.getMessage().replace("%kit%", kit.getName()).replace("%type%", kitType.toString()));
    }

    @Command(name = "kit.setdamageticks", permission = "practice.command.kit", inGameOnly = true)
    public void kitSetDamageTicks(CommandArgs args) {
        if (args.length() != 2) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name> <ticks>");
            return;
        }
        String name = args.getArgs(0);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(name);

        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }
        if (!NumberUtils.isDigits(args.getArgs(1))) {
            args.getSender().sendMessage(CC.RED + "Invalid amount.");
            return;
        }
        int ticks = Integer.parseInt(args.getArgs(1));
        if (ticks < 0 || ticks > 20) {
            args.getSender().sendMessage(CC.RED + "Please enter a valid amount between 0 and 20.");
            return;
        }
        kit.setDamageTicks(ticks);
        args.getSender().sendMessage(Messages.KIT_TICKS_UPDATED.getMessage().replace("%kit%", kit.getName()).replace("%ticks%", String.valueOf(ticks)));
    }

    @Command(name = "kit.setranked", permission = "practice.command.kit", inGameOnly = true)
    public void kitSetRanked(CommandArgs args) {
        if (args.length() != 2) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name> <true:false>");
            return;
        }
        String name = args.getArgs(0);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(name);

        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }
        try {
            boolean value = Boolean.parseBoolean(args.getArgs(1));

            kit.setRanked(value);
            args.getSender().sendMessage(Messages.KIT_RANKED_UPDATED.getMessage().replace("%kit%", kit.getName()).replace("%value%", String.valueOf(value)));
        } catch (Exception ex) {
            args.getSender().sendMessage(CC.RED + "Invalid value.");
        }
    }

    @Command(name = "kit.setunranked", permission = "practice.command.kit", inGameOnly = true)
    public void kitSetUnranked(CommandArgs args) {
        if (args.length() != 2) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name> <true:false>");
            return;
        }
        String name = args.getArgs(0);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(name);

        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }
        try {
            boolean value = Boolean.parseBoolean(args.getArgs(1));

            kit.setUnranked(value);
            args.getSender().sendMessage(Messages.KIT_UNRANKED_UPDATED.getMessage().replace("%kit%", kit.getName()).replace("%value%", String.valueOf(value)));
        } catch (Exception ex) {
            args.getSender().sendMessage(CC.RED + "Invalid value.");
        }
    }

    @Command(name = "kit.seteditable", permission = "practice.command.kit", inGameOnly = true)
    public void kitSetEditable(CommandArgs args) {
        if (args.length() != 2) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name> <true:false>");
            return;
        }
        String name = args.getArgs(0);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(name);

        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }
        try {
            boolean value = Boolean.parseBoolean(args.getArgs(1));

            kit.setEditable(value);
            args.getSender().sendMessage(Messages.KIT_EDITABLE_UPDATED.getMessage().replace("%kit%", kit.getName()).replace("%value%", String.valueOf(value)));
        } catch (Exception ex) {
            args.getSender().sendMessage(CC.RED + "Invalid value.");
        }
    }

    @Command(name = "kit.loadkit", permission = "practice.command.kit", inGameOnly = true)
    public void kitLoadKit(CommandArgs args) {
        if (args.length() != 1) {
            args.getSender().sendMessage(CC.RED + "Usage: /" + args.getLabel() + " <name>");
            return;
        }
        String name = args.getArgs(0);
        Kit kit = this.plugin.getManagerHandler().getKitManager().getKit(name);

        if (kit == null) {
            args.getSender().sendMessage(Messages.KIT_DOESNT_EXIST.getMessage());
            return;
        }
        if (kit.getInventory() == null || kit.getArmor() == null) {
            args.getSender().sendMessage(Messages.KIT_NOT_SET.getMessage());
            return;
        }
        Player player = args.getPlayer();

        player.getInventory().setContents(kit.getInventory().getContents());
        player.getInventory().setArmorContents(kit.getArmor());
        player.updateInventory();

        args.getSender().sendMessage(Messages.LOADED_KIT.getMessage().replace("%kit%", kit.getName()));
    }
}
