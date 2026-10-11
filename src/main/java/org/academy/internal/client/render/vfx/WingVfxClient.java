package org.academy.internal.client.render.vfx;

import org.academy.api.client.render.LevelRenderEvent;
import org.academy.api.client.render.vfx.VfxManager;
import org.academy.api.client.render.vfx.VfxPhase;
import org.academy.api.client.render.vfx.VfxRegistry;
import org.academy.api.client.renderer.RendererManager;
import net.neoforged.neoforge.common.NeoForge;

public final class WingVfxClient {
    private WingVfxClient() {
    }

    public static void register() {
        VfxRegistry.register(WingData.class, VfxPhase.WORLD_TRANSLUCENT, new WingRenderer());
        RendererManager.registerEffectRenderer(WingFirstPersonBridge.INSTANCE);
        VfxManager.INSTANCE.spawn(WingVfx.INSTANCE);
        // 1.21.1: 标准 bufferSource 世界管线在 renderLevel RETURN 经 LevelRenderEvent 落屏
        // (与 teleport cursor/beam 等已确认可见的世界渲染器同路径); 立即态 VfxManager.renderFrame()
        // 在该上下文不能合成像素, 故翅膀几何只在 render() 中缓存, 在此事件监听里真正冲刷。
        NeoForge.EVENT_BUS.addListener(WingRenderer::onLevelRender);
    }
}
