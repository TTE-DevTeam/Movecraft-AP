
/*
 * This file is part of Movecraft.
 *
 *     Movecraft is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Movecraft is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Movecraft.  If not, see <http://www.gnu.org/licenses/>.
 */


package net.countercraft.movecraft.craft;

import org.bukkit.util.Vector;
import org.bukkit.Axis;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Campfire;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.EndGateway;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Lectern;
import org.bukkit.block.Lockable;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.Structure;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftPreTranslateEvent;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.CraftPreSinkEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.events.TypesReloadedEvent;
import net.countercraft.movecraft.exception.NonCancellableReleaseException;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.processing.MovecraftWorld;
import net.countercraft.movecraft.processing.CachedMovecraftWorld;
import net.countercraft.movecraft.processing.WorldManager;
import net.countercraft.movecraft.processing.effects.Effect;
import net.countercraft.movecraft.processing.functions.CraftSupplier;
import net.countercraft.movecraft.processing.functions.Result;
import net.countercraft.movecraft.craft.type.property.RequiredBlockProperty;
import net.countercraft.movecraft.craft.type.RequiredBlockEntry;
import net.countercraft.movecraft.processing.tasks.detection.DetectionTask;
import net.countercraft.movecraft.processing.tasks.detection.IgnoreDetectionTask;
import net.countercraft.movecraft.async.rotation.RotationTask;
import net.countercraft.movecraft.util.Pair;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.CollectionUtils;
import net.countercraft.movecraft.util.hitboxes.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.bukkit.Axis;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Campfire;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.EndGateway;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Lectern;
import org.bukkit.block.Lockable;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.Structure;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.CraftMergeEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.events.PlayerCraftMovementEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.io.File;
import java.util.Queue;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static net.countercraft.movecraft.util.ChatUtils.ERROR_PREFIX;



import static net.countercraft.movecraft.util.ChatUtils.ERROR_PREFIX;

public class CraftManager implements Iterable<Craft>{
    private final String HEADER = "Remote Sign";
    private static CraftManager ourInstance;
    @NotNull public final Set<Craft> crafts = new ConcurrentSkipListSet<>(Comparator.comparingInt(Craft::hashCode));
    @NotNull public final ConcurrentMap<Player, PlayerCraft> craftPlayerIndex = new ConcurrentHashMap<>();
    @NotNull private Set<CraftType> craftTypes;
    public ArrayList<ItemStack> fuelTypes = new ArrayList<>();
    public HashMap<ItemStack,Double> fuelTypeMap = new HashMap<>();
    public ArrayList<Double> fuelTypeChance = new ArrayList<>();
    @NotNull private final WeakHashMap<Player, Long> overboards = new WeakHashMap<>();
    @NotNull private final Set<Craft> sinking = Collections.newSetFromMap(new ConcurrentHashMap<>());
    @NotNull private final Set<Craft> craftList = crafts;
    /**
        * Map of players to their current craft.
        */
    private final java.util.Random rand = new java.util.Random();
    @NotNull public final ConcurrentMap<Player, PlayerCraft> playerCrafts = new ConcurrentHashMap<>();
    @NotNull private final ConcurrentMap<Craft, BukkitTask> releaseEvents = new ConcurrentHashMap<>();
    public static ItemStack wasteItem = new ItemStack(Material.AIR);
    public static void initialize(boolean loadCraftTypes) {
        ourInstance = new CraftManager(loadCraftTypes);
    }
    public static <T> int getIndex(Set<T> set, T value) {
        int result = 0;
        for (T entry:set) {
            if (entry.equals(value)) return result;
                result++;
            }
        return -1;
    }
    private CraftManager(boolean loadCraftTypes) {
        this.addFuelType(new ItemStack(Material.COAL),45.0);
        this.addFuelType(new ItemStack(Material.CHARCOAL),45.0);
        this.addFuelType(new ItemStack(Material.COAL_BLOCK),125.0);
        this.addFuelType(new ItemStack(Material.DRIED_KELP_BLOCK),125.0);
        if(loadCraftTypes) {
            this.craftTypes = loadCraftTypes();
        }
        else {
            this.craftTypes = new HashSet<>();
        }
    }

    public static CraftManager getInstance() {
        return ourInstance;
    }
    @NotNull
    public List<ItemStack> getFuelTypes() {
        return fuelTypes;
    }
    @NotNull
    public double getFuelIndexBurnChance(int indx) {
      return fuelTypeChance.get(indx);
    }
    public HitBox translateBox(HitBox box, MovecraftLocation v) {
        SetHitBox newBox = new SetHitBox();
        for(MovecraftLocation oldLoc : box){
          MovecraftLocation newLoc = oldLoc.translate(v.x,v.y,v.z);
          newBox.add(newLoc);
        }
        return newBox;
    }
    public HitBox rotateBox(HitBox box, MovecraftLocation p, MovecraftRotation r) {
        SetHitBox newBox = new SetHitBox();
        for(MovecraftLocation oldLoc : box){
          MovecraftLocation newLoc = MathUtils.rotateVec(r,oldLoc.subtract(p)).add(p);
          newBox.add(newLoc);
        }
        return newBox;
    }
    public void teleportCraft(Craft c, Location bl) {
       int dx, dy, dz = 0;
       World dw = c.getWorld();
       if (bl.getWorld() == null) {
          dw = c.getWorld();
       } else {
          dw = bl.getWorld();
       }
       Location center = c.getHitBox().getMidPoint().toBukkit(c.getWorld());
       dx = bl.getBlockX() - center.getBlockX();
       dy = bl.getBlockY() - center.getBlockY();
       dz = bl.getBlockZ() - center.getBlockZ();
       c.translate(dw,dx,dy,dz);
    }

