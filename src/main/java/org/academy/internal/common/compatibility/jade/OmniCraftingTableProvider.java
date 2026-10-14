package org.academy.internal.common.compatibility.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;
import org.academy.internal.common.world.level.block.entity.OmniCraftingTableBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;

public enum OmniCraftingTableProvider implements
        IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(
            ITooltip tooltip,
            BlockAccessor accessor,
            IPluginConfig config
    ) {
        if (accessor.getServerData().contains("AcademyImagPhaseFluid")) {
            IElementHelper helper = IElementHelper.get();
            int current = accessor.getServerData().getInt("AcademyImagPhaseFluid");
            int max = OmniCraftingTableBlockEntity.MAX_FLUID_STORAGE;
            float ratio = (float) current / max;
            Component text = Component.translatable("jade.academy.omni_crafting_table", current, max).withStyle(ChatFormatting.WHITE);
            ProgressStyle progressStyle = helper.progressStyle().color(0x9908050D);
            tooltip.add(helper.progress(ratio, text, progressStyle, BoxStyle.getNestedBox(), true));
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        OmniCraftingTableBlockEntity blockEntity = (OmniCraftingTableBlockEntity) accessor.getBlockEntity();
        blockEntity = (OmniCraftingTableBlockEntity) blockEntity.getMain();
        if (blockEntity != null) {
            data.putInt("AcademyImagPhaseFluid", blockEntity.imagPhaseFluidStored);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return AcademyCraft.academy("omni_crafting_table");
    }
}
