package org.academy.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.academy.internal.client.ability.mentalout.MentaloutRosterClientState;
import org.academy.internal.client.renderer.entity.layers.quantum.QuantumInterferenceLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.21.1 适配版 (C-stun 简化)。
 *
 * 26.2 通过 {@code LivingEntityRenderState.getRenderData(QuantumInterferenceLayer.CONTEXT_KEY)} 注入
 * {@code getRenderType} (量子激活时强制透明) 与 {@code getModelTint} (白色染色)。1.21.1 的
 * {@code LivingEntityRenderer} 签名不同 (实体直传, 无 render state), 也没有 {@code getModelTint} 钩子,
 * 因此此处只移植可用的 {@code getRenderType} 注入: 量子数据处于激活态时把实体模型强制为半透明渲染。
 * 模型自身的白色/蓝色染色由上一层 {@link QuantumInterferenceLayer} 的叠加渲染完成, 故无需模型染色钩子。
 * mentalout 受控目标的橙色描边由 {@link org.academy.mixin.client.MixinLevelRenderer} 的
 * `getTeamColor`/`shouldEntityAppearGlowing` 重定向实现 (1.21.1 无 render state 的 outlineColor)。
 */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer extends EntityRenderer<LivingEntity> {
    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void getRenderType(LivingEntity livingEntity, boolean bodyVisible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        var quantum = livingEntity.getData(QuantumInterferenceLayer.CONTEXT_KEY);
        if (quantum == null || !quantum.active()) return;
        cir.setReturnValue(RenderType.itemEntityTranslucentCull(this.getTextureLocation(livingEntity)));
    }

    /**
     * 修改 {@code render} 内 {@code this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, i, flag1 ? 654311423 : -1)}
     * 的最后一个参数 (color)。26.2 的 {@code getModelTint} 同理: 量子激活时改为随 tickCount 脉冲的白色。
     *
     * <p>从 merged jar 反汇编确认, {@code render} 内该调用点的 owner 为 {@code EntityModel} (而非 {@code Model}).
     * {@code LivingEntityRenderer.render(Entity,...)} 桥接方法为 synthetic, mixin 方法选择器默认忽略,
     * 因此 {@code method = "render"} (仅名字) 可唯一匹配目标, 与本文件 {@code getRenderType} 同款。
     */
    @ModifyArg(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"),
            index = 4)
    private int getModelTint(int color, @Local(argsOnly = true) LivingEntity livingEntity) {
        var quantum = livingEntity.getData(QuantumInterferenceLayer.CONTEXT_KEY);
        if (quantum == null || !quantum.active()) return color;
        return FastColor.ARGB32.color(
                (int) (Math.max(Mth.sin(livingEntity.tickCount * 0.125f), 0) * 255),
                255, 255, 255
        );
    }

    protected MixinLivingEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
