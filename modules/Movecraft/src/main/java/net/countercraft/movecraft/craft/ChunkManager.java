package net.countercraft.movecraft.craft;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftChunk;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Deprecated
public class ChunkManager implements Listener {

  private static final Set<MovecraftChunk> chunks = new HashSet<>();

  public static void addChunksToLoad(final Iterable<MovecraftChunk> list) {
    //World world = null;
    addChunksToLoadAsync(list);
    /*ArrayList<MovecraftChunk> arr = new ArrayList<>();
    list.forEach(arr::add);
    if ((arr).size() > 0)
      world = arr.get(0).getWorld();
        for (MovecraftChunk chunk : list) {
            if (!chunks.contains(chunk)) {
                if (!chunk.isLoaded()) {
                    try {
                          chunk.toBukkit().load(true);
                        CompletableFuture<Chunk> cf = chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ(),true);
                        //final Chunk chnk = cf.get();
                        //cf.thenAccept(chunke -> { });
                        //chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ(), false, false);
                    } catch(Exception e) {
                        if (Settings.Debug)
                            Movecraft.getInstance().getLogger().info("Failed to load chunk at: " + chunk.getX() + "," + chunk.getZ() + "; Defaulting to Spigot API");
                      chunk.toBukkit().load(true);
                      //chunk.toBukkit().setForceLoaded(true);

                    }
                }
            }

        }
      (new BukkitRunnable() {
          @Override
          public void run() {
            ChunkManager.removeChunksToLoad(list);
          }
        }).runTaskLaterAsynchronously(Movecraft.getInstance(), 100L);*/
    }

    public static void addChunksToLoadAsync(final Iterable<MovecraftChunk> list) {
        for (MovecraftChunk chunk : list) {
            if (!chunks.contains(chunk)) {
                if (!chunk.isLoaded()) {
                    try { 
                        CompletableFuture<Chunk> cf = chunk.getWorld().getChunkAtAsync(chunk.getX(), chunk.getZ(),true);
                        cf.thenAccept(chunke -> { });
                        //chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ(), false, false);
                    } catch(Exception e) {
                        if (Settings.Debug)
                            Movecraft.getInstance().getLogger().info("Failed to load chunk at: " + chunk.getX() + "," + chunk.getZ() + "; Defaulting to Spigot API");
                    chunk.toBukkit().load(true);
                    //chunk.toBukkit().setForceLoaded(true);

                    }
                }
            }

        }
        (new BukkitRunnable() {
            @Override
            public void run() {
                ChunkManager.removeChunksToLoad(list);
            }
        }).runTaskLaterAsynchronously(Movecraft.getInstance(), 100L);
    }

    public static void asyncLoadChunk(Chunk chunk) {
          MovecraftChunk nchunk = new MovecraftChunk(chunk.getX(),chunk.getZ(),chunk.getWorld());
            if (!chunk.isLoaded()) {
                try {
                  CompletableFuture<Chunk> cf =chunk.getWorld().getChunkAtAsyncUrgently(chunk.getX(), chunk.getZ());
                  cf.thenAccept(chunke -> { });
                  //chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ(), false, false);
                } catch(Exception e) {
                    if (Settings.Debug)
                      Movecraft.getInstance().getLogger().info("Failed to load chunk at: " + chunk.getX() + "," + chunk.getZ() + "; Defaulting to Spigot API");
                  chunk.load(true);

                }
            }

        // remove chunks after 10 seconds
        new BukkitRunnable() {

            @Override
            public void run() {
                ChunkManager.removeChunkToLoad(nchunk);
            }

        }.runTaskLaterAsynchronously(Movecraft.getInstance(), 100L);
    }

