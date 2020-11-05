package eu.revamp.practice.util.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;

import java.util.List;

@Getter
@AllArgsConstructor
public enum Menus {

    UNRANKED(null, null, null),
    RANKED(null, null, null),
    EDITKIT(null, null, null),
    PARTYFFA(null, null, null),
    PARTYSPLIT(null, null, null),
    OTHERPARTIES(null, null, null),
    MATCHES(null, null, null),
    DUEL(null, null, null),
    TOURNAMENT(null, null, null),
    LEADERBOARDS(null, null, null),
    SETTINGS(null, null, null);

    @Setter
    private Inventory inventory;

    @Setter
    private String itemPrefix;

    @Setter
    private List<String> itemLore;

}
