package eu.revamp.practice.listener.hcf;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.event.impl.BardHoldItemEvent;
import eu.revamp.practice.event.impl.BardTaskUpdateEvent;
import eu.revamp.practice.kit.hcf.HCFKit;
import eu.revamp.practice.kit.hcf.data.bard.BardData;
import eu.revamp.practice.kit.hcf.data.bard.BardItem;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.party.Party;
import eu.revamp.practice.party.PartyState;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import eu.revamp.practice.util.enums.Messages;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
@RequiredArgsConstructor
public class BardListener implements Listener {

    private final RevampPractice plugin;
    private BardData bardData = new BardData();

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

        if (match.getKit() != null || practiceProfile.getHcfKit() != HCFKit.BARD) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {

            BardItem bardItem = bardData.getBardItemList().stream().filter(b -> b.getMaterial() == player.getItemInHand().getType()).findFirst().orElse(null);

            if (bardItem == null) {
                return;
            }

            if (practiceProfile.getBardEnergy() < bardItem.getEnergy()) {
                event.setCancelled(true);
                player.sendMessage(Messages.NEED_MORE_ENERGY.getMessage().replace("%energy%", String.valueOf(bardItem.getEnergy())));
                return;
            }

            if (System.currentTimeMillis() - practiceProfile.getLastBardBuff() <= 6000) {
                event.setCancelled(true);

                long timeLeft = 6 - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - practiceProfile.getLastBardBuff());
                player.sendMessage(Messages.MUST_WAIT_TO_BARD_BUFF.getMessage().replace("%time%", String.valueOf(timeLeft)));
                return;
            }

            practiceProfile.setBardEnergy(practiceProfile.getBardEnergy() - bardItem.getEnergy());
            practiceProfile.setLastBardBuff(System.currentTimeMillis());

            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
            player.updateInventory();

            player.sendMessage(Messages.ACTIVATED_BARD_BUFF.getMessage().replace("%buff%", bardItem.getName()));
            party.getMembers().stream().filter(u -> !match.getDead().contains(u) && this.plugin.getServer().getPlayer(u).getLocation().distanceSquared(player.getLocation()) <= 400).forEach(u -> {
                Player member = this.plugin.getServer().getPlayer(u);
                if ((member == player && bardItem.isBard()) || member != player) {
                    member.removePotionEffect(bardItem.getHeld().getType());
                    member.addPotionEffect(bardItem.getActivated());
                }
            });
        }
    }

    @EventHandler
    public void onBardHold(BardHoldItemEvent event) {
        Player player = event.getPlayer();
        BardItem bardItem = event.getBardItem();

        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
        Party party = practiceProfile.getParty();
        Match match = party.getMatch();

        party.getMembers().stream().filter(u -> !match.getDead().contains(u) && this.plugin.getServer().getPlayer(u).getLocation().distanceSquared(player.getLocation()) <= 400).forEach(u -> {
            Player member = this.plugin.getServer().getPlayer(u);
            PracticeProfile memberProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(member);

            if (this.plugin.getManagerHandler().getPlayerManager().getPotionEffect(member, bardItem.getHeld().getType()) != null) {
                PotionEffect potionEffect = this.plugin.getManagerHandler().getPlayerManager().getPotionEffect(member, bardItem.getHeld().getType());

                if (potionEffect.getDuration() > 140) {
                    memberProfile.getPotionEffectsList().add(potionEffect);

                    if ((member == player && bardItem.isBard() && bardItem.getHeld().getType() != PotionEffectType.DAMAGE_RESISTANCE && bardItem.getHeld().getType() != PotionEffectType.REGENERATION) || (member != player || potionEffect.getAmplifier() <= bardItem.getHeld().getAmplifier()))
                        member.removePotionEffect(bardItem.getHeld().getType());
                }

            }
            if ((member == player && bardItem.isBard() && bardItem.getHeld().getType() != PotionEffectType.DAMAGE_RESISTANCE && bardItem.getHeld().getType() != PotionEffectType.REGENERATION) || member != player)
                member.addPotionEffect(bardItem.getHeld());
        });
    }

    @EventHandler
    public void onBardTask(BardTaskUpdateEvent event) {
        try {
            Player player = event.getPlayer();

            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
            Party party = practiceProfile.getParty();
            Match match = party.getMatch();

            party.getMembers().stream().filter(u -> !match.getDead().contains(u)).forEach(u -> {
                Player member = this.plugin.getServer().getPlayer(u);
                restoreEffects(member);
            });
        } catch (Exception ex) {
            // remove random errors :D
        }
    }

    private void restoreEffects(Player player) {
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        for (PotionEffect potionEffect : practiceProfile.getPotionEffectsList()) {

            if (bardData.getBlockedList().contains(potionEffect.getType())) {
                practiceProfile.getPotionEffectsList().remove(potionEffect);
                return;
            }

            if (!player.hasPotionEffect(potionEffect.getType())) removeType(player, potionEffect.getType());
            player.addPotionEffect(potionEffect);
        }
    }

    private void removeType(Player player, PotionEffectType potionEffectType) {
        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        for (int i = 0; i < practiceProfile.getPotionEffectsList().size(); i++) {
            PotionEffect potionEffect = practiceProfile.getPotionEffectsList().get(i);

            if (potionEffect.getType() == potionEffectType && potionEffect.getDuration() <= 100) {
                practiceProfile.getPotionEffectsList().remove(potionEffect);
            }
        }
    }
}
