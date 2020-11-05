package eu.revamp.practice.manager.impl;

import eu.revamp.practice.kit.Kit;
import eu.revamp.practice.kit.KitType;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.util.misc.Logger;
import eu.revamp.spigot.utils.item.ItemBuilder;
import eu.revamp.spigot.utils.serialize.BukkitSerilization;
import lombok.Getter;
import eu.revamp.practice.manager.Manager;
import eu.revamp.practice.manager.ManagerHandler;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class KitManager extends Manager {

    private List<Kit> kitsList;

    public KitManager(ManagerHandler managerHandler) {
        super(managerHandler);

        kitsList = new ArrayList<>();
        fetch();
    }

    private void fetch() {
        if (managerHandler.getConfigurationManager().getKitsFile().getConfigurationSection("kits") == null) {
            return;
        }

        managerHandler.getConfigurationManager().getKitsFile().getConfigurationSection("kits").getKeys(false).forEach(k -> {
            Kit kit = new Kit(k);

            String[] material = managerHandler.getConfigurationManager().getKitsFile().getString("kits." + k + ".display").split(":");

            int id = Integer.parseInt(material[0]);
            short durability = (short) Integer.parseInt(material[1]);

            kit.setDisplay(new ItemBuilder(Material.getMaterial(id)).setDurability(durability).toItemStack());

            if (managerHandler.getConfigurationManager().getKitsFile().get("kits." + k + ".inventory") != null)
                kit.setInventory(BukkitSerilization.fromBase64(managerHandler.getConfigurationManager().getKitsFile().getString("kits." + k + ".inventory")));
            if (managerHandler.getConfigurationManager().getKitsFile().get("kits." + k + ".armor") != null)
                kit.setArmor(BukkitSerilization.itemStackArrayFromBase64(managerHandler.getConfigurationManager().getKitsFile().getString("kits." + k + ".armor")));
            if (managerHandler.getConfigurationManager().getKitsFile().get("kits." + k + ".editInventory") != null)
                kit.setEditInventory(BukkitSerilization.fromBase64(managerHandler.getConfigurationManager().getKitsFile().getString("kits." + k + ".editInventory")));

            kit.setKitType(KitType.getType(managerHandler.getConfigurationManager().getKitsFile().getString("kits." + k + ".type")));
            kit.setEditChest(managerHandler.getConfigurationManager().getKitsFile().getBoolean("kits." + k + ".editChest"));
            kit.setRanked(managerHandler.getConfigurationManager().getKitsFile().getBoolean("kits." + k + ".ranked"));
            kit.setUnranked(managerHandler.getConfigurationManager().getKitsFile().getBoolean("kits." + k + ".unranked"));
            kit.setEditable(managerHandler.getConfigurationManager().getKitsFile().getBoolean("kits." + k + ".editable"));

            addKit(kit);

            if (!this.managerHandler.getMongoManager().collectionExists(k)) {
                this.managerHandler.getMongoManager().getMongoDatabase().createCollection(k);

                Logger.success("Created Mongo collection " + k + ".");
            }
        });
        if (managerHandler.getConfigurationManager().getKitsFile().getConfigurationSection("hcf") == null) {
            return;
        }

        managerHandler.getConfigurationManager().getKitsFile().getConfigurationSection("hcf").getKeys(false).forEach(k -> {
            HCFKit hcfKit = HCFKit.valueOf(k);

            if (managerHandler.getConfigurationManager().getKitsFile().get("hcf." + k + ".inventory") != null) hcfKit.setInventory(BukkitSerilization.fromBase64(managerHandler.getConfigurationManager().getKitsFile().getString("hcf." + k + ".inventory")));
            if (managerHandler.getConfigurationManager().getKitsFile().get("hcf." + k + ".armor") != null) hcfKit.setArmor(BukkitSerilization.itemStackArrayFromBase64(managerHandler.getConfigurationManager().getKitsFile().getString("hcf." + k + ".armor")));
        });
    }

    public Kit getKit(String name) {
        return kitsList.stream().filter(k -> k.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void addKit(Kit kit) {
        kitsList.add(kit);
    }

    public void removeKit(Kit kit) {
        kitsList.remove(kit);
    }

    public void save() {
        managerHandler.getConfigurationManager().getKitsFile().set("kits", null);
        kitsList.forEach(k -> {
            managerHandler.getConfigurationManager().getKitsFile().set("kits." + k.getName() + ".display", k.getDisplay().getTypeId() + ":" + k.getDisplay().getDurability());
            if (k.getInventory() != null)
                managerHandler.getConfigurationManager().getKitsFile().set("kits." + k.getName() + ".inventory", BukkitSerilization.toBase64(k.getInventory()));
            if (k.getArmor() != null)
                managerHandler.getConfigurationManager().getKitsFile().set("kits." + k.getName() + ".armor", BukkitSerilization.itemStackArrayToBase64(k.getArmor()));
            if (k.getEditInventory() != null)
                managerHandler.getConfigurationManager().getKitsFile().set("kits." + k.getName() + ".editInventory", BukkitSerilization.toBase64(k.getEditInventory()));
            managerHandler.getConfigurationManager().getKitsFile().set("kits." + k.getName() + ".type", k.getKitType().toString());
            managerHandler.getConfigurationManager().getKitsFile().set("kits." + k.getName() + ".editChest", k.isEditChest());
            managerHandler.getConfigurationManager().getKitsFile().set("kits." + k.getName() + ".ranked", k.isRanked());
            managerHandler.getConfigurationManager().getKitsFile().set("kits." + k.getName() + ".unranked", k.isUnranked());
            managerHandler.getConfigurationManager().getKitsFile().set("kits." + k.getName() + ".editable", k.isEditable());

        });
        managerHandler.getConfigurationManager().getKitsFile().set("hcf", null);
        Arrays.stream(HCFKit.values()).forEach(k -> {
            if (k.getInventory() != null)
                managerHandler.getConfigurationManager().getKitsFile().set("hcf." + k.toString() + ".inventory", BukkitSerilization.toBase64(k.getInventory()));
            if (k.getArmor() != null)
                managerHandler.getConfigurationManager().getKitsFile().set("hcf." + k.toString() + ".armor", BukkitSerilization.itemStackArrayToBase64(k.getArmor()));
        });
        managerHandler.getConfigurationManager().saveKitsFile();
    }
}
