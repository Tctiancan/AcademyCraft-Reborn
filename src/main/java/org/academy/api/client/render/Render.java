package org.academy.api.client.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.academy.api.client.resources.R;
import org.academy.blaze3d.buffers.GpuBuffer;
import org.academy.blaze3d.textures.GpuTextureView;
import org.academy.internal.client.render.AcademyShaders;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.OptionalDouble;

/**
 * 移植自 Minecraft 26.2 模组渲染后端, 适配 1.21.1 即时渲染管线.
 *
 * 迁移策略 (C-stun 世界级 VFX / ImGui 双缓冲后处理):
 * - [RenderPipelines] 全部常量名保留, 统一映射到 1.21.1 [RenderType] 与 [VertexFormat.Mode];
 *   世界级 VFX 管线 (LEVEL_* / CUTOUT_* / PLATINUM_COSMIC_WING / CLOUDROOM 等) 暂以桩 [RenderType] 呈现.
 * - [GaussianSamples]/[BlurUniforms] 为纯 std140 数据, 原样保留.
 * - [Buffers] 提供 1.21.1 [ByteBufferBuilder] 及全屏四边形几何.
 * - 26.2 专属的 GPU 缓冲 (GpuBuffer/CrossFrameResourcePool/UiLightmap) 与设备命令编码已移除.
 */
public final class Render {
    private static final Logger LOGGER = org.academy.AcademyCraft.getLogger();

    private Render() {
    }

    public static void init() {
        Buffers.init();
    }

    public static void resize() {
        Buffers.recreate();
    }

    /**
     * 26.2 `Render.runBlitPass` 的 C-stun 适配: 以全屏三角形把一张纹理拷贝到目标视口.
     * 1.21.1 的即时渲染在挂接点 (GuiRender/后处理) 已经完成合成, 这里不产生 GPU 提交,
     * 仅为桌面编辑器已有调用保持成立. 参数全部保留以守住调用形态.
     */
    public static void runBlitPass(
            GpuTextureView viewport,
            RenderPipeline pipeline,
            GpuBuffer vertexBuffer,
            List<TextureBinding> textures,
            List<UniformBinding> uniforms,
            boolean clear) {
        // C-stun: 1.21.1 即时渲染管线中该拷贝为目标合成所隐含, 不再显式执行.
    }

    public static void close() {
        Buffers.close();
    }

    /**
     * 1.21.1 全屏四边形顶点数据 (NDC -1~1), 供即时重写后的后处理/合成使用.
     */
    public static final Buffers.FullscreenQuadGeometry FS_QUAD_NDC =
            Buffers.FullscreenQuadGeometry.ndc();

    /**
     * 连续高斯核. 权重在 σ 单位下预计算, 只由相对位置决定, 与 radius 无关;
     * 仅有像素偏移 offset = rel * sigma 随 radius 连续缩放. 因此 radius 取任意
     * 小数都能平滑过渡, radius -> 0 时核收敛为恒等 (不模糊).
     */
    public record GaussianSamples(int sampleCount, Vector4f[] samples) {
        public static final int MAX_GAUSSIAN_SAMPLES = 12;
        /**
         * sigma = radius * SIGMA_SCALE.
         */
        private static final float SIGMA_SCALE = 0.5f;
        /**
         * 覆盖范围, 单位 sigma, ~3σ 捕获高斯绝大部分.
         */
        private static final float MAX_SIGMA = 3.0f;
        /**
         * tap 在 σ 单位下的相对偏移 (首个为中心, sampleCount) 与归一化权重.
         */
        private static final float[] BASE_OFFSET;
        private static final float[] BASE_WEIGHT;

        static {
            BASE_OFFSET = new float[MAX_GAUSSIAN_SAMPLES];
            BASE_WEIGHT = new float[MAX_GAUSSIAN_SAMPLES];
            var total = 0.0f;
            var step = MAX_SIGMA / (MAX_GAUSSIAN_SAMPLES - 1);
            for (var i = 0; i < MAX_GAUSSIAN_SAMPLES; i++) {
                BASE_OFFSET[i] = i * step;
                BASE_WEIGHT[i] = (float) Math.exp(-0.5 * BASE_OFFSET[i] * BASE_OFFSET[i]);
                total += (i == 0 ? 1.0f : 2.0f) * BASE_WEIGHT[i];
            }
            for (var i = 0; i < MAX_GAUSSIAN_SAMPLES; i++) {
                BASE_WEIGHT[i] /= total;
            }
        }

