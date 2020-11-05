package eu.revamp.practice.listener.hcf;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.kit.hcf.data.rogue.RogueData;
import eu.revamp.practice.kit.hcf.data.rogue.RogueItem;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyState;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
@RequiredArgsConstructor
public class RogueListener implements Listener {

    private final RevampPractice plugin;
    private RogueData rogueData = new RogueData();

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

        if (match.getKit() != null || practiceProfile.getHcfKit() != HCFKit.ROGUE) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {

            RogueItem rogueItem = rogueData.getRogueItemList().stream().filter(r -> r.getMaterial() == player.getItemInHand().getType()).findFirst().orElse(null);

            if (rogueItem == null) {
                return;
            }

            if (System.currentTimeMillis() - (rogueItem.getActivated().getType() == PotionEffectType.SPEED ? practiceProfile.getLastRogueSpeedBuff() : practiceProfile.getLastRogueJumpBuff()) <= 60000) {
                event.setCancelled(true);

                long timeLeft = 60 - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - (rogueItem.getActivated().getType() == PotionEffectType.SPEED ? practiceProfile.getLastRogueSpeedBuff() : practiceProfile.getLastRogueJumpBuff()));

                player.sendMessage(Messages.MUST_WAIT_TO_ROGUE_BUFF.getMessage().replace("%time%", String.valueOf(timeLeft)));
                return;
            }
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
            player.updateInventory();

            if (rogueItem.getActivated().getType() == PotionEffectType.SPEED) {
                practiceProfile.setLastRogueSpeedBuff(System.currentTimeMillis());
            } else {
                practiceProfile.setLastRogueJumpBuff(System.currentTimeMillis());
            }

            player.removePotionEffect(rogueItem.getActivated().getType());
            player.addPotionEffect(rogueItem.getActivated());

            player.sendMessage(Messages.ACTIVATED_ROGUE_BUFF.getMessage().replace("%buff%", rogueItem.getName()));

            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                if (party.getPartyState() != PartyState.MATCH || party.getMatch() != match || !player.isOnline() || practiceProfile.getHcfKit() != HCFKit.ROGUE) {
                    return;
                }
                player.removePotionEffect(rogueItem.getActivated().getType());
                player.addPotionEffect(new PotionEffect(rogueItem.getActivated().getType(), Integer.MAX_VALUE, 2));
            }, 195L);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player attacked = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();

            PracticeProfile damagerProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(damager);

            if (damagerProfile.getPlayerState() != PlayerState.PARTY) {
                return;
            }

            Party party = damagerProfile.getParty();
            if (party.getPartyState() != PartyState.MATCH) {
                return;
            }

            Match match = party.getMatch();

            if (match.getKit() != null || damagerProfile.getHcfKit() != HCFKit.ROGUE) {
                return;
            }

            if (damager.getItemInHand().getType() != Material.GOLD_SWORD) {
                return;
            }

            if (System.currentTimeMillis() - damagerProfile.getLastRogueBackstab() <= 6000) {
                long timeLeft = 6 - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - damagerProfile.getLastRogueBackstab());
                damager.sendMessage(Messages.MUST_WAIT_TO_BACKSTAB.getMessage().replace("%time%", String.valueOf(timeLeft)));
                return;
            }

            if (this.direction(attacked).equals(this.direction(damager))) {
                damager.setItemInHand(new ItemStack(Material.AIR, 1));

                damager.playSound(damager.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);
                attacked.playSound(damager.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);

                attacked.sendMessage(Messages.BACKSTABBED.getMessage().replace("%player%", damager.getName()));
                damager.sendMessage(Messages.BACKSTABBED_PLAYER.getMessage().replace("%player%", attacked.getName()));


                if (attacked.getHealth() <= 0.0) return;
                if (attacked.getHealth() <= 4.0) {
                    attacked.damage(20.0);
                }
                else {
                    attacked.setHealth(attacked.getHealth() - 4.0);
                }


                damagerProfile.setLastRogueBackstab(System.currentTimeMillis());

                damager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                damager.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 0));
                event.setCancelled(true);
            }
        }
    }

    public Byte direction(Player player) {
        double rotation = (player.getLocation().getYaw() - 90.0f) % 360.0f;
        if (rotation < 0.0) {
            rotation += 360.0;
        }
        if (0.0 <= rotation && rotation < 22.5) {
            return 12;
        }
        if (22.5 <= rotation && rotation < 67.5) {
            return 14;
        }
        if (67.5 <= rotation && rotation < 112.5) {
            return 0;
        }
        if (112.5 <= rotation && rotation < 157.5) {
            return 2;
        }
        if (157.5 <= rotation && rotation < 202.5) {
            return 4;
        }
        if (202.5 <= rotation && rotation < 247.5) {
            return 6;
        }
        if (247.5 <= rotation && rotation < 292.5) {
            return 8;
        }
        if (292.5 <= rotation && rotation < 337.5) {
            return 10;
        }
        if (337.5 <= rotation && rotation < 360.0) {
            return 12;
        }
        return null;
    }
}
