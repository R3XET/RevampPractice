package eu.revamp.practice.event.impl;

import eu.revamp.practice.event.PlayerEvent;
import eu.revamp.practice.kit.hcf.data.bard.BardItem;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class BardHoldItemEvent extends PlayerEvent {

    private BardItem bardItem;

    public BardHoldItemEvent(Player player, BardItem bardItem) {
        super(player);
        this.bardItem = bardItem;
    }
}
