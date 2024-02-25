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

package net.countercraft.movecraft.async.rotation;

import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.MovecraftChunk;
import net.countercraft.movecraft.craft.ChunkManager;
import net.countercraft.movecraft.async.AsyncTask;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.SinkingCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftTeleportEntityEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.mapUpdater.update.AccessLocationUpdateCommand;
import net.countercraft.movecraft.mapUpdater.update.CraftRotateCommand;
import net.countercraft.movecraft.mapUpdater.update.EntityUpdateCommand;
import net.countercraft.movecraft.mapUpdater.update.UpdateCommand;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.util.hitboxes.MutableHitBox;
import net.countercraft.movecraft.util.hitboxes.SetHitBox;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import static net.countercraft.movecraft.util.MathUtils.withinWorldBorder;

public class RotationTask extends AsyncTask {
    private MovecraftLocation originPoint;
    private final MovecraftRotation rotation;
    private final World w;
    private final boolean isSubCraft;
    private boolean failed = false;
    private String failMessage;
    //private final MovecraftLocation[] blockList;    // used to be final, not sure why. Changed by Mark / Loraxe42
    private Set<UpdateCommand> updates = new HashSet<>();

    private final MutableHitBox oldHitBox;
    private final MutableHitBox newHitBox;
    private final MutableHitBox oldFluidList;
    private final MutableHitBox newFluidList;

    public RotationTask(Craft c, MovecraftLocation originPoint, MovecraftRotation rotation, World w, boolean isSubCraft) {
        super(c);
        this.rotation = rotation;
        this.w = w;
        this.isSubCraft = isSubCraft;
        if (!this.isSubCraft) {
            this.originPoint = c.getHitBox().getMidPoint();
        } else {
            this.originPoint = originPoint;
        }
        this.newHitBox = new SetHitBox();
        this.oldHitBox = new SetHitBox(c.getHitBox());
        this.oldFluidList = new SetHitBox(c.getFluidLocations());
        this.newFluidList = new SetHitBox(c.getFluidLocations());
    }

    public RotationTask(Craft c, MovecraftLocation originPoint, MovecraftRotation rotation, World w) {
        this(c,originPoint,rotation,w,false);
    }

