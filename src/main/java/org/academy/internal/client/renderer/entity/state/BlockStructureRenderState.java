package org.academy.internal.client.renderer.entity.state;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.academy.api.common.structure.BlockStructureSnapshot;

import java.util.ArrayList;
import java.util.List;

/** Frame state with a snapshot-stable per-block model cache for the structure entity. */
public final class BlockStructureRenderState {
    public final List<BlockRenderData> blocks = new ArrayList<>();
    public float yawDegrees;
    public double pivotX;
    public double pivotZ;

    public void updateBlocks(BlockStructureSnapshot snapshot) {
        blocks.clear();
        for (var block : snapshot.blocks()) {
            blocks.add(new BlockRenderData(
                    block.relativePosition(),
                    block.state()
            ));
        }
    }

    public record BlockRenderData(
            BlockPos relativePosition,
            BlockState state
    ) {
    }
}
