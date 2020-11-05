package eu.revamp.practice.kit.hcf.data.bard;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BardData {

    private List<BardItem> bardItemList;
    private List<PotionEffectType> blockedList;

    public BardData() {
        bardItemList = new ArrayList<>();

        bardItemList.add(new BardItem("Speed III", Material.SUGAR, 35, new PotionEffect(PotionEffectType.SPEED, 120, 1), new PotionEffect(PotionEffectType.SPEED, 120, 2), true));
        bardItemList.add(new BardItem("Resistance III", Material.IRON_INGOT, 50, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 0), new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 2), true));
        bardItemList.add(new BardItem("Jumpboost VII", Material.FEATHER, 40, new PotionEffect(PotionEffectType.JUMP, 120, 1), new PotionEffect(PotionEffectType.JUMP, 120, 6), true));
        bardItemList.add(new BardItem("Strength II", Material.BLAZE_POWDER, 50, new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 0), new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 1), false));
        bardItemList.add(new BardItem("Regen IV", Material.GHAST_TEAR, 50, new PotionEffect(PotionEffectType.REGENERATION, 120, 0), new PotionEffect(PotionEffectType.REGENERATION, 100, 3), true));
        bardItemList.add(new BardItem("Fire Resistance", Material.MAGMA_CREAM, 40, new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 120, 0), new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 400, 0), true));

        blockedList = new ArrayList<>();
        blockedList.add(PotionEffectType.FIRE_RESISTANCE);
        blockedList.add(PotionEffectType.INCREASE_DAMAGE);
        blockedList.add(PotionEffectType.JUMP);
    }

}
