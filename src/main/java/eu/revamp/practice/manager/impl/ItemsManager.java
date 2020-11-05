package eu.revamp.practice.manager.impl;

import eu.revamp.practice.util.enums.Items;
import eu.revamp.spigot.utils.chat.color.CC;
import eu.revamp.spigot.utils.item.ItemBuilder;
import eu.revamp.practice.manager.Manager;
import eu.revamp.practice.manager.ManagerHandler;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class ItemsManager extends Manager {

    public ItemsManager(ManagerHandler managerHandler) {
        super(managerHandler);

        fetch();
    }

    private void fetch() {
        Arrays.stream(Items.values()).forEach(item -> {
            String[] material = managerHandler.getPlugin().getConfig().getString("items." + item.toString() + ".id").split(":");
            String name = CC.translate(managerHandler.getPlugin().getConfig().getString("items." + item.toString() + ".name"));
            int slot = managerHandler.getPlugin().getConfig().getInt("items." + item.toString() + ".slot");
            boolean loreEnabled = managerHandler.getPlugin().getConfig().getBoolean("items." + item.toString() + ".lore-enabled");
            List<String> lore = managerHandler.getPlugin().getConfig().getStringList("items." + item.toString() + ".lore");

            int id = Integer.parseInt(material[0]);
            short durability = (short) Integer.parseInt(material[1]);

            item.setItem(new ItemBuilder(Material.getMaterial(id)).setDurability(durability).setName(name).setLore(loreEnabled ? lore : null).toItemStack());
            item.setSlot(slot);
        });
    }
}
