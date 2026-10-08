package org.academy.internal.client.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.academy.AcademyCraft;
import org.academy.internal.common.world.level.block.Blocks;

/**
 * 1.21.1 移植版 (基于 NeoForge 生成器框架)。
 *
 * 26.2 的 `ModelProvider` 暴露 `registerModels(BlockModelGenerators, ItemModelGenerators)`,
 * 可注册自定义贴图模型与 `ItemModelUtils.specialModel` 等 26.2 专属物品模型; 1.21.1 中
 * `net.minecraft.data.models.ModelProvider` 为具体自动生成类且无法为自定义方块定制,
 * 自 26.2 起移除的 `ItemModelUtils`/`ClientItem`/special model 数据管线也不存在。
 * 因此本类改用 1.21.1 NeoForge 的 `BlockStateProvider` + `ItemModelProvider` 组合,
 * 以可运行 `runData` 的方式为注册方块/物品产出 blockstate 与模型, 尽量贴近 26.2 的自定义贴图。
 */
public final class AcademyCraftModelProvider extends BlockStateProvider {
    public AcademyCraftModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, AcademyCraft.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // === 全方块单一贴图的方块 (26.2 的 CUBE_ALL 自定义贴图) ===
        simpleCubeTextured(Blocks.WIRELESS_NODE.get(), "block/break_w");
        simpleCubeTextured(Blocks.WIND_GEN_TOP.get(), "block/break_w");
        simpleCubeTextured(Blocks.WIND_GEN_PILLAR.get(), "block/break_w");
        simpleCubeTextured(Blocks.COMPRESSED_AIR_PLATFORM.get(), "block/break_w");
        simpleCubeTextured(Blocks.WIND_GEN_BASE.get(), "block/break_db");
        simpleCubeTextured(Blocks.OMNI_CRAFTING_TABLE.get(), "block/break_db");
        simpleCubeTextured(Blocks.SOLAR_GEN.get(), "block/break_db");
        simpleCubeTextured(Blocks.ABILITY_DEVELOPER.get(), "block/break_db");
        simpleCubeTextured(Blocks.IMAG_PHASE.get(), "block/black");
        simpleCubeTextured(Blocks.DARKMATTER_BLOCK.get(), "block/darkmatter");
        simpleCubeTextured(Blocks.CAT_ENGINE.get(), "item/cat_engine");

        // === 实验室墙板 (26.2 的 TexturedModel.COLUMN) ===
        simpleColumnBlock(Blocks.LABORATORY_LIGHT_WALL_PANEL.get());
        simpleColumnBlock(Blocks.LABORATORY_GRAY_WALL_PANEL.get());
        simpleColumnBlock(Blocks.LABORATORY_DARK_WALL_PANEL.get());

        // === 实验室砖 (26.2 的 TexturedModel.CUBE) ===
        simpleCubeOfName(Blocks.LABORATORY_LIGHT_BRICKS.get());
        simpleCubeOfName(Blocks.LABORATORY_DARK_BRICKS.get());

        // === 实验室格栅 (26.2 的 TexturedModel.CUBE) ===
        simpleCubeOfName(Blocks.LABORATORY_LIGHT_GRATE.get());
        simpleCubeOfName(Blocks.LABORATORY_GRAY_GRATE.get());
        simpleCubeOfName(Blocks.LABORATORY_DARK_GRATE.get());

        // === 金属活板门 ===
        trapdoorBlock(
                Blocks.LABORATORY_METAL_TRAPDOOR.get(),
                modLoc("block/laboratory_metal_trapdoor"),
                false
        );
        itemModels().withExistingParent(
                "laboratory_metal_trapdoor",
                modLoc("block/laboratory_metal_trapdoor_bottom")
        );

        // === 幻能植物 (26.2 的自定义六面 CUBE) ===
        {
            Block block = Blocks.IMAG_PHASE_VEGETATION.get();
            String name = name(block);
            ModelFile model = models().cube(
                    name,
                    mcLoc("block/deepslate_top"),
                    modLoc("block/imag_phase_vegetation_top"),
                    modLoc("block/imag_phase_vegetation_side"),
                    modLoc("block/imag_phase_vegetation_side"),
                    modLoc("block/imag_phase_vegetation_side"),
                    modLoc("block/imag_phase_vegetation_side")
            );
            simpleBlockWithItem(block, model);
        }

