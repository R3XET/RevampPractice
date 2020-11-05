package eu.revamp.practice.party;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PartyInvite {

    private final Party party;
    private long timestamp = System.currentTimeMillis();
}
