package org.academy.internal.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.academy.api.client.ability.AbilitySystemClient;
import org.academy.internal.common.ability.Skills;
import org.academy.internal.common.ability.meltdowner.skills.lv3.Cloudroom;

import java.util.function.Function;

import static org.academy.AcademyCraft.academy;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过节点化提交 (collector.order().submitModel(...)) 与自定义 RenderType/RenderSetup 渲染"云室烟雾填充"
 * 特效。该管线不写深度且绕过深度测试, 使标记在墙后依旧可见。1.21.1 无节点化提交 API,改为沿用 26.2 的
 * shouldReveal 判定:对标记实体用其自身贴图、以半透明绿色、无深度/不写深度的 RenderType 叠加渲染一遍模型。
 */
public final class CloudroomLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {
    public static final ResourceLocation VISIBLE = academy("cloudroom_visible");
    /** 绿色标记层的 ARGB 顶点色 (半透明绿)。 */
    private static final int TINT_COLOR = 0xFF6BFF4D;
    /**
     * 云室绿色标记 RenderType: 实体半透明全亮渲染, 关闭深度测试且不写深度, 保证墙后可见。
     */
    private static final Function<ResourceLocation, RenderType> CLOUDROOM_OVERLAY =
            Util.memoize(CloudroomLayer::buildOverlayType);

    private final LivingEntityRenderer<? super T, M> renderer;

    public CloudroomLayer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    private static RenderType buildOverlayType(ResourceLocation texture) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .createCompositeState(true);
        return RenderType.create("cloudroom", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 1536, true, true, state);
    }

    public static boolean shouldReveal(LivingEntity entity) {
        var player = Minecraft.getInstance().player;
        if (player == null || !player.isAlive() || entity == player || entity.level() != player.level()
                || !entity.isAlive() || entity.isSpectator()) return false;
        var skill = Skills.CLOUDROOM.get();
        if (!AbilitySystemClient.getSkillData(skill).map(data -> data.isEnabled()).orElse(false)) return false;
        var milestone = AbilitySystemClient.getSkillProficiencyMilestone(skill);
        if (entity.isInvisibleTo(player) && milestone < 3) return false;
        var radius = Cloudroom.detectionRadius(milestone);
        return player.getBoundingBox().inflate(radius).intersects(entity.getBoundingBox());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!shouldReveal(entity)) return;
        // 用实体自身贴图渲染一遍半透明绿色叠加层, 作为云室标记 (关闭深度测试, 穿墙可见)。
        var model = getParentModel();
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        var consumer = bufferSource.getBuffer(
                CLOUDROOM_OVERLAY.apply(renderer.getTextureLocation(entity)));
        model.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, TINT_COLOR);
    }
}
