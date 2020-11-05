package eu.revamp.practice.event.impl;

import eu.revamp.practice.event.PlayerEvent;
import org.bukkit.entity.Player;

public class BardTaskUpdateEvent extends PlayerEvent {

    public BardTaskUpdateEvent(Player player) {
        super(player);
    }
}
