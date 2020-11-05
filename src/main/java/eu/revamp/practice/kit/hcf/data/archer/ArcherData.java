package eu.revamp.practice.kit.hcf.data.archer;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ArcherData {

    private List<ArcherItem> archerItemList;

    public ArcherData() {
        archerItemList = new ArrayList<>();

        archerItemList.add(new ArcherItem("Speed IV", Material.SUGAR, new PotionEffect(PotionEffectType.SPEED, 200, 3)));
        archerItemList.add(new ArcherItem("Jumpboost V", Material.FEATHER, new PotionEffect(PotionEffectType.JUMP, 200, 4)));
    }
}
