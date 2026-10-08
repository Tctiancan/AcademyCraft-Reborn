package org.academy.mixin.client;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过 {@code LivingEntityRenderState.getRenderData(QuantumInterferenceLayer.CONTEXT_KEY)} 与
 * {@code state.ageInTicks/state.outlineColor} 实现量子干涉半透明染色、模型色调与心灵描边。
 * 这些依赖 1.21.1 不存在的渲染状态 (LivingEntityRenderState) 与 {@code LivingEntityRenderer}
 * 新签名 ({@code getRenderType(getRenderData, forceTransparent)} / {@code getModelTint} / {@code extractRenderState})。
 * 按首版简化策略保留判色常量，清空渲染相关注入。
 */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {
    private static final int MENTALOUT_OUTLINE_COLOR = 0xFFFF7A18;
}