    @Override
    protected void execute() throws InterruptedException, ExecutionException {

        if(oldHitBox.isEmpty())
            return;
        if (getCraft().getDisabled() && !(getCraft() instanceof SinkingCraft)) {
            failed = true;
            failMessage = I18nSupport.getInternationalisedString("Rotation - Failed Craft Is Disabled");
        }
        // check for fuel, burn some from a furnace if needed. Blocks of coal are supported, in addition to coal and charcoal
        if (!checkFuel() && craft.getNotificationPlayer() != null) {
            failMessage = I18nSupport.getInternationalisedString("Rotation - Failed Craft out of fuel");
            failed = true;
            return;
        }

        if (!this.isSubCraft) {
            this.originPoint = this.oldHitBox.getMidPoint();
        }
        // if a subcraft, find the parent craft. If not a subcraft, it is it's own parent
        Set<Craft> craftsInWorld = CraftManager.getInstance().getCraftsInWorld(getCraft().getWorld());
        Craft parentC = this.getCraft();
        for (Craft craft : craftsInWorld) {
            if ( craft != getCraft() && !craft.getHitBox().intersection(oldHitBox).isEmpty()) {
                parentC = craft;
                break;
            }
        }
        final Craft parentCraft = parentC;
        final boolean subCraft = this.isSubCraft;

        final Craft craft = getCraft(); 
        boolean ran = false;
        for(MovecraftLocation originalLocation : oldHitBox){
                MovecraftLocation newLocation = MathUtils.rotateVec(rotation,originalLocation.subtract(originPoint)).add(originPoint);
                newHitBox.add(newLocation);

                Material oldMaterial = originalLocation.toBukkit(w).getBlock().getType();
                //prevent chests collision
                if (Tags.CHESTS.contains(oldMaterial) && !checkChests(oldMaterial, newLocation)) {
                    failed = true;
                    failMessage = String.format(I18nSupport.getInternationalisedString("Rotation - Craft is obstructed") + " @ %d,%d,%d", newLocation.getX(), newLocation.getY(), newLocation.getZ());
                    break;
                }

                if (!withinWorldBorder(craft.getWorld(), newLocation)) {
                    failMessage = I18nSupport.getInternationalisedString("Rotation - Failed Craft cannot pass world border") + String.format(" @ %d,%d,%d", newLocation.getX(), newLocation.getY(), newLocation.getZ());
                    failed = true;
                    return;
                }

                Material newMaterial = newLocation.toBukkit(w).getBlock().getType();
                if (newMaterial.isAir() || (newMaterial == Material.PISTON_HEAD) || craft.getType().getMaterialSetProperty(CraftType.PASSTHROUGH_BLOCKS).contains(newMaterial))
                    continue;

                if (!oldHitBox.contains(newLocation)) {
                    failed = true;
                    failMessage = String.format(I18nSupport.getInternationalisedString("Rotation - Craft is obstructed") + " @ %d,%d,%d", newLocation.getX(), newLocation.getY(), newLocation.getZ());
                    break;
                }
            }

            if (!oldFluidList.isEmpty()) {
                for (MovecraftLocation fluidLoc : oldFluidList) {
                    newFluidList.add(MathUtils.rotateVec(rotation, fluidLoc.subtract(originPoint)).add(originPoint));
                }
            }

            if (failed) {
                if (subCraft && parentCraft != getCraft()) {
                    parentCraft.setProcessing(false);
                }
                return;
            }
              //call event
        if (!ran) {
            final Set<MovecraftChunk> chunksToLoad = ChunkManager.getChunks(oldHitBox, craft.getWorld());
            MovecraftChunk.addSurroundingChunks(chunksToLoad, 2);
            ChunkManager.checkChunks(chunksToLoad);
            if (!chunksToLoad.isEmpty())
                ChunkManager.addChunksToLoad(chunksToLoad);//.get()
            chunksToLoad.clear();
            chunksToLoad.addAll(ChunkManager.getChunks(newHitBox, w, 0, 0, 0));
            MovecraftChunk.addSurroundingChunks(chunksToLoad, 2);
            ChunkManager.checkChunks(chunksToLoad);
            if (!chunksToLoad.isEmpty())
                ChunkManager.addChunksToLoad(chunksToLoad);//.get()
        }

              CraftRotateEvent event = new CraftRotateEvent(craft, rotation, originPoint, oldHitBox, newHitBox);
              Bukkit.getServer().getPluginManager().callEvent(event);
              if(event.isCancelled()){
                  failed = true;
                  failMessage = event.getFailMessage();
                  return;
              }
              if (parentCraft != craft) {
                  parentCraft.getFluidLocations().removeAll(oldFluidList);
                  parentCraft.getFluidLocations().addAll(newFluidList);
              }
              //rotate entities in the craft
              Location tOP = new Location(getCraft().getWorld(), originPoint.getX(), originPoint.getY(), originPoint.getZ());
              tOP.setX(tOP.getBlockX() + 0.5);
              tOP.setZ(tOP.getBlockZ() + 0.5);
              
              if (craft.getType().getBoolProperty(CraftType.MOVE_ENTITIES) && !(craft.getSinking() && craft.getType().getBoolProperty(CraftType.ONLY_MOVE_PLAYERS))) {
                  Location midpoint = oldHitBox.getMidPoint().toBukkit(craft.getWorld());
                  Entity passenger = null;
                  List<Entity> nearEntites = new ArrayList<>();
                  nearEntites.addAll(craft.getWorld().getNearbyEntities(midpoint, oldHitBox.getXLength() / 2.0 + 3, oldHitBox.getYLength() / 1.5 + 3, oldHitBox.getZLength() / 2.0 + 3));
                  nearEntites.addAll(((BaseCraft)craft).getPassengers());
                  for (Entity entity : nearEntites) {
                      if (entity == null)
                        continue;
                      if (entity.getType() != EntityType.FIREWORK && entity.getType() != EntityType.ARROW) {
                        if (entity.getType().toString().contains("Display")) {
                            if (!(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(entity.getLocation())))) {
                                ((BaseCraft)craft).removePassenger(entity);
                                continue;
                            }
                            if ((((BaseCraft)craft).getPassengers().contains(entity) == false)) {
                                ((BaseCraft)craft).addPassenger(entity);
                            }
                        }
                      }
                  }
                  for (Entity entity : nearEntites) {
                      if (entity == null)
                          continue;
                      if ((((BaseCraft)craft).getPassengers().contains(entity) == false)) {
                          continue;
                      }
                        if (craft instanceof BaseCraft) {
                            if (!((BaseCraft)craft).hasPassenger(entity)) continue;
                        }
                      InventoryView inventoryView = null;
                      Location iLoc = null;
                      MovecraftLocation invMoveLoc = null;
                      if (entity instanceof HumanEntity) {
                          inventoryView = ((HumanEntity) entity).getOpenInventory();
                          if (inventoryView.getType() != InventoryType.CRAFTING) {
                              iLoc = Movecraft.getInstance().getWorldHandler().getAccessLocation(inventoryView);
                              if (iLoc != null) {
                                  invMoveLoc = new MovecraftLocation(iLoc.getBlockX(), iLoc.getBlockY(), iLoc.getBlockZ());
                                  if (inventoryView.getTopInventory().getHolder() == null) {
                                      invMoveLoc = null;
                                  }
                              }
                          }
                      }
                      if (((entity.getType() == EntityType.PLAYER || entity.getType() == EntityType.PRIMED_TNT) && !craft.getSinking()) || !craft.getType().getBoolProperty(CraftType.ONLY_MOVE_PLAYERS) || ((BaseCraft)craft).getPassengers().contains(entity)) {
                          // Player is onboard this craft
                          if (!MathUtils.locationNearHitBox(craft.getHitBox(),entity.getLocation(),3.5)) {
                            if (!MathUtils.locationNearHitBox(craft.getHitBox().boundingHitBox(),entity.getLocation(),3.5)) {
                              //Movecraft.getInstance().getLogger().warning("Skipping Entity: "+entity+", Not Aboard");
                              continue;
                            }
                          }
                          Location adjustedPLoc = entity.getLocation().subtract(tOP);

                          double[] rotatedCoords = MathUtils.rotateVecNoRound(rotation,
                                  adjustedPLoc.getX(), adjustedPLoc.getZ());
                          float newYaw = rotation == MovecraftRotation.CLOCKWISE ? 90F : -90F;
                          if (entity.getVehicle() != null){
                              continue;
                          }
                          EntityUpdateCommand eUp = new EntityUpdateCommand(entity, rotatedCoords[0] + tOP.getX() - entity.getLocation().getX(), 0, rotatedCoords[1] + tOP.getZ() - entity.getLocation().getZ(), newYaw, 0);
                          if (entity.getVehicle() != null){
                                eUp = new EntityUpdateCommand(entity.getVehicle(), rotatedCoords[0] + tOP.getX() - entity.getLocation().getX(), 0, rotatedCoords[1] + tOP.getZ() - entity.getLocation().getZ(), newYaw, 0);
                          }
                          updates.add(eUp);
                          if (entity instanceof HumanEntity) {
                              if (iLoc != null && invMoveLoc != null) {
                                  if (oldHitBox.contains(invMoveLoc)) {
                                      invMoveLoc = MathUtils.rotateVec(rotation, invMoveLoc.subtract(originPoint)).add(originPoint);
                                      updates.add(new AccessLocationUpdateCommand(inventoryView, invMoveLoc.toBukkit(w)));
                                  }
                              }
                          }
                      }
                  }
              }

