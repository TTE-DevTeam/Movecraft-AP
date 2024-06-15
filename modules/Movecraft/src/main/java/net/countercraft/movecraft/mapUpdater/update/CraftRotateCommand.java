package net.countercraft.movecraft.mapUpdater.update;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.TrackedLocation;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.WorldHandler;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.SinkingCraft;
import net.countercraft.movecraft.craft.PlayerCraftImpl;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.SignTranslateEvent;
import net.countercraft.movecraft.util.CollectionUtils;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.countercraft.movecraft.util.hitboxes.SetHitBox;
import net.countercraft.movecraft.util.hitboxes.SolidHitBox;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.jetbrains.annotations.NotNull;
import org.bukkit.scheduler.BukkitRunnable;
//import com.jeff_media.customblockdata.*;
//import com.jeff_media.morepersistentdatatypes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;


public class CraftRotateCommand extends UpdateCommand {
    @NotNull
    private final Craft craft;
    @NotNull
    private final MovecraftRotation rotation;
    @NotNull
    private final MovecraftLocation originLocation;
    @NotNull
    private final World world;


    public CraftRotateCommand(@NotNull final Craft craft, @NotNull final MovecraftLocation originLocation, @NotNull final MovecraftRotation rotation) {
        this.craft = craft;
        this.rotation = rotation;
        this.originLocation = originLocation;
        this.world = craft.getWorld();
    }

