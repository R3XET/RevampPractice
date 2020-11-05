package eu.revamp.practice.listener;

import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class NPCListener implements Listener {

    @EventHandler
    public void onNPCClick(PlayerInteractAtEntityEvent event){
        Player player = event.getPlayer();
        if (event.getRightClicked() instanceof NPC){
            NPC clickedNPC = (NPC) event.getRightClicked();
            if (clickedNPC.getName().equals("KitEditor")) {

            }
        }
    }
}
