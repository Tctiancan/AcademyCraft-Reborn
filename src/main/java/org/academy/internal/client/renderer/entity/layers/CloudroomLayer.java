package org.academy.internal.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.academy.api.client.ability.AbilitySystemClient;
import org.academy.internal.common.ability.Skills;
import org.academy.internal.common.ability.meltdowner.skills.lv3.Cloudroom;

import static org.academy.AcademyCraft.academy;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过节点化提交 (collector.order().submitModel(...)) 与自定义 RenderType/RenderSetup 渲染"云室烟雾填充"
 * 特效。1.21.1 无节点化提交 API,且该图层依赖世界级 VFX 渲染。按首版简化策略保留公共 API
 * (shouldReveal/VISIBLE),清空 render 实现。
 */
public final class CloudroomLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {
    public static final ResourceLocation VISIBLE = academy("cloudroom_visible");

    public CloudroomLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
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
        // C-stun: 1.21.1 云室烟雾填充特效留空。
    }
}
