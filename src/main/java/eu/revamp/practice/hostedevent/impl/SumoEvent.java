package eu.revamp.practice.hostedevent.impl;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.hostedevent.EventState;
import eu.revamp.practice.util.enums.Messages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import eu.revamp.practice.hostedevent.HostedEvent;
import eu.revamp.practice.player.PlayerState;
import eu.revamp.practice.player.PracticeProfile;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class SumoEvent extends HostedEvent {

    private final RevampPractice plugin;
    
    private Player p1;
    private Player p2;

    @Setter
    private SumoState sumoState = SumoState.STARTING;

    @Override
    public String getName() {
        return "Sumo";
    }

    @Override
    public void startRound() {
        if (!check()) {

            sumoState = SumoState.STARTING;

            Collections.shuffle(getPlayers());
            p1 = this.plugin.getServer().getPlayer(getPlayers().get(0));
            p2 = this.plugin.getServer().getPlayer(getPlayers().get(1));

            PracticeProfile profile1 = this.plugin.getManagerHandler().getProfileManager().getProfile(p1);
            PracticeProfile profile2 = this.plugin.getManagerHandler().getProfileManager().getProfile(p2);


            profile1.setPlayerState(PlayerState.EVENT_MATCH);
            profile2.setPlayerState(PlayerState.EVENT_MATCH);

            broadcast(Messages.SUMO_EVENT_MATCHUP.getMessage().replace("%player1%", p1.getName()).replace("%player2%", p2.getName()));

            p1.teleport(getFirst());
            p2.teleport(getSecond());

            p1.getInventory().clear();
            p1.updateInventory();

            p2.getInventory().clear();
            p2.updateInventory();

            new BukkitRunnable() {
                int i = 5;

                public void run() {
                    if (sumoState != SumoState.STARTING) {
                        this.cancel();
                        return;
                    }
                    if (i > 0) {
                        broadcast(Messages.SUMO_EVENT_MATCH_STARTING.getMessage().replace("%time%", String.valueOf(i)));
                        playSound(Sound.NOTE_STICKS);
                        i -= 1;
                    } else {
                        sumoState = SumoState.STARTED;
                        broadcast(Messages.SUMO_EVENT_MATCH_STARTED.getMessage());
                        playSound(Sound.NOTE_PLING);
                        this.cancel();
                    }
                }
            }.runTaskTimer(this.plugin, 20L, 20L);
        }
    }

    @Override
    public void eliminatePlayer(Player player) {
        super.eliminatePlayer(player);

        p1.teleport(getLobby());
        p2.teleport(getLobby());

        PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);

        getSpectators().add(player.getUniqueId());
        practiceProfile.setPlayerState(PlayerState.SPECTATING_EVENT);
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);

        this.plugin.getManagerHandler().getPlayerManager().giveItems(p1, false);
        this.plugin.getManagerHandler().getPlayerManager().giveItems(p2, false);

        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            getPlayers().forEach(u -> {
                Player players = this.plugin.getServer().getPlayer(u);

                players.hidePlayer(player);
            });
        }, 1L);
        p1 = p2 = null;

        if (!check()) {
            new BukkitRunnable() {
                int i = 5;

                public void run() {

                    if (i > 0) {
                        broadcast(Messages.SUMO_ROUND_STARTING.getMessage().replace("%time%", String.valueOf(i)));
                        playSound(Sound.NOTE_STICKS);
                        i -= 1;
                    } else {
                        startRound();
                        playSound(Sound.NOTE_PLING);
                        this.cancel();
                    }
                }
            }.runTaskTimer(this.plugin, 20L, 20L);
        }
    }

    @Override
    public void end(String name, String winner) {
        super.end(name, winner);

        List<UUID> allPlayers = new ArrayList<>();
        allPlayers.addAll(getPlayers());
        allPlayers.addAll(getSpectators());

        allPlayers.forEach(u -> {
            Player player = this.plugin.getServer().getPlayer(u);

            PracticeProfile practiceProfile = this.plugin.getManagerHandler().getProfileManager().getProfile(player);
            practiceProfile.setPlayerState(PlayerState.LOBBY);
            practiceProfile.setHostedEvent(null);

            this.plugin.getManagerHandler().getPlayerManager().giveItems(player, true);
            this.plugin.getManagerHandler().getPlayerManager().teleportSpawn(player);
        });

        getPlayers().clear();
        getSpectators().clear();

        setDown(0);
        setStartTime(0);

        sumoState = SumoState.STARTING;
        setEventState(EventState.STARTING);
        this.plugin.getManagerHandler().getEventManager().getCurrentEvents().remove(this);

        p1 = p2 = null;
    }

    private boolean check() {
        if (getPlayers().size() == 0) {
            end(getName(), null);
            return true;
        } else if (getPlayers().size() == 1) {
            end(getName(), this.plugin.getServer().getPlayer(getPlayers().get(0)).getName());
            return true;
        }
        return false;
    }

    public enum SumoState {

        STARTING, STARTED
    }

}
