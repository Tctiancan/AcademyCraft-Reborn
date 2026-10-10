package org.academy.api.client.render.vfx;

import org.academy.api.common.profiler.AcademyProfiler;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @deprecated use {@link org.academy.api.client.render.vfxgraph.runtime.VfxGraphManager}
 */
@Deprecated(since = "0.0.4")
public final class VfxManager {
    public static final VfxManager INSTANCE = new VfxManager();

    private final Set<Vfx> activeEffects = new LinkedHashSet<>();
    private final VfxFrameData frameData = new VfxFrameData();
    private final MainRenderContext renderContext = new MainRenderContext();
    private boolean initialized;

    private VfxManager() {
    }

    public void init() {
        if (initialized) return;
        VfxRegistry.forEachRenderer(VfxRenderer::init);
        initialized = true;
    }

    public void close() {
        if (!initialized) return;
        VfxRegistry.forEachRenderer(VfxRenderer::close);
        activeEffects.clear();
        frameData.clear();
        initialized = false;
    }

    public void spawn(Vfx effect) {
        activeEffects.add(effect);
    }

    public void clearEffects() {
        activeEffects.clear();
    }

    public void sampleFrame(VfxFrameContext ctx) {
        frameData.clear();
        renderContext.update(ctx);
        var iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            var effect = iterator.next();
            effect.update(ctx.dt(), ctx);
            if (!effect.isAlive()) {
                iterator.remove();
                continue;
            }
            effect.sample(ctx, frameData);
        }
    }

    public void renderFrame() {
        if (!initialized) return;
        if (frameData.isEmpty()) return;
        AcademyProfiler.runZone("academy.vfx.frame", () -> {
            AcademyProfiler.runZone("academy.vfx.translucent", () ->
                    VfxRegistry.renderPhase(VfxPhase.WORLD_TRANSLUCENT, frameData, renderContext));
            AcademyProfiler.runZone("academy.vfx.top", () ->
                    VfxRegistry.renderPhase(VfxPhase.WORLD_ALWAYS_ON_TOP, frameData, renderContext));
            AcademyProfiler.runZone("academy.vfx.screen", () ->
                    VfxRegistry.renderPhase(VfxPhase.SCREEN_SPACE_POST, frameData, renderContext));
            // 26.2 的 WORLD_GLOW 数据由 bloom/glow 后处理 pass 延迟渲染; 1.21.1 无该统一 pass,
            // 改为在 translucent/top/screen 之后以内联加法方式渲染到主缓冲 (渲染器自包含 RenderSystem 态)。
            AcademyProfiler.runZone("academy.vfx.glow", () ->
                    VfxRegistry.renderPhase(VfxPhase.WORLD_GLOW, frameData, renderContext));
            frameData.clear();
        });
    }

    public void renderAfterSkyFrame() {
        if (!initialized || frameData.isEmpty()) return;
        AcademyProfiler.runZone("academy.vfx.after_sky", () ->
                VfxRegistry.renderPhase(VfxPhase.WORLD_AFTER_SKY, frameData, renderContext));
    }

    public boolean hasGlowData() {
        return hasPhaseData(VfxPhase.WORLD_GLOW);
    }

    private boolean hasPhaseData(VfxPhase phase) {
        for (var registration : VfxRegistry.entries()) {
            if (registration.phase() != phase) continue;
            var bucket = frameData.get(registration.dataType());
            if (bucket != null && !bucket.isEmpty()) return true;
        }
        return false;
    }

    private static final class MainRenderContext implements VfxRenderContext {
        private final Vector3f cameraPos = new Vector3f();
        private final Quaternionf cameraOrientation = new Quaternionf();
        private final Matrix4f viewRotationMatrix = new Matrix4f();
        private final Matrix4f projection = new Matrix4f();
        private float gameTime;

        void update(VfxFrameContext frame) {
            gameTime = frame.gameTime();
            var camera = frame.camera();
            cameraPos.set(camera.pos());
            cameraOrientation.set(camera.orientation());
            viewRotationMatrix.set(camera.viewRotationMatrix());
            projection.set(camera.projectionMatrix());
        }

        @Override
        public float gameTime() {
            return gameTime;
        }

        @Override
        public Vector3f cameraPos() {
            return cameraPos;
        }

        @Override
        public Quaternionf cameraOrientation() {
            return cameraOrientation;
        }

        @Override
        public Matrix4f viewRotationMatrix() {
            return viewRotationMatrix;
        }
    }
}
