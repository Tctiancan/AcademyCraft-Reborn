package org.com.mojang.blaze3d.shaders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.shaders.ShaderSource}.
 *
 * <p>26.2 以 {@code ShaderSource} 描述一张尚未写入 GPU 的着色器源, 供管线预编译时按
 * {@code ResourceLocation} 查询各阶段的 GLSL 源码; 1.21.1 无对应抽象, 编译期直接读取
 * 资源. 本垫层以一张记住源码的映射实现该契约, 使 {@link #get} 的既有调用保持成立.
 */
public interface ShaderSource {
    /**
     * 按 (唯一标识, 阶段) 返回对应 GLSL 源码; 不存在时返回 {@code null}.
     */
    @Nullable
    String get(ResourceLocation id, ShaderType type);

    /** 一个持有空源码集的默认实现, 供无需注册源码的调用方使用. */
    static ShaderSource empty() {
        return new Empty();
    }

    /** 将给定源码登记进一张以路径为键的映射的简易实现. */
    final class Empty implements ShaderSource {
        private final Map<String, String> sources = new ConcurrentHashMap<>();

        @Override
        @Nullable
        public String get(ResourceLocation id, ShaderType type) {
            return sources.get(id.getPath());
        }

        /** 登记一份以内容哈希为路径的源码, 返回其唯一标识. */
        public ResourceLocation register(String path, String source) {
            sources.put(path, source);
            return ResourceLocation.fromNamespaceAndPath("academy", path);
        }
    }
}
