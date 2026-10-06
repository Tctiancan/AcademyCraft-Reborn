package org.academy.internal.client;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.academy.internal.client.ability.aeromanip.FlowSenseClient;
import org.academy.internal.client.ability.aeromanip.VacuumVisualClient;
import org.academy.internal.common.ability.teleport.skills.lv5.SpacialExcision;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 26.2 的 ClientResourceLoadFinishedEvent 在 1.21.1 不存在。
 * 等价物是在 mod 总线 RegisterClientReloadListenersEvent 上注册一个重载监听器，
 * 每次资源(重新)加载完成时都会触发，用于释放能力视觉的临时资源。
 */
@EventBusSubscriber(Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientResourceReloadListener implements PreparableReloadListener {

    @SubscribeEvent
    public static void onAddReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ClientResourceReloadListener());
    }

    @Override
    public CompletableFuture<Void> reload(
            PreparableReloadListener.PreparationBarrier preparationBarrier,
            ResourceManager resourceManager,
            ProfilerFiller preparationsProfiler,
            ProfilerFiller reloadProfiler,
            Executor backgroundExecutor,
            Executor gameExecutor
    ) {
        return CompletableFuture.completedFuture(Unit.INSTANCE)
                .thenCompose(preparationBarrier::wait)
                .thenRun(this::reloadCompleted);
    }

    private void reloadCompleted() {
        FlowSenseClient.onClientResourcesReloaded();
        VacuumVisualClient.onClientResourcesReloaded();
        SpacialExcision.Client.onClientResourcesReloaded();
    }
}