        public static GaussianSamples getGaussianSamples(float radius) {
            var sigma = radius * SIGMA_SCALE;
            var samples = new Vector4f[MAX_GAUSSIAN_SAMPLES];
            for (var i = 0; i < MAX_GAUSSIAN_SAMPLES; i++) {
                var offset = BASE_OFFSET[i] * sigma;
                samples[i] = new Vector4f(offset, offset, BASE_WEIGHT[i], 0.0f);
            }
            return new GaussianSamples(MAX_GAUSSIAN_SAMPLES, samples);
        }
    }

    /**
     * 高斯模糊 uniform 数据块 (std140). 1.21.1 下由 [writeBlurUniforms] 序列化为 ByteBuffer,
     * 供自研 blur shader 在缓存中读取.
     */
    public record BlurUniforms(Vector2f outSize, Vector2f blurDir, int sampleCount, Vector4f[] samples) {
        public static final int UBO_SIZE;
        static {
            var calculator = new Std140SizeCalculator().putVec2().putVec2().putInt();
            for (var i = 0; i < GaussianSamples.MAX_GAUSSIAN_SAMPLES; i++) {
                calculator.putVec4();
            }
            UBO_SIZE = calculator.get();
        }

        public static byte[] serialize(Vector2f outSize, float dirX, float dirY, float radius) {
            var samples = GaussianSamples.getGaussianSamples(radius);
            var builder = org.academy.api.client.render.Std140Builder.allocate(UBO_SIZE);
            builder.putVec2(outSize).putVec2(new Vector2f(dirX, dirY)).putInt(samples.sampleCount());
            for (var sample : samples.samples()) {
                builder.putVec4(sample);
            }
            return builder.toArray();
        }
    }

    public static final class Buffers {
        public static final int PROJECTION_UBO_SIZE = 64;
        private static final ByteBufferBuilder BYTE_BUFFER_BUILDER = new ByteBufferBuilder(786432);
        @Nullable
        private static Buffers instance;

        /**
         * 26.2 `fsQuadVBNDC` (全屏四边形顶点缓冲) 的 C-stun 占位: 1.21.1 无 GPU 顶点缓冲,
         * 以 CPU 侧 {@code GpuBuffer} 承载 NDC 四边形几何, 供桌面编辑器既有调用保持成立.
         */
        public final GpuBuffer fsQuadVBNDC =
                GpuBuffer.create(
                        () -> "fsQuadVBNDC", GpuBuffer.USAGE_VERTEX,
                        FS_QUAD_NDC.vertices().length * 4L);

        private Buffers() {
        }

        public static void init() {
            if (instance == null) {
                instance = new Buffers();
            }
        }

        public static void close() {
            instance = null;
        }

        public static void recreate() {
            init();
        }

        public static Buffers getInstance() {
            if (instance == null) {
                throw new IllegalStateException("Render.Buffers has not been initialized.");
            }
            return instance;
        }

        public static ByteBufferBuilder getByteBufferBuilder() {
            return BYTE_BUFFER_BUILDER;
        }

        /**
         * 1.21.1 全屏四边形, 以预构建的顶点数组提供, 供即时模式直接写入 BufferBuilder.
         */
        public record FullscreenQuadGeometry(VertexFormat format, VertexFormat.Mode mode, float[] vertices) {
            public static FullscreenQuadGeometry ndc() {
                // POSITION quad, NDC -1~1
                return new FullscreenQuadGeometry(
                        DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS,
                        new float[]{
                                -1.0F, -1.0F, 0.0F,
                                1.0F, -1.0F, 0.0F,
                                1.0F, 1.0F, 0.0F,
                                -1.0F, 1.0F, 0.0F
                        }
                );
            }

            public static FullscreenQuadGeometry uv(float width, float height) {
                // POSITION_TEX quad, 0~scaled
                return new FullscreenQuadGeometry(
                        DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS,
                        new float[]{
                                0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                                width, 0.0F, 0.0F, 1.0F, 0.0F,
                                width, height, 0.0F, 1.0F, 1.0F,
                                0.0F, height, 0.0F, 0.0F, 1.0F
                        }
                );
            }
        }

        public Matrix4f projection() {
            return new Matrix4f();
        }
    }

    /**
     * 移植自 26.2 `Render.RenderPipelines`. 常量名全量保留; 各管线映射为 1.21.1 [RenderType].
     * 世界级 VFX 与后处理管线暂映射到基础 GUI/透明类型作为首版桩.
     */
    public static final class RenderPipelines {
        private static RenderPipeline textured(VertexFormat.Mode mode, String pipelineName) {
            return new RenderPipeline(
                    pipelineName, List.of(DefaultVertexFormat.POSITION_TEX_COLOR), mode,
                    RenderType.gui()
            );
        }

