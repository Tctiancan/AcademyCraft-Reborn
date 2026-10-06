package org.academy.internal.client.data;

import net.minecraft.data.PackOutput;
import net.minecraft.data.models.ModelProvider;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 的 `ModelProvider` 暴露 `registerModels(BlockModelGenerators, ItemModelGenerators)`
 * 并通过 `super(output, modid)` 构造; 1.21.1 该基类为具体类且构造仅接受 PackOutput,
 * 无对应注册钩子。为优先打通 GUI 主链路, 此处保留类与构造签名, 自定义模型生成整体跳过。
 */
public final class AcademyCraftModelProvider extends ModelProvider {
    public AcademyCraftModelProvider(PackOutput output) {
        super(output);
    }
}
