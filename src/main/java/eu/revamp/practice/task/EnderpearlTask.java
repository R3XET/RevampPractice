package eu.revamp.practice.task;

import eu.revamp.practice.RevampPractice;
import lombok.AllArgsConstructor;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class EnderpearlTask extends BukkitRunnable {

    private final RevampPractice plugin;

    @Override
    public void run() {
        this.plugin.getServer().getOnlinePlayers().forEach(p -> {
            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(p);

            if (practiceProfile.getLastEnderpearl() != 0) {

                int difference = 16 - (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - practiceProfile.getLastEnderpearl());
                p.setLevel(difference);

                p.setExp(p.getExp() - 0.003125f);

                if (System.currentTimeMillis() >= practiceProfile.getLastEnderpearl() + 16000) {
                    practiceProfile.setLastEnderpearl(0);

                    p.setLevel(0);
                    p.setExp(0f);
                    p.sendMessage(Messages.MAY_PEARL_AGAIN.getMessage());
                }
            }
        });
    }
}
