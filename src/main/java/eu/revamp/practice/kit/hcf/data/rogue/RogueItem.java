package eu.revamp.practice.kit.hcf.data.rogue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;

@Getter
@AllArgsConstructor
public class RogueItem {

    private String name;
    private Material material;
    private PotionEffect activated;

}