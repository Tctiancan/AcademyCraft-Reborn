package org.academy.internal.common.compatibility.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;
import org.academy.api.client.gui.screen.ContainerUiScreen;
import org.academy.api.client.gui.widget.Widget;
import org.academy.internal.client.gui.screen.OmniCraftingTableScreen;
import org.academy.internal.client.gui.screen.SolarGenScreen;
import org.academy.internal.client.gui.screen.WindGenScreen;
import org.academy.internal.client.gui.screen.WirelessNodeScreen;
import org.academy.internal.common.world.inventory.MenuTypes;
import org.academy.internal.common.world.inventory.OmniCraftingMenu;
import org.academy.internal.common.world.item.Items;
import org.academy.internal.common.world.level.material.ImagPhaseItemFusion;

import java.util.List;

@JeiPlugin
public final class JEIPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return AcademyCraft.academy("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new ImagPhaseFusionRecipeCategory(guiHelper),
                new OmniCraftingRecipeCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
                ImagPhaseFusionRecipeCategory.TYPE,
                ImagPhaseItemFusion.displayRecipes()
        );
        registration.addRecipes(
                OmniCraftingRecipeCategory.TYPE,
                List.of(OmniCraftingRecipeCategory.ABILITY_DEVELOPER)
        );
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // 普通 3x3 合成配方: 使用 JEI 内置 basic handler 转移.
        registration.addRecipeTransferHandler(
                OmniCraftingMenu.class,
                MenuTypes.OMNI_CRAFTING_TABLE.get(),
                RecipeTypes.CRAFTING,
                2,  // recipeSlotStart: 3x3 合成格起始槽(槽 2..10)
                9,  // recipeSlotCount: 3x3 合成格共 9 槽
                11, // inventorySlotStart: 玩家背包起始槽(槽 11..46)
                36  // inventorySlotCount: 背包 27 + 快捷栏 9 = 36 槽
        );
        // Ability Developer 专用配方: 配方界面含额外流体输入槽, 需自定义处理器过滤掉流体.
        registration.addRecipeTransferHandler(
                new OmniCraftingRecipeTransferHandler(registration.getTransferHelper()),
                OmniCraftingRecipeCategory.TYPE
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                Items.OMNI_CRAFTING_TABLE.get(), OmniCraftingRecipeCategory.TYPE
        );
        registration.addRecipeCatalyst(
                Items.IMAG_PHASE_UNIT.get(), ImagPhaseFusionRecipeCategory.TYPE
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(WindGenScreen.class,
                new IGuiContainerHandler<WindGenScreen>() {
                    @Override
                    public List<Rect2i> getGuiExtraAreas(WindGenScreen containerScreen) {
                        return guiAreas(containerScreen);
                    }
                }
        );
        registration.addGenericGuiContainerHandler(WirelessNodeScreen.class,
                new IGuiContainerHandler<WirelessNodeScreen>() {
                    @Override
                    public List<Rect2i> getGuiExtraAreas(WirelessNodeScreen containerScreen) {
                        return guiAreas(containerScreen);
                    }
                }
        );
        registration.addGenericGuiContainerHandler(SolarGenScreen.class,
                new IGuiContainerHandler<SolarGenScreen>() {
                    @Override
                    public List<Rect2i> getGuiExtraAreas(SolarGenScreen containerScreen) {
                        return guiAreas(containerScreen);
                    }
                }
        );
        registration.addGenericGuiContainerHandler(OmniCraftingTableScreen.class,
                new IGuiContainerHandler<OmniCraftingTableScreen>() {
                    @Override
                    public List<Rect2i> getGuiExtraAreas(OmniCraftingTableScreen containerScreen) {
                        return guiAreas(containerScreen);
                    }
                }
        );
    }

    private static List<Rect2i> guiAreas(ContainerUiScreen<?> containerScreen) {
        return containerScreen.getRoot().getChildren().values().stream()
                .filter(Widget::isVisible)
                .map(widget -> new Rect2i(
                        (int) widget.getAbsoluteX(), (int) widget.getAbsoluteY(),
                        (int) widget.getWidth(), (int) widget.getHeight()
                )).toList();
    }
}
