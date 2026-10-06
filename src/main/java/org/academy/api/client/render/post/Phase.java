package org.academy.api.client.render.post;

import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过 StagedVertexBuffer + RenderType.drawFromBuffer 延迟提交阶段顶点; 1.21.1
 * 无对应 API。按首版简化策略, getBuffer 返回 Tesselator 的单例 BufferBuilder (不被 flush,
 * 顶点随即丢弃), draw/close 为空操作, 保证调用方 (粒子/实体渲染器) 仍可编译运行但不产生辉光。
 */
public class Phase {
    public Phase(String name) {
    }

    public VertexConsumer getBuffer(RenderType renderType) {
        return Tesselator.getInstance().getBuilder();
    }

    public void draw() {
        // C-stun: 1.21.1 不提交阶段缓存。
    }

    public void close() {
    }
}
