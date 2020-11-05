package eu.revamp.practice.task.player;

import eu.revamp.practice.RevampPractice;
import lombok.AllArgsConstructor;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class RogueBackstabTask extends BukkitRunnable {

    private final RevampPractice plugin;

    @Override
    public void run() {
        this.plugin.getServer().getOnlinePlayers().forEach(p -> {
            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(p);

            if (practiceProfile.getLastRogueBackstab() != 0) {

                if (System.currentTimeMillis() >= practiceProfile.getLastRogueBackstab() + 6000) {

                    practiceProfile.setLastRogueBackstab(0);
                    p.sendMessage(Messages.MAY_BACKSTAB_AGAIN.getMessage());
                }
            }
        });
    }
}
