package eu.revamp.practice.util.world.chunkgenerator;
/*
import eu.revamp.practice.RevampPractice;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;

@Getter
public class EmptyChunkGeneratorProvider {

    private final RevampPractice plugin;

    private ChunkGenerator generator;

    public EmptyChunkGeneratorProvider(RevampPractice plugin) {
        this.plugin = plugin;
        if(this.plugin.getNMSAccessProvider().versionHasNoItemIDs) {
            generator = new EmptyChunkGeneratorNewer();
            Bukkit.getLogger().info("Using newer empty world generator!");
        }
        else {
            generator = new EmptyChunkGeneratorOlder();
            Bukkit.getLogger().info("Using older empty world generator!");
        }
    }
}*/