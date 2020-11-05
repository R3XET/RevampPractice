package eu.revamp.practice.kit.hcf;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public enum HCFKit {

    DIAMOND("Diamond", null, null),
    BARD("Bard", null, null),
    ARCHER("Archer", null, null),
    ROGUE("Rogue", null, null);

    private String name;

    @Setter
    private Inventory inventory;

    @Setter
    private ItemStack[] armor;
}