        private static RenderPipeline textured(RenderPipeline p) {
            return new RenderPipeline(
                    p.getName(), List.of(DefaultVertexFormat.POSITION_TEX_COLOR), p.getPrimitiveTopology(),
                    RenderType.gui()
            );
        }

        private static RenderPipeline color() {
            return new RenderPipeline(
                    "pos_color", List.of(DefaultVertexFormat.POSITION_COLOR), VertexFormat.Mode.QUADS,
                    RenderType.gui()
            );
        }

        private static RenderPipeline bit(String name) {
            return new RenderPipeline(
                    name, List.of(DefaultVertexFormat.POSITION_TEX), VertexFormat.Mode.QUADS,
                    RenderType.gui()
            );
        }

        /**
         * 文本管线: 1.21.1 底层 (BufferUploader.drawWithShader + 内置 positionTexColorShader)
         * 不支持 [GlyphRunDrawCommand] 在 26.2 依赖的两绑定 GPU 实例化布局
         * (slot0 单位四边形 + slot1 实例数据), 故塌陷为单绑定 POSITION_TEX_COLOR,
         * 由字形命令直接生成完整四边形(位置+UV+颜色), 与图片命令同一即时上载路径.
         */
        private static RenderPipeline textPipeline(String name) {
            return new RenderPipeline(
                    name, List.of(DefaultVertexFormat.POSITION_TEX_COLOR), VertexFormat.Mode.QUADS,
                    RenderType.gui()
            );
        }

        // ---------------- GUI 管线 ----------------
        public static final RenderPipeline IMAGE = textured(VertexFormat.Mode.QUADS, "image");
        public static final RenderPipeline IMAGE_OPAQUE = textured(VertexFormat.Mode.QUADS, "image_opaque");
        public static final RenderPipeline IMAGE_PREMULTIPLIED_ALPHA = textured(VertexFormat.Mode.QUADS, "image_premultiplied_alpha");
        public static final RenderPipeline IMAGE_CIRCLE = textured(VertexFormat.Mode.QUADS, "image_circle");
        public static final RenderPipeline IMAGE_MONOCHROME = textured(VertexFormat.Mode.QUADS, "image_monochrome");
        public static final RenderPipeline IMAGE_STENCIL_PREMULTIPLIED_ALPHA = textured(VertexFormat.Mode.QUADS, "image_stencil_premultiplied_alpha");
        public static final RenderPipeline POS_COLOR = color();
        public static final RenderPipeline ROUNDED_RECT = textured(VertexFormat.Mode.QUADS, "rounded_rect");
        public static final RenderPipeline SKILL_PROGRESS = textured(VertexFormat.Mode.QUADS, "skill_progress");
        public static final RenderPipeline SDF_SHARP_MARGIN = bit("sdf_sharp_margin");
        public static final RenderPipeline SDF_CIRCLE_GLOW = bit("sdf_circle_glow");
        public static final RenderPipeline GLOW_CIRCLE = bit("glow_circle");
        public static final RenderPipeline BITMAP_TEXT = textPipeline("bitmap_text");
        public static final RenderPipeline MSDF_TEXT = textPipeline("msdf_text");
        public static final RenderPipeline IMGUI = textured(VertexFormat.Mode.TRIANGLES, "imgui");

        // ---------------- 屏面 / 后处理管线 (C-stun) ----------------
        public static final RenderPipeline BLIT_SCREEN_WITH_BLEND = textured(VertexFormat.Mode.QUADS, "blit_screen_with_blend");
        public static final RenderPipeline BLIT_SCREEN_PREMULTIPLIED_ALPHA = textured(VertexFormat.Mode.QUADS, "blit_screen_premultiplied_alpha");
        public static final RenderPipeline BLIT_SCREEN_WITHOUT_BLEND = textured(VertexFormat.Mode.QUADS, "blit_screen_without_blend");
        public static final RenderPipeline BLIT_SCREEN_WITHOUT_BLEND_INVERSE_CUTOUT = textured(VertexFormat.Mode.QUADS, "blit_screen_without_blend_inverse_cutout");
        public static final RenderPipeline GAUSSIAN_BLUR = textured(VertexFormat.Mode.QUADS, "gaussian_blur");
        public static final RenderPipeline CUTOUT_GAUSSIAN_BLUR = textured(VertexFormat.Mode.QUADS, "cutout_gaussian_blur");
        public static final RenderPipeline BACKDROP_SAMPLE = textured(VertexFormat.Mode.QUADS, "backdrop_sample");
        public static final RenderPipeline GLOW_BLEND = textured(VertexFormat.Mode.QUADS, "glow_blend");

