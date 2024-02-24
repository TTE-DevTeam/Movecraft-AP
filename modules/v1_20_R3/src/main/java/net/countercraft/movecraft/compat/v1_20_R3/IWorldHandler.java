package net.countercraft.movecraft.compat.v1_20_R3;

import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.WorldHandler;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.util.CollectionUtils;
import net.countercraft.movecraft.util.NMSBlockUtils;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.UnsafeUtils;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_20_R3.util.RandomSourceWrapper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.ticks.ScheduledTick;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("unused")
public class IWorldHandler extends WorldHandler {
    private static final Rotation ROTATION[];
    static {
        ROTATION = new Rotation[3];
        ROTATION[MovecraftRotation.NONE.ordinal()] = Rotation.NONE;
        ROTATION[MovecraftRotation.CLOCKWISE.ordinal()] = Rotation.CLOCKWISE_90;
        ROTATION[MovecraftRotation.ANTICLOCKWISE.ordinal()] = Rotation.COUNTERCLOCKWISE_90;
    }
    private final BlockData air = Bukkit.getServer().createBlockData("minecraft:air");
    private final NextTickProvider tickProvider = new NextTickProvider();
    private static final Plugin PLUGIN = Bukkit.getPluginManager().getPlugin("Movecraft");
    private static final RandomSource RANDOM = new RandomSourceWrapper(new java.util.Random());
    private static final BukkitAudiences audience = BukkitAudiences.create(PLUGIN);
    

    public IWorldHandler() {}
//    @Override
//    public void addPlayerLocation(Player player, double x, double y, double z, float yaw, float pitch){
//        ServerPlayer ePlayer = ((CraftPlayer) player).getHandle();
//        ePlayer.connection.teleport(x, y, z, yaw, pitch, EnumSet.allOf(ClientboundPlayerPositionPacket.RelativeArgument.class));
//    }

    @Override
    public boolean runTaskInCraftWorld(@NotNull Runnable runMe, Craft craft) {
        return runTaskInWorld(runMe,craft.getWorld());
    }

    @Override
    public boolean runTaskInWorld(@NotNull Runnable runMe, org.bukkit.World world) {
            final ServerLevel nativeWorld = ((CraftWorld) world).getHandle();
            try {
                Bukkit.getScheduler().runTask(PLUGIN, runMe);
                return true;
            } catch (Exception exc) {}
            return false;
        //return true;
    }

