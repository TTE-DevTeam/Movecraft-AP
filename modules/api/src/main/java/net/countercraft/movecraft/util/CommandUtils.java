package net.countercraft.movecraft.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.GameRule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.entity.minecart.CommandMinecart;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.bukkit.event.inventory.InventoryType;
/**
 * Code taken with permission from MicleBrick
 * https://www.spigotmc.org/threads/teleport-player-smoothly.317416/
 */
public class CommandUtils {
    public static boolean hideOutput = true;
    public static void runCommand(World world, String command){
        CommandMinecart cart = (CommandMinecart)world.spawnEntity(new Location(world, 0, -120, 0), EntityType.MINECART_COMMAND);
        cart.setCustomName("Movecraft");
        cart.setGravity(false);
        Bukkit.dispatchCommand(cart, command);
        cart.remove();
    }
    public static void runSetBlock(World world, Location loc, String blockId){
        boolean val = false;
        System.out.println("SetBlock ID: "+blockId);
        CommandMinecart cart = (CommandMinecart)world.spawnEntity(new Location(world, 0, -128, 0), EntityType.MINECART_COMMAND);
        cart.setCustomName("Movecraft");
        Bukkit.dispatchCommand(cart, "execute as "+cart.getUniqueId().toString()+" run setBlock "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ()+" "+blockId);
        cart.remove();
    }
}
