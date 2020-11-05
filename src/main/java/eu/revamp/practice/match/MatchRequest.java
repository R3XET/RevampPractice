package eu.revamp.practice.match;

import eu.revamp.practice.kit.Kit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class MatchRequest {

    private final UUID requester;
    private final Kit kit;
    private final boolean party;
    private long timestamp = System.currentTimeMillis();

}
