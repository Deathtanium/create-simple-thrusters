package com.deathtanium.simplethrusters.content.thruster;

import java.util.ArrayDeque;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Finds connected thrusters of the same block type; lowest BlockPos order is master.
 */
public final class ThrusterCluster {
    private ThrusterCluster() {}

    public static ThrusterClusterResult compute(Level level, BlockPos start, Block blockType) {
        HashSet<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        HashSet<BlockPos> members = new HashSet<>();

        queue.add(start.immutable());
        visited.add(start.immutable());

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            BlockState state = level.getBlockState(pos);
            if (!state.is(blockType)) continue;

            members.add(pos.immutable());

            for (Direction dir : Direction.values()) {
                BlockPos nb = pos.relative(dir);
                if (visited.add(nb)) {
                    queue.add(nb);
                }
            }
        }

        BlockPos master = members.stream().min(BlockPos::compareTo).orElse(start);
        return new ThrusterClusterResult(master, members);
    }

    public record ThrusterClusterResult(BlockPos master, java.util.Set<BlockPos> members) {
        public boolean isMaster(BlockPos pos) {
            return master.equals(pos);
        }

        public <T extends BlockEntity> T getMasterEntity(Level level, java.util.function.Function<BlockPos, T> getter) {
            return getter.apply(master);
        }
    }
}