              updates.add(new CraftRotateCommand(craft, originPoint, rotation));
              //rotate entities in the craft
              tOP = new Location(getCraft().getWorld(), originPoint.getX(), originPoint.getY(), originPoint.getZ());
              tOP.setX(tOP.getBlockX() + 0.5);
              tOP.setZ(tOP.getBlockZ() + 0.5);
              if (craft.getType().getBoolProperty(CraftType.MOVE_ENTITIES) && !(craft.getSinking() && craft.getType().getBoolProperty(CraftType.ONLY_MOVE_PLAYERS))) {
                  Location midpoint = oldHitBox.getMidPoint().toBukkit(craft.getWorld());
                  Entity passenger = null;
                  List<Entity> nearEntites = new ArrayList<>();
                  nearEntites.addAll(craft.getWorld().getNearbyEntities(midpoint, oldHitBox.getXLength() / 2.0 + 3, oldHitBox.getYLength() / 1.5 + 3, oldHitBox.getZLength() / 2.0 + 3));
                  nearEntites.addAll(((BaseCraft)craft).getPassengers());
                  for (Entity entity : nearEntites) {
                      if (entity == null)
                        continue;
                      if (entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.FIREWORK && entity.getType() != EntityType.ARROW) {
                        if (entity.getType().toString().contains("Display")) {
                            if (!(craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(entity.getLocation())))) {
                                ((BaseCraft)craft).removePassenger(entity);
                                continue;
                            }
                            if ((((BaseCraft)craft).getPassengers().contains(entity) == false)) {
                                ((BaseCraft)craft).addPassenger(entity);
                            }
                        }
                      }
                  }
                  for (Entity entity : nearEntites) {
                      if (entity == null)
                          continue;
                      if ((((BaseCraft)craft).getPassengers().contains(entity) == false)) {
                          continue;
                      }
                        if (craft instanceof BaseCraft) {
                            if (!((BaseCraft)craft).hasPassenger(entity)) continue;
                        }
                      InventoryView inventoryView = null;
                      Location iLoc = null;
                      MovecraftLocation invMoveLoc = null;
                      if (entity instanceof HumanEntity) {
                          inventoryView = ((HumanEntity) entity).getOpenInventory();
                          if (inventoryView.getType() != InventoryType.CRAFTING) {
                              iLoc = Movecraft.getInstance().getWorldHandler().getAccessLocation(inventoryView);
                              if (iLoc != null) {
                                  invMoveLoc = new MovecraftLocation(iLoc.getBlockX(), iLoc.getBlockY(), iLoc.getBlockZ());
                                  if (inventoryView.getTopInventory().getHolder() == null) {
                                      invMoveLoc = null;
                                  }
                              }
                          }
                      }
                      if (((entity.getType() == EntityType.PRIMED_TNT) && !craft.getSinking()) || !craft.getType().getBoolProperty(CraftType.ONLY_MOVE_PLAYERS) || ((BaseCraft)craft).getPassengers().contains(entity)) {
                          // Player is onboard this craft
                          if (!MathUtils.locationNearHitBox(craft.getHitBox(),entity.getLocation(),3.5)) {
                            if (!MathUtils.locationNearHitBox(craft.getHitBox().boundingHitBox(),entity.getLocation(),3.5)) {
                              //Movecraft.getInstance().getLogger().warning("Skipping Entity: "+entity+", Not Aboard");
                              continue;
                            }
                          }
                          Location adjustedPLoc = entity.getLocation().subtract(tOP);

                          double[] rotatedCoords = MathUtils.rotateVecNoRound(rotation,
                                  adjustedPLoc.getX(), adjustedPLoc.getZ());
                          float newYaw = rotation == MovecraftRotation.CLOCKWISE ? 90F : -90F;
                          EntityUpdateCommand eUp = new EntityUpdateCommand(entity, rotatedCoords[0] + tOP.getX() - entity.getLocation().getX(), 0, rotatedCoords[1] + tOP.getZ() - entity.getLocation().getZ(), newYaw, 0);
                          if (entity.getVehicle() != null){
                                eUp = new EntityUpdateCommand(entity.getVehicle(), rotatedCoords[0] + tOP.getX() - entity.getLocation().getX(), 0, rotatedCoords[1] + tOP.getZ() - entity.getLocation().getZ(), newYaw, 0);
                          }
                          updates.add(eUp);
                          if (entity instanceof HumanEntity) {
                              if (iLoc != null && invMoveLoc != null) {
                                  if (oldHitBox.contains(invMoveLoc)) {
                                      invMoveLoc = MathUtils.rotateVec(rotation, invMoveLoc.subtract(originPoint)).add(originPoint);
                                      updates.add(new AccessLocationUpdateCommand(inventoryView, invMoveLoc.toBukkit(w)));
                                  }
                              }
                          }
                      }
                  }
              }

            if (getCraft().getCruising()) {
                if (rotation == MovecraftRotation.ANTICLOCKWISE) {
                    // ship faces west
                    switch (getCraft().getCruiseDirection()) {
                        case WEST:
                            getCraft().setCruiseDirection(CruiseDirection.SOUTH);
                            break;
                        // ship faces east
                        case EAST:
                            getCraft().setCruiseDirection(CruiseDirection.NORTH);
                            break;
                        // ship faces north
                        case SOUTH:
                            getCraft().setCruiseDirection(CruiseDirection.EAST);
                            break;
                        // ship faces south
                        case NORTH:
                            getCraft().setCruiseDirection(CruiseDirection.WEST);
                            break;
                    }
                } else if (rotation == MovecraftRotation.CLOCKWISE) {
                    // ship faces west
                    switch (getCraft().getCruiseDirection()) {
                        case WEST:
                            getCraft().setCruiseDirection(CruiseDirection.NORTH);
                            break;
                        // ship faces east
                        case EAST:
                            getCraft().setCruiseDirection(CruiseDirection.SOUTH);
                            break;
                        // ship faces north
                        case SOUTH:
                            getCraft().setCruiseDirection(CruiseDirection.WEST);
                            break;
                        // ship faces south
                        case NORTH:
                            getCraft().setCruiseDirection(CruiseDirection.EAST);
                            break;
                    }
                }
            }

            // if you rotated a subcraft, update the parent with the new blocks
            if (subCraft) {
                // also find the furthest extent from center and notify the player of the new direction
                int farthestX = 0;
                int farthestZ = 0;
                for (MovecraftLocation loc : newHitBox) {
                    if (Math.abs(loc.getX() - originPoint.getX()) > Math.abs(farthestX))
                        farthestX = loc.getX() - originPoint.getX();
                    if (Math.abs(loc.getZ() - originPoint.getZ()) > Math.abs(farthestZ))
                        farthestZ = loc.getZ() - originPoint.getZ();
                }
                Component faceMessage = I18nSupport.getInternationalisedComponent("Rotation - Farthest Extent Facing")
                        .append(Component.text(" "));
                if (Math.abs(farthestX) > Math.abs(farthestZ)) {
                    if (farthestX > 0) {
                        faceMessage = faceMessage.append(I18nSupport.getInternationalisedComponent("Contact/Subcraft Rotate - East"));
                    } else {
                        faceMessage = faceMessage.append(I18nSupport.getInternationalisedComponent("Contact/Subcraft Rotate - West"));
                    }
                } else {
                    if (farthestZ > 0) {
                        faceMessage = faceMessage.append(I18nSupport.getInternationalisedComponent("Contact/Subcraft Rotate - South"));
                    } else {
                        faceMessage = faceMessage.append(I18nSupport.getInternationalisedComponent("Contact/Subcraft Rotate - North"));
                    }
                }
                getCraft().getAudience().sendMessage(faceMessage);

                for (final Craft craft2 : CraftManager.getInstance().getCraftsInWorld(craft.getWorld())) {
                    if (!newHitBox.intersection(craft2.getHitBox()).isEmpty() && craft != getCraft()) {
                        //newHitBox.addAll(CollectionUtils.filter(craft.getHitBox(),newHitBox));
                        //craft.setHitBox(newHitBox);
                        if (Settings.Debug) {
                            Bukkit.broadcastMessage(String.format("Size of %s hitbox: %d, Size of %s hitbox: %d", craft2.getType().getStringProperty(CraftType.NAME), newHitBox.size(), craft.getType().getStringProperty(CraftType.NAME), craft.getHitBox().size()));
                        }
                        craft2.setHitBox(craft.getHitBox().difference(oldHitBox).union(newHitBox));
                        if (Settings.Debug){
                            Bukkit.broadcastMessage(String.format("Hitbox of craft %s intersects hitbox of craft %s", craft2.getType().getStringProperty(CraftType.NAME), craft.getType().getStringProperty(CraftType.NAME)));
                            Bukkit.broadcastMessage(String.format("Size of %s hitbox: %d, Size of %s hitbox: %d", craft2.getType().getStringProperty(CraftType.NAME), newHitBox.size(), craft.getType().getStringProperty(CraftType.NAME), craft.getHitBox().size()));
                        }
                        break;
                    }
                }
            }

    }

    public MovecraftLocation getOriginPoint() {
        return originPoint;
    }

    public boolean isFailed() {
        return failed;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public Set<UpdateCommand> getUpdates() {
        return updates;
    }

    public MovecraftRotation getRotation() {
        return rotation;
    }

    public boolean getIsSubCraft() {
        return isSubCraft;
    }

    private boolean checkChests(Material mBlock, MovecraftLocation newLoc) {
        Material testMaterial;
        MovecraftLocation aroundNewLoc;

        aroundNewLoc = newLoc.translate(1, 0, 0);
        testMaterial = craft.getWorld().getBlockAt(aroundNewLoc.getX(), aroundNewLoc.getY(), aroundNewLoc.getZ()).getType();
        if (testMaterial.equals(mBlock)) {
            if (!oldHitBox.contains(aroundNewLoc)) {
                return false;
            }
        }

        aroundNewLoc = newLoc.translate(-1, 0, 0);
        testMaterial = craft.getWorld().getBlockAt(aroundNewLoc.getX(), aroundNewLoc.getY(), aroundNewLoc.getZ()).getType();
        if (testMaterial.equals(mBlock)) {
            if (!oldHitBox.contains(aroundNewLoc)) {
                return false;
            }
        }

        aroundNewLoc = newLoc.translate(0, 0, 1);
        testMaterial = craft.getWorld().getBlockAt(aroundNewLoc.getX(), aroundNewLoc.getY(), aroundNewLoc.getZ()).getType();
        if (testMaterial.equals(mBlock)) {
            if (!oldHitBox.contains(aroundNewLoc)) {
                return false;
            }
        }

        aroundNewLoc = newLoc.translate(0, 0, -1);
        testMaterial = craft.getWorld().getBlockAt(aroundNewLoc.getX(), aroundNewLoc.getY(), aroundNewLoc.getZ()).getType();
        return !testMaterial.equals(mBlock) || oldHitBox.contains(aroundNewLoc);
    }

    @Override
    public World getWorld() {
        return w;
    }

    public MutableHitBox getNewHitBox() {
        return newHitBox;
    }

    public MutableHitBox getNewFluidList() {
        return newFluidList;
    }
}
