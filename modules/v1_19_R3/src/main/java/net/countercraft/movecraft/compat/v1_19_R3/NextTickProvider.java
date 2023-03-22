package net.countercraft.movecraft.compat.v1_19_R3;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Queue;

public class NextTickProvider {

    @Nullable
    public ScheduledTick<?> getNextTick(@NotNull ServerLevel world, @NotNull BlockPos position){
        return null;
    }
}
