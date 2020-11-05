package eu.revamp.practice.util.world.chunkgenerator;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

import java.util.Random;

@Getter
public class EmptyChunkGeneratorNewer extends ChunkGenerator {

    private final EmptyChunkGeneratorNewer emptyChunkGenerator = new EmptyChunkGeneratorNewer();

    private final byte[] buf = new byte[0x10000];

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        return new ChunkData() {

            @Override
            public void setRegion(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7) {}

            @Override
            public void setRegion(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6) {}

            @Override
            public void setRegion(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, MaterialData arg6) {}

            @Override
            public void setRegion(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, Material arg6) {}

            @Override
            public void setBlock(int arg0, int arg1, int arg2, int arg3, byte arg4) {}

            @Override
            public void setBlock(int arg0, int arg1, int arg2, int arg3) {}

            @Override
            public void setBlock(int arg0, int arg1, int arg2, MaterialData arg3) {}

            @Override
            public void setBlock(int arg0, int arg1, int arg2, Material arg3) {}

            @Override
            public int getTypeId(int arg0, int arg1, int arg2) {
                return 0;
            }

            @Override
            public MaterialData getTypeAndData(int arg0, int arg1, int arg2) {
                return null;
            }

            @Override
            public Material getType(int arg0, int arg1, int arg2) {
                return null;
            }

            @Override
            public int getMaxHeight() {
                return 0;
            }

            @Override
            public byte getData(int arg0, int arg1, int arg2) {
                return 0;
            }
        };
    }
}