    @Override
    public void rotateCraft(@NotNull final Craft craft, @NotNull final MovecraftLocation originPoint, @NotNull final MovecraftRotation rotation) {
        //*******************************************
        //*      Step one: Convert to Positions     *
        //*******************************************

        final ServerLevel nativeWorld = ((CraftWorld) craft.getWorld()).getHandle();

        final HashMap<BlockPos, BlockPos> rotatedPositions = new HashMap<>(128000,1.5f);
        final MovecraftRotation counterRotation = rotation == MovecraftRotation.CLOCKWISE ? MovecraftRotation.ANTICLOCKWISE : MovecraftRotation.CLOCKWISE;
        final HashMap<BlockPos, BlockState> blockData = new HashMap<>(128000,1.5f);
        final HashMap<BlockPos, BlockState> redstoneComps = new HashMap<>(128000,1.5f);
        final List<TileHolder> tiles = new ArrayList<>();
        //  HashMap<Location, BlockData> sendAir = new HashMap<>(128000,1.5f);
          for (MovecraftLocation newLocation : craft.getHitBox()) {
              rotatedPositions.put(locationToPosition(MathUtils.rotateVec(counterRotation, newLocation.subtract(originPoint)).add(originPoint)), locationToPosition(newLocation));
              //Location pos = new Location(craft.getWorld(),newLocation.getX(),newLocation.getY(),newLocation.getZ());
              //sendBlockFast(nativeWorld, locationToPosition(MathUtils.rotateVec(counterRotation, newLocation.subtract(originPoint)).add(originPoint)), Blocks.AIR.defaultBlockState());
              //sendAir.put(pos,air);
          }
          //*******************************************
          //*         Step two: Get the tiles         *
          //*******************************************
          //get the tiles
          for (BlockPos position : rotatedPositions.keySet()) {
              //BlockEntity tile = nativeWorld.removeBlockEntity(position);
              BlockEntity tile = removeBlockEntity(nativeWorld, position);
              
              BlockPos pos = position;
              BlockState data = (getBlockFastest(nativeWorld,position)).rotate(ROTATION[rotation.ordinal()]);
              blockData.put(position, data);
              if (tile == null)
                  continue;
      //            tile.a(ROTATION[rotation.ordinal()]);
              //get the nextTick to move with the tile
              tiles.add(new TileHolder(tile, tickProvider.getNextTick(nativeWorld, position), position));
          }

          //*******************************************
          //*   Step three: Translate all the blocks  *
          //*******************************************
          // blockedByWater=false means an ocean-going vessel
          //TODO: Simplify
          //TODO: go by chunks
          //TODO: Don't move unnecessary blocks
          //get the blocks and rotate them
          //MovecraftLocation.sendBlockUpdated(craft,sendAir);
          //create the new block
          for (Map.Entry<BlockPos, BlockState> entry : blockData.entrySet()) {
            setBlockFastest(nativeWorld, rotatedPositions.get(entry.getKey()), entry.getValue());
            if (isRedstoneComponent(entry.getValue().getBlock())) redstoneComps.put(rotatedPositions.get(entry.getKey()), entry.getValue()); //Determine Redstone Blocks
          }


          //*******************************************
          //*    Step four: replace all the tiles     *
          //*******************************************
          //TODO: go by chunks
            for (TileHolder tileHolder : tiles) {
                moveBlockEntity(nativeWorld, rotatedPositions.get(tileHolder.getTilePosition()), tileHolder.getTile());
                //if (tileHolder.getNextTick() == null)
                //    continue;
                //if (tileHolder.getTile() instanceof BeaconBlockEntity)
                //    continue;
                //final long currentTime = nativeWorld.K.getGameTime();
                //nativeWorld.getBlockTicks().schedule(new ScheduledTick<>((Block) tileHolder.getNextTick().type(), rotatedPositions.get(tileHolder.getNextTick().pos()), tileHolder.getNextTick().triggerTick() - currentTime, tileHolder.getNextTick().priority(), tileHolder.getNextTick().subTickOrder()));
            }
          //MovecraftLocation.sendBlockUpdated(craft,sendAir);
      //*******************************************
      //*   Step five: Destroy the leftovers      *
      //*******************************************
      //TODO: add support for pass-through
      Collection<BlockPos> deletePositions = CollectionUtils.oldFilter(rotatedPositions.keySet(), rotatedPositions.values());
      for (BlockPos position : deletePositions) {
          //sendBlockFast(nativeWorld, position, Blocks.AIR.defaultBlockState());
          //if (craft.getOrigBlockCount()>=900000)
          setBlockFast(nativeWorld, position, Blocks.AIR.defaultBlockState());
          //else
          //  setBlockFast(nativeWorld, position, Blocks.AIR.defaultBlockState());
          //Location bloc = new Location(craft.getWorld(),pos.getX(),pos.getY(),pos.getZ());
          //sendAir.put(bloc,air);
      }

        if (craft.getAudience() == null) return;
        if (craft.getAudience().equals(audience.console())) return;
        processLight(craft.getHitBox(),craft.getWorld());
        processRedstone(redstoneComps.keySet(), nativeWorld);
    }

