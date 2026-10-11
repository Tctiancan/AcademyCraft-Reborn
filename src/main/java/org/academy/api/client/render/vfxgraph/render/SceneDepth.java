package org.academy.api.client.render.vfxgraph.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

/**
 * 场景深度快照（soft particles）：26.2 把深度附件拷到可采样离屏纹理，
 * 供 billboard 片元着色器软粒子（近表面软化）采样，避免渲染目标附件反馈。
 *
 * <p><b>1.21.1 语义调整：</b>即时 GL 后端无法自由创建 GpuTextureView，但主帧目标是
 * 可采样的浮点深度纹理（GL_DEPTH_COMPONENT FLOAT），且世界 VFX 仅在当前渲染通道写
 * 颜色附件、深度只读——直接引用它不构成 GL 渲染反馈，故无需 26.2 的离屏拷贝。于是
 * {@code copyFrom} 抓取并缓存主帧深度纹理 id，{@code view()} 以 Integer 形式暴露该 id。</p>
 *
 * <p><b>z 约定：</b>1.21.1 为主光源非反转深度（近=0、远=1，见 {@code _clearDepth(1.0)}）。
 * 片元着色器以此来判遮挡（见 cloud_vortex 片元的 soft-particle 项）。</p>
 */
public final class SceneDepth {
    private int depthTextureId = -1;
    private int width;
    private int height;

    /**
     * 抓取主渲染目标的当前场景深度。必须在 render pass 外、主帧绑定期间调用。
     */
    public void copyFrom(Object depth) {
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if (main == null || !main.useDepth) {
            depthTextureId = -1;
            width = height = 0;
            return;
        }
        depthTextureId = main.getDepthTextureId();
        width = main.width;
        height = main.height;
    }

    /**
     * 1.21.1 即时 GL 后端下返回主帧深度纹理 id（等价 26.2 的可采样视图），不可用为 null。
     */
    @Nullable
    public Object view() {
        return depthTextureId < 0 ? null : depthTextureId;
    }

    public void close() {
        depthTextureId = -1;
        width = height = 0;
    }
}
