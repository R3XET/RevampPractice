package eu.revamp.practice.queue;

import eu.revamp.practice.kit.Kit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class Queue {

    private final Kit kit;
    private final boolean ranked;
    private final long startTime;

    @Setter
    private int minElo;

    @Setter
    private int maxElo;
}
