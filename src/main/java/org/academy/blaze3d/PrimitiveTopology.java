package org.academy.blaze3d;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.PrimitiveTopology}.
 *
 * <p>26.2 以 {@code PrimitiveTopology} 描述图元装配拓扑; 1.21.1 的图元由
 * {@code VertexFormat.Mode} (GL 常量 {@link org.lwjgl.opengl.GL11}) 承担.
 * 本垫层仅保留符号, 供既有形如 {@code PrimitiveTopology.QUADS} 的调用及
 * {@code RenderSystem.getSequentialBuffer(topology)} 保持成立.
 */
public enum PrimitiveTopology {
    POINT_LIST,
    LINE_LIST,
    LINE_STRIP,
    TRIANGLE_LIST,
    TRIANGLE_STRIP,
    QUADS
}
