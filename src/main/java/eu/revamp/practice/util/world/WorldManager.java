package eu.revamp.practice.util.world;
/*
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.revamp.practice.RevampPractice;
import eu.revamp.practice.arena.Arena;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

@RequiredArgsConstructor
public class WorldManager {
    private final RevampPractice plugin;

    public void copyWorld(File source, File target){
        try {
            ArrayList<String> ignore = new ArrayList<>(Arrays.asList("uid.dat", "session.dat"));
            if(!ignore.contains(source.getName())) {
                if(source.isDirectory()) {
                    if(!target.exists()) target.mkdirs();
                    String files[] = source.list();
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyWorld(srcFile, destFile);
                    }
                }
                else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteWorld(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteWorld(file);
                } else {
                    file.delete();
                }
            }
        }
        return path.delete();
    }

    public void stack(World stackableWorld, String newWorld) {
        long st = System.currentTimeMillis();
        File source = new File(Bukkit.getWorldContainer().getAbsolutePath(), stackableWorld.getName() + "/region");
        File target = new File(Bukkit.getWorldContainer().getAbsolutePath(), newWorld + "/region");
        if(Bukkit.getWorlds().contains(newWorld)) {
            deleteWorld(target);
        }
        copyWorld(source, target);
        WorldCreator creator = new WorldCreator(newWorld).type(WorldType.FLAT).environment(Environment.NORMAL).generateStructures(false);
        if(this.plugin.getConfig().getBoolean("empty-arenas-world")) {
            creator.generator(this.plugin.getEmptyChunkGeneratorProvider().getGenerator());
        }
        World world = creator.createWorld();
        world.setPVP(true);
        world.setDifficulty(Difficulty.HARD);
        world.setSpawnFlags(false, false);
        for(String s : stackableWorld.getGameRules()) {
            world.setGameRuleValue(s, stackableWorld.getGameRuleValue(s));
        }
        List<Arena> newArenas = new ArrayList<>();
        for(Arena ar : this.plugin.arenas) {
            if(!ar.getName().toLowerCase().contains("brackets")
                    && ar.getCenter() != null && ar.getCenter() != null && ar.getCenter().getWorld() != null && ar.getCenter().getWorld().getName().equals(stackableWorld.getName())) {
                Arena copy = Arena.getCopy(ar, world, world.getName() + ":" + ar.getName());
                if(copy != null) {
                    newArenas.add(copy);
                }
            }
        }
        for(Arena newArena : newArenas) {
            newArena.saveForRevampPractice();
        }
        Bukkit.getLogger().info("Copied " + newArenas.size() + " arenas to the new world.");
        Bukkit.getLogger().info("Copied the worlds and the arenas in "
                + (System.currentTimeMillis()-st) + " ms.");
    }
}
*/