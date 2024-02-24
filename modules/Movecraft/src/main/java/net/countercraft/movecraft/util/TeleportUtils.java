package net.countercraft.movecraft.util;

import net.countercraft.movecraft.craft.PlayerCraftImpl;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.Movecraft;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.GameRule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.inventory.InventoryType;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.function.Supplier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class TeleportUtils {
    public static boolean hideOutput = true;

    public static void teleportEntity(Entity entity, Location location) {
        Location to = location;
        boolean tp = false;
        BaseCraft pcraft = null;
        if (entity instanceof Player) pcraft = CraftManager.getInstance().getCraftFromPlayer((Player)entity);
        try {
            if (!tp && entity instanceof Player && pcraft instanceof BaseCraft) {
                if (entity.getWorld().equals(location.getWorld())) {
                  if (((Player)entity).getOpenInventory().getType() != InventoryType.CRAFTING && ((Player)entity).getOpenInventory().getType() != null && !(((PlayerCraftImpl)pcraft).getPilotLocked())) {
                    try {
                      Movecraft.getInstance().getSmoothTeleport().teleport((Player) entity, location, 0.0f, 0.0f);
                    } catch (Exception e){
                      tp = (entity).teleport(to,io.papermc.paper.entity.TeleportFlag.Relative.values());
                    }
                  } else {
                    tp = (entity).teleport(to,io.papermc.paper.entity.TeleportFlag.EntityState.values());
                  }
              }
            } else {
              if (entity instanceof Player) {
                tp = (entity).teleport(to,io.papermc.paper.entity.TeleportFlag.Relative.values());
              } else {
                tp = (entity).teleport(to,io.papermc.paper.entity.TeleportFlag.EntityState.values());

              }

            }
        } catch (Exception e) {
            (entity).teleportAsync(to);
        }
        return;
    }

    public static void teleport(Entity player, Location location, float yawChange) {
        if (!player.getWorld().equals(location.getWorld())) {
          (player).teleport(location,io.papermc.paper.entity.TeleportFlag.EntityState.values());
          return;
        }
        if (player.getVehicle()!=null) {
          Entity vehicle = player.getVehicle();
          player.setRotation(player.getLocation().getYaw() + yawChange, player.getLocation().getPitch());
          teleportEntity(vehicle,location);
          return;
        }
        if (yawChange != 0F) {
          (player).teleport(location,io.papermc.paper.entity.TeleportFlag.Relative.values());
          return;
        }
        teleportEntity(player,location);
    }
}
