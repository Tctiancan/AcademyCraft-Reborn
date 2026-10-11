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
    private static @org.jspecify.annotations.Nullable RenderType distortionRingType;

    /** 向量爆发 (VectorBlast) 粒子云实例化顶点格式: 四角 Position + 每粒子 Instance* 属性. */
    private static final VertexFormatElement INSTANCE_POS = VertexFormatElement.register(6, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 3);
    private static final VertexFormatElement INSTANCE_VEL = VertexFormatElement.register(7, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 3);
    private static final VertexFormatElement INSTANCE_SIZE = VertexFormatElement.register(8, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);
    private static final VertexFormatElement INSTANCE_COLOR = VertexFormatElement.register(9, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4);
    private static final VertexFormatElement INSTANCE_ROT = VertexFormatElement.register(10, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);
    private static final VertexFormatElement INSTANCE_SEED = VertexFormatElement.register(11, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);
    private static final VertexFormatElement INSTANCE_AGE = VertexFormatElement.register(12, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1);

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

    /** 按实体贴图缓存云室绿色烟雾 RenderType。 */
    private static final Function<ResourceLocation, RenderType> CLOUDROOM_TYPES =
            Util.memoize(AcademyShaders::buildCloudroomType);

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
