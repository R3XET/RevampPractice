package eu.revamp.practice.task.player;

import eu.revamp.practice.RevampPractice;
import lombok.AllArgsConstructor;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class ArcherMarkTask extends BukkitRunnable {

    private final RevampPractice plugin;

    @Override
    public void run() {
        this.plugin.getServer().getOnlinePlayers().forEach(p -> {
            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(p);

            if (practiceProfile.getLastArcherMark() != 0) {

                if (System.currentTimeMillis() >= practiceProfile.getLastArcherMark() + 15000) {
                    practiceProfile.setLastArcherMark(0);

                    p.sendMessage(Messages.NO_LONGER_ARCHER_MARKED.getMessage());
                }
            }
        });
    }
}
