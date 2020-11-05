package eu.revamp.practice.kit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public class CustomKit {

    private Inventory inventory;
    private ItemStack[] armor;
    private int number;

    @Setter
    private String name;
}
