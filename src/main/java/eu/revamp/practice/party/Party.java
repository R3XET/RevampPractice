package eu.revamp.practice.party;

import eu.revamp.practice.RevampPractice;
import lombok.Getter;
import lombok.Setter;
import eu.revamp.practice.match.Match;
import eu.revamp.practice.match.MatchRequest;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Party {

    private UUID leader;
    private PartyState partyState = PartyState.LOBBY;
    private Match match;
    private List<UUID> members = new ArrayList<>();
    private boolean open;
    private List<MatchRequest> matchRequestList = new ArrayList<>();
    private Party dueling;

    public void broadcast(String message) {
        members.forEach(u -> RevampPractice.getInstance().getServer().getPlayer(u).sendMessage(message));
    }

    public void broadcast(TextComponent message) {
        members.forEach(u -> RevampPractice.getInstance().getServer().getPlayer(u).spigot().sendMessage(message));
    }

}
