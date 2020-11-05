package eu.revamp.practice.kit;

import java.util.Arrays;

public enum KitType {

    NORMAL, SUMO, HCF, BUILDUHC, BEDWARS, SPLEEF;

    public static KitType getType(String name) {
        return Arrays.stream(values()).filter(t -> t.toString().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
