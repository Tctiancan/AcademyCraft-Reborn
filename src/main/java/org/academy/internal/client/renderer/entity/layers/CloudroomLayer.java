package org.academy.internal.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.academy.api.client.ability.AbilitySystemClient;
import org.academy.internal.client.render.AcademyShaders;
import org.academy.internal.common.ability.Skills;
import org.academy.internal.common.ability.meltdowner.skills.lv3.Cloudroom;

import static org.academy.AcademyCraft.academy;

/**
 * 云室 (Cloudroom) 实体标记层。
 * <p>
 * 复用 26.2 的完整版程序化噪声烟雾着色器管线 (AcademyShaders.cloudroomType): 对检测范围内的实体用其
 * 自身贴图、以半透明绿色噪声烟雾、无深度/不写深度的 RenderType 叠加渲染一遍模型。着色器内部自带 rim 光、
 * 剪影 alpha 裁剪与全亮, 不依赖环境光照。
 */
public final class CloudroomLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {
    public static final ResourceLocation VISIBLE = academy("cloudroom_visible");

    private final LivingEntityRenderer<? super T, M> renderer;

    public CloudroomLayer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
        this.renderer = renderer;
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
        var model = getParentModel();
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        var consumer = bufferSource.getBuffer(
                AcademyShaders.cloudroomType(renderer.getTextureLocation(entity)));
        // 着色器为程序化配色, 顶点色取默认白; 全亮与环境无关。
        model.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