        // === 幻能树叶 (26.2 的 TexturedModel.LEAVES) ===
        simpleCubeOfName(Blocks.IMAG_PHASE_LEAVES.get());

        // === 幻能木头 (旋转柱, 26.2 的 CUBE_COLUMN + 水平变体) ===
        String logName = name(Blocks.IMAG_PHASE_LOG.get());
        ModelFile logSide = models().cubeColumn(
                logName,
                modLoc("block/imag_phase_log"),
                modLoc("block/imag_phase_log_top")
        );
        ModelFile logHorizontal = models().cubeColumnHorizontal(
                logName + "_horizontal",
                modLoc("block/imag_phase_log"),
                modLoc("block/imag_phase_log_top")
        );
        axisBlock(Blocks.IMAG_PHASE_LOG.get(), logSide, logHorizontal);

        // === 幻能地衣 (多面贴附, 兜底用简易 cross 模型) ===
        simpleBlockWithItem(
                Blocks.IMAG_PHASE_LICHEN.get(),
                models().cross("imag_phase_lichen", modLoc("block/imag_phase_lichen"))
        );

        // === 纯物品模型 ===
        flatItem("icon");
        flatItem("darkmatter");
        // 26.2 中幻能涂层复用暗物质的 item 贴图 (TextureMapping.layer0(DARKMATTER))
        flatItemTexture("darkmatter_coating", "item/darkmatter");
        handheldItem("darkmatter_tool");
        handheldItem("darkmatter_sword");
        handheldItem("darkmatter_spear");
        handheldItem("darkmatter_trident");
        handheldItem("darkmatter_bow");
        handheldItem("darkmatter_crossbow");
        handheldItem("darkmatter_mace");
        handheldItem("darkmatter_arrow");
        flatItem("darkmatter_feather");
        flatItem("imag_phase_ingot");
        flatItem("imag_phase_crystal");
        flatItem("imag_phase_polymer");
        flatItem("imag_phase_plate");
        flatItem("imag_phase_circuit");
        flatItem("needle");
        // 26.2 中风机底座屏幕复用 screen 的 item 贴图
        flatItemTexture("wind_gen_base_screen", "item/screen");
        // 26.2 中以下两项依赖 26.2 专属的 special model 渲染器 (1.21.1 不存在),
        // 此处不再生成模型以避免引用不存在的贴图。
        // handheldItem("imag_phase_dowsing_rod");
        // handheldItem("ability_control_tablet");
        flatItem("dark_matter_helmet");
        flatItem("dark_matter_chestplate");
        flatItem("dark_matter_leggings");
        flatItem("dark_matter_boots");
        flatItem("data_terminal");
        flatItem("tutorial");
        flatItem("coin");
        // 26.2 中纸飞机复用原版 paper 的 item 贴图
        flatItemVanillaTexture("paper_airplane", "item/paper");
        handheldItem("magnetic_hook");
        flatItem("empty_unit");
        flatItem("imag_phase_unit");
        flatItem("spatial_storage_unit");
        flatItem("wind_gen_fan");
    }

    private static String name(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    private void simpleCubeTextured(Block block, String texturePath) {
        simpleBlockWithItem(block, models().cubeAll(name(block), modLoc(texturePath)));
    }

    private void simpleCubeOfName(Block block) {
        simpleBlockWithItem(block, models().cubeAll(name(block), modLoc("block/" + name(block))));
    }

    private void simpleColumnBlock(Block block) {
        String name = name(block);
        simpleBlockWithItem(
                block,
                models().cubeColumn(name, modLoc("block/" + name + "_side"), modLoc("block/" + name + "_top"))
        );
    }

    private void flatItem(String name) {
        itemModels().singleTexture(name, mcLoc("item/generated"), "layer0", modLoc("item/" + name));
    }

    private void flatItemTexture(String name, String texturePath) {
        itemModels().singleTexture(name, mcLoc("item/generated"), "layer0", modLoc(texturePath));
    }

    private void flatItemVanillaTexture(String name, String texturePath) {
        itemModels().singleTexture(name, mcLoc("item/generated"), "layer0", mcLoc(texturePath));
    }

    private void handheldItem(String name) {
        itemModels().singleTexture(name, mcLoc("item/handheld"), "layer0", modLoc("item/" + name));
    }
}
