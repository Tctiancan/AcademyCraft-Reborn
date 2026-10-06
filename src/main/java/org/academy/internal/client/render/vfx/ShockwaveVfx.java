package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.academy.api.client.render.vfx.*;
import org.academy.api.common.vfx.SkillVfxState;
import org.academy.internal.client.renderer.entity.KineticShockwaveRenderer;

/** Short local playback survives the original server tick and entity lifetime. */
public final class ShockwaveVfx implements Vfx {
    private final SkillVfxState.Burst burst;
    private final ClientLevel level = Minecraft.getInstance().level;
    private final long startedAt = System.nanoTime();
    private final long lifetime;
    public ShockwaveVfx(SkillVfxState.Burst burst) {
        this.burst = burst;
        lifetime = Math.max(1, burst.lifetimeTicks()) * 50_000_000L;
    }
    public static void register() {
        VfxRegistry.register(Data.class, VfxPhase.WORLD_TRANSLUCENT, new Renderer());
    }
    @Override
    public void sample(VfxFrameContext ctx, VfxSink sink) {
        if (!VfxVisibility.sphere(ctx.camera(), burst.position(), burst.radius() + 2f)) return;
        float progress = Math.clamp((System.nanoTime() - startedAt) / (float) lifetime, 0f, 1f);
        sink.push(new Data(burst, progress));
    }
    @Override
    public boolean isAlive() {
        return Minecraft.getInstance().level == level && System.nanoTime() - startedAt < lifetime;
    }

    public record Data(SkillVfxState.Burst burst, float progress) implements VfxRenderData {}
    private static final class Renderer implements VfxRenderer<Data> {
        private final PoseStack pose = new PoseStack();
        @Override public void init() {}
        @Override public void render(VfxRenderContext ctx, List<? extends Data> data) {
            // C-stun: 1.21.1 不渲染冲击波环 (世界级 VFX)。
        }
    }
}
