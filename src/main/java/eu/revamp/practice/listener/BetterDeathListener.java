package eu.revamp.practice.listener;
/*
import eu.revamp.practice.RevampPractice;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
public class BetterDeathListener implements Listener {

    private final RevampPractice plugin;

    public class MatchListener implements Listener {

        @EventHandler
        public void onDeath(PlayerDeathEvent event) {
            (new BukkitRunnable() {
                public void run() {
                    List<ItemStack> list = event.getDrops();
                    Iterator<ItemStack> i = list.iterator();
                    while (i.hasNext()) {
                        ItemStack item = i.next();
                        i.remove();
                    }
                }
            }).runTaskLater(RevampPractice.getInstance(), 10L);
        }
    }
}
*/