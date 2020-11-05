package eu.revamp.practice.util.world.chunkgenerator;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class EmptyChunkGeneratorOlder extends ChunkGenerator {

    private final byte[] buf = new byte[0x10000];

    @Override
    public byte[] generate(World world, Random random, int x, int z) {
        return buf;
    }

}
