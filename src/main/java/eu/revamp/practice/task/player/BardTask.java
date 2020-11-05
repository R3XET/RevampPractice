package eu.revamp.practice.task.player;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.kit.hcf.data.bard.BardData;
import lombok.RequiredArgsConstructor;
import eu.revamp.practice.event.impl.BardHoldItemEvent;
import eu.revamp.practice.event.impl.BardTaskUpdateEvent;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyState;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class BardTask extends BukkitRunnable {

    private final RevampPractice plugin;
    private final Player player;
    private int i;
    private BardData bardData = new BardData();

    @Override
    public void run() {
        if (!player.isOnline()) {
            this.cancel();
            return;
        }

        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        i += 1;

        if (i % 10 == 0) {
            practiceProfile.setBardEnergy(practiceProfile.getBardEnergy() + 1 >= 100 ? 100 : practiceProfile.getBardEnergy() + 1);
        }

        bardData.getBardItemList().stream().filter(b -> player.getItemInHand().getType() == b.getMaterial()).findFirst().ifPresent(bardItem -> new BardHoldItemEvent(player, bardItem).call());

        new BardTaskUpdateEvent(player).call();

        if (practiceProfile.getPlayerState() != PlayerState.PARTY) {
            this.cancel();
            return;
        }

        Party party = practiceProfile.getParty();
        if (party.getPartyState() != PartyState.MATCH) {
            this.cancel();
            return;
        }
        Match match = party.getMatch();
        if (match.getKit() != null || practiceProfile.getHcfKit() != HCFKit.BARD || match.getDead().contains(player.getUniqueId())) {
            this.cancel();
        }
    }
}
