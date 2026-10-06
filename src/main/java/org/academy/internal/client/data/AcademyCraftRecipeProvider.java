package org.academy.internal.client.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过 `RecipeProvider(HolderLookup.Provider, RecipeOutput)` + 自建 Runner 生成配方;
 * 1.21.1 基类构造签名与 Runner 协议不同。为优先打通 GUI 主链路, 此处保留 Runner 构造签名
 * (PackOutput, CompletableFuture) 以匹配 `GatherDataEvent.createProvider`, 配方生成整体跳过。
 */
public final class AcademyCraftRecipeProvider {
    private AcademyCraftRecipeProvider() {
    }

    public static class Runner implements DataProvider {
        private final CompletableFuture<HolderLookup.Provider> registries;

        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            this.registries = registries;
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
            return registries.thenCompose(unused -> CompletableFuture.completedFuture(null));
        }

        @Override
        public String getName() {
            return "AcademyCraft recipes (stubbed)";
        }
    }
}