    @Override
    public void doUpdate() {
        final Logger logger = Movecraft.getInstance().getLogger();
        if (craft.getHitBox().isEmpty()) {
            logger.warning("Attempted to move craft with empty HashHitBox!");
            CraftManager.getInstance().release(craft, CraftReleaseEvent.Reason.EMPTY, false);
            return;
        }
        final MovecraftRotation counterRotation = rotation == MovecraftRotation.CLOCKWISE ? MovecraftRotation.ANTICLOCKWISE : MovecraftRotation.CLOCKWISE;
        long time = System.nanoTime();
        final Set<Material> passthroughBlocks = new HashSet<>(craft.getType().getMaterialSetProperty(CraftType.PASSTHROUGH_BLOCKS));
        if (craft instanceof SinkingCraft) {
            passthroughBlocks.addAll(Tags.FLUID);
            passthroughBlocks.addAll(Tag.LEAVES.getValues());
            passthroughBlocks.addAll(Tags.SINKING_PASSTHROUGH);
        }
        if (craft instanceof BaseCraft) {
            if (((BaseCraft)craft).getRawTrackedMap().size() > 0) {
                for (Object key : ((BaseCraft)craft).getRawTrackedMap().keySet()) {
                    //Set<Object> mlocs = ((BaseCraft)craft).getRawTrackedMap().get(key);
                    final ArrayList<TrackedLocation> clone = new ArrayList<>();
                    for (Object tracked : ((BaseCraft)craft).getRawTrackedMap().get(key)) {
                        if (tracked != null) {
                            TrackedLocation newTracked = ((TrackedLocation) tracked).rotateTracked(originLocation,rotation);
                            clone.add(newTracked);
                        }
                    }
                    if (!clone.isEmpty() || clone != null || clone.size() > 0) {
                        ((BaseCraft)craft).getRawTrackedMap().put(key,clone);
                    }
                }
            }
        }
        if (!passthroughBlocks.isEmpty()) {
            SetHitBox originalLocations = new SetHitBox();
            for (MovecraftLocation movecraftLocation : craft.getHitBox()) {
                originalLocations.add(MathUtils.rotateVec(counterRotation, movecraftLocation.subtract(originLocation)).add(originLocation));
            }

            final HitBox to = craft.getHitBox().difference(originalLocations);

            for (MovecraftLocation location : to) {
                BlockData data = location.toBukkit(craft.getWorld()).getBlock().getBlockData();
                if ((data instanceof Waterlogged))
                    ((Waterlogged)data).setWaterlogged(false);
                if (passthroughBlocks.contains(location.toBukkit(craft.getWorld()).getBlock().getType())) {
                    craft.getPhaseBlocks().put(location.toBukkit(craft.getWorld()), data);
                }
            }
            //The subtraction of the set of coordinates in the HitBox cube and the HitBox itself
            final HitBox invertedHitBox = new SetHitBox(craft.getHitBox().boundingHitBox()).difference(craft.getHitBox());
            //A set of locations that are confirmed to be "exterior" locations
            final SetHitBox exterior = new SetHitBox();
            final SetHitBox interior = new SetHitBox();

            //place phased blocks
            final Set<Location> overlap = new HashSet<>(craft.getPhaseBlocks().keySet());
            overlap.retainAll(craft.getHitBox().asSet().stream().map(l -> l.toBukkit(craft.getWorld())).collect(Collectors.toSet()));
            final int minX = craft.getHitBox().getMinX();
            final int maxX = craft.getHitBox().getMaxX();
            final int minY = craft.getHitBox().getMinY();
            final int maxY = overlap.isEmpty() ? craft.getHitBox().getMaxY() : Collections.max(overlap, Comparator.comparingInt(Location::getBlockY)).getBlockY();
            final int minZ = craft.getHitBox().getMinZ();
            final int maxZ = craft.getHitBox().getMaxZ();
            final HitBox[] surfaces = {
                    new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(minX, maxY, maxZ)),
                    new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, maxY, minZ)),
                    new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(minX, maxY, maxZ)),
                    new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(maxX, maxY, minZ)),
                    new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, minY, maxZ))};
            //Valid exterior starts as the 6 surface planes of the HitBox with the locations that lie in the HitBox removed
            final SetHitBox validExterior = new SetHitBox();
            for (HitBox hitBox : surfaces) {
                validExterior.addAll(hitBox.difference(craft.getHitBox()));
            }
            //Check to see which locations in the from set are actually outside of the craft
            for (MovecraftLocation location : validExterior) {
                if (craft.getHitBox().contains(location) || exterior.contains(location)) {
                    continue;
                }
                //use a modified BFS for multiple origin elements
                SetHitBox visited = new SetHitBox();
                Queue<MovecraftLocation> queue = new LinkedList<>();
                queue.add(location);
                while (!queue.isEmpty()) {
                    MovecraftLocation node = queue.poll();
                    //If the node is already a valid member of the exterior of the HitBox, continued search is unitary.
                    for (MovecraftLocation neighbor : CollectionUtils.neighbors(invertedHitBox, node)) {
                        if (visited.contains(neighbor)) {
                            continue;
                        }
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
                exterior.addAll(visited);
            }
            interior.addAll(invertedHitBox.difference(exterior));

            final WorldHandler handler = Movecraft.getInstance().getWorldHandler();

            //rotate the craft

            handler.rotateCraft(craft, originLocation, rotation);
            //trigger sign events
            sendSignEvents();
            for (MovecraftLocation location : invertedHitBox.difference(exterior)) {
                BlockData data = location.toBukkit(craft.getWorld()).getBlock().getBlockData();
                if ((data instanceof Waterlogged))
                    ((Waterlogged)data).setWaterlogged(false);
                if (!passthroughBlocks.contains(location.toBukkit(craft.getWorld()).getBlock().getType())) {
                    continue;
                }
                craft.getPhaseBlocks().put(location.toBukkit(craft.getWorld()), data);
            }

            //place confirmed blocks if they have been un-phased
            for (MovecraftLocation location : exterior) {
                Location bukkit = location.toBukkit(craft.getWorld());
                if (!craft.getPhaseBlocks().containsKey(bukkit)) {
                    continue;
                }
                var phaseBlock = craft.getPhaseBlocks().remove(bukkit);
                handler.setBlockFast(bukkit, phaseBlock);
                craft.getPhaseBlocks().remove(bukkit);
            }

            for (MovecraftLocation location : originalLocations.boundingHitBox()) {
                Location bukkit = location.toBukkit(craft.getWorld());
                if (!craft.getHitBox().inBounds(location) && craft.getPhaseBlocks().containsKey(bukkit)) {
                    var phaseBlock = craft.getPhaseBlocks().remove(bukkit);
                    handler.setBlockFast(bukkit, phaseBlock);
                }
            }

            for (MovecraftLocation location : interior) {
                Location bukkit = location.toBukkit(craft.getWorld());
                BlockData data = location.toBukkit(craft.getWorld()).getBlock().getBlockData();
                if ((data instanceof Waterlogged))
                    ((Waterlogged)data).setWaterlogged(false);
                if (passthroughBlocks.contains(location.toBukkit(craft.getWorld()).getBlock().getType())) {
                    craft.getPhaseBlocks().put(bukkit, data);
                    handler.setBlockFast(bukkit, Movecraft.getInstance().getAirBlockData());

                }
            }
        }

        if (!craft.isNotProcessing())
            craft.setProcessing(false);
        time = System.nanoTime() - time;
        if (Settings.Debug)
            logger.info("Total time: " + (time / 1e6) + " milliseconds. Moving with cooldown of " + craft.getTickCooldown() + ". Speed of: " + String.format("%.2f", craft.getSpeed()));
    }

    private void sendSignEvents() {
        Object2ObjectMap<String[], List<MovecraftLocation>> signs = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<String[]>() {
            @Override
            public int hashCode(String[] strings) {
                return Arrays.hashCode(strings);
            }

            @Override
            public boolean equals(String[] a, String[] b) {
                return Arrays.equals(a, b);
            }
        });
        Map<MovecraftLocation, Sign> signStates = new HashMap<>();

        for (MovecraftLocation location : craft.getHitBox()) {
            Block block = location.toBukkit(craft.getWorld()).getBlock();
            BlockState state = block.getState();
            if (state instanceof Sign) {
                Sign sign = (Sign) block.getState();
                if (!signs.containsKey(sign.getLines()))
                    signs.put(sign.getLines(), new ArrayList<>());
                signs.get(sign.getLines()).add(location);
                signStates.put(location, sign);
            }
        }
        for (Map.Entry<String[], List<MovecraftLocation>> entry : signs.entrySet()) {
            SignTranslateEvent event = new SignTranslateEvent(craft, entry.getKey(), entry.getValue());
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isUpdated()) {
                continue;
            }
            for (MovecraftLocation location : entry.getValue()) {
                Block block = location.toBukkit(craft.getWorld()).getBlock();
                BlockState state = block.getState();
                BlockData data = location.toBukkit(craft.getWorld()).getBlock().getBlockData();
                if (!(state instanceof Sign)) {
                    continue;
                }
                Sign sign = signStates.get(location);
                for (int i = 0; i < 4; i++) {
                    sign.setLine(i, entry.getKey()[i]);
                }
                //sign.setBlockData(data);
                sign.update(false, false);
                block.setBlockData(data,false);
            }
        }
    }


    @NotNull
    public Craft getCraft() {
        return craft;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CraftRotateCommand)) {
            return false;
        }
        CraftRotateCommand other = (CraftRotateCommand) obj;
        return other.craft.equals(this.craft) &&
                other.rotation == this.rotation &&
                other.originLocation.equals(this.originLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(craft, rotation, originLocation);
    }
}
