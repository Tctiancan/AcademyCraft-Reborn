package org.academy.internal.client.renderer.entity.layers.quantum;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import static org.academy.AcademyCraft.academy;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过节点化提交渲染量子干涉特效,并依赖 GlowEffect (26.2 后处理管线) 与 FastColor/LightCoordsUtil 等
 * 新版 API。1.21.1 无对应节点化提交与后处理管线。按首版简化策略保留公共 API (CONTEXT_KEY),清空 render 实现。
 */
public class QuantumInterferenceLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {
    public static final ResourceLocation CONTEXT_KEY = academy("quantum");

    public QuantumInterferenceLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        // C-stun: 1.21.1 量子干涉特效留空。
    }
}
