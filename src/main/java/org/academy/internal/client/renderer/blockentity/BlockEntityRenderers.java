package org.academy.internal.client.renderer.blockentity;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.academy.internal.common.world.level.block.entity.BlockEntityTypes;

@EventBusSubscriber(Dist.CLIENT)
public final class BlockEntityRenderers {
    private BlockEntityRenderers() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityTypes.WIND_GEN_TOP.get(),
                unused224 -> WindGenTopRenderer.INSTANCE);
        event.registerBlockEntityRenderer(BlockEntityTypes.ABILITY_DEVELOPER.get(),
                unused225 -> AbilityDeveloperRenderer.INSTANCE
        );
        event.registerBlockEntityRenderer(BlockEntityTypes.WIRELESS_NODE.get(),
                unused226 -> WirelessNodeRenderer.INSTANCE);
        event.registerBlockEntityRenderer(BlockEntityTypes.OMNI_CRAFTING_TABLE.get(),
                unused227 -> OmniCraftingTableRenderer.INSTANCE);
        event.registerBlockEntityRenderer(BlockEntityTypes.CAT_ENGINE.get(),
                unused228 -> CatEngineRenderer.INSTANCE);
        event.registerBlockEntityRenderer(BlockEntityTypes.WIND_GEN_BASE.get(),
                unused229 -> WindGenBaseRenderer.INSTANCE);
        event.registerBlockEntityRenderer(BlockEntityTypes.WIND_GEN_PILLAR.get(),
                unused230 -> WindGenPillarRenderer.INSTANCE);
        event.registerBlockEntityRenderer(BlockEntityTypes.SOLAR_GEN.get(),
                unused231 -> SolarGenRenderer.INSTANCE);
    }
}
