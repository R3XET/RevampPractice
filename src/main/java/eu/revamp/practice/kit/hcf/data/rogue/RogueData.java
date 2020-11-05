package eu.revamp.practice.kit.hcf.data.rogue;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RogueData {

    private List<RogueItem> rogueItemList;

    public RogueData() {
        rogueItemList = new ArrayList<>();

        rogueItemList.add(new RogueItem("Speed IV", Material.SUGAR, new PotionEffect(PotionEffectType.SPEED, 200, 3)));
        rogueItemList.add(new RogueItem("Jumpboost V", Material.FEATHER, new PotionEffect(PotionEffectType.JUMP, 200, 4)));
    }

}