    @Override
    public void translateCraft(@NotNull final Craft craft, @NotNull final MovecraftLocation displacement, @NotNull final org.bukkit.World world) {
        //TODO: Add support for rotations
        //A craftTranslateCommand should only occur if the craft is moving to a valid position
        //*******************************************
        //*      Step one: Convert to Positions     *
        //*******************************************
      final ServerLevel oldNativeWorld = ((CraftWorld) craft.getWorld()).getHandle();
      final ServerLevel nativeWorld = ((CraftWorld) world).getHandle();
      final BlockPos translateVector = locationToPosition(displacement);
      final List<BlockPos> positions = new ArrayList<>(craft.getHitBox().size());
      final List<BlockState> blockData = new ArrayList<>();
      final List<BlockPos> redstoneComps = new ArrayList<>();
      final List<BlockPos> newPositions = new ArrayList<>();
      craft.getHitBox().forEach((movecraftLocation) -> positions.add(locationToPosition((movecraftLocation)).subtract(translateVector)));
        //*******************************************
        //*         Step two: Get the tiles         *
        //*******************************************
        final List<TileHolder> tiles = new ArrayList<>();
        //get the tiles
        //*******************************************
        //*   Step three: Translate all the blocks  *
        //*******************************************
        // blockedByWater=false means an ocean-going vessel
        //TODO: Simplify
        //TODO: go by chunks
        //TODO: Don't move unnecessary blocks
        //get the blocks and translate the positions
        //HashMap<Location, BlockData> sendAir = new HashMap<>(positions.size())
        for (BlockPos position : positions) {
            BlockState data;
            try {
                data = getBlockFastest(oldNativeWorld,position);
            } catch (Exception exc) {
                data = oldNativeWorld.getBlockState(position);
            } if (data == null) {
                data = oldNativeWorld.getBlockState(position);
            }
            blockData.add(data);
            newPositions.add(position.offset(translateVector));
            BlockEntity tile = removeBlockEntity(oldNativeWorld, position);
            if (tile == null) continue;
            //get the nextTick to move with the tile
            //nativeWorld.capturedTileEntities.remove(position);
            //nativeWorld.getChunkAtWorldCoords(position).getTileEntities().remove(position);
            tiles.add(new TileHolder(tile, tickProvider.getNextTick(oldNativeWorld, position), position));
        }
        final BlockState air = Blocks.AIR.defaultBlockState();
        //create the new block
        for(int i = 0, positionSize = newPositions.size(); i<positionSize; i++) {
            BlockPos pos = newPositions.get(i);
            BlockState state = blockData.get(i);
            setBlockFastest(nativeWorld, pos, state);
            if (isRedstoneComponent(nativeWorld.getBlockState(pos).getBlock())) redstoneComps.add(pos);
        }
        //*******************************************
        //*    Step four: replace all the tiles     *
        //*******************************************
        //TODO: go by chunks
        for (TileHolder tileHolder : tiles) {
            setBlockFastest(nativeWorld, tileHolder.getTilePosition().offset(translateVector), tileHolder.getTile().getBlockState());
            moveBlockEntity(nativeWorld, tileHolder.getTilePosition().offset(translateVector), tileHolder.getTile());
            //if (tileHolder.getNextTick() == null)
            //    continue;
            //if (tileHolder.getTile() instanceof BeaconBlockEntity)
            //    continue;
            //final long currentTime = nativeWorld.K.getGameTime();
            //nativeWorld.getBlockTicks().schedule(new ScheduledTick<>((Block) tileHolder.getNextTick().type(), tileHolder.getTilePosition().offset(translateVector), tileHolder.getNextTick().triggerTick() - currentTime, tileHolder.getNextTick().priority(), tileHolder.getNextTick().subTickOrder()));
            
            /*
            Original
            moveBlockEntity(nativeWorld, tileHolder.getTilePosition().offset(translateVector), tileHolder.getTile());
            if (tileHolder.getNextTick() == null)
                continue;
            final long currentTime = nativeWorld.getGameTime();
            nativeWorld.getBlockTicks().schedule(new ScheduledTick<>((Block) tileHolder.getNextTick().type(), tileHolder.getTilePosition().offset(translateVector), tileHolder.getNextTick().triggerTick() - currentTime, tileHolder.getNextTick().priority(), tileHolder.getNextTick().subTickOrder()));
            */
        }
        //MovecraftLocation.sendBlockUpdated(craft,sendBlocks);
        //*******************************************
        //*   Step five: Destroy the leftovers      *
        //*******************************************
        List<BlockPos> deletePositions = positions;
        if (oldNativeWorld == nativeWorld) deletePositions = CollectionUtils.filter(positions,newPositions);
        for (BlockPos position : deletePositions) {
            BlockPos pos = position;
            setBlockFastest(oldNativeWorld, position, air);
        }

        //*******************************************
        //*      Step six: Process redstone         *
        //*******************************************
        if (craft.getHitBox().size()>=256000) return;
        if (craft.getAudience() == null) return;
        if (craft.getAudience().equals(audience.console())) return;
        processLight(craft.getHitBox(),craft.getWorld());
        processRedstone(redstoneComps, nativeWorld);

            //*******************************************
            //*        Step seven: Process fire         *
            //*******************************************X
    }

