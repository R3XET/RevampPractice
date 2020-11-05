package eu.revamp.practice.match;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.kit.KitType;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class MatchResetRunnable extends BukkitRunnable {

    private final RevampPractice plugin = RevampPractice.getInstance();
    private final Match match;

    @Override
    public void run() {
        int count = 0;
        try {
            if (this.match.getKit() != null) {
                if (this.match.getKit().getKitType() == KitType.BUILDUHC) {
                    for (Location location : this.match.getPlacedBlockLocations()) {
                        if (++count <= 15) {
                            location.getBlock().setType(Material.AIR);
                            this.match.removePlacedBlockLocation(location);
                        } else {
                            break;
                        }
                    }
                } else {
                    for (BlockState blockState : this.match.getOriginalBlockChanges()) {
                        if (++count <= 15) {
                            blockState.getLocation().getBlock().setType(blockState.getType());
                            this.match.removeOriginalBlockChange(blockState);
                        } else {
                            break;
                        }
                    }
                }
                if (count < 15) {
                    //this.match.getArena().addAvailableArena(this.match.getArena());
                    this.cancel();
                }
            }
        }
        catch(IllegalStateException ignored){
        }
    }
}

