package eu.revamp.practice.listener.hcf;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.kit.hcf.data.archer.ArcherData;
import eu.revamp.practice.kit.hcf.data.archer.ArcherItem;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyState;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
@RequiredArgsConstructor
public class ArcherListener implements Listener {

    private final RevampPractice plugin;
    private ArcherData archerData = new ArcherData();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        if (practiceProfile.getPlayerState() != PlayerState.PARTY) {
            return;
        }

        Party party = practiceProfile.getParty();
        if (party.getPartyState() != PartyState.MATCH) {
            return;
        }

        Match match = party.getMatch();

        if (match.getKit() != null || practiceProfile.getHcfKit() != HCFKit.ARCHER) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {

            ArcherItem archerItem = archerData.getArcherItemList().stream().filter(a -> a.getMaterial() == player.getItemInHand().getType()).findFirst().orElse(null);

            if (archerItem == null) {
                return;
            }

            if (System.currentTimeMillis() - (archerItem.getActivated().getType() == PotionEffectType.SPEED ? practiceProfile.getLastArcherSpeedBuff() : practiceProfile.getLastArcherJumpBuff()) <= 60000) {
                event.setCancelled(true);

                long timeLeft = 60 - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - (archerItem.getActivated().getType() == PotionEffectType.SPEED ? practiceProfile.getLastArcherSpeedBuff() : practiceProfile.getLastArcherJumpBuff()));

                player.sendMessage(Messages.MUST_WAIT_TO_ARCHER_BUFF.getMessage().replace("%time%", String.valueOf(timeLeft)));
                return;
            }
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
            player.updateInventory();

            if (archerItem.getActivated().getType() == PotionEffectType.SPEED) {
                practiceProfile.setLastArcherSpeedBuff(System.currentTimeMillis());
            } else {
                practiceProfile.setLastArcherJumpBuff(System.currentTimeMillis());
            }

            player.removePotionEffect(archerItem.getActivated().getType());
            player.addPotionEffect(archerItem.getActivated());

            player.sendMessage(Messages.ACTIVATED_ARCHER_BUFF.getMessage().replace("%buff%", archerItem.getName()));

            if (archerItem.getActivated().getType() == PotionEffectType.SPEED) {
                this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                    if (party.getPartyState() != PartyState.MATCH || party.getMatch() != match || !player.isOnline() || practiceProfile.getHcfKit() != HCFKit.ARCHER) {
                        return;
                    }
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                }, 195L);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent  event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            Player attacked = (Player) event.getEntity();
            PracticeProfile attackedProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(attacked);

            if (System.currentTimeMillis() - attackedProfile.getLastArcherMark() <= 15000) {
                event.setDamage(event.getDamage() * 1.3);
            }

            if (event.getDamager() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getDamager();

                if (arrow.getShooter() instanceof Player) {
                    Player damager = (Player) arrow.getShooter();
                    PracticeProfile damagerProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(damager);

                    if (damagerProfile.getPlayerState() != PlayerState.PARTY) {
                        return;
                    }

                    Party party = damagerProfile.getParty();
                    if (party.getPartyState() != PartyState.MATCH) {
                        return;
                    }

                    Match match = party.getMatch();

                    if (match.getKit() != null || damagerProfile.getHcfKit() != HCFKit.ARCHER) {
                        return;
                    }
                    if (attackedProfile.getLastArcherMark() == 0) {
                        attacked.sendMessage(Messages.ARCHER_TAGGED.getMessage());
                        damager.sendMessage(Messages.PLAYER_ARCHER_TAGGED.getMessage().replace("%player%", attacked.getName()));
                    }
                    attackedProfile.setLastArcherMark(System.currentTimeMillis());
                }
            }
        }
    }
}