        // ---------------- 世界级 VFX 管线 (C-stun 桩) ----------------
        public static final RenderPipeline LEVEL_POS_TEX_COLOR = textured(VertexFormat.Mode.QUADS, "level_pos_tex_color");
        public static final RenderPipeline LEVEL_POS_TEX_COLOR_NO_DEPTH_WRITE = textured(VertexFormat.Mode.QUADS, "level_pos_tex_color_no_depth_write");
        public static final RenderPipeline LEVEL_POS_COLOR_QUADS = color();
        public static final RenderPipeline LEVEL_POS_COLOR_QUADS_NO_DEPTH_WRITE =
                new RenderPipeline("level_pos_color_quads_no_depth_write", List.of(DefaultVertexFormat.POSITION_COLOR), VertexFormat.Mode.QUADS, RenderType.gui());
        public static final RenderPipeline LEVEL_POS_COLOR_QUADS_ADDITIVE =
                new RenderPipeline("level_pos_color_quads_additive", List.of(DefaultVertexFormat.POSITION_COLOR), VertexFormat.Mode.QUADS, RenderType.gui());
        public static final RenderPipeline LEVEL_POS_COLOR_TRANGLES =
                new RenderPipeline("level_pos_color_trangles", List.of(DefaultVertexFormat.POSITION_COLOR), VertexFormat.Mode.TRIANGLES, RenderType.gui());
        public static final RenderPipeline LEVEL_POS_COLOR_TRANGLES_ADDITIVE =
                new RenderPipeline("level_pos_color_additive", List.of(DefaultVertexFormat.POSITION_COLOR), VertexFormat.Mode.TRIANGLES, RenderType.gui());
        public static final RenderPipeline MINE_DETECT_LINES =
                new RenderPipeline("mine_detect_lines", List.of(DefaultVertexFormat.POSITION_COLOR), VertexFormat.Mode.LINES, RenderType.gui());
        public static final RenderPipeline CLOUDROOM =
                new RenderPipeline("cloudroom", List.of(DefaultVertexFormat.NEW_ENTITY),
                        VertexFormat.Mode.QUADS,
                        AcademyShaders.cloudroomType(R.textures.item.cat_engine));
        public static final RenderPipeline TELEPORT_CURSOR = textured(VertexFormat.Mode.QUADS, "teleport_cursor");
        public static final RenderPipeline IMAG_PHASE_PARTICLE_POST = textured(VertexFormat.Mode.QUADS, "imag_phase_particle_post");
        public static final RenderPipeline PLATINUM_COSMIC_WING = textured(VertexFormat.Mode.QUADS, "platinum_cosmic_wing");
        public static final RenderPipeline PLATINUM_COSMIC_WING_NO_DEPTH_WRITE = textured(VertexFormat.Mode.QUADS, "platinum_cosmic_wing_no_depth_write");
        public static final RenderPipeline DISTORTION_RING =
                new RenderPipeline("distortion_ring", List.of(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), VertexFormat.Mode.QUADS, RenderType.gui());
        public static final RenderPipeline SPATIAL_DISTORTION = bit("spatial_distortion");

        private RenderPipelines() {
        }
    }

