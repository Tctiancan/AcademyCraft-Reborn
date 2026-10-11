package org.academy.api.client.render.post;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;

/**
 * 1.21.1 实装 (原 26.2 后处理阶段缓冲)。
 *
 * <p>26.2 通过 {@code StagedVertexBuffer} + {@code RenderType.drawFromBuffer} 延迟提交阶段顶点;
 * 1.21.1 无对应 API。本实现以即时态缓冲等价物保持同一调用形态 ({@link #getBuffer(RenderType)} 返回
 * 顶点消费者), {@link #draw()} 把阶段内累积的三角形列表以 {@code position_color} shader 冲刷进
 * 当前绑定的帧缓冲 (由 {@link PostEffect#pre()} 切换为离屏目标 {@code MAIN_SCENE}), 再清空缓冲
 * 供下一帧复用。
 *
 * <p>每个 {@code Phase} 拥有私有 {@link ByteBufferBuilder}/{@link BufferBuilder}, 绝不借用全局
 * {@link com.mojang.blaze3d.vertex.Tesselator} 的共享缓冲区。世界渲染在帧内会在共享缓冲上反复
 * {@code begin/build/clear}, 若阶段跨帧持有该共享缓冲, 其游标被推进或区间被废弃, 会令跨帧累加的
 * 顶点失效并在 {@code build()} 上抛 "Not building!"。私有分配器保证阶段几何在整帧到
 * {@link #draw()} 之前始终有效。格式保持 {@code POSITION_COLOR}/{@code TRIANGLES}。
 */
public class Phase {
    private static final int INITIAL_CAPACITY = 786432;
    private final ByteBufferBuilder allocator;
    private BufferBuilder builder;

    public Phase(String name) {
        this.allocator = new ByteBufferBuilder(INITIAL_CAPACITY);
        this.builder = newBuffer();
    }

    private BufferBuilder newBuffer() {
        return new BufferBuilder(allocator, VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    }

    public VertexConsumer getBuffer(RenderType renderType) {
        return builder;
    }

    public void draw() {
        MeshData meshdata = this.builder.build();
        if (meshdata != null) {
            // 相位几何在 renderLevel RETURN 时冲刷, 此时世界渲染遗留的背面剔除/深度测试处于开启,
            // 单面粒子这样随意旋转的三角形绕向往往为背面而被剔除; 相位又常被已有几何遮挡。相位几何
            // 的语义是叠加在其上的光效/辉光层 (26.2 经 bloom 加法合成), 故此处显式关闭剔除与深度,
            // 用加法混合让辉光始终叠加在最上层可见。画完恢复, 以免影响随后的第一人称手部渲染。
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE
            );
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            // 必须用 drawWithShader 而非 draw: drawWithShader 会把 RenderSystem 当前 modelview/
            // projection/shader 作为 uniform 上传给 position_color 的 ModelViewMat/ProjMat; 而
            // draw() 只画不更新 uniform, 会让几何套用残留矩阵而被投到屏外 (此前不可见的总根因).
            BufferUploader.drawWithShader(meshdata);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        this.allocator.clear();
        this.builder = newBuffer();
    }

    public void close() {
        this.allocator.close();
    }
}
