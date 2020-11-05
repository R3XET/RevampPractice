package eu.revamp.practice.kit.hcf.data.archer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;

@Getter
@AllArgsConstructor
public class ArcherItem {

    private String name;
    private Material material;
    private PotionEffect activated;

}