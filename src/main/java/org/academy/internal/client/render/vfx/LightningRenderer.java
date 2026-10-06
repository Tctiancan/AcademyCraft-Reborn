package org.academy.internal.client.render.vfx;

import org.academy.api.client.render.vfx.VfxRenderContext;
import org.academy.api.client.render.vfx.VfxRenderer;

import java.util.List;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过 GpuDevice/命令编码器/TubeMesh 顶点缓冲渲染闪电网格; 1.21.1 无对应 API,
 * 且该渲染位于世界级 VFX 链路上。按首版简化策略清空 GPU 实现, 仅保留对外 API 契约
 * (构造器 LightningRenderer(boolean), 及 VfxRenderer 生命周期方法 init/render/close)。
 */
public final class LightningRenderer implements VfxRenderer<LightningMeshData> {
    private final boolean glow;

    public LightningRenderer(boolean glow) {
        this.glow = glow;
    }

    @Override
    public void init() {
        // C-stun: 1.21.1 不创建 GPU 缓冲。
    }

    @Override
    public void render(VfxRenderContext ctx, List<? extends LightningMeshData> data) {
        // C-stun: 1.21.1 不绘制闪电网格。
    }

    @Override
    public void close() {
        // C-stun: 1.21.1 无 GPU 资源需要释放。
    }
}
