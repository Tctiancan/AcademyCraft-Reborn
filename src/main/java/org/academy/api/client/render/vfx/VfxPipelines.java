package org.academy.api.client.render.vfx;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.academy.api.client.render.RenderPipeline;

import java.util.List;

import static org.academy.AcademyCraft.academy;

/**
 * 世界级 VFX 管线的 1.21.1 简化版 (C-stun)。
 *
 * 26.2 版本通过 `RenderPipeline.builder()` + GPU 延迟渲染 (GpuFormat/PrimitiveTopology/
 * BindGroupLayout/ColorTargetState/Shader) 描述每条 VFX 管线; 1.21.1 无对应 API。
 * 为优先打通 GUI 主链路, 此处按批准的首版简化策略, 将全部 VFX 管线收口到本地抽象
 * [RenderPipeline] (vertexFormatBindings + VertexFormat.Mode + RenderType), 顶点格式
 * 统一落到内置 DefaultVertexFormat, shader/纹理/后处理相关绑定一律占位, 不参与真实渲染。
 */
public final class VfxPipelines {
    /** 简化用统一渲染类型 (即时渲染 C-stun 占位, VFX 不真正执行). */
    private static final RenderType STUB_RENDER_TYPE = RenderType.debugFilledBox();

    /** 简化用统一顶点格式 (替代 26.2 自定义 position/color 布局). */
    private static final VertexFormat STUB_VERTEX_FORMAT = DefaultVertexFormat.POSITION_COLOR;

    /** spatial_cut_line 线条的 FLOAT 版颜色元素 (内置 COLOR 是 UBYTE, 与 10-float 交错布局不匹配). */
    private static final VertexFormatElement SPATIAL_LINE_FLOAT_COLOR =
            VertexFormatElement.register(16, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.COLOR, 4);
    /** 沿裂缝前进的流动坐标. */
    private static final VertexFormatElement SPATIAL_LINE_FLOW =
            VertexFormatElement.register(17, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);
    /** 材质/辉光双通道的 (速度, 横向位置) 参数. */
    private static final VertexFormatElement SPATIAL_LINE_UV =
            VertexFormatElement.register(18, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 2);

    /** Position, color, flow, and portal UV for the main spatial fissure.
     *  40 字节/顶点 (3+4+1+2 个 float), 与 CPU 侧 SpacialExcisionVfxClient.putLineVertex 布局一致,
     *  也对应 spatial_cut_line shader 的 in 顶点属性。 */
    public static final VertexFormat SPATIAL_CUT_LINE_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", SPATIAL_LINE_FLOAT_COLOR)
            .add("Flow", SPATIAL_LINE_FLOW)
            .add("MaterialUv", SPATIAL_LINE_UV)
            .build();

    /** Draws the narrow End Portal material after the scene displacement pass. */
    private static final VertexFormat SPATIAL_CUT_MASK_FORMAT = STUB_VERTEX_FORMAT;

    /** Draws the narrow End Portal material after the scene displacement pass. */
    public static final RenderPipeline SPATIAL_CUT_MATERIAL =
            pipeline("pipeline/spatial_cut_material", STUB_VERTEX_FORMAT, VertexFormat.Mode.TRIANGLES);

    /** Draws the End Portal material into the bloom input. */
    public static final RenderPipeline SPATIAL_CUT_GLOW =
            pipeline("pipeline/spatial_cut_glow", STUB_VERTEX_FORMAT, VertexFormat.Mode.TRIANGLES);

    /** Writes a world-space cut mask while reading the main scene depth without modifying it. */
    public static final RenderPipeline SPATIAL_CUT_MASK =
            pipeline("pipeline/spatial_cut_mask", SPATIAL_CUT_MASK_FORMAT, VertexFormat.Mode.TRIANGLES);

    /** Reduces visible overlapping cut planes to the farthest reversed-Z depth. */
    public static final RenderPipeline SPATIAL_CUT_DEPTH =
            pipeline("pipeline/spatial_cut_depth", SPATIAL_CUT_MASK_FORMAT, VertexFormat.Mode.TRIANGLES);

    /** Applies the scene slide from the world-space mask to the copied main scene. */
    public static final RenderPipeline SPATIAL_CUT_POST =
            pipeline("pipeline/spatial_cut_post", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS);

