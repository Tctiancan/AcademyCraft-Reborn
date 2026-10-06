package org.academy.internal.client.render.vfx;

import org.academy.api.client.render.vfx.VfxRenderContext;
import org.academy.api.client.render.vfx.VfxRenderer;

import java.util.List;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过 GpuDevice/命令编码器/RenderPass 实例化球体与盒子光线实体; 1.21.1 无对应 API,
 * 且该渲染位于世界级 VFX 链路上。按首版简化策略清空 GPU 实现, 仅保留对外 API 契约
 * (构造器 BeamRenderer(boolean), 及 VfxRenderer 生命周期方法 init/render/close)。
 */
public final class BeamRenderer implements VfxRenderer<BeamData> {
    private final boolean glow;
    private final float ballFactor;
    private final float boxFactorXZ;

    public BeamRenderer(boolean glow) {
        this.glow = glow;
        if (glow) {
            ballFactor = 1.0f;
            boxFactorXZ = 1.0f;
        } else {
            ballFactor = 0.85f;
            boxFactorXZ = 0.75f;
        }
    }

    @Override
    public void init() {
        // C-stun: 1.21.1 不创建 GPU 几何与实例缓冲。
    }

    @Override
    public void close() {
        // C-stun: 1.21.1 无 GPU 资源需要释放。
    }

    @Override
    public void render(VfxRenderContext ctx, List<? extends BeamData> data) {
        // C-stun: 1.21.1 不绘制光线实体。
    }
}
