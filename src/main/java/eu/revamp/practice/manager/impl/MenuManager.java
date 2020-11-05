package eu.revamp.practice.manager.impl;

import eu.revamp.spigot.utils.chat.color.CC;
import eu.revamp.spigot.utils.item.ItemBuilder;
import eu.revamp.practice.manager.Manager;
import eu.revamp.practice.manager.ManagerHandler;
import eu.revamp.practice.util.enums.Menus;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuManager extends Manager {

    public MenuManager(ManagerHandler managerHandler) {
        super(managerHandler);

        fetch();
    }

    private void fetch() {
        Arrays.stream(Menus.values()).forEach(m -> {
            String name = CC.translate(managerHandler.getPlugin().getConfig().getString("menus." + m.toString() + ".name"));
            int slots = 9 * managerHandler.getPlugin().getConfig().getInt("menus." + m.toString() + ".rows");
            String prefix = managerHandler.getPlugin().getConfig().getString("menus." + m.toString() + ".item-prefix");
            String itemPrefix = CC.translate(prefix == null ? "" : prefix);
            List<String> lore = managerHandler.getPlugin().getConfig().getStringList("menus." + m.toString() + ".item-lore");
            Inventory inventory = managerHandler.getPlugin().getServer().createInventory(null, slots, name);

            m.setInventory(inventory);

            if (lore != null) m.setItemLore(lore);

            if (m == Menus.SETTINGS) {
                if (!managerHandler.getPlugin().getConfig().getBoolean("settings.settings-gui")) return;

                List<String> stringList = new ArrayList<>(managerHandler.getPlugin().getConfig().getConfigurationSection("menus.SETTINGS.items").getKeys(false));

                stringList.forEach(s -> {
                    Menus.SETTINGS.getInventory().addItem(
                            new ItemBuilder(Material.getMaterial(managerHandler.getPlugin().getConfig().getInt("menus.SETTINGS.items." + s + ".id")))
                                    .setName(CC.translate(managerHandler.getPlugin().getConfig().getString("menus.SETTINGS.items." + s + ".name")))
                                    .toItemStack());
                });
            }
            m.setItemPrefix(itemPrefix);
        });
    }
}