    @NotNull
    public void addFuelType(ItemStack stack, double chance) {
        fuelTypes.add(stack);
        fuelTypeChance.add(chance);
        fuelTypeMap.put(stack,chance);
    }
    @NotNull
    public Set<CraftType> getCraftTypes() {
        return Collections.unmodifiableSet(craftTypes);
    }
    public Craft sink(@NotNull Craft craft) {
        CraftPreSinkEvent event = new CraftPreSinkEvent(craft);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ((BaseCraft)craft).setSinking(false);
            return craft;
        }
        ((BaseCraft)craft).setSinking(true);
        CraftSinkEvent sinkevent = new CraftSinkEvent(craft);
        Bukkit.getServer().getPluginManager().callEvent(sinkevent);

        return craft;
    }
    public Craft quietSink(@NotNull Craft craft) {
        CraftPreSinkEvent event = new CraftPreSinkEvent(craft);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ((BaseCraft)craft).setSinking(false);
            return craft;
        }
        ((BaseCraft)craft).setSinking(true);
        return craft;
    }

    public boolean forceBurnFuel(Craft craft) {
        boolean found = false;
        if (craft.getSinking() || craft instanceof SinkingCraft){
            return true;
        }
        if (craft.getNotificationPlayer() == null){
            return true;
        }
        if (craft.getType().getDoubleProperty(CraftType.FUEL_BURN_RATE) <= 0.0) {
            return true;
        }
        if (craft instanceof PlayerCraftImpl) {
            int iters = 0;
            for (ItemStack istack : fuelTypeMap.keySet()) {
                double fuelBurnChance = this.fuelTypeMap.get(istack);
                iters++;
                found = this.forceCheckFuel(craft,1+((int)(craft.getCurrentGear()/2)+2),fuelBurnChance,istack,wasteItem);
                if (found)
                    break;
            }
            }
        if (craft instanceof BaseCraft) {
            ((BaseCraft)craft).setDataTag("has_fuel",found);
        }
        return found;
    }
    public boolean forceCheckFuel(Craft craft, int fuelBurnRate, double percBurnChance, ItemStack fuelItem, ItemStack wasteItem) {
        if (craft.getSinking())
            return true;
        if (craft instanceof SinkingCraftImpl)
            return true;
        int chance = 5;
        if (fuelBurnRate > -5) {
            Block invBlock = null;
            final Set<Block> blocks = new HashSet<>();
                blocks.addAll((((BaseCraft)craft)).getBlockType(Material.FURNACE));
            if (((BaseCraft)craft).getTrackedMovecraftLocs("fuel_locations").size() > 0) {
                blocks.addAll((((BaseCraft)craft)).getTrackedBlocks("fuel_locations"));
            }
            for (Block b : blocks) {
                if (b.getState() instanceof Container) {
                    InventoryHolder inventoryHolder = (InventoryHolder)b.getState();
                    if((((InventoryHolder)b.getState()).getInventory().getContents()) == null) continue;
                    ListIterator<ItemStack> listIterator1 = inventoryHolder.getInventory().iterator();
                    while (listIterator1.hasNext()) {
                        ItemStack stack = listIterator1.next();
                        invBlock = b;
                        if (stack != null && (stack.isSimilar(fuelItem))) {
                            chance = rand.nextInt((int)percBurnChance+1);
                            if ((int)chance >= (int)percBurnChance - (int)(((int)percBurnChance)-1)) {
                                int amount = stack.getAmount();
                                stack.setAmount(amount);
                            } else {
                                int amount = stack.getAmount();
                                stack.setAmount(amount - (int)fuelBurnRate);
                                if (wasteItem.getType() != Material.AIR)
                                (invBlock.getWorld()).dropItem(invBlock.getLocation(),wasteItem);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @NotNull
    private Set<CraftType> loadCraftTypes() {
        File craftsFile = new File(Movecraft.getInstance().getDataFolder().getAbsolutePath() + "/types");

        if (craftsFile.mkdirs()) {
            Movecraft.getInstance().saveResource("types/Airship.craft", false);
            Movecraft.getInstance().saveResource("types/Airskiff.craft", false);
            Movecraft.getInstance().saveResource("types/BigAirship.craft", false);
            Movecraft.getInstance().saveResource("types/BigSubAirship.craft", false);
            Movecraft.getInstance().saveResource("types/Elevator.craft", false);
            Movecraft.getInstance().saveResource("types/LaunchTorpedo.craft", false);
            Movecraft.getInstance().saveResource("types/Ship.craft", false);
            Movecraft.getInstance().saveResource("types/SubAirship.craft", false);
            Movecraft.getInstance().saveResource("types/Submarine.craft", false);
            Movecraft.getInstance().saveResource("types/Turret.craft", false);
        }

        Set<CraftType> craftTypes = new HashSet<>();
        File[] files = craftsFile.listFiles();
        if (files == null) {
            return craftTypes;
        }

        for (File file : files) {
            if (file.isFile()) {

                if (file.getName().contains(".craft")) {
                    try {
                        CraftType type = new CraftType(file);
                        craftTypes.add(type);
                    }
                    catch (IllegalArgumentException | CraftType.TypeNotFoundException | ParserException | ScannerException e) {
                        Movecraft.getInstance().getLogger().log(Level.SEVERE, I18nSupport.getInternationalisedString("Startup - failure to load craft type") + " '" + file.getName() + "' " + e.getMessage());
                    }
                }
            }
        }
        if (craftTypes.isEmpty()) {
            Movecraft.getInstance().getLogger().log(Level.SEVERE, ERROR_PREFIX + I18nSupport.getInternationalisedString("Startup - No Crafts Found"));
        }
        Movecraft.getInstance().getLogger().log(Level.INFO, String.format(I18nSupport.getInternationalisedString("Startup - Number of craft files loaded"), craftTypes.size()));
        return craftTypes;
    }

    public void reloadCraftTypes() {
        this.craftTypes = loadCraftTypes();
        Bukkit.getServer().getPluginManager().callEvent(new TypesReloadedEvent());
    }

    public void addCraft(@NotNull PlayerCraft c) {
        if(craftPlayerIndex.containsKey(c.getPilot())) {
            throw new IllegalStateException("Players may only have one PlayerCraft associated with them!");
        }

        crafts.add(c);
        craftPlayerIndex.put(c.getPilot(), c);
    }

    public void addCraft(@NotNull Craft c) {
        if(c instanceof PlayerCraft){
            addCraft((PlayerCraft) c);
        }
        else {
            this.crafts.add(c);
        }
    }

    public void add(@NotNull Craft c) {
        //Redirected to old Method-Name
        addCraft(c);
    }
    
    public SetHitBox detectCraftExterior(Craft craft) {
        SetHitBox confirmedExtBox = new SetHitBox();
        if (craft instanceof BaseCraft) {
            if (((BaseCraft)craft).getTrackedMovecraftLocs("valid_exterior").size() <= 0) {
                final HitBox hitBox = new BitmapHitBox(craft.getHitBox());
                final HitBox boundingHitBox = new BitmapHitBox(craft.getHitBox().boundingHitBox());
                final HitBox invertBox = new SetHitBox(Sets.difference(boundingHitBox.asSet(), hitBox.asSet()));
                HitBox validExterior = detectInvertedBox(craft);
                confirmedExtBox = new SetHitBox(verifyDetectedExterior(invertBox.asSet(),validExterior));
                ((BaseCraft)craft).setTrackedMovecraftLocs("valid_exterior",confirmedExtBox.asSet());
            } else {
                confirmedExtBox = new SetHitBox(((BaseCraft)craft).getTrackedMovecraftLocs("valid_exterior"));
            }
            return confirmedExtBox;
        }
        final HitBox hitBox = new BitmapHitBox(craft.getHitBox());
        final HitBox boundingHitBox = new BitmapHitBox(craft.getHitBox().boundingHitBox());
        final HitBox invertBox = new SetHitBox(Sets.difference(boundingHitBox.asSet(), hitBox.asSet()));
        HitBox validExterior = detectInvertedBox(craft);
        confirmedExtBox = new SetHitBox(verifyDetectedExterior(invertBox.asSet(),validExterior));
        return confirmedExtBox;
    }
    
    public SetHitBox detectCraftInterior(Craft craft) {
        final HitBox hitBox = new BitmapHitBox(craft.getHitBox());
        final HitBox boundingHitBox = new BitmapHitBox(craft.getHitBox().boundingHitBox());
        final HitBox invertBox = new SetHitBox(Sets.difference(boundingHitBox.asSet(), hitBox.asSet()));
        HitBox validExterior = detectInvertedBox(craft);
        SetHitBox confirmedExtBox = new SetHitBox(verifyDetectedExterior(invertBox.asSet(),validExterior));
        HitBox box = new BitmapHitBox(boundingHitBox.difference(confirmedExtBox));
        final SetHitBox interior = new SetHitBox(box.difference(hitBox));
        return interior;
    }
    public HitBox detectInterior(Craft craft) {
        return detectCraftInterior(craft);
    }

    public BitmapHitBox detectExterior(Craft craft) {
        BitmapHitBox full = new BitmapHitBox(craft.getHitBox());
        World world = craft.getWorld();
        int minX = full.getMinX();
        int maxX = full.getMaxX();
        int minY = full.getMinY();
        int maxY = full.getMaxY();
        int minZ = full.getMinZ();
        int maxZ = full.getMaxZ();
        HitBox hitBox = new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, maxY, maxZ));
        BitmapHitBox internalHitbox = new BitmapHitBox(hitBox);
        for (MovecraftLocation loc : internalHitbox) {
            if (loc.toBukkit(world).getBlock().getType().isAir()) {
                internalHitbox.remove(loc);
            }
        }
        return internalHitbox;
    }

    public Set<MovecraftLocation> verifyDetectedExterior(Set<MovecraftLocation> invertedHitBox, HitBox validExterior) {
        var shifts = new MovecraftLocation[]{new MovecraftLocation(0,-1,0),
                new MovecraftLocation(1,0,0),
                new MovecraftLocation(-1,0,0),
                new MovecraftLocation(0,0,1),
                new MovecraftLocation(0,0,-1)};
        Set<MovecraftLocation> visited = new LinkedHashSet<>(validExterior.asSet());
        Queue<MovecraftLocation> queue = new ArrayDeque<>();
        for(var node : validExterior){
            //If the node is already a valid member of the exterior of the HitBox, continued search is unitary.
            for(var shift : shifts){
                var shifted = node.add(shift);
                if(invertedHitBox.contains(shifted) && visited.add(shifted)){
                    queue.add(shifted);
                }
            }
        }
        while (!queue.isEmpty()) {
            var node = queue.poll();
            //If the node is already a valid member of the exterior of the HitBox, continued search is unitary.
            for(var shift : shifts){
                var shifted = node.add(shift);
                if(invertedHitBox.contains(shifted) && visited.add(shifted)){
                    queue.add(shifted);
                }
            }
        }
        return visited;
    }
    public BitmapHitBox detectValidExterior(Craft craft) {
        var invertedHitBox = Sets.difference(craft.getHitBox().boundingHitBox().asSet(), craft.getHitBox().asSet());
        int minX = craft.getHitBox().getMinX();
        int maxX = craft.getHitBox().getMaxX();
        int minY = craft.getHitBox().getMinY();
        int maxY = craft.getHitBox().getMaxY();
        int minZ = craft.getHitBox().getMinZ();
        int maxZ = craft.getHitBox().getMaxZ();
        HitBox[] surfaces = {
                new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(minX, maxY, maxZ)),
                new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, maxY, minZ)),
                new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(minX, maxY, maxZ)),
                new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(maxX, maxY, minZ)),
                new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, minY, maxZ))};
        SetHitBox validExterior = new SetHitBox();
        for (HitBox hitBox : surfaces) {
            validExterior.addAll(Sets.difference(hitBox.asSet(),craft.getHitBox().asSet()));
        }
        BitmapHitBox validExteriorBitMap = new BitmapHitBox(validExterior);
        return validExteriorBitMap;
    }

    public BitmapHitBox detectInvertedBox(Craft craft) {
      var invertedHitBox = Sets.difference(craft.getHitBox().boundingHitBox().asSet(), craft.getHitBox().asSet());
      int minX = craft.getHitBox().getMinX();
      int maxX = craft.getHitBox().getMaxX();
      int minY = craft.getHitBox().getMinY();
      int maxY = craft.getHitBox().getMaxY();
      int minZ = craft.getHitBox().getMinZ();
      int maxZ = craft.getHitBox().getMaxZ();
      HitBox[] surfaces = {
              new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(minX, maxY, maxZ)),
              new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, maxY, minZ)),
              new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(minX, maxY, maxZ)),
              new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(maxX, maxY, minZ)),
              new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, minY, maxZ))};
      SetHitBox validExterior = new SetHitBox();
      for (HitBox hitBox : surfaces) {
          validExterior.addAll(Sets.difference(hitBox.asSet(),craft.getHitBox().asSet()));
      }
      BitmapHitBox validExteriorBitMap = new BitmapHitBox(validExterior);
      return validExteriorBitMap;
    }

    public void release(@NotNull Craft craft, @NotNull CraftReleaseEvent.Reason reason, boolean force) {
        CraftReleaseEvent e = new CraftReleaseEvent(craft, reason);
        Bukkit.getServer().getPluginManager().callEvent(e);
        if (e.isCancelled()) {
            if (force)
                throw new NonCancellableReleaseException();
            else
                return;
        }
        if (craft instanceof BaseCraft) {
            ((BaseCraft)craft).getCraftTags().clear();
            ((BaseCraft)craft).getRawTrackedMap().clear();
        }
        crafts.remove(craft);
        if(craft instanceof PlayerCraft)
            playerCrafts.remove(((PlayerCraft) craft).getPilot());

        if(craft.getHitBox().isEmpty())
            Movecraft.getInstance().getLogger().warning(I18nSupport.getInternationalisedString(
                    "Release - Empty Craft Release Console"));
        else {
            if (craft instanceof PlayerCraft)
                craft.getAudience().sendMessage(Component.text(I18nSupport.getInternationalisedString(
                        "Release - Craft has been released")));
            if (craft instanceof PilotedCraft)
                Movecraft.getInstance().getLogger().info(String.format(I18nSupport.getInternationalisedString(
                        "Release - Player has released a craft console"),
                        ((PilotedCraft) craft).getPilot().getName(),
                        craft.getType().getStringProperty(CraftType.NAME),
                        craft.getHitBox().size(),
                        craft.getHitBox().getMinX(),
                        craft.getHitBox().getMinZ())
                );
            else
                Movecraft.getInstance().getLogger().info(String.format(I18nSupport.getInternationalisedString(
                        "Release - Null Craft Release Console"),
                        craft.getType().getStringProperty(CraftType.NAME),
                        craft.getHitBox().size(),
                        craft.getHitBox().getMinX(),
                        craft.getHitBox().getMinZ())
                );
        }
        Movecraft.getInstance().getAsyncManager().addWreck(craft);
    }
    public void removeCraft(@NotNull Craft c, @NotNull CraftReleaseEvent.Reason reason) {
        CraftReleaseEvent e = new CraftReleaseEvent(c, reason);
        Bukkit.getServer().getPluginManager().callEvent(e);
        if (e.isCancelled())
            return;

        Craft craft = c;
        removeReleaseTask(c);

        if (craft instanceof BaseCraft) {
            ((BaseCraft)craft).getCraftTags().clear();
            ((BaseCraft)craft).getRawTrackedMap().clear();
        }
        crafts.remove(c);
        if(c instanceof PlayerCraft)
          this.craftPlayerIndex.remove(((PlayerCraft) c).getPilot());
        // if its sinking, just remove the craft without notifying or checking
        if(craft.getHitBox().isEmpty())
            Movecraft.getInstance().getLogger().warning(I18nSupport.getInternationalisedString("Release - Empty Craft Release Console"));
        else {
            if (c instanceof PlayerCraft)
                craft.getAudience().sendMessage(Component.text(I18nSupport.getInternationalisedString("Release - Craft has been released")));
            if (c instanceof BaseCraft) {
                BaseCraft bc = (BaseCraft)c;
                if (c instanceof PilotedCraft)
                    Movecraft.getInstance().getLogger().info(String.format(I18nSupport.getInternationalisedString("Release - Player has released a craft console"), ((PilotedCraft) c).getPilot().getName(), craft.getType().getStringProperty(CraftType.NAME), craft.getHitBox().size()-bc.getTrackedMovecraftLocs("air").size(), craft.getHitBox().getMinX(), craft.getHitBox().getMinZ()));
                else
                    Movecraft.getInstance().getLogger().info(String.format(I18nSupport.getInternationalisedString("Release - Null Craft Release Console"), craft.getType().getStringProperty(CraftType.NAME), craft.getHitBox().size()-bc.getTrackedMovecraftLocs("air").size(), craft.getHitBox().getMinX(), craft.getHitBox().getMinZ()));
            }
        }
        if (c instanceof Craft)
          Movecraft.getInstance().getAsyncManager().addWreck(c);
    }

    public void forceRemoveCraft(@NotNull Craft c) {
        this.crafts.remove(c);
        if (c instanceof BaseCraft) {
            ((BaseCraft)c).getCraftTags().clear();
            ((BaseCraft)c).getRawTrackedMap().clear();
        }
        if(c instanceof PlayerCraft)
            craftPlayerIndex.remove(((PlayerCraft) c).getPilot());
        CraftReleaseEvent e = new CraftReleaseEvent(c, CraftReleaseEvent.Reason.FORCE);
        Bukkit.getServer().getPluginManager().callEvent(e);
        if(e.isCancelled()) {
            throw new NonCancellableReleaseException();
        }
    }

    /**
     * Detect a craft and add it to the craft manager
     *
     * @param startPoint the starting point of the detection process
     * @param type the type of craft to detect
     * @param supplier the supplier run post-detection to create the craft.
     *   Note: This is where you can construct a custom Craft object if you want to, or tailor the detection process.
     * @param world the world to detect in
     * @param player the player who is causing the detection
     *   Note: This is only used for logging and forwarded to the supplier.
     *   - It is highly encouraged to pass in a non-null value if a player is causing the detection.
     *   - If player is null, this will bypass protections like pilot signs and the like.
     * @param audience the audience to send detection messages to
     * @param postDetection the function run post-supplying to perform post-detection actions.
     *   Note: This is where you can perform any post-detection actions, such as starting a torpedo cruising.
     */

    public void detect(@NotNull MovecraftLocation startPoint,
                        @NotNull CraftType type, @NotNull CraftSupplier supplier,
                        @NotNull World world, @Nullable Player player,
                        @NotNull Audience audience,
                        @NotNull Function<Craft, Effect> postDetection) {
        WorldManager.INSTANCE.submit(new DetectionTask(
                startPoint, CachedMovecraftWorld.of(world),
                type, supplier,
                world, player,
                audience,
                postDetection
        ));
        if (getCraftFromBlock(startPoint.toBukkit(world).getBlock()) instanceof BaseCraft) {
            BaseCraft craft = (BaseCraft)getCraftFromBlock(startPoint.toBukkit(world).getBlock());
            int origin_lift = 0;
            //craft.midPoint = craft.getHitBox().getMidPoint();
            for (RequiredBlockEntry entry : craft.getType().getRequiredBlockProperty(CraftType.FLY_BLOCKS)) {
                for (Material mats : entry.getMaterials()) {
                    origin_lift += (((craft).getBlockType(mats)).size());
                }
            }
            craft.setDataTag("origin_lift",(origin_lift));
            int origin_engine = 0;
            for(RequiredBlockEntry entry : craft.getType().getRequiredBlockProperty(CraftType.MOVE_BLOCKS)) {
                for (Material mat : entry.getMaterials()) {
                    origin_engine += (((craft).getBlockType(mat)).size());
                }
            }
            craft.setDataTag("origin_engine", (int)(origin_engine));
        }
    }

    public void detectIgnoreBlockData(@NotNull List<BlockData> mat, @NotNull MovecraftLocation startPoint,
                        @NotNull CraftType type, @NotNull CraftSupplier supplier,
                        @NotNull World world, @Nullable Player player,
                        @NotNull Audience audience,
                        @NotNull Function<Craft, Effect> postDetection) {
        WorldManager.INSTANCE.submit(new IgnoreDetectionTask(mat,
                startPoint, CachedMovecraftWorld.of(world),
                type, supplier,
                world, player,
                audience,
                postDetection
        ));
        if (getCraftFromBlock(startPoint.toBukkit(world).getBlock()) instanceof BaseCraft) {
            BaseCraft craft = (BaseCraft)getCraftFromBlock(startPoint.toBukkit(world).getBlock());
            int origin_lift = 0;
            //craft.midPoint = craft.getHitBox().getMidPoint();
            for (RequiredBlockEntry entry : craft.getType().getRequiredBlockProperty(CraftType.FLY_BLOCKS)) {
                for (Material mats : entry.getMaterials()) {
                    origin_lift += (((craft).getBlockType(mats)).size());
                }
            }
            craft.setDataTag("origin_lift",(origin_lift));
            int origin_engine = 0;
            for(RequiredBlockEntry entry : craft.getType().getRequiredBlockProperty(CraftType.MOVE_BLOCKS)) {
                for (Material mats : entry.getMaterials()) {
                    origin_engine += (((craft).getBlockType(mats)).size());
                }
            }
            craft.setDataTag("origin_engine", (int)(origin_engine));
        }
    }

    @NotNull
    public Set<Craft> getCraftsInWorld(@NotNull World w) {
        Set<Craft> crafts = new HashSet<>();
        for (Craft c : craftList) {
            if (c instanceof Craft) {
                if (((Craft)c).getWorld() == w)
                    crafts.add(((Craft)c));
            }
        }
        return crafts;
    }

    @Contract("null -> null")
    @Nullable
    public PlayerCraft getCraftByPlayer(@Nullable Player p) {
        if(p == null)
            return null;
        return craftPlayerIndex.get(p);
    }


    public PlayerCraft getCraftByPlayerName(String name) {
        for (var entry : craftPlayerIndex.entrySet()) {
            if (entry.getKey() != null && entry.getKey().getName().equals(name))
                return entry.getValue();
        }
        return null;
    }

    public void removeCraftByPlayer(Player player) {
        PilotedCraft craft = craftPlayerIndex.remove(player);
        if (craft != null) {
            forceRemoveCraft(craft);
        }
    }



    public Craft forcePilotIgnoreBlockData(@NotNull CraftType craftType, Block b, Player player, List<BlockData> matIgnored) {
        MovecraftLocation startPoint = new MovecraftLocation(b.getX(), b.getY(), b.getZ());
        World world = (World)b.getWorld();
        this.detectIgnoreBlockData(
                matIgnored,
                startPoint,
                craftType, (type, w, p, parents) -> {
                        if (parents.size() > 0)
                            return new Pair<>(Result.failWithMessage(I18nSupport.getInternationalisedString(
                                    "Detection - Failed - Already commanding a craft")), null);

                        return new Pair<>(Result.succeed(),
                                new PlayerCraftImpl(type, w, p));
                },
                world, player,
                Movecraft.getAdventure().player(player),
                craft -> () -> {
                    Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(craft, CraftPilotEvent.Reason.PLAYER));
                    if (craft instanceof PlayerCraftImpl) { // Subtract craft from the parent
                        // Release old craft if it exists
                        Craft oldCraft = this.getCraftByPlayer(player);
                        if (oldCraft != null)
                            this.removeCraft(oldCraft, CraftReleaseEvent.Reason.PLAYER);
                    }
                }
        );
        return (this.getCraftFromBlock(b));
    }


    public Craft forcePilotAndSplit(@NotNull CraftType craftType, Block b, Player player) {
        MovecraftLocation startPoint = new MovecraftLocation(b.getX(), b.getY(), b.getZ());
        World world = (World)b.getWorld();
        Craft originCraft = (this.getCraftFromBlock(b));
        this.detect(
                startPoint,
                craftType, (type, w, p, parents) -> {
                    return new Pair<>(Result.succeed(),
                            new PlayerCraftImpl(type, w, p));
                },
                world, player,
                Movecraft.getAdventure().player(player),
                craft -> () -> {
                    Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(craft, CraftPilotEvent.Reason.PLAYER));
                }
        );
        return (this.getCraftFromBlock(b));
    }


    public Craft forcePilot(@NotNull CraftType craftType, @NotNull Block b, @NotNull Player player) {
        MovecraftLocation startPoint = new MovecraftLocation(b.getX(), b.getY(), b.getZ());
        World world = (World)b.getWorld();
        this.detect(
                startPoint,
                craftType, (type, w, p, parents) -> {
                    if (type.getBoolProperty(CraftType.CRUISE_ON_PILOT)) {
                        if (parents.size() >= 1)
                            return new Pair<>(Result.failWithMessage(I18nSupport.getInternationalisedString(
                                    "Detection - Failed - Already commanding a craft")), null);
                        if (parents.size() == 1) {
                            Craft parent = parents.iterator().next();
                            return new Pair<>(Result.succeed(),
                                    new CruiseOnPilotSubCraft(type, world, p, parent));
                        }

                        return new Pair<>(Result.succeed(),
                                new CruiseOnPilotCraft(type, world, p));
                    }
                    else {
                        if (parents.size() > 0)
                            return new Pair<>(Result.failWithMessage(I18nSupport.getInternationalisedString(
                                    "Detection - Failed - Already commanding a craft")), null);

                        return new Pair<>(Result.succeed(),
                                new PlayerCraftImpl(type, w, p));
                    }
                },
                world, player,
                Movecraft.getAdventure().player(player),
                craft -> () -> {
                    Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(craft, CraftPilotEvent.Reason.PLAYER));
                    if (craft instanceof SubCraft) { // Subtract craft from the parent
                        Craft parent = ((SubCraft) craft).getParent();
                        var newHitbox = parent.getHitBox().difference(craft.getHitBox());
                        parent.setHitBox(newHitbox);
                        parent.setOrigBlockCount(parent.getOrigBlockCount() - craft.getHitBox().size());
                    }
                    else {
                        // Release old craft if it exists
                        Craft oldCraft = this.getCraftByPlayer(player);
                        if (oldCraft != null)
                            this.removeCraft(oldCraft, CraftReleaseEvent.Reason.PLAYER);
                    }
                }
        );
        return (this.getCraftFromBlock(b));
    }

    @Nullable
    @Deprecated
    public Player getPlayerFromCraft(@NotNull Craft c) {
        for (var entry : craftPlayerIndex.entrySet()) {
            if (entry.getValue() == c)
                return entry.getKey();
        }
        return null;
    }
    public BaseCraft getCraftFromPlayer(@NotNull Player p) {
        for (var entry : craftPlayerIndex.entrySet()) {
            if (entry.getKey() == p)
                return (BaseCraft)entry.getValue();
        }
        return null;
    }

    @NotNull
    public Set<PlayerCraft> getPlayerCraftsInWorld(World world) {
        Set<PlayerCraft> crafts = new HashSet<>();
        for (PlayerCraft craft : craftPlayerIndex.values()) {
            if (craft.getWorld() == world)
                crafts.add(craft);
        }
        return crafts;
    }
    public Set<PlayerCraft> getPlayerCrafts() {
        Set<PlayerCraft> crafts = new HashSet<>();
        for (PlayerCraft craft : craftPlayerIndex.values()) {
          if (craft instanceof PlayerCraft){
              crafts.add(craft);
          }
        }
      return crafts;
    }
    public Set<PlayerCraft> getPlayerCraftList() {
        Set<PlayerCraft> crafts = new HashSet<>();
        for (PlayerCraft craft : craftPlayerIndex.values()) {
          if (craft instanceof PlayerCraft){
              crafts.add(craft);
          }
        }
      return crafts;
    }

    public static <K, V> Map<K, V> reverseMap(Map<K, V> map) {
        LinkedHashMap<K, V> reversed = new LinkedHashMap<>();
        List<K> keys = new ArrayList<>(map.keySet());
        Collections.reverse(keys);
        keys.forEach((key) -> reversed.put(key, map.get(key)));
        return reversed;
    }

    public static boolean containsIgnoreCase(String src, String what) {
        final int length = what.length();
        if (length == 0)
            return true; // Empty string is contained

        final char firstLo = Character.toLowerCase(what.charAt(0));
        final char firstUp = Character.toUpperCase(what.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp)
                continue;

            if (src.regionMatches(true, i, what, 0, length))
                return true;
        }

        return false;
    }

    public Craft getCraftFromBlock(Block b){
      Set<Craft> craftsList = this.getCraftList();
      for (Craft i : craftsList) {
          if (i instanceof Craft){
            if (((Craft)i).getHitBox().contains(MathUtils.bukkit2MovecraftLoc(b.getLocation()))) {
              return i;
            }
          }
        }
     return null;
    }
    public boolean isEqualSign(Sign test, String target) {
        return !ChatColor.stripColor(test.getLine(0)).equalsIgnoreCase(HEADER) && ( ChatColor.stripColor(test.getLine(0)).equalsIgnoreCase(target)
                || ChatColor.stripColor(test.getLine(1)).equalsIgnoreCase(target)
                || ChatColor.stripColor(test.getLine(2)).equalsIgnoreCase(target)
                || ChatColor.stripColor(test.getLine(3)).equalsIgnoreCase(target) );
    }
    public boolean isForbidden(Sign test) {
        for (int i = 0; i < 4; i++) {
            String t = test.getLine(i).toLowerCase();
            if(Settings.ForbiddenRemoteSigns.contains(t))
                return true;
        }
        return false;
    }
    public Set<Location> checkCraftBorders(Craft craft) {
        Set<Location> mergePoints = new HashSet<>();
        final EnumSet<Material> ALLOWED_BLOCKS = craft.getType().getMaterialSetProperty(CraftType.ALLOWED_BLOCKS);
        final EnumSet<Material> FORBIDDEN_BLOCKS = craft.getType().getMaterialSetProperty(CraftType.FORBIDDEN_BLOCKS);
        final MovecraftLocation[] SHIFTS = {
                //x
                new MovecraftLocation(-1, 0, 0),
                new MovecraftLocation(-1, -1, 0),
                new MovecraftLocation(-1,1,0),
                new MovecraftLocation(1, -1, 0),
                new MovecraftLocation(1, 1, 0),
                new MovecraftLocation(1, 0, 0),
                //z
                new MovecraftLocation(0, 1, 1),
                new MovecraftLocation(0, 0, 1),
                new MovecraftLocation(0, -1, 1),
                new MovecraftLocation(0, 1, -1),
                new MovecraftLocation(0, 0, -1),
                new MovecraftLocation(0, -1, -1),
                //y
                new MovecraftLocation(0, 1, 0),
                new MovecraftLocation(0, -1, 0)};
        //Check each location in the hitbox
        for (MovecraftLocation ml : craft.getHitBox()){
            //Check the surroundings of each location
            for (MovecraftLocation shift : SHIFTS){
                MovecraftLocation test = ml.add(shift);
                //Ignore locations contained in the craft's hitbox
                if (craft.getHitBox().contains(test)){
                    continue;
                }
                Block testBlock = test.toBukkit(craft.getWorld()).getBlock();
                Material testMaterial = testBlock.getType();
                //Break the loop if an allowed block is found adjacent to the craft's hitbox
                if (ALLOWED_BLOCKS.contains(testMaterial)){
                    mergePoints.add(testBlock.getLocation());
                }
                //Do the same if a forbidden block is found
                else if (FORBIDDEN_BLOCKS.contains(testMaterial)){
                    mergePoints.add(testBlock.getLocation());
                }
            }
        }
        //Return the string representation of the merging point and alert the pilot
        return mergePoints;
    }


    @Deprecated
    public void removePlayerFromCraft(Craft c) {
        if (!(c instanceof PlayerCraft)) {
            return;
        }
        removeReleaseTask(c);
        Player p = ((PlayerCraft) c).getPilot();
        p.sendMessage(I18nSupport.getInternationalisedString("Release - Craft has been released message"));
        Movecraft.getInstance().getLogger().info(String.format(I18nSupport.getInternationalisedString("Release - Player has released a craft console"), p.getName(), c.getType().getStringProperty(CraftType.NAME), c.getHitBox().size(), c.getHitBox().getMinX(), c.getHitBox().getMinZ()));
        c.setNotificationPlayer(null);
        craftPlayerIndex.remove(p);
    }


    @Deprecated
    public final void addReleaseTask(Craft c) {
        if (c instanceof Craft) {
          Craft craft = (Craft)c;
          Player p = getPlayerFromCraft(craft);
          if (p != null) {
              p.sendMessage(I18nSupport.getInternationalisedString("Release - Player has left craft"));
          }
          BukkitTask releaseTask = new BukkitRunnable() {
              @Override
              public void run() {
                  removeCraft(craft, CraftReleaseEvent.Reason.PLAYER);
                  // I'm aware this is not ideal, but you shouldn't be using this anyways.
              }
          }.runTaskLater(Movecraft.getInstance(), (20 * 15));
          releaseEvents.put(craft, releaseTask);
      }

    }

    @Deprecated
    public final void removeReleaseTask(Object c) {
        Player p = getPlayerFromCraft(((Craft)c));
        if (p != null) {
            if (releaseEvents.containsKey(c)) {
                if (releaseEvents.get(c) != null)
                    releaseEvents.get(c).cancel();
                releaseEvents.remove(c);
            }
        }
    }

    @Deprecated
    public boolean isReleasing(Object craft) {
        return releaseEvents.containsKey(craft);
    }

    public Set<Craft> getNormalCraftList() {
        Set<Craft> crafts = new HashSet<>();
        for (Craft craft : craftList) {
            if (craft instanceof Craft) {
                crafts.add(((Craft)craft));
            }
        }
        return crafts;
    }
    @NotNull
    public Set<Craft> getCrafts() {
        return Collections.unmodifiableSet(craftList);
    }

    @NotNull
    public Set<Craft> getCraftList() {
        return getCrafts();
    }

    @Nullable
    public CraftType getCraftTypeFromString(String s) {
        for (CraftType t : craftTypes) {
            if (s.equalsIgnoreCase(t.getStringProperty(CraftType.NAME))) {
                return t;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return crafts.isEmpty();
    }

    @NotNull
    @Override
    public Iterator<Craft> iterator() {
        return Collections.unmodifiableSet(craftList).iterator();
    }

    public void addOverboard(Player player) {
        overboards.put(player, System.currentTimeMillis());
    }

    @NotNull
    public long getTimeFromOverboard(Player player) {
        return overboards.getOrDefault(player, 0L);
    }

    @Nullable
    public Craft fastNearestCraftToLoc(Location loc) {
        Craft ret = null;
        long closestDistSquared = Long.MAX_VALUE;
        Set<Craft> craftsList = this.getCraftsInWorld(loc.getWorld());
        for (Craft i : craftsList) {
            if (i.getHitBox().isEmpty())
                continue;
            int midX = (i.getHitBox().getMaxX() + i.getHitBox().getMinX()) >> 1;
//				int midY=(i.getMaxY()+i.getMinY())>>1; don't check Y because it is slow
            int midZ = (i.getHitBox().getMaxZ() + i.getHitBox().getMinZ()) >> 1;
            long distSquared = (long) (Math.pow(midX -  loc.getX(), 2) + Math.pow(midZ - (int) loc.getZ(), 2));
            if (distSquared < closestDistSquared) {
                closestDistSquared = distSquared;
                ret = i;
            }
        }
        return ret;
    }
}
