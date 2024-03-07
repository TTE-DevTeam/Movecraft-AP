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

package net.countercraft.movecraft.listener;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.type.RequiredBlockEntry;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftPreTranslateEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.events.PlayerCraftMovementEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.hitboxes.BitmapHitBox;
import net.countercraft.movecraft.util.hitboxes.SetHitBox;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
//import com.jeff_media.customblockdata.*;
//import com.jeff_media.morepersistentdatatypes.*;

import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;


public class PlayerListener implements Listener {
    private final Map<Craft, Long> timeToReleaseAfter = new WeakHashMap<>();
    private final Map<Player, Long> timeMap = new WeakHashMap<>();
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
                //Break the loop if an allowed block is found adjacent = the craft's hitbox
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

    @EventHandler
    public void onCraftTranslate(CraftTranslateEvent e) {}
    
    @EventHandler
    public void onCraftRotate(CraftRotateEvent e) {
    }

    @EventHandler
    public void onCraftPreTranslate(CraftPreTranslateEvent e) {
    }

    @EventHandler
    public void onCraftRelease(CraftReleaseEvent e) {
        if (e.isCancelled()) return;
        Craft crft = e.getCraft();
        if (crft instanceof BaseCraft) {
          BaseCraft craft = (BaseCraft)crft;
          if (craft.getOrigBlockCount() < 1000000) {
            for (Block block : craft.getBlockName("SIGN")) {
              Sign sign = (Sign)block.getState();
              if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(craft.getType().getName())) continue;
              if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("pilot:")) continue;
              if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[private]")) continue;
              try {
                sign.setEditable(true);
                sign.update(true,false);
              } catch (Exception exc) {}
            }
          }
          craft.getRawTrackedMap().clear();
          craft.getCraftTags().clear();
        }
    }

    @EventHandler
    public void onCraftPilotEvent(final CraftPilotEvent e) {
        Movecraft instance = Movecraft.getInstance();
        if (e.getCraft() instanceof BaseCraft) {
          final BaseCraft craft = (BaseCraft)e.getCraft();
          final Player player = craft.getNotificationPlayer();
          if (player != null) craft.addPassenger(player);
          if (craft.getOrigBlockCount() >= 1000000) return;
          final int waterline = craft.getWaterLine();
          final SetHitBox interior = CraftManager.getInstance().detectCraftInterior(craft);
          final SetHitBox fullBox = new SetHitBox(craft.getHitBox().union(interior));
          if (craft.getOrigBlockCount() < 1000000) {
            for (Block block : craft.getBlockName("SIGN")) {
              Sign sign = (Sign)block.getState();
              try {
                sign.setEditable(false);
                sign.update(true,false);
              } catch (Exception exc) {}
            }
            for (MovecraftLocation loc : fullBox.boundingHitBox()) {
              if (fullBox.contains(loc)) continue;
              if (loc.getY() <= waterline) {
                if (Tags.FLUID.contains(loc.toBukkit(craft.getWorld()).getBlock().getType())) {
                  craft.getPhaseBlocks().put(loc.toBukkit(craft.getWorld()),Movecraft.getInstance().getWaterBlockData());
                  continue;
                }
              }
            }
          }
          if (player != null) Movecraft.getInstance().getLogger().info(player.getName()+"'s Craft ("+craft+") Internal Air-Hitbox Size: "+interior.size());
          else Movecraft.getInstance().getLogger().info("N/A's Craft ("+craft+") Internal Air-Hitbox Size: "+interior.size());
          craft.setDataTag("origin_size",craft.getHitBox().size()-interior.size());
          craft.setTrackedMovecraftLocs("air",interior.asSet());
          craft.setHitBox(fullBox);
        }
        e.getCraft().setLastBlockCheck(System.currentTimeMillis());
    }

    @EventHandler
    public void onPLayerLogout(PlayerQuitEvent e) {
        try {
          CraftManager.getInstance().forceRemoveCraft(CraftManager.getInstance().getCraftByPlayer(e.getPlayer()));
        } catch (Exception ex) {}
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent e) {}

    @EventHandler
    public void onPlayerDeath(EntityDamageByEntityEvent e) {  
        if (true) return; // changed = death so when you shoot up an airship and hit the pilot, it still sinks
        if (e instanceof Player) {
            Player p = (Player) e;
            //CraftManager.getInstance().removeCraft(CraftManager.getInstance().getCraftByPlayer(p), CraftReleaseEvent.Reason.DEATH);
        }
    }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    Player p = event.getPlayer();
    PlayerCraft c = CraftManager.getInstance().getCraftByPlayer(p);
    Craft craft = c;
    if (c == null)
      return;
    int dx, dy, dz;
    dx = dy = dz = 0;
    if (!MathUtils.locationNearHitBox((c.getHitBox()),p.getLocation(),2.0D)) {
        //Movecraft.getInstance().getLogger().warning("Skipping Entity: "+p+", Not Aboard");
        if (Settings.ManOverboardTimeout != 0) {
          //p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Manoverboard - Player has left craft"));
          //CraftManager.getInstance().addOverboard(p);
        } else {
          //p.sendMessage(I18nSupport.getInternationalisedString("Release - Player has left craft"));
        }
    }
    if (MathUtils.locationNearHitBox(c.getHitBox(), p.getLocation(), 2.0D)) {
      //this.timeToReleaseAfter.remove(c);
      if (!(CraftManager.getInstance().getCraftsInWorld(p.getWorld()).contains(c))) {
        return;
      }
      if (c.getPilotLocked()) {
        CraftType type = c.getType();
        Vector from = event.getFrom().toVector();
        Vector to = event.getTo().toVector();
        Location loc = event.getTo();
        Location toloc = loc.clone();
        Location fromloc = event.getFrom();

        if (!p.hasPermission("movecraft." + craft.getType().getStringProperty(CraftType.NAME) + ".move")) {
            p.sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return; // Player doesn't have permission = move this craft, so don't do anything
        }

        int tickCooldown = (int) craft.getType().getPerWorldProperty(
                CraftType.PER_WORLD_TICK_COOLDOWN, craft.getWorld());
        if (type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_DIRECT_MOVEMENT)
                && type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN))
            tickCooldown *= c.getCurrentGear(); // Account for gear shifts
        Long lastTime = timeMap.get(p);
        Location newloc = new Location(fromloc.getWorld(), fromloc.getBlockX()+0.5,fromloc.getY(),fromloc.getBlockZ()+0.5);
        newloc.setYaw(toloc.getYaw());
        newloc.setPitch(toloc.getPitch());
        if (lastTime != null) {
            long ticksElapsed = (System.currentTimeMillis() - lastTime) / 50;

            // if the craft should go slower underwater, make time pass more slowly there
            if (craft.getType().getBoolProperty(CraftType.HALF_SPEED_UNDERWATER)
                    && craft.getHitBox().getMinY() < craft.getWorld().getSeaLevel())
                ticksElapsed /= 2;

              event.setTo(newloc);
            if (ticksElapsed < tickCooldown)
                return; // Not enough time has passed, so don't do anything
        }
        if (to.getX() - from.getX() != 0.0D || to.getZ() - from.getZ() != 0.0D || to.getY() - from.getY() != 0.0D) {
          dx = (int)Math.signum((Math.abs(to.getX() - from.getX()) > 0.07D) ? (to.getX() - from.getX()) : 0.0D);
          dz = (int)Math.signum((Math.abs(to.getZ() - from.getZ()) > 0.07D) ? (to.getZ() - from.getZ()) : 0.0D);
          if (c.getCurrentGear() > 1) {
            dx *= c.getCurrentGear();
            dz *= c.getCurrentGear();
          }
          if (dx != 0 || dz != 0) {
            PlayerCraftMovementEvent pcme = new PlayerCraftMovementEvent(c,dx,dy,dz);
            Bukkit.getPluginManager().callEvent(pcme);
            if (pcme.isCancelled()) return;
            newloc.setYaw(p.getLocation().getYaw());
            newloc.setPitch(p.getLocation().getPitch());
            //event.setTo(newloc);
            p.teleport(newloc,org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN,io.papermc.paper.entity.TeleportFlag.Relative.values());
            timeMap.put(p, System.currentTimeMillis());
            craft.setLastCruiseUpdate(System.currentTimeMillis());
            c.translate(dx, 0, dz);
          }
          return;
        }
      }
    }
  }
}
