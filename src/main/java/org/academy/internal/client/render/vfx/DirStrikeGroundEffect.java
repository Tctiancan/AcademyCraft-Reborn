package org.academy.internal.client.render.vfx;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.render.vfx.Vfx;
import org.academy.api.client.render.vfx.VfxFrameContext;
import org.academy.api.client.render.vfx.VfxSink;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 该效果通过 ModelBlockRenderer/BlockStateModelSet/ChunkSectionLayer 等新版渲染 API 采样
 * 方块网格并上传 GPU 绘制地面打击解体; 1.21.1 无对应 API, 且该渲染位于世界级 VFX 链路上。
 * 按首版简化策略保留对外 API 契约 (spawn/无参构造器及 Vfx 生命周期方法), 清空整体实现。
 */
public final class DirStrikeGroundEffect implements Vfx {
    public static void spawn(
            Vec3 center,
            BlockPos origin,
            int radius,
            boolean airborne,
            float lookX,
            float lookZ,
            long seed
    ) {
        // C-stun: 1.21.1 不再采样/解体方块网格。
    }

    @Override
    public void sample(VfxFrameContext ctx, VfxSink sink) {
        // C-stun: 1.21.1 不产生地面打击渲染数据。
    }

    @Override
    public boolean isAlive() {
        return true;
    }
}
