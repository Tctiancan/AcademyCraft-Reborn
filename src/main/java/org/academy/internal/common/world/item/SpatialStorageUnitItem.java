package org.academy.internal.common.world.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.academy.internal.server.storage.SpatialStorageSavedData;
import org.academy.internal.server.storage.SpatialStorageService;

import java.util.List;

public final class SpatialStorageUnitItem extends Item {
    public SpatialStorageUnitItem(Properties properties) {
        super(properties.stacksTo(1).component(ItemDataComponents.SPATIAL_STORAGE_ENABLED.get(), false));
    }

    public static boolean isEnabled(ItemStack stack) {
        return stack.getItem() instanceof SpatialStorageUnitItem
                && stack.getOrDefault(ItemDataComponents.SPATIAL_STORAGE_ENABLED.get(), false);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isEnabled(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) toggle(serverPlayer, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand();
        if (!context.isSecondaryUseActive()) return InteractionResult.PASS;
        var level = context.getLevel();
        var player = context.getPlayer();
        var pos = context.getClickedPos();
        if (player == null || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), stack)) return InteractionResult.FAIL;
        var destination = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, context.getClickedFace());
        if (destination == null) return InteractionResult.PASS;
        if (player instanceof ServerPlayer serverPlayer) {
            var data = SpatialStorageSavedData.get(serverPlayer.level().getServer());
            long moved = data.transfer(SpatialStorageService.id(stack), Integer.MAX_VALUE, (resource, count) -> {
                int inserted = 0;
                var toInsert = resource.copyWithCount((int) Math.min(count, Integer.MAX_VALUE));
                for (int slot = 0; slot < destination.getSlots() && !toInsert.isEmpty(); slot++) {
                    var remainder = destination.insertItem(slot, toInsert, false);
                    inserted += toInsert.getCount() - remainder.getCount();
                    toInsert = remainder;
                }
                return inserted;
            });
            serverPlayer.sendSystemMessage(Component.translatable(
                    "item.academy.spatial_storage_unit.transferred", moved,
                    data.count(SpatialStorageService.id(stack))), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void toggle(ServerPlayer player, ItemStack stack) {
        SpatialStorageService.id(stack);
        stack.set(ItemDataComponents.SPATIAL_STORAGE_ENABLED.get(), !isEnabled(stack));
        player.sendSystemMessage(Component.translatable(isEnabled(stack)
                ? "item.academy.spatial_storage_unit.enabled" : "item.academy.spatial_storage_unit.disabled"), true);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(isEnabled(stack)
                ? "item.academy.spatial_storage_unit.enabled" : "item.academy.spatial_storage_unit.disabled")
                .withStyle(isEnabled(stack) ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        for (int line = 1; line <= 4; line++) tooltipComponents.add(Component.translatable(
                "item.academy.spatial_storage_unit.tooltip." + line).withStyle(ChatFormatting.GRAY));
    }
}
