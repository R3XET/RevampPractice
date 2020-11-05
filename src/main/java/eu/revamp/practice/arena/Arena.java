package eu.revamp.practice.arena;

import eu.revamp.practice.kit.KitType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class Arena {

    private final String name;
    private Location l1;
    private Location l2;
    private KitType kitType = KitType.NORMAL;

    private List<Arena> standaloneArenas;
    private List<Arena> availableArenas;

    private boolean enabled;

    public Arena getAvailableArena() {
        Arena arena = this.availableArenas.get(0);

        this.availableArenas.remove(0);

        return arena;
    }

    public void addStandaloneArena(Arena arena) {
        this.standaloneArenas.add(arena);
    }

    public void addAvailableArena(Arena arena) {
        this.availableArenas.add(arena);
    }
}
