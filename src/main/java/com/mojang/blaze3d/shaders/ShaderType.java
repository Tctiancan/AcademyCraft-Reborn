package com.mojang.blaze3d.shaders;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.shaders.ShaderType}.
 *
 * <p>26.2 以 {@code ShaderType} 区分顶点/片元/计算着色阶段; 1.21.1 的
 * {@code ShaderProgram} 内部以 {@code ShaderProgram.Type} 承担同类职责.
 * 本垫层仅保留符号, 供既有形如 {@code get(id, ShaderType.VERTEX)} 的调用保持成立.
 */
public enum ShaderType {
    VERTEX,
    FRAGMENT,
    COMPUTE
}
