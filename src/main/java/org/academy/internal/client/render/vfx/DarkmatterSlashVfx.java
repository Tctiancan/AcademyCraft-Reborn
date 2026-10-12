package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.academy.api.client.render.vfx.*;
import org.academy.api.client.resources.R;
import org.academy.api.common.vfx.SkillVfxState;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.List;

/** Locally timed slash; no client entity or per-effect mesh allocation is needed. */
public final class DarkmatterSlashVfx implements Vfx {
    private final SkillVfxState.Slash state;
    private final @Nullable ClientLevel level = Minecraft.getInstance().level;
    private float elapsed;
    private boolean sampled;

    public DarkmatterSlashVfx(SkillVfxState.Slash state) {
        this.state = state;
    }

    public static void register() {
        VfxRegistry.register(Data.class, VfxPhase.WORLD_TRANSLUCENT, new Renderer());
    }

    @Override
    public void update(float dt, VfxFrameContext ctx) {
        // Start on the first frame so a short slash cannot expire while waiting to render.
        if (sampled && !Minecraft.getInstance().isPaused()) elapsed += Math.max(0, dt);
    }

    @Override
    public void sample(VfxFrameContext ctx, VfxSink sink) {
        sampled = true;
        if (!VfxVisibility.sphere(ctx.camera(), state.position(), 4f * state.scale())) return;
        sink.push(new Data(state, Math.clamp(Math.max(0.1f, elapsed / state.lifetimeTicks()), 0, 1)));
    }

    @Override
    public boolean isAlive() {
        return Minecraft.getInstance().level == level && elapsed < state.lifetimeTicks();
    }

    public record Data(SkillVfxState.Slash state, float progress) implements VfxRenderData {
        int frame() { return Math.clamp((int) (progress * 4), 0, 3); }
    }

    /**
     * 1.21.1 实装 (原 26.2 斩击刀光)。
     *
     * <p>26.2 通过 GpuDevice/RenderPass 实例化四边形绘制; 1.21.1 无对应 API, 改用即时态提交:
     * 以相机-相对视旋转矩阵 ({@link VfxRenderContext#viewRotationMatrix()}) 左乘每实例变换
     * ({@code translate→rotateY→rotateX→translate→scale}) 合成模型视图, 把单位四边形以
     * {@code positional_tex_color} shader 冲刷, 颜色 alpha 为逐实例生命周期强度, 按进度选中
     * 4 帧贴图中的一帧, 透明混合。
     */
    private static final class Renderer implements VfxRenderer<Data> {
        private static final boolean[] INNER = {false, true};
        private static final ResourceLocation[] FRAMES = {
                R.textures.darkmatter_cut_slash_effect_1, R.textures.darkmatter_cut_slash_effect_2,
                R.textures.darkmatter_cut_slash_effect_3, R.textures.darkmatter_cut_slash_effect_4
        };

        @Override
        public void init() {
        }

        @Override
        public void render(VfxRenderContext ctx, List<? extends Data> data) {
            if (data.isEmpty()) {
                return;
            }
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            for (var item : data) {
                for (boolean inner : INNER) {
                    drawSlash(ctx, item, inner);
                }
            }
            RenderSystem.enableDepthTest();
        }

        private static void drawSlash(VfxRenderContext ctx, Data item, boolean inner) {
            int frame = item.frame();
            float life = Mth.sin(item.progress * Mth.PI);
            float scale = item.state.scale() * (.92f + .18f * life) * (inner ? .76f : 1f);
            float width = 5f * scale * (.82f + .18f * Math.min(1.0f, item.progress * 1.7f));
            float height = 1.05f * scale;
            boolean mirrored = (item.state.direction() < 0) != inner;
            var p = item.state.position();
            var camera = ctx.cameraPos();
            var transform = new Matrix4f()
                    .translation((float) (p.x - camera.x), (float) (p.y - camera.y), (float) (p.z - camera.z))
                    .rotateY((270 - item.state.yRot()) * Mth.DEG_TO_RAD)
                    .rotateX(-item.state.xRot() * Mth.DEG_TO_RAD)
                    .translate(0, height * .5f, 0).scale(width, 1, mirrored ? -width : width);
            float alpha = life * .92f * (inner ? .52f : 1f);

            var modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.set(new Matrix4f(ctx.viewRotationMatrix()).mul(transform));
            RenderSystem.applyModelViewMatrix();

            var builder = Tesselator.getInstance().begin(
                    VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            emitCorner(builder, -0.5f, 0.0f, -0.5f, 0.0f, 0.0f, alpha);
            emitCorner(builder, 0.5f, 0.0f, -0.5f, 1.0f, 0.0f, alpha);
            emitCorner(builder, 0.5f, 0.0f, 0.5f, 1.0f, 1.0f, alpha);
            emitCorner(builder, -0.5f, 0.0f, 0.5f, 0.0f, 1.0f, alpha);
            MeshData meshData = builder.build();
            if (meshData != null) {
                RenderSystem.setShaderTexture(0, FRAMES[frame]);
                // 1.21.1: draw(MeshData) 走 VertexBuffer.draw()，只发 glDraw 事件、不向 shader
                // 上传 ModelViewMat/ProjMat uniform，导致四边形顶点被陈旧矩阵变换而不可见。
                // drawWithShader 会用 RenderSystem 当前模型视图(=viewRotation*transform)与投影填充
                // 矩阵 uniform 并 apply，与 Beam 等正常绘制路径一致。
                BufferUploader.drawWithShader(meshData);
            }

            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }

        private static void emitCorner(VertexConsumer consumer, float x, float y, float z,
                                       float u, float v, float alpha) {
            consumer.addVertex(x, y, z).setUv(u, v).setColor(1.0f, 1.0f, 1.0f, alpha);
        }

        @Override
        public void close() {
        }
    }
}
