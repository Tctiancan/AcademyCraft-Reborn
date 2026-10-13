package org.academy.internal.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import org.academy.AcademyCraft;
import org.academy.api.client.render.vfx.VfxPipelines;

import java.io.IOException;
import java.util.function.Function;

import static org.academy.AcademyCraft.academy;

/**
 * 云室 (Cloudroom) 标记特效的完整版着色器管线。
 * <p>
 * 与 26.2 一致使用程序化噪声烟雾着色器 (cloudroom.vsh/.fsh): view-space 法线 rim 光、
 * 模型剪影 alpha 裁剪、全亮。关闭深度测试且不写深度, 保证标记在墙后依旧可见。
 */
@EventBusSubscriber(modid = AcademyCraft.MOD_ID, value = Dist.CLIENT)
public final class AcademyShaders {
    private static ShaderInstance cloudroomShader;
    private static ShaderInstance distortionShader;
    private static ShaderInstance flowSenseShader;
    private static @org.jspecify.annotations.Nullable RenderType distortionRingType;

    /** 向量爆发 (VectorBlast) 粒子云实例化顶点格式: 四角 Position + 每粒子 Instance* 属性. */
    private static final VertexFormatElement INSTANCE_POS = VertexFormatElement.register(6, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 3);
    private static final VertexFormatElement INSTANCE_VEL = VertexFormatElement.register(7, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 3);
    private static final VertexFormatElement INSTANCE_SIZE = VertexFormatElement.register(8, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);
    private static final VertexFormatElement INSTANCE_COLOR = VertexFormatElement.register(9, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
    private static final VertexFormatElement INSTANCE_ROT = VertexFormatElement.register(10, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);
    private static final VertexFormatElement INSTANCE_SEED = VertexFormatElement.register(11, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);
    private static final VertexFormatElement INSTANCE_AGE = VertexFormatElement.register(12, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);
    /** 弧管使用 FLOAT 版法线/颜色元素: 内置 NORMAL(BYTE,3)/COLOR(UBYTE,4) 字节数与
     *  CurveToMeshBuilder 的 12float 交错布局不匹配, 会导致顶点属性错位与颜色/几何错乱. */
    private static final VertexFormatElement ARC_NORMAL = VertexFormatElement.register(13, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.NORMAL, 3);
    private static final VertexFormatElement ARC_COLOR = VertexFormatElement.register(14, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.COLOR, 4);

    public static final VertexFormat VFX_GRAPH_CLOUD_VORTEX = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("InstancePos", INSTANCE_POS)
            .add("InstanceVel", INSTANCE_VEL)
            .add("InstanceSize", INSTANCE_SIZE)
            .add("InstanceColor", INSTANCE_COLOR)
            .add("InstanceRot", INSTANCE_ROT)
            .add("InstanceSeed", INSTANCE_SEED)
            .add("InstanceAge", INSTANCE_AGE)
            .build();
    private static ShaderInstance vfxgraphCloudVortexShader;
    private static ShaderInstance vfxgraphPlasmaLiquidShader;
    private static ShaderInstance vfxgraphPlasmaHaloShader;

    /** 弧光管线 (黑翼/电弧) 顶点格式: 与 CurveToMeshBuilder 12float 交错布局完全吻合 (pos/normal/uv/color). */
    public static final VertexFormat VFX_GRAPH_ARC_TUBE = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Normal", ARC_NORMAL)
            .add("UV0", VertexFormatElement.UV0)
            .add("Color", ARC_COLOR)
            .build();
    private static ShaderInstance vfxgraphInkStormShader;
    private static ShaderInstance vfxgraphArcShader;
    private static ShaderInstance vfxgraphArc2Shader;
    private static ShaderInstance vfxgraphAxialBurstShader;
    private static ShaderInstance vfxgraphSkyDischargeShader;
    private static ShaderInstance directionalFieldShader;
    private static ShaderInstance screenBlitShader;

    /** 简单实例(non-instanced)顶点格式: 相机相对 Position + FLOAT Color，供拖尾条带等 vfxgraph_simple 绘制。 */
    private static final VertexFormatElement SIMPLE_COLOR = VertexFormatElement.register(15, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.COLOR, 4);
    public static final VertexFormat VFX_GRAPH_SIMPLE = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", SIMPLE_COLOR)
            .build();
    private static ShaderInstance vfxgraphSimpleShader;
    private static ShaderInstance vfxgraphPlasmaFlashShader;
    private static ShaderInstance vfxgraphPlasmaSparkShader;
    private static ShaderInstance vfxgraphPlasmaSmokeShader;
    private static ShaderInstance vfxgraphImpactMonochromeShader;
    private static ShaderInstance vfxgraphTornadoVolumeShader;
    private static ShaderInstance vfxgraphTornadoDustShader;
    private static ShaderInstance vfxgraphAeromanipMistShader;
    private static ShaderInstance vfxgraphAeromanipBladeShader;
    private static ShaderInstance vfxgraphAeromanipBladeMistShader;
    private static ShaderInstance vfxgraphAeromanipBladeGrainShader;
    private static ShaderInstance vfxgraphMaterialErosionShader;
    private static ShaderInstance vfxgraphMaterialSmokeShader;
    private static ShaderInstance vfxgraphMaterialGrowthShader;
    private static ShaderInstance vfxgraphMaterialLightShader;
    private static ShaderInstance vfxgraphMaterialMediumShader;
    private static ShaderInstance vfxgraphMaterialContactShader;
    private static ShaderInstance vfxgraphMaterialBladeShader;
    private static ShaderInstance vfxgraphIronSandShader;

    /** 按实体贴图缓存云室绿色烟雾 RenderType。 */
    private static final Function<ResourceLocation, RenderType> CLOUDROOM_TYPES =
            Util.memoize(AcademyShaders::buildCloudroomType);

    /** 空间切割 (SpacialExcision) 描线/材质/辉光双通道着色器实例与顶点格式。 */
    public static final VertexFormat SPATIAL_CUT_LINE = VfxPipelines.SPATIAL_CUT_LINE_FORMAT;
    private static ShaderInstance spatialCutMaterialShader;
    private static ShaderInstance spatialCutGlowShader;

    private AcademyShaders() {
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("cloudroom"), DefaultVertexFormat.NEW_ENTITY);
            event.registerShader(shader, instance -> cloudroomShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic cloudroom shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("distortion_ring"),
                    DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            event.registerShader(shader, instance -> distortionShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic distortion ring shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_cloud_vortex"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphCloudVortexShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph cloud vortex shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_ink_storm"), VFX_GRAPH_ARC_TUBE);
            event.registerShader(shader, instance -> vfxgraphInkStormShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph ink storm shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_arc"), VFX_GRAPH_ARC_TUBE);
            event.registerShader(shader, instance -> vfxgraphArcShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph arc shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_arc2"), VFX_GRAPH_ARC_TUBE);
            event.registerShader(shader, instance -> vfxgraphArc2Shader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph arc2 shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_axial_burst"), VFX_GRAPH_ARC_TUBE);
            event.registerShader(shader, instance -> vfxgraphAxialBurstShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph axial burst shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_sky_discharge"), VFX_GRAPH_ARC_TUBE);
            event.registerShader(shader, instance -> vfxgraphSkyDischargeShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph sky discharge shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_plasma_liquid"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphPlasmaLiquidShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph plasma liquid shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_plasma_halo"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphPlasmaHaloShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph plasma halo shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_plasma_flash"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphPlasmaFlashShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph plasma flash shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_plasma_spark"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphPlasmaSparkShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph plasma spark shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_plasma_smoke"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphPlasmaSmokeShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph plasma smoke shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_impact_monochrome"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphImpactMonochromeShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph impact monochrome shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_simple"), VFX_GRAPH_SIMPLE);
            event.registerShader(shader, instance -> vfxgraphSimpleShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph simple shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_tornado_volume"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphTornadoVolumeShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph tornado volume shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_tornado_dust"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphTornadoDustShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph tornado dust shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_aeromanip_mist"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphAeromanipMistShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph aeromanip mist shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("flow_sense"), DefaultVertexFormat.POSITION_TEX_COLOR);
            event.registerShader(shader, instance -> flowSenseShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic flow sense shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_aeromanip_blade"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphAeromanipBladeShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph aeromanip blade shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_aeromanip_blade_mist"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphAeromanipBladeMistShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph aeromanip blade mist shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_aeromanip_blade_grain"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphAeromanipBladeGrainShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph aeromanip blade grain shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_material_erosion"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphMaterialErosionShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph material erosion shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_material_smoke"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphMaterialSmokeShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph material smoke shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_material_growth"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphMaterialGrowthShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph material growth shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_material_light"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphMaterialLightShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph material light shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_material_medium"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphMaterialMediumShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph material medium shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_material_contact"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphMaterialContactShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph material contact shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_material_blade"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphMaterialBladeShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph material blade shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("vfxgraph_iron_sand"), VFX_GRAPH_CLOUD_VORTEX);
            event.registerShader(shader, instance -> vfxgraphIronSandShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic vfxgraph iron sand shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("directional_field"), DefaultVertexFormat.POSITION);
            event.registerShader(shader, instance -> directionalFieldShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic directional field shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("screen_blit"), DefaultVertexFormat.POSITION);
            event.registerShader(shader, instance -> screenBlitShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic screen blit shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("spatial_cut_material"), SPATIAL_CUT_LINE);
            event.registerShader(shader, instance -> spatialCutMaterialShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic spatial cut material shader", e);
        }
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(), academy("spatial_cut_glow"), SPATIAL_CUT_LINE);
            event.registerShader(shader, instance -> spatialCutGlowShader = instance);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load academic spatial cut glow shader", e);
        }
    }

    public static ShaderInstance getCloudroomShader() {
        return cloudroomShader;
    }

    public static ShaderInstance getDistortionShader() {
        return distortionShader;
    }

    public static ShaderInstance getVfxgraphCloudVortexShader() {
        return vfxgraphCloudVortexShader;
    }

    public static ShaderInstance getVfxgraphInkStormShader() {
        return vfxgraphInkStormShader;
    }

    public static ShaderInstance getVfxgraphArcShader() {
        return vfxgraphArcShader;
    }

    public static ShaderInstance getVfxgraphArc2Shader() {
        return vfxgraphArc2Shader;
    }

    public static ShaderInstance getVfxgraphAxialBurstShader() {
        return vfxgraphAxialBurstShader;
    }

    public static ShaderInstance getVfxgraphSkyDischargeShader() {
        return vfxgraphSkyDischargeShader;
    }

    public static ShaderInstance getVfxgraphPlasmaLiquidShader() {
        return vfxgraphPlasmaLiquidShader;
    }

    public static ShaderInstance getVfxgraphPlasmaHaloShader() {
        return vfxgraphPlasmaHaloShader;
    }

    public static ShaderInstance getVfxgraphPlasmaFlashShader() {
        return vfxgraphPlasmaFlashShader;
    }

    public static ShaderInstance getVfxgraphPlasmaSparkShader() {
        return vfxgraphPlasmaSparkShader;
    }

    public static ShaderInstance getVfxgraphPlasmaSmokeShader() {
        return vfxgraphPlasmaSmokeShader;
    }

    public static ShaderInstance getVfxgraphImpactMonochromeShader() {
        return vfxgraphImpactMonochromeShader;
    }

    public static ShaderInstance getVfxgraphSimpleShader() {
        return vfxgraphSimpleShader;
    }

    public static ShaderInstance getVfxgraphTornadoVolumeShader() {
        return vfxgraphTornadoVolumeShader;
    }

    public static ShaderInstance getVfxgraphTornadoDustShader() {
        return vfxgraphTornadoDustShader;
    }

    public static ShaderInstance getVfxgraphAeromanipMistShader() {
        return vfxgraphAeromanipMistShader;
    }

    public static ShaderInstance getVfxgraphAeromanipBladeShader() {
        return vfxgraphAeromanipBladeShader;
    }

    public static ShaderInstance getVfxgraphAeromanipBladeMistShader() {
        return vfxgraphAeromanipBladeMistShader;
    }

    public static ShaderInstance getVfxgraphAeromanipBladeGrainShader() {
        return vfxgraphAeromanipBladeGrainShader;
    }

    public static ShaderInstance getVfxgraphMaterialErosionShader() {
        return vfxgraphMaterialErosionShader;
    }

    public static ShaderInstance getVfxgraphMaterialSmokeShader() {
        return vfxgraphMaterialSmokeShader;
    }

    public static ShaderInstance getVfxgraphMaterialGrowthShader() {
        return vfxgraphMaterialGrowthShader;
    }

    public static ShaderInstance getVfxgraphMaterialLightShader() {
        return vfxgraphMaterialLightShader;
    }

    public static ShaderInstance getVfxgraphMaterialMediumShader() {
        return vfxgraphMaterialMediumShader;
    }

    public static ShaderInstance getVfxgraphMaterialContactShader() {
        return vfxgraphMaterialContactShader;
    }

    public static ShaderInstance getVfxgraphMaterialBladeShader() {
        return vfxgraphMaterialBladeShader;
    }

    public static ShaderInstance getVfxgraphIronSandShader() {
        return vfxgraphIronSandShader;
    }

    public static ShaderInstance getFlowSenseShader() {
        return flowSenseShader;
    }

    public static ShaderInstance getDirectionalFieldShader() {
        return directionalFieldShader;
    }

    public static ShaderInstance getScreenBlitShader() {
        return screenBlitShader;
    }

    public static ShaderInstance getSpatialCutMaterialShader() {
        return spatialCutMaterialShader;
    }

    public static ShaderInstance getSpatialCutGlowShader() {
        return spatialCutGlowShader;
    }

    public static RenderType cloudroomType(ResourceLocation texture) {
        return CLOUDROOM_TYPES.apply(texture);
    }

    private static RenderType buildCloudroomType(ResourceLocation texture) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(AcademyShaders::getCloudroomShader))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .createCompositeState(true);
        return RenderType.create("cloudroom", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 1536, true, true, state);
    }

    public static RenderType distortionRingType() {
        if (distortionRingType == null) {
            distortionRingType = buildDistortionRingType();
        }
        return distortionRingType;
    }

    private static RenderType buildDistortionRingType() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(AcademyShaders::getDistortionShader))
                .setTextureState(RenderStateShard.NO_TEXTURE)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .createCompositeState(true);
        return RenderType.create("distortion_ring", DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL,
                VertexFormat.Mode.QUADS, 1536, true, false, state);
    }
}
