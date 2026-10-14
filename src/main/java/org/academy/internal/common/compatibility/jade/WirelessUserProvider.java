package org.academy.internal.common.compatibility.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;
import org.academy.api.common.wireless.WirelessUser;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;

public enum WirelessUserProvider implements
        IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(
            ITooltip tooltip,
            BlockAccessor accessor,
            IPluginConfig config
    ) {
        if (accessor.getServerData().contains("AcademyEnergyStored")
                && accessor.getServerData().contains("AcademyMaxEnergyStored")) {
            IElementHelper helper = IElementHelper.get();
            int current = accessor.getServerData().getInt("AcademyEnergyStored");
            int max = accessor.getServerData().getInt("AcademyMaxEnergyStored");
            float ratio = (float) current / max;
            Component text = Component.translatable("jade.academy.energy", current, max).withStyle(ChatFormatting.WHITE);
            ProgressStyle progressStyle = helper.progressStyle().color(0xFF25C4FF);
            tooltip.add(helper.progress(ratio, text, progressStyle, BoxStyle.getNestedBox(), true));
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof WirelessUser wirelessUser) {
            data.putInt("AcademyEnergyStored", wirelessUser.getEnergyStored());
            data.putInt("AcademyMaxEnergyStored", wirelessUser.getMaxEnergyStorage());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return AcademyCraft.academy("energy");
    }
}
