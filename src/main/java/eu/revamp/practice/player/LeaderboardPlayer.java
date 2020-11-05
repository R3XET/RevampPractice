package eu.revamp.practice.player;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LeaderboardPlayer {

    private String name;
    private int elo;
}
