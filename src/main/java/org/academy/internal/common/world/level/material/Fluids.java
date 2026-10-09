package org.academy.internal.common.world.level.material;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Consumer;

import static org.academy.AcademyCraft.MOD_ID;

public final class Fluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MOD_ID);
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> IMAG_PHASE_TYPE =
            FLUID_TYPES.register("imag_phase", Fluids::createFluidType);

    public static final DeferredHolder<Fluid, ImagPhaseFluid.Source> IMAG_PHASE =
            FLUIDS.register("imag_phase", ImagPhaseFluid.Source::new);
    public static final DeferredHolder<Fluid, ImagPhaseFluid.Flowing> FLOWING_IMAG_PHASE =
            FLUIDS.register("flowing_imag_phase", ImagPhaseFluid.Flowing::new);

    // 1.21.1 用 IClientFluidTypeExtensions 提供流体渲染, 等价于 26.2 的 RegisterFluidModelsEvent+FluidModel.Unbaked.
    // 必须给出 still/flowing 纹理, 否则默认扩展返回 null, FluidSpriteCache.getFluidSprites 会对 null 键 NPE 崩溃.
    // 沿用 26.2 水的贴图与恒定色调 0x9908050D 以保持外观.
    public static FluidType createFluidType() {
        return new FluidType(
                FluidType.Properties.create()
                        .descriptionId("fluid.academy.imag_phase")
                        .canSwim(true)
                        .canPushEntity(true)
        ) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                ResourceLocation still = ResourceLocation.withDefaultNamespace("block/water_still");
                ResourceLocation flowing = ResourceLocation.withDefaultNamespace("block/water_flow");
                ResourceLocation overlay = ResourceLocation.withDefaultNamespace("block/water_overlay");
                int tint = 0x9908050D;
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return still;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return flowing;
                    }

                    @Override
                    public ResourceLocation getOverlayTexture() {
                        return overlay;
                    }

                    @Override
                    public int getTintColor() {
                        return tint;
                    }
                });
            }
        };
    }

    private Fluids() {
    }
}
