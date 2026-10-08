package com.mojang.blaze3d.shaders;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.shaders.UniformType}.
 *
 * <p>26.2 以 {@code UniformType} 描述着色器 bind group 中每个绑定的数据种类;
 * 1.21.1 无对应抽象 (uniform 一律通过 {@code RenderSystem.setShaderUniform} 提交).
 * 本垫层仅保留符号, 供既有形如 {@code UniformType.UNIFORM_BUFFER} 的调用保持成立.
 */
public enum UniformType {
    UNIFORM_BUFFER,
    STORAGE_BUFFER,
    SAMPLER
}
