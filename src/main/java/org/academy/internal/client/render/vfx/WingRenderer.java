package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import org.academy.api.client.render.LevelRenderEvent;
import org.academy.api.client.render.vfx.VfxRenderContext;
import org.academy.api.client.render.vfx.VfxRenderer;
import org.academy.api.client.resources.R;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1.21.1 实装 (原 26.2 翅膀世界级渲染器)。
 *
 * <p>26.2 以实例化环面顶点/Stable/Instant 两个附加着色器阶段延迟提交; 1.21.1 无对应管线,
 * 改为由 {@link WingVfx#sample} 预烘焙的世界坐标 POSITION_TEX 四边形流 (每顶点 20 字节)。
 * 绘制经 {@link LevelRenderEvent} 的标准 {@code bufferSource} 管线提交 (与 teleport cursor 等
 * 已确认可见的世界渲染器同一路径), 几何以相机相对坐标、逐顶点由 PoseStack 变换落屏。
 */
public final class WingRenderer implements VfxRenderer<WingData> {
    @Override
    public void init() {
    }

    private static final class PendingBatch {
        final WingKind kind;
        final boolean additive;
        final ResourceLocation texture;
        final ByteBuffer vertices;

        PendingBatch(WingKind kind, boolean additive, ResourceLocation texture, ByteBuffer vertices) {
            this.kind = kind;
            this.additive = additive;
            this.texture = texture;
            this.vertices = vertices;
        }
    }

    private static final List<PendingBatch> PENDING = new ArrayList<>();

    @Override
    public void render(VfxRenderContext ctx, List<? extends WingData> data) {
        PENDING.clear();
        if (data.isEmpty()) {
            return;
        }
        data.forEach(item -> {
            var vertices = item.instances().duplicate().order(java.nio.ByteOrder.nativeOrder());
            if (!vertices.hasRemaining()) {
                return;
            }
            var additive = item.layer() == WingData.Layer.ASCENSION;
            PENDING.add(new PendingBatch(item.kind(), additive, tornadoTexture(item.kind(), additive), vertices));
        });
    }

    /** 由 {@link LevelRenderEvent} 监听调用: 在 renderLevel RETURN 后经 bufferSource 标准管线落屏。 */
    public static void onLevelRender(LevelRenderEvent event) {
        if (PENDING.isEmpty()) {
            return;
        }
        for (var batch : PENDING) {
            var renderType = renderTypeFor(batch.texture, batch.additive);
            event.submitPoseGeometry(renderType, (pose, consumer) -> {
                pose.pushPose();
                // 烘焙坐标已是相机相对空间(WingVfx 直接取 capture 的矩阵), 勿再叠 -camera。
                var src = batch.vertices.duplicate().order(java.nio.ByteOrder.nativeOrder());
                while (src.hasRemaining()) {
                    addWingVertex(consumer, pose, src.getFloat(), src.getFloat(), src.getFloat(),
                            src.getFloat(), src.getFloat());
                }
                pose.popPose();
            });
        }
        PENDING.clear();
    }

    private static void addWingVertex(VertexConsumer consumer, PoseStack pose,
                                      float x, float y, float z, float u, float v) {
        consumer.addVertex(pose.last().pose(), x, y, z).setUv(u, v);
    }

    private static final Map<String, RenderType> RENDER_TYPES = new HashMap<>();

    private static RenderType renderTypeFor(ResourceLocation texture, boolean additive) {
        var key = texture + (additive ? "#add" : "");
        var cached = RENDER_TYPES.get(key);
        if (cached != null) {
            return cached;
        }
        var composite = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_TEX_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(additive
                        ? RenderStateShard.LIGHTNING_TRANSPARENCY
                        : RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .createCompositeState(false);
        var renderType = RenderType.create(
                "academy_storm_wing",
                DefaultVertexFormat.POSITION_TEX,
                VertexFormat.Mode.QUADS,
                262144,
                false,
                false,
                composite
        );
        RENDER_TYPES.put(key, renderType);
        return renderType;
    }

    private static ResourceLocation tornadoTexture(WingKind kind, boolean additive) {
        if (additive) {
            return R.textures.white_wing_ascension;
        }
        return switch (kind) {
            case STORM -> R.textures.storm_wing;
            case BLACK -> R.textures.black_wing;
            case WHITE -> R.textures.white_wing;
            case PLATINUM -> R.textures.white_wing;
        };
    }

    @Override
    public void close() {
    }
}