    @Nullable
    private BlockEntity removeBlockEntity(@NotNull Level world, @NotNull BlockPos position){
        return world.getChunkAt(position).blockEntities.remove(position);
    }

    @NotNull
    public BlockPos locationToPosition(@NotNull MovecraftLocation loc) {
        return new BlockPos(loc.getX(), loc.getY(), loc.getZ());
    }

    @NotNull
    public BlockPos locationToPosition(@NotNull Location loc) {
        return new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private void setBlockFast(@NotNull Level world, @NotNull BlockPos position, @NotNull BlockState data) {
        LevelChunk chunk = world.getChunkIfLoaded(position);
        if (chunk == null) chunk = world.getChunkAt(position);
        int chunkSection = (position.getY() >> 4) - chunk.getMinSection();
        LevelChunkSection section = chunk.getSections()[chunkSection];
        if (section == null) {
            // Put a GLASS block to initialize the section. It will be replaced next with the real block.
            chunk.setBlockState(position, Blocks.AIR.defaultBlockState(), false);
            section = chunk.getSections()[chunkSection];
        }
        section.setBlockState(position.getX() & 15, position.getY() & 15, position.getZ() & 15, data);
        ((ServerLevel)world).getChunkSource().blockChanged(position); //.sendBlockUpdated(position, data, data, 2);
        //world.getLightEngine().checkBlock(position); // boolean corresponds to if chunk section empty
        chunk.setUnsaved(true);
    }
    private void setBlockFastest(@NotNull Level world, @NotNull BlockPos position, @NotNull BlockState data) {
        LevelChunk chunk = world.getChunkIfLoaded(position);
        if (chunk == null) chunk = world.getChunkAt(position);
        int chunkSection = (position.getY() >> 4) - chunk.getMinSection();
        LevelChunkSection section = chunk.getSections()[chunkSection];
        if (section == null) {
            // Put a GLASS block to initialize the section. It will be replaced next with the real block.
            chunk.setBlockState(position, Blocks.AIR.defaultBlockState(), false);
            section = chunk.getSections()[chunkSection];
        }
        if (world.getChunkAt(position).blockEntities.get(position) != null) {
            section.setBlockState(position.getX() & 15, position.getY() & 15, position.getZ() & 15, data);
            ((ServerLevel)world).getChunkSource().blockChanged(position); //.sendBlockUpdated(position, data, data, 2);
            chunk.setUnsaved(true);
        } else {
            section.setBlockState(position.getX() & 15, position.getY() & 15, position.getZ() & 15, data);
            ((ServerLevel)world).getChunkSource().blockChanged(position); //.sendBlockUpdated(position, data, data, 2);
            chunk.setUnsaved(true);
        }
    }
    @Nullable
    private BlockState getBlockFastest(@NotNull Level world, @NotNull BlockPos position) {
        LevelChunk chunk = world.getChunkIfLoaded(position);
        if (chunk == null) {
            chunk = world.getChunkAt(position);
        }
        int chunkSection = (position.getY() >> 4) - chunk.getMinSection();
        LevelChunkSection section = chunk.getSections()[chunkSection];
        BlockState data = null;
        if (section == null) {
            // Put a GLASS block to initialize the section. It will be replaced next with the real block.
            chunk.setBlockState(position, Blocks.AIR.defaultBlockState(), false);
            section = chunk.getSections()[chunkSection];
        }
        data = section.getBlockState(position.getX() & 15, position.getY() & 15, position.getZ() & 15);
        return data;
    }

    
    @Override
    public void setBlockFast(@NotNull Location location, @NotNull Material mat){
        Level world = ((CraftWorld) (location.getWorld())).getHandle();
        setBlockFast(location, mat.createBlockData());
    }

    private void processRedstone(Collection<BlockPos> redstone, Level world) {
        for (final BlockPos pos : redstone) {
            BlockState data = getBlockFastest(world,pos);
            if (isRedstoneComponent(data.getBlock())) {
                world.updateNeighborsAt(pos, data.getBlock());
                world.sendBlockUpdated(pos, data, data, 3);
                if (isToggleableRedstoneComponent(data.getBlock())) {
                    data.getBlock().tick(data,(ServerLevel)world,pos,RANDOM);
                }
            }
        }
    }
    @Override
    public @Nullable Location getAccessLocation(@NotNull InventoryView inventoryView) {
        AbstractContainerMenu menu = ((CraftInventoryView) inventoryView).getHandle();
        Field field = UnsafeUtils.getFieldOfType(ContainerLevelAccess.class, menu.getClass());
        if (field != null) {
            try {
                field.setAccessible(true);
                return ((ContainerLevelAccess) field.get(menu)).getLocation();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void setAccessLocation(@NotNull InventoryView inventoryView, @NotNull Location location) {
        if (location.getWorld() == null)
            return;
        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        BlockPos position = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ContainerLevelAccess access = ContainerLevelAccess.create(level, position);

        AbstractContainerMenu menu = ((CraftInventoryView) inventoryView).getHandle();
        UnsafeUtils.trySetFieldOfType(ContainerLevelAccess.class, menu, access);
    }

    private void processFireSpread(final HashMap<BlockPos, BlockState> fireStates, ServerLevel world) {
        for (var entry: fireStates.entrySet()) {
            BlockState state = entry.getValue();
            if (state.getBlock() instanceof FireBlock fireBlock) {
                fireBlock.tick(state, world, entry.getKey(), world.random);
            }
        }
    }

    @Override
    public void setBlockFast(@NotNull Location location, @NotNull BlockData data){
        setBlockFast(location, MovecraftRotation.NONE, data);
    }

    public void setBlockFast(@NotNull World world, @NotNull MovecraftLocation location, @NotNull BlockData data){
        setBlockFast(world, location, MovecraftRotation.NONE, data);
    }

    public void setBlockFast(@NotNull World world, @NotNull MovecraftLocation location, @NotNull MovecraftRotation rotation, @NotNull BlockData data) {
        BlockState blockData;
        if(data instanceof CraftBlockData){
            blockData = ((CraftBlockData) data).getState();
        } else {
            blockData = (BlockState) data;
        }
        blockData = blockData.rotate(ROTATION[rotation.ordinal()]);
        Level nmsWorld = ((CraftWorld)(world)).getHandle();
        BlockPos BlockPos = locationToPosition(location);
        setBlockFast(nmsWorld, BlockPos, blockData);
    }

    @Override
    public void setBlockFast(@NotNull Location location, @NotNull MovecraftRotation rotation, @NotNull BlockData data) {
        BlockState blockData;
        if (data instanceof CraftBlockData) {
            blockData = ((CraftBlockData) data).getState();
        }
        else {
            blockData = (BlockState) data;
        }
        blockData = blockData.rotate(ROTATION[rotation.ordinal()]);
        Level world = ((CraftWorld) (location.getWorld())).getHandle();
        BlockPos BlockPos = locationToPosition(MathUtils.bukkit2MovecraftLoc(location));
        setBlockFast(world, BlockPos, blockData);
    }

    @Override
    public void disableShadow(@NotNull Material type) {
        // Disabled
    }

    public void processLight(HitBox hitBox, @NotNull World world) {
        new BukkitRunnable() {
            @Override
            public void run() {
              ServerLevel nativeWorld = ((CraftWorld) world).getHandle();
              for (MovecraftLocation loc: hitBox) {
                  nativeWorld.getLightEngine().checkBlock(locationToPosition(loc));
              }
            }
        }.runTaskLater(PLUGIN, 200L);
    }


    private Field getField(String name) {
        try {
            var field = ServerGamePacketListenerImpl.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        }
        catch (NoSuchFieldException ex) {
            System.out.println("Failed to find field " + name);
            return null;
        }
    }

    private final Field justTeleportedField = getField("justTeleported");
    private final Field awaitingPositionFromClientField = getField("B");
    private final Field lastPosXField = getField("lastPosX");
    private final Field lastPosYField = getField("lastPosY");
    private final Field lastPosZField = getField("lastPosZ");
    private final Field awaitingTeleportField = getField("C");
    private final Field awaitingTeleportTimeField = getField("D");
    private final Field aboveGroundVehicleTickCountField = getField("H");

    private boolean isRedstoneComponent(Block block) {
      return block instanceof RedStoneWireBlock ||
              block instanceof DiodeBlock ||
              block instanceof TargetBlock ||
              block instanceof PressurePlateBlock ||
              block instanceof ButtonBlock ||
              block instanceof BasePressurePlateBlock ||
              block instanceof LeverBlock ||
              block instanceof HopperBlock ||
              block instanceof ObserverBlock ||
              block instanceof DaylightDetectorBlock ||
              block instanceof DispenserBlock ||
              block instanceof RedstoneLampBlock ||
              block instanceof RedstoneTorchBlock ||
              block instanceof ComparatorBlock ||
              block instanceof SculkSensorBlock ||
              block instanceof PistonBaseBlock ||
              block instanceof MovingPistonBlock ||
              block instanceof CrafterBlock ||
              block instanceof CopperBulbBlock;
    }
    private boolean isToggleableRedstoneComponent(Block block) {
      return block instanceof PressurePlateBlock ||
              block instanceof ButtonBlock ||
              block instanceof BasePressurePlateBlock ||
              block instanceof RedstoneLampBlock ||
              block instanceof RedstoneTorchBlock ||
              block instanceof PistonBaseBlock ||
              block instanceof MovingPistonBlock;
    }

    private void moveBlockEntity(@NotNull Level nativeWorld, @NotNull BlockPos newPosition, @NotNull BlockEntity tile) {
        LevelChunk chunk = nativeWorld.getChunkAt(newPosition);
        try {
            var positionField = BlockEntity.class.getDeclaredField("p"); // o is obfuscated worldPosition
            UnsafeUtils.setField(positionField, tile, newPosition);
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        tile.setLevel(nativeWorld);
        tile.clearRemoved();
        if (nativeWorld.captureBlockStates) {
            nativeWorld.capturedTileEntities.put(newPosition, tile);
            return;
        }
        chunk.setBlockEntity(tile);
        chunk.blockEntities.put(newPosition, tile);
    }

    private static class TileHolder {
        @NotNull
        private final BlockEntity tile;
        @Nullable
        private final ScheduledTick<?> nextTick;
        @NotNull
        private final BlockPos tilePosition;

        public TileHolder(@NotNull BlockEntity tile, @Nullable ScheduledTick<?> nextTick, @NotNull BlockPos tilePosition) {
            this.tile = tile;
            this.nextTick = nextTick;
            this.tilePosition = tilePosition;
        }


        @NotNull
        public BlockEntity getTile() {
            return tile;
        }

        @Nullable
        public ScheduledTick<?> getNextTick() {
            return nextTick;
        }

        @NotNull
        public BlockPos getTilePosition() {
            return tilePosition;
        }
    }
}
