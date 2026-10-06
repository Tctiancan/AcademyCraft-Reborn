package org.academy.api.client.render.vfxgraph.render;

/**
 * C-stun: 26.2 {@code GpuTexture}/{@code GpuTextureView} 场景深度拷贝暂不移植。
 * 保留类结构与方法签名，实现为空占位。
 */
public final class SceneDepth {
    public void copyFrom(Object depth) {
        // C-stun: 世界 VFX 深度拷贝暂不移植。
    }

    public Object view() {
        return null;
    }

    public void close() {
        // C-stun: 26.2 GPU 资源释放暂不移植。
    }
}
