package eu.revamp.practice.hostedevent;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.util.enums.Messages;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class HostedEvent {

    private String name;
    private Location lobby;
    private Location first;
    private Location second;
    private EventState eventState = EventState.STARTING;
    private List<UUID> players = new ArrayList<>();
    private List<UUID> spectators = new ArrayList<>();
    private long startTime;
    private long down;

    public void startRound() {
    }

    public void eliminatePlayer(Player player) {
        if (!getPlayers().contains(player.getUniqueId())) return;

        getPlayers().remove(player.getUniqueId());
        getSpectators().add(player.getUniqueId());

        broadcast(Messages.EVENT_PLAYER_ELIMINATED.getMessage().replace("%player%", player.getName()));

    }

    public void end(String name, String winner) {
        RevampPractice.getInstance().getServer().broadcastMessage(winner != null ? Messages.EVENT_WINNER.getMessage().replace("%event%", name).replace("%player%", winner) : Messages.EVENT_CANCELLED.getMessage().replace("%event%", name));
    }

    public void broadcast(String message) {
        getPlayers().forEach(u -> RevampPractice.getInstance().getServer().getPlayer(u).sendMessage(message));
        getSpectators().forEach(u -> RevampPractice.getInstance().getServer().getPlayer(u).sendMessage(message));
    }

    public void playSound(Sound sound) {
        getPlayers().forEach(u -> RevampPractice.getInstance().getServer().getPlayer(u).playSound(RevampPractice.getInstance().getServer().getPlayer(u).getLocation(), sound, 20f, 20f));
        getSpectators().forEach(u -> RevampPractice.getInstance().getServer().getPlayer(u).playSound(RevampPractice.getInstance().getServer().getPlayer(u).getLocation(), sound, 20f, 20f));
    }

}
