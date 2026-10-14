package org.academy.internal.common.compatibility.jade;

import net.minecraft.world.level.block.BaseEntityBlock;
import org.academy.internal.common.world.level.block.OmniCraftingTableBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class JadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(OmniCraftingTableProvider.INSTANCE, OmniCraftingTableBlock.class);
        registration.registerBlockDataProvider(WirelessUserProvider.INSTANCE, BaseEntityBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(OmniCraftingTableProvider.INSTANCE, OmniCraftingTableBlock.class);
        registration.registerBlockComponent(WirelessUserProvider.INSTANCE, BaseEntityBlock.class);
    }
}