    public static void asyncLoadChunk(Location loc) {
        int chunkX = loc.getBlockX() / 16;
        if (loc.getBlockX() < 0) chunkX--;
        int chunkZ = loc.getBlockZ() / 16;
        if (loc.getBlockZ() < 0) chunkZ--;
          MovecraftChunk nchunk = new MovecraftChunk(chunkX,chunkZ,loc.getWorld());
          if (chunks.add(nchunk)) {
              if (!nchunk.isLoaded()) {
                  try {
                    CompletableFuture<Chunk> cf =loc.getWorld().getChunkAtAsyncUrgently(chunkX, chunkZ);
                    cf.thenAccept(chunke -> { });
                    //chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ(), false, false);
                  } catch(Exception e) {
                      if (Settings.Debug)
                        Movecraft.getInstance().getLogger().info("Failed to load chunk at: " + chunkX + "," + chunkZ + "; Defaulting to Spigot API");
                    nchunk.toBukkit().load(true);

                  }
              }
          }

        // remove chunks after 10 seconds
        new BukkitRunnable() {

            @Override
            public void run() {
                ChunkManager.removeChunkToLoad(nchunk);
            }

        }.runTaskLaterAsynchronously(Movecraft.getInstance(), 100L);
    }

    public static void asyncLoadChunk(MovecraftLocation loc, World world) {
        int chunkX = loc.getX() / 16;
        if (loc.getX() < 0) chunkX--;
        int chunkZ = loc.getZ() / 16;
        if (loc.getZ() < 0) chunkZ--;
          MovecraftChunk nchunk = new MovecraftChunk(chunkX,chunkZ,world);
          if (chunks.add(nchunk)) {
              if (!nchunk.isLoaded()) {
                  try {
                    CompletableFuture<Chunk> cf =world.getChunkAtAsyncUrgently(chunkX, chunkZ);
                    cf.thenAccept(chunke -> { });
                    //chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ(), false, false);
                  } catch(Exception e) {
                      if (Settings.Debug)
                        Movecraft.getInstance().getLogger().info("Failed to load chunk at: " + chunkX + "," + chunkZ + "; Defaulting to Spigot API");
                    nchunk.toBukkit().load(false);

                  }
              }
          }

        // remove chunks after 10 seconds
        new BukkitRunnable() {

            @Override
            public void run() {
                ChunkManager.removeChunkToLoad(nchunk);
            }

        }.runTaskLaterAsynchronously(Movecraft.getInstance(), 100L);
    }

    public static void removeChunksToLoad(Iterable<MovecraftChunk> list) {
        for (MovecraftChunk chunk : list) {
            chunk.toBukkit().setForceLoaded(false);
            chunks.remove(chunk);
        }
    }

    public static void removeChunkToLoad(MovecraftChunk list) {
        list.toBukkit().setForceLoaded(false);
        chunks.remove(list);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
      final MovecraftChunk nchunk = new MovecraftChunk(event.getChunk());
      if (chunks.contains(nchunk)) {
        //asyncLoadChunk(event.getChunk());
        return;
      }
      /*new BukkitRunnable() {

            @Override
            public void run() {
                ChunkManager.removeChunkToLoad(nchunk);
            }

        }.runTaskLater(Movecraft.getInstance(), 100L);*/
    }

    public static Set<MovecraftChunk> getChunks(Iterable<MovecraftLocation> hitBox, World world) {
        return getChunks(hitBox, world, 0,0,0);

    }

    public static Set<MovecraftChunk> getChunks(Iterable<MovecraftLocation> oldHitBox, World world, int dx, int dy, int dz) {
        Set<MovecraftChunk> chunks = new HashSet<>();
        for (MovecraftLocation oldLocation : oldHitBox) {
            var location = oldLocation.translate(dx, dy, dz);
            int chunkX = location.getX() / 16;
            if (location.getX() < 0) chunkX--;
            int chunkZ = location.getZ() / 16;
            if (location.getZ() < 0) chunkZ--;

            MovecraftChunk chunk = new MovecraftChunk(chunkX, chunkZ, world);
            chunks.add(chunk);

        }
        return chunks;
    }
    public static void checkChunks(Set<MovecraftChunk> chunks) {
        chunks.removeIf(MovecraftChunk::isLoaded);
    }

    public static Future<Boolean> syncLoadChunks(Set<MovecraftChunk> chunks) {
        if (Settings.Debug)
            Movecraft.getInstance().getLogger().info("Loading " + chunks.size() + " chunks...");
        if(Bukkit.isPrimaryThread()){
            ChunkManager.addChunksToLoadAsync(chunks);
            return CompletableFuture.completedFuture(true);
        }
        return Bukkit.getScheduler().callSyncMethod(Movecraft.getInstance(), () -> {
            ChunkManager.addChunksToLoadAsync(chunks);
            return true;
        });
    }

}
