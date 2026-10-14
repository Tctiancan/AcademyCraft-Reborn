package org.academy.api.client.render;

import net.minecraft.resources.ResourceLocation;
import org.academy.api.client.render.texture.GpuSampler;
import org.academy.api.client.render.texture.GpuTextureView;
import org.jetbrains.annotations.Nullable;

/**
 * 移植自 26.2 `TextureBinding` (持 GpuTextureView/GpuSampler).
 * 1.21.1 无 GpuTextureView/GpuSampler, 改为持可绑定的 [ResourceLocation] 与 shader 采样槽.
 * [location] 为 null 时表示该槽位无需绑定 (控件仅依赖顶点颜色等).
 */
public record TextureBinding(String name, @Nullable ResourceLocation location, int samplerSlot) {
    public static final int DEFAULT_SLOT = 0;

    public TextureBinding(String name, @Nullable ResourceLocation location) {
        this(name, location, DEFAULT_SLOT);
    }

    /**
     * 兼容 26.2 语义的便捷构造: 从 [view] 提取其位置作为绑定目标.
     * 纹理采样槽从 [name] 推导 ("Sampler0"->0, "Sampler1"->1 ...), 以便多纹理绑定
     * (如 SkillProgressDrawCommand 的 Sampler0 轮廓 + Sampler1 径向遮罩) 各占独立 GL 单元;
     * 一律落到 0 会让后一个绑定覆盖前一个, 导致把径向遮罩误画在图标上方.
     */
    public TextureBinding(String name, @Nullable GpuTextureView view, GpuSampler sampler) {
        this(name, view == null ? null : view.location(), samplerSlotOf(name));
    }

    /**
     * 兼容桌面编辑器 (org.academy.desktop) 使用的 26.2 纹理视图/采样器类型:
     * 从 com.mojang 纹理视图提取资源路径作绑定目标, 采样槽同样从 [name] 推导.
     */
    public TextureBinding(String name, @Nullable org.academy.blaze3d.textures.GpuTextureView view,
                          org.academy.blaze3d.textures.GpuSampler sampler) {
        this(name, view == null ? null : view.location(), samplerSlotOf(name));
    }

    private static int samplerSlotOf(String name) {
        if (name == null) return DEFAULT_SLOT;
        int idx = name.toLowerCase().lastIndexOf("sampler");
        if (idx < 0) return DEFAULT_SLOT;
        int acc = 0;
        boolean hasDigit = false;
        for (int i = idx + "sampler".length(); i < name.length(); i++) {
            char c = name.charAt(i);
            if (c >= '0' && c <= '9') {
                hasDigit = true;
                acc = acc * 10 + (c - '0');
            } else if (hasDigit) {
                return acc;
            }
        }
        return hasDigit ? acc : DEFAULT_SLOT;
    }
}
