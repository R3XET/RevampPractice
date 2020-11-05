package eu.revamp.practice.task;

import eu.revamp.practice.RevampPractice;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class MongoTask extends BukkitRunnable {

    private final RevampPractice plugin;

    @Override
    public void run() {
        this.plugin.getServer().getOnlinePlayers().forEach(this.plugin.getManagerHandler().getPlayerManager()::save);
    }
}
