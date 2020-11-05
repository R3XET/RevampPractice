package eu.revamp.practice.util.world;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

@Getter @Setter
public class WorldResetManager {

    private String world;
    private boolean goingToRollBack;

    public boolean rollBack() {
        World w = Bukkit.getWorld(world);
        if(w == null) return false;
        if(getPlayersInWorld() > 0) return false;
        if(Bukkit.getServer().unloadWorld(w, false)) {
            Bukkit.getLogger().info("Unloaded the world " + w.getName());
        }
        else {
            Bukkit.getLogger().severe("Failed to unload the world " + w.getName());
            return false;
        }
        Bukkit.getServer().createWorld(new WorldCreator(w.getName()));
        setGoingToRollBack(false);
        return true;
    }

    public int getPlayersInWorld() {
        World w = Bukkit.getWorld(world);
        if(w == null) return 0;
        return w.getPlayers().size();
    }
}
