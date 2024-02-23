package net.countercraft.movecraft.listener;

import java.util.Map;
import java.util.WeakHashMap;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.*;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.PlayerCraftMovementEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.LivingEntity;
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
import org.jetbrains.annotations.NotNull;


public final class InteractListener implements Listener {
    private final Map<Player, Long> timeMap = new WeakHashMap<>();

    @EventHandler(priority = EventPriority.LOWEST) // LOWEST so that it runs before the other events
    public void onPlayerInteract(@NotNull PlayerInteractEvent e) {
        int dx, dy, dz;
        dx = dy = dz = 0;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) {
            if (e.getItem() != null && e.getItem().getType() == Settings.PilotTool) {
                // Handle pilot tool left clicks
                e.setCancelled(true);

                Player p = e.getPlayer();
                PlayerCraft craft = CraftManager.getInstance().getCraftByPlayer(p);
                if (craft == null)
                    return;

                if (craft.getPilotLocked()) {
                    // Allow all players to leave direct control mode
                    craft.setPilotLocked(false);
                    p.sendMessage(I18nSupport.getInternationalisedString("Direct Control - Leaving"));
                }
                else if (!p.hasPermission(
                        "movecraft." + craft.getType().getStringProperty(CraftType.NAME) + ".move")
                        || !craft.getType().getBoolProperty(CraftType.CAN_DIRECT_CONTROL)) {
                    // Deny players from entering direct control mode
                    p.sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
                }
                else {
                    // Enter direct control mode
                    return;
                    //craft.setPilotLocked(true);
                    //craft.setPilotLockedX(p.getLocation().getBlockX() + 0.5);
                    //craft.setPilotLockedY(p.getLocation().getY());
                    //craft.setPilotLockedZ(p.getLocation().getBlockZ() + 0.5);
                    //p.sendMessage(I18nSupport.getInternationalisedString("Direct Control - Entering"));
                }
            }
            else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                // Handle button left clicks
                BlockState state = e.getClickedBlock().getState();
                if (!(state instanceof Switch))
                    return;

                Switch data = (Switch) state.getBlockData();
                if (data.isPowered()) {
                    // Depower the button
                    data.setPowered(false);
                    e.getClickedBlock().setBlockData(data);
                    e.setCancelled(true);
                }
            }
        }
        else if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (e.getItem() == null || e.getItem().getType() != Settings.PilotTool)
                return;

            // Handle pilot tool right clicks

            e.setCancelled(true);
            Player p = e.getPlayer();
            PlayerCraft craft = CraftManager.getInstance().getCraftByPlayer(p);
            if (craft == null)
                return;
            if (!MathUtils.locationNearHitBox((craft).getHitBox(),p.getLocation(),0.5)) {
              if (!MathUtils.locationNearHitBox((craft).getHitBox(),p.getLocation(),1.5)) {
                  //Movecraft.getInstance().getLogger().warning("Skipping Entity: "+p+", Not Aboard");
                  return;
                }
            }
            CraftType type = craft.getType();
            int currentGear = craft.getCurrentGear();
            if (p.isSneaking() && !craft.getPilotLocked()) {
                // Handle shift right clicks (when not in direct control mode)
                int gearShifts = type.getIntProperty(CraftType.GEAR_SHIFTS);
                if (gearShifts <= 1) {
                    p.sendMessage(I18nSupport.getInternationalisedString("Gearshift - Disabled for craft type"));
                    return;
                }
                currentGear++;
                if (currentGear > gearShifts)
                    currentGear = 1;
                p.sendMessage(I18nSupport.getInternationalisedString("Gearshift - Gear changed")
                        + " " + currentGear + " / " + gearShifts);
                craft.setCurrentGear(currentGear);
                return;
            }

            int tickCooldown = (int) craft.getType().getPerWorldProperty(
                    CraftType.PER_WORLD_TICK_COOLDOWN, craft.getWorld());
            if (type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_DIRECT_MOVEMENT)
                    && type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN))
                tickCooldown *= currentGear; // Account for gear shifts
            Long lastTime = timeMap.get(p);
            if (lastTime != null) {
                long ticksElapsed = (System.currentTimeMillis() - lastTime) / 50;

                // if the craft should go slower underwater, make time pass more slowly there
                if (craft.getType().getBoolProperty(CraftType.HALF_SPEED_UNDERWATER)
                        && craft.getHitBox().getMinY() < craft.getWorld().getSeaLevel())
                    ticksElapsed /= 2;

                if (ticksElapsed < tickCooldown)
                    return; // Not enough time has passed, so don't do anything
            }

            if (!p.hasPermission("movecraft." + craft.getType().getStringProperty(CraftType.NAME) + ".move")) {
                p.sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
                return; // Player doesn't have permission to move this craft, so don't do anything
            }

            if (!MathUtils.locationNearHitBox(craft.getHitBox(), p.getLocation(), 2))
                return; // Player is not near the craft, so don't do anything

            int gear = craft.getCurrentGear();
            if (craft.getPilotLocked()) {
                // Direct control mode allows vertical movements when right-clicking
                dy = 1*gear; // Default to up
                if (p.isSneaking())
                    dy = -1*gear; // Down if sneaking
                PlayerCraftMovementEvent event = new PlayerCraftMovementEvent(craft,0,dy,0);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                dx = event.getDx();
                dy = event.getDy();
                dz = event.getDx();
                craft.translate(craft.getWorld(), 0, dy, 0);
                timeMap.put(p, System.currentTimeMillis());
                craft.setLastCruiseUpdate(System.currentTimeMillis());
                return;
            }

            double rotation = p.getLocation().getYaw() * Math.PI / 180.0;
            float nx = -(float) Math.sin(rotation);
            float nz = (float) Math.cos(rotation);
            dx = (Math.abs(nx) >= 0.5 ? 1 : 0) * (int) Math.signum(nx);
            dz = (Math.abs(nz) > 0.5 ? 1 : 0) * (int) Math.signum(nz);

            float pitch = p.getLocation().getPitch();
            dy = -(Math.abs(pitch) >= 25 ? 1 : 0) * (int) Math.signum(pitch);
            if (Math.abs(pitch) >= 75) {
                dx = 0;
                dz = 0;
            }


            PlayerCraftMovementEvent event = new PlayerCraftMovementEvent(craft,dx,dy,dz);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            craft.translate(craft.getWorld(), dx, dy, dz);
            timeMap.put(p, System.currentTimeMillis());
            craft.setLastCruiseUpdate(System.currentTimeMillis());
        }
    }
    public static int[] getCardinalDirection(Player player) {
            double rotation = (player.getEyeLocation().getYaw() - 180) % 360;
            if (rotation < 0) {
                rotation += 360.0;
            }
            int[] dxyz = new int[3];
            dxyz[0] = 0;
            dxyz[1] = 0;
            dxyz[2] = 0;
            if (0 <= rotation && rotation < 22.5) {
                //return "N";
                dxyz[2] = -1;
            } else if (22.5 <= rotation && rotation < 67.5) {
                //return "NE";
                dxyz[0] = 1;
                dxyz[2] = -1;
            } else if (67.5 <= rotation && rotation < 112.5) {
                //return "E";
                dxyz[0] = 1;
            } else if (112.5 <= rotation && rotation < 157.5) {
                //return "SE";
                dxyz[0] = 1;
                dxyz[2] = 1;
            } else if (157.5 <= rotation && rotation < 202.5) {
                //return "S";
                dxyz[2] = 1;
            } else if (202.5 <= rotation && rotation < 247.5) {
                //return "SW";
                dxyz[0] = -1;
                dxyz[2] = 1;
            } else if (247.5 <= rotation && rotation < 292.5) {
                //return "W";
                dxyz[0] = -1;
            } else if (292.5 <= rotation && rotation < 337.5) {
                //return "NW";
                dxyz[0] = -1;
                dxyz[2] = -1;
            } else if (337.5 <= rotation && rotation < 360.0) {
                //return "N";
                dxyz[2] = -1;
            } else {
                //return null;
                dxyz[0] = 0;
                dxyz[1] = 0;
                dxyz[2] = 0;
            }
            return dxyz;

        }
}