    /**
     * 26.2 `Render.RenderTypes` (世界实体 RenderType) 的 C-stun 桩; 全部在 [RenderPipelines]
     * 基础上映射到基础透明类型, 供实体层引用以维持编译.
     */
    public abstract static class RenderTypes {
        // sqrtf: 该常量对应 26.2 的线框管线 (LINES)。旧桩用 RenderType.gui()(QUADS) 会把
        // 每段 2 顶点的线框线段当四边形解释, 导致描边显示为三角面/填充面; 改用真正的
        // 线渲染类型 (LINES + POSITION_COLOR_NORMAL)。26.2 的 mine_detect_lines 管线使用
        // DepthStencilState(CompareOp.ALWAYS_PASS, false), 即禁用深度测试/深度写入, 使矿物
        // 轮廓能穿过地形显示; 此处对应地以 NO_DEPTH_TEST + COLOR_WRITE 复现该行为 (不会因
        // 遮挡方块而裁掉)。注意: 渲染器会逐个 setColor/setNormal, 因此必须保持 vanilla
        // LINES 的 POSITION_COLOR_NORMAL 顶点格式与逐顶点 (非 setColor 整体) 写法兼容。
        public static final RenderType MINE_DETECT_LINES = RenderType.create(
                "mine_detect_lines",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINES,
                1536,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderType.RENDERTYPE_LINES_SHADER)
                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                        .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(RenderType.ITEM_ENTITY_TARGET)
                        .setWriteMaskState(RenderType.COLOR_WRITE)
                        .setDepthTestState(RenderType.NO_DEPTH_TEST)
                        .setCullState(RenderType.NO_CULL)
                        .createCompositeState(false)
        );
        // sqrtf: 复刻 26.2 `RenderTypes.TELEPORT_CURSOR` 的管线状态 (着色器/混合/深度/排序)。
        // 26.2 原管线用 cull(true), 但其 GeckoLib 模型为外翻绕序; 本移植模型在
        // TeleportCursorRenderer 中 BODY 以 inwardFacing=true (反向绕序) 建模, 是供双面绘制
        // 的, 若开启背面剔除会让摄像机面向的面被裁掉、只剩背面, 顶部盘面也可能整片消失。
        // 因此这里必须用 NO_CULL 才能完整显示长方体与顶部盘面。
        // 此处的 RenderType.gui() 桩不绑定 teleport_cursor 纹理: 复刻 26.2 用 RenderSetup 显式
        // bind 的 "Sampler0"=teleport_cursor。顶点格式 POSITION_TEX_COLOR 逐顶点 (u,v)+setColor,
        // 着色器由逐顶点色乘纹理色; 纹理本身为灰度+alpha(目标半透明设计)。
        // 深度改为 ALWAYS 通过且不写深度, 使瞄准光标作为始终置顶的叠加层绘制, 避免被地形裁剪/深度冲突。
        public static final RenderType TELEPORT_CURSOR = RenderType.create(
                "academy_teleport_cursor",
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                786432,
                false,
                true,
                RenderType.CompositeState.builder()
                        // 26.2 光标用的是 position_tex_color 程序 (逐顶点色); 不能用
                        // RENDERTYPE_GUI_SHADER (rendertype_gui) —— 其片元为
                        // texture*ColorModulator, 忽略逐顶点 setColor, 会继承冲刷时残留的全局
                        // 颜色 (如绿色), 使光标整体变绿。position_tex_color 用 texture*vertexColor,
                        // 光标颜色严格由顶点色 (白) 决定, 与背景无关。
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader))
                        .setTextureState(new RenderStateShard.TextureStateShard(
                                R.textures.teleport_cursor, false, false))
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderType.CULL)
                        .setDepthTestState(RenderType.NO_DEPTH_TEST)
                        .setWriteMaskState(RenderType.COLOR_WRITE)
                        .createCompositeState(false)
        );
        public static final RenderType STORM_WING = RenderType.gui();
        public static final RenderType PLASMA_CLOUD = RenderType.gui();
        public static final RenderType BLACK_WING = RenderType.gui();
        public static final RenderType WHITE_WING = RenderType.gui();
        public static final RenderType PLATINUM_WING = RenderType.gui();
        public static final RenderType BLACK_WING_FIRST_PERSON = RenderType.gui();
        public static final RenderType WHITE_WING_FIRST_PERSON = RenderType.gui();
        public static final RenderType PLATINUM_WING_FIRST_PERSON = RenderType.gui();
        public static final RenderType PLATINUM_WING_BYPASS = RenderType.gui();
        public static final RenderType IRON_SAND_FIRST_PERSON = RenderType.gui();
        public static final RenderType IMAG_PHASE_PARTICLE_POST = RenderType.gui();
        public static final RenderType CLOUDROOM = AcademyShaders.cloudroomType(R.textures.item.cat_engine);
        public static final RenderType CLEANING_ROBOT = RenderType.gui();
        public static final RenderType DISTORTION_RING = AcademyShaders.distortionRingType();
        public static final RenderType CAT_ENGINE = RenderType.entityTranslucent(R.textures.item.cat_engine);
        public static final RenderType POS_COLOR_TRANGLES = RenderType.gui();
        public static final RenderType POS_COLOR_TRANGLES_GLOW_ADDITIVE = RenderType.gui();
        public static final RenderType POS_COLOR_QUADS = RenderType.gui();
        public static final RenderType POS_COLOR_QUADS_NO_DEPTH_WRITE = RenderType.gui();

        private RenderTypes() {
        }
    }
}
