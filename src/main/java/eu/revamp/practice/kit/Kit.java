package eu.revamp.practice.kit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class Kit {

    private final String name;
    private ItemStack display = new ItemStack(Material.IRON_SWORD);
    private Inventory inventory;
    private ItemStack[] armor;
    private boolean editChest;
    private Inventory editInventory;
    private KitType kitType = KitType.NORMAL;
    private int damageTicks = 19;
    private boolean ranked;
    private boolean unranked;
    private boolean editable;

    private List<UUID> unrankedQueue = new ArrayList<>();
    private List<UUID> unrankedMatch = new ArrayList<>();
    private List<UUID> rankedQueue = new ArrayList<>();
    private List<UUID> rankedMatch = new ArrayList<>();
}
