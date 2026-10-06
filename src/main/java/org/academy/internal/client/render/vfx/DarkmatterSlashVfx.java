package org.academy.internal.client.render.vfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import org.academy.api.client.render.vfx.*;
import org.academy.api.client.resources.R;
import org.academy.api.common.vfx.SkillVfxState;
import org.jspecify.annotations.Nullable;

import java.util.List;

/** Locally timed slash; no client entity or per-effect mesh allocation is needed. */
public final class DarkmatterSlashVfx implements Vfx {
    private final SkillVfxState.Slash state;
    private final @Nullable ClientLevel level = Minecraft.getInstance().level;
    private float elapsed;
    private boolean sampled;

    public DarkmatterSlashVfx(SkillVfxState.Slash state) {
        this.state = state;
    }

    public static void register() {
        VfxRegistry.register(Data.class, VfxPhase.WORLD_TRANSLUCENT, new Renderer());
    }

    @Override
    public void update(float dt, VfxFrameContext ctx) {
        // Start on the first frame so a short slash cannot expire while waiting to render.
        if (sampled && !Minecraft.getInstance().isPaused()) elapsed += Math.max(0, dt);
    }

    @Override
    public void sample(VfxFrameContext ctx, VfxSink sink) {
        sampled = true;
        if (!VfxVisibility.sphere(ctx.camera(), state.position(), 4f * state.scale())) return;
        sink.push(new Data(state, Math.clamp(Math.max(0.1f, elapsed / state.lifetimeTicks()), 0, 1)));
    }

    @Override
    public boolean isAlive() {
        return Minecraft.getInstance().level == level && elapsed < state.lifetimeTicks();
    }

    public record Data(SkillVfxState.Slash state, float progress) implements VfxRenderData {
        int frame() { return Math.clamp((int) (progress * 4), 0, 3); }
    }

    /**
     * 1.21.1 简化版 (C-stun)。
     *
     * 26.2 通过 GpuDevice/RenderPass 实例化四边形绘制斩击刀光; 1.21.1 无对应 API, 且该渲染位于
     * 世界级 VFX 链路上。按首版简化策略清空 GPU 实现, 仅保留 VfxRenderer 生命周期方法。
     */
    private static final class Renderer implements VfxRenderer<Data> {
        private static final ResourceLocation[] FRAMES = {
                R.textures.darkmatter_cut_slash_effect_1, R.textures.darkmatter_cut_slash_effect_2,
                R.textures.darkmatter_cut_slash_effect_3, R.textures.darkmatter_cut_slash_effect_4
        };

        @Override
        public void init() {
            // C-stun: 1.21.1 不创建 GPU 缓冲。
        }

        @Override
        public void render(VfxRenderContext ctx, List<? extends Data> data) {
            // C-stun: 1.21.1 不绘制斩击刀光。
        }

        @Override
        public void close() {
            // C-stun: 1.21.1 无 GPU 资源需要释放。
        }
    }
}
