package org.academy.desktop.platform

/**
 * 编辑器桌面运行时的 GPU 兼容垫层.
 *
 * <p>26.2 桌面编辑器大量调用 {@code RenderSystem.executePendingTasks()} (促使排队 GPU 任务立即
 * 执行); 1.21.1 的 {@code RenderSystem} 没有该方法且使用即时态渲染, 这里以同名顶层函数保符号
 * (C-stun): 渲染为目标合成所隐含, 无需显式刷队.
 */
fun executePendingTasks() {
    // C-stun: 1.21.1 即时渲染无排队 GPU 任务.
}
