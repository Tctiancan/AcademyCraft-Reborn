package org.academy.internal.client.renderer.entity.layers.quantum;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.academy.internal.common.attachment.AttachmentTypes;

/**
 * 量子干涉实体层:对带 {@link org.academy.internal.common.attachment.AttachmentTypes#QUANTUM_DATA}
 * 数据且处于激活态的实体,以全亮、随 ageInTicks 脉冲的蓝色半透明叠加渲染其模型,呈现"击中后闪蓝光"效果。
 *
 * 26.2 通过节点化提交 {@code submit} + GlowEffect 后处理管线绘制。1.21.1 无对应 API,移植为
 * 等效视觉:用 {@link RenderType#entityTranslucent} 全亮半透明叠加模型自身贴图并按量子色染色,
 * 顶点色 alpha 使用与 26.2 一致的 {@code intensity * |sin(ageInTicks * 0.1)|} 脉冲。
 */
public class QuantumInterferenceLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {
public static final AttachmentType<QuantumData> CONTEXT_KEY = AttachmentTypes.QUANTUM_DATA.get();

    public QuantumInterferenceLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        var quantum = entity.getData(CONTEXT_KEY);
        if (quantum == null || !quantum.active()) return;

        var intensity = quantum.intensity();
        var color = quantum.color();
        var r = ((color >> 16) & 0xFF);
        var g = ((color >> 8) & 0xFF);
        var b = (color & 0xFF);

        var model = getParentModel();
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        var consumer = bufferSource.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        var alpha = Mth.floor(intensity * Math.abs(Mth.sin(ageInTicks * 0.1f)) * 255.0f);
        model.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, (alpha << 24) | (r << 16) | (g << 8) | b);
    }
}
