package eu.revamp.practice.util.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public enum Items {

    SETTINGS(null, 0),
    UNRANKED(null, 0),
    RANKED(null, 0),
    PARTY(null, 0),
    LEADERBOARDS(null, 0),
    EDITKIT(null, 0),
    LEAVE_QUEUE(null, 0),
    CUSTOM_KIT(null, 0),
    DEFAULT_KIT(null, 0),
    PARTY_EVENTS(null, 0),
    HCF_SELECTOR(null, 0),
    LEADER_PARTY_INFO(null, 0),
    PLAYER_PARTY_INFO(null, 0),
    DISBAND_PARTY(null, 0),
    LEAVE_PARTY(null, 0),
    LEAVE_EVENT(null, 0),
    LEAVE_TOURNAMENT(null, 0),
    STOP_SPECTATING(null, 0);

    @Setter
    private ItemStack item;

    @Setter
    private int slot;
}
