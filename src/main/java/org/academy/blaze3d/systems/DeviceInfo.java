package org.academy.blaze3d.systems;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.systems.DeviceInfo}.
 *
 * <p>26.2 以 {@code DeviceInfo} 描述 GPU 供应商/渲染器/版本等设备静态信息; 1.21.1 通过
 * {@code GL11.glGetString} 直接读取. 本垫层保留符号, 使既有形如
 * {@code device.getDeviceInfo().glRenderer()} 的调用保持成立.
 */
public final class DeviceInfo {
    private final String glVendor;
    private final String glRenderer;
    private final String glVersion;

    public DeviceInfo(String glVendor, String glRenderer, String glVersion) {
        this.glVendor = glVendor;
        this.glRenderer = glRenderer;
        this.glVersion = glVersion;
    }

    public String glVendor() {
        return glVendor;
    }

    public String glRenderer() {
        return glRenderer;
    }

    public String glVersion() {
        return glVersion;
    }
}