    public static final RenderPipeline PARTICLE_ADDITIVE =
            pipeline("pipeline/vfx_particle", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS);

    public static final RenderPipeline BEAM_BALL =
            pipeline("pipeline/vfx_beam_ball", STUB_VERTEX_FORMAT, VertexFormat.Mode.TRIANGLES);

    public static final RenderPipeline BEAM_BOX =
            pipeline("pipeline/vfx_beam_box", STUB_VERTEX_FORMAT, VertexFormat.Mode.QUADS);

    public static final RenderPipeline BEAM_CORE_BALL =
            pipeline("pipeline/vfx_beam_core_ball", STUB_VERTEX_FORMAT, VertexFormat.Mode.TRIANGLES);

    public static final RenderPipeline BEAM_CORE_BOX =
            pipeline("pipeline/vfx_beam_core_box", STUB_VERTEX_FORMAT, VertexFormat.Mode.QUADS);

    public static final RenderPipeline TEX_BILLBOARD_TRANSLUCENT =
            pipeline("pipeline/vfx_tex_billboard_translucent", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS);

    public static final RenderPipeline TEX_PLANE_TRANSLUCENT =
            pipeline("pipeline/vfx_tex_plane", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS);

    public static final RenderPipeline TEX_RING_TRANSLUCENT =
            pipeline("pipeline/vfx_tex_ring_translucent", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS);

    public static final RenderPipeline TEX_RING_ADDITIVE =
            pipeline("pipeline/vfx_tex_ring_additive", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS);

    public static final RenderPipeline BLOCK_MESH =
            pipeline("pipeline/vfx_block_mesh", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS);

    public static final RenderPipeline COLOR_MESH =
            pipeline("pipeline/vfx_color_mesh", STUB_VERTEX_FORMAT, VertexFormat.Mode.TRIANGLES);

    public static final RenderPipeline MODEL_MESH =
            pipeline("pipeline/vfx_model_mesh", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.TRIANGLES);

    public static final RenderPipeline WING_RING_COSMIC =
            pipeline("pipeline/vfx_wing_ring_cosmic", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS);

    public static final RenderPipeline LIGHTNING_TUBE =
            lightningTube(academy("pipeline/vfx_lightning"));

    public static final RenderPipeline SCREEN_FLASH =
            pipeline("pipeline/vfx_screen_flash", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS);

    public static final RenderPipeline LIGHTNING_TUBE_BLOOM =
            lightningTube(academy("pipeline/vfx_lightning_bloom"));

    public static final RenderPipeline SKY_STRIKE_QUAD_TRANSLUCENT =
            skyStrikeQuad("pipeline/vfx_sky_strike_quad_translucent");

    public static final RenderPipeline SKY_STRIKE_QUAD_ADDITIVE =
            skyStrikeQuad("pipeline/vfx_sky_strike_quad_additive");

    private VfxPipelines() {
    }

    private static RenderPipeline pipeline(String location, VertexFormat format, VertexFormat.Mode mode) {
        return new RenderPipeline(
                academy(location).toString(),
                List.of(format),
                mode,
                STUB_RENDER_TYPE
        );
    }

    private static RenderPipeline lightningTube(ResourceLocation location) {
        return new RenderPipeline(
                location.toString(),
                List.of(DefaultVertexFormat.POSITION_TEX),
                VertexFormat.Mode.TRIANGLES,
                STUB_RENDER_TYPE
        );
    }

    private static RenderPipeline skyStrikeQuad(String location) {
        return new RenderPipeline(
                academy(location).toString(),
                List.of(DefaultVertexFormat.POSITION_TEX_COLOR),
                VertexFormat.Mode.QUADS,
                STUB_RENDER_TYPE
        );
    }

    /**
     * 26.2 由 NeoForge 在管线注册阶段调用, 1.21.1 无对应事件, 保留方法契约但为空操作.
     */
    @Deprecated
    public static void onRegisterRenderPipelinesEvent() {
        for (var field : VfxPipelines.class.getDeclaredFields()) {
            if (field.getType() != RenderPipeline.class) continue;
            try {
                field.setAccessible(true);
                field.get(null);
            } catch (IllegalAccessException e) {
                // 1.21.1 C-stun: 管线不再注册到 GPU 队列, 仅保证字段可达.
            }
        }
    }
}
