package eu.revamp.practice.kit.hcf.data.bard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;

@Getter
@AllArgsConstructor
public class BardItem {

    private String name;
    private Material material;
    private double energy;
    private PotionEffect held;
    private PotionEffect activated;
    private boolean bard;

}
