package eu.revamp.practice.util.misc;

import eu.revamp.practice.RevampPractice;
import eu.revamp.spigot.utils.chat.color.CC;
import eu.revamp.spigot.utils.item.ItemBuilder;
import lombok.Getter;
import eu.revamp.practice.player.PracticeProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class InventorySnapshot {

    private Inventory inventory;
    private Player opponent;

    private static HashMap<UUID, InventorySnapshot> inv = new HashMap<>();

    public InventorySnapshot(Player player) {
        PracticeProfile practiceProfile = RevampPractice.getInstance().getManagerHandler().getProfileManager().getProfile(player);
        final ItemStack[] contents1 = player.getInventory().getContents();
        final ItemStack[] armor1 = player.getInventory().getArmorContents();
        List<ItemStack> contents = new ArrayList<>();
        List<ItemStack> armor = new ArrayList<>();
        for (int i = 0; i < contents1.length; i++) {
            ItemStack itemStack = contents1[i];
            contents.add(i, itemStack);
        }
        for (int i = 0; i < armor1.length; i++) {
            ItemStack itemStack = armor1[i];
            armor.add(i, itemStack);
        }

        this.inventory = Bukkit.createInventory(null, 54, CC.translate(RevampPractice.getInstance().getConfig().getString("menus.SNAPSHOT.name")).replace("%player%", player.getName()));
        final int potCount = (int) Arrays.stream(contents1).filter(Objects::nonNull).map(ItemStack::getDurability).filter(d -> d == 16421).count();
        final double health = player.getHealth();
        for (int i = 0; i < 9; ++i) {
            if (contents.size() >= (i + 27) + 1) {
                this.inventory.setItem(i + 27, contents.get(i));
                this.inventory.setItem(i + 18, contents.get(i + 27));
                this.inventory.setItem(i + 9, contents.get(i + 18));
                this.inventory.setItem(i, contents.get(i + 9));
            }
        }
        double multiplier = 100 / (practiceProfile.getThrownPots() > 0 ? practiceProfile.getThrownPots() : 1);
        double potAccuracy = (multiplier * practiceProfile.getFullyLandedPots());
        if (potAccuracy > 100) {
            potAccuracy = 100;
        }
        Map<String, ItemStack> itemMap = new HashMap<>();
        for (String i : RevampPractice.getInstance().getConfig().getConfigurationSection("menus.SNAPSHOT.items").getKeys(false)) {
            String[] material = RevampPractice.getInstance().getConfig().getString("menus.SNAPSHOT.items." + i + ".id").split(":");
            String name = CC.translate(RevampPractice.getInstance().getConfig().getString("menus.SNAPSHOT.items." + i + ".name"));
            List<String> lore = new ArrayList<>();
            for (String l : RevampPractice.getInstance().getConfig().getStringList("menus.SNAPSHOT.items." + i + ".lore")) {
                l = l.replace("%hits%", String.valueOf(practiceProfile.getHits()));
                l = l.replace("%combo%", String.valueOf(practiceProfile.getCombo()));
                l = l.replace("%longestCombo%", String.valueOf(practiceProfile.getLongestCombo()));
                l = l.replace("%accuracy%", String.valueOf(potAccuracy));
                l = l.replace("%player%", player.getName());
                lore.add(l);
            }
            itemMap.put(i, new ItemBuilder(Material.getMaterial(Integer.parseInt(material[0]))).setDurability((short) Integer.parseInt(material[1])).setName(name.replace("%potions%", String.valueOf(potCount)).replace("%health%", String.valueOf(roundToHalves(health / 2)))).setLore(lore).toItemStack());
        }
        this.inventory.setItem(RevampPractice.getInstance().getConfig().getInt("menus.SNAPSHOT.items.player.slot"), itemMap.get("player"));
        this.inventory.setItem(RevampPractice.getInstance().getConfig().getInt("menus.SNAPSHOT.items.stats.slot"), itemMap.get("stats"));
        this.inventory.setItem(RevampPractice.getInstance().getConfig().getInt("menus.SNAPSHOT.items.pots.slot"), itemMap.get("pots"));
        this.inventory.setItem(RevampPractice.getInstance().getConfig().getInt("menus.SNAPSHOT.items.switch-inventory.slot"), itemMap.get("switch-inventory"));

        for (int i = 0; i < 4; ++i) {
            if (contents.size() >= i) {
                this.inventory.setItem(39 - i, armor.get(i));
            }
            inv.put(player.getUniqueId(), this);
        }
    }

    public static InventorySnapshot getByUUID(UUID uuid) {
        return inv.get(uuid);
    }

    public static double roundToHalves(double d) {
        return Math.round(d * 2.0D) / 2.0D;
    }

}
