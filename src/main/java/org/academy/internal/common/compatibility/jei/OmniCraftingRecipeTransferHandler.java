package org.academy.internal.common.compatibility.jei;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.academy.internal.common.world.inventory.MenuTypes;
import org.academy.internal.common.world.inventory.OmniCraftingMenu;

import java.util.List;
import java.util.Optional;

/**
 * Ability Developer 专用配方(OmniCraftingRecipeCategory.TYPE)的 JEI 转移处理器.
 *
 * 该配方界面有 10 个输入槽(3x3 合成格 + 1 个 imag phase 流体槽),
 * 而 OmniCraftingTable 的合成格只有 9 格. JEI 内置的 basic handler 要求输入槽数不得
 * 超过合成格数, 因此这里自定义一个处理器: 仅保留 INPUT 角色的物品输入槽(过滤掉流体槽),
 * 再委托给 JEI 默认的转移逻辑填入 3x3 合成格.
 */
public final class OmniCraftingRecipeTransferHandler
        implements IRecipeTransferHandler<OmniCraftingMenu, OmniCraftingRecipeCategory.DisplayRecipe> {

    private static final int CRAFT_SLOT_START = 2;
    private static final int CRAFT_SLOT_COUNT = 9;
    private static final int INVENTORY_SLOT_START = 11;
    private static final int INVENTORY_SLOT_COUNT = 36;

    private final IRecipeTransferHandlerHelper helper;
    private final IRecipeTransferHandler<OmniCraftingMenu, OmniCraftingRecipeCategory.DisplayRecipe> delegate;

    public OmniCraftingRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
        this.delegate = helper.createUnregisteredRecipeTransferHandler(
                helper.createBasicRecipeTransferInfo(
                        OmniCraftingMenu.class,
                        MenuTypes.OMNI_CRAFTING_TABLE.get(),
                        OmniCraftingRecipeCategory.TYPE,
                        CRAFT_SLOT_START,
                        CRAFT_SLOT_COUNT,
                        INVENTORY_SLOT_START,
                        INVENTORY_SLOT_COUNT
                )
        );
    }

    @Override
    public Class<? extends OmniCraftingMenu> getContainerClass() {
        return OmniCraftingMenu.class;
    }

    @Override
    public Optional<MenuType<OmniCraftingMenu>> getMenuType() {
        return Optional.of(MenuTypes.OMNI_CRAFTING_TABLE.get());
    }

    @Override
    public RecipeType<OmniCraftingRecipeCategory.DisplayRecipe> getRecipeType() {
        return OmniCraftingRecipeCategory.TYPE;
    }

    @Override
    public IRecipeTransferError transferRecipe(
            IRecipeTransferContext<OmniCraftingRecipeCategory.DisplayRecipe, OmniCraftingMenu> context,
            boolean doTransfer
    ) {
        var result = delegate.transferRecipe(
                context.getContainer(),
                context.getRecipe(),
                filterToItemInputs(context.getRecipeSlots()),
                context.getPlayer(),
                context.isMaxTransfer(),
                doTransfer
        );
        if (doTransfer) {
            context.completeRecipeTransfer(
                    result == null ? RecipeTransferResult.SUCCESS : RecipeTransferResult.REJECTED
            );
        }
        return result;
    }

    @Override
    public IRecipeTransferError transferRecipe(
            OmniCraftingMenu container,
            OmniCraftingRecipeCategory.DisplayRecipe recipe,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer
    ) {
        // 旧版签名: 由新版 context 签名委托, 此处不做 completeRecipeTransfer 上报.
        return delegate.transferRecipe(
                container,
                recipe,
                filterToItemInputs(recipeSlots),
                player,
                maxTransfer,
                doTransfer
        );
    }

    private IRecipeSlotsView filterToItemInputs(IRecipeSlotsView recipeSlots) {
        List<IRecipeSlotView> itemInputs = recipeSlots.getSlotViews().stream()
                .filter(slot -> slot.getRole() == RecipeIngredientRole.INPUT)
                .filter(slot -> slot.getItemStacks().findAny().isPresent())
                .toList();
        return helper.createRecipeSlotsView(itemInputs);
    }
}
