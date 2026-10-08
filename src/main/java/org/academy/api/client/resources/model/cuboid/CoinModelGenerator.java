package org.academy.api.client.resources.model.cuboid;

import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static org.academy.AcademyCraft.academy;

/**
 * 26.2 的 coin 特殊模型 (cuboid) 在 1.21.1 移植为 {@link UnbakedModel#bake}。
 * 生成一个 16x16x1 的立方体: 正面(SOUTH)/背面(NORTH) 使用 front/back 材质, 侧面沿用
 * 1.21.1 {@code ItemModelGenerator} 的 span 扫描算法生成的边缘条带。
 */
public class CoinModelGenerator implements UnbakedModel {
    public static final ResourceLocation COIN_ITEM_MODEL_ID = academy("builtin/coin");
    public static final CoinModelGenerator INSTANCE = new CoinModelGenerator();

    public static final String TEXTURE_KEY_FRONT = "front";
    public static final String TEXTURE_KEY_BACK = "back";

    private static final float MIN_Z = 7.5F;
    private static final float MAX_Z = 8.5F;

    private static final Material FRONT_MATERIAL =
            new Material(TextureAtlas.LOCATION_BLOCKS, academy("item/coin_front"));
    private static final Material BACK_MATERIAL =
            new Material(TextureAtlas.LOCATION_BLOCKS, academy("item/coin_back"));

    private CoinModelGenerator() {
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return List.of();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
    }

    @Override
    public @Nullable BakedModel bake(
            ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState state
    ) {
        var frontSprite = spriteGetter.apply(FRONT_MATERIAL);
        var backSprite = spriteGetter.apply(BACK_MATERIAL);
        if (frontSprite == null || backSprite == null) return null;

        var faceBroker = new FaceBakery();
        var builder = new SimpleBakedModel.Builder(
                false, false, true, ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY
        );
        builder.particle(frontSprite);
        var from = new Vector3f(0.0F, 0.0F, MIN_Z);
        var to = new Vector3f(16.0F, 16.0F, MAX_Z);

        var frontFace = new BlockElementFace(null, 0, TEXTURE_KEY_FRONT,
                new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0));
        builder.addUnculledFace(faceBroker.bakeQuad(
                from, to, frontFace, frontSprite, Direction.SOUTH, state, null, false
        ));

        var backFace = new BlockElementFace(null, 0, TEXTURE_KEY_BACK,
                new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0));
        builder.addUnculledFace(faceBroker.bakeQuad(
                from, to, backFace, backSprite, Direction.NORTH, state, null, false
        ));

        addSideFaces(builder, faceBroker, frontSprite, state);

        return builder.build();
    }

    private static void addSideFaces(
            SimpleBakedModel.Builder builder,
            FaceBakery faceBroker,
            TextureAtlasSprite sprite,
            ModelState state
    ) {
        var contents = sprite.contents();
        int cw = contents.width();
        int ch = contents.height();
        List<Span> spans = new ArrayList<>();

        contents.getUniqueFrames().forEach(frame -> {
            for (int row = 0; row < ch; row++) {
                for (int col = 0; col < cw; col++) {
                    boolean opaque = !isTransparent(contents, frame, col, row, cw, ch);
                    checkTransition(Side.UP, spans, contents, frame, col, row, cw, ch, opaque);
                    checkTransition(Side.DOWN, spans, contents, frame, col, row, cw, ch, opaque);
                    checkTransition(Side.LEFT, spans, contents, frame, col, row, cw, ch, opaque);
                    checkTransition(Side.RIGHT, spans, contents, frame, col, row, cw, ch, opaque);
                }
            }
        });

        for (var span : spans) {
            float f10 = 16.0F / cw;
            float f11 = 16.0F / ch;
            float f12 = span.min;
            float f13 = span.max;
            float f14 = span.anchor;
            float f2 = 0.0F, f3 = 0.0F, f4 = 0.0F, f5 = 0.0F;
            float f6 = 0.0F, f7 = 0.0F, f8 = 0.0F, f9 = 0.0F;

            switch (span.facing) {
                case UP -> {
                    f6 = f12; f2 = f12; f4 = f7 = f13 + 1.0F;
                    f8 = f14; f3 = f14; f5 = f14; f9 = f14 + 1.0F;
                }
                case DOWN -> {
                    f8 = f14; f9 = f14 + 1.0F;
                    f6 = f12; f2 = f12; f4 = f7 = f13 + 1.0F;
                    f3 = f14 + 1.0F; f5 = f14 + 1.0F;
                }
                case LEFT -> {
                    f6 = f14; f2 = f14; f4 = f14; f7 = f14 + 1.0F;
                    f9 = f12; f3 = f12; f5 = f8 = f13 + 1.0F;
                }
                case RIGHT -> {
                    f6 = f14; f7 = f14 + 1.0F;
                    f2 = f14 + 1.0F; f4 = f14 + 1.0F;
                    f9 = f12; f3 = f12; f5 = f8 = f13 + 1.0F;
                }
            }

            f2 *= f10;
            f4 *= f10;
            f3 *= f11;
            f5 *= f11;
            f3 = 16.0F - f3;
            f5 = 16.0F - f5;
            f6 *= f10;
            f7 *= f10;
            f8 *= f11;
            f9 *= f11;

            Direction dir = span.facing.direction;
            var face = new BlockElementFace(null, 0, TEXTURE_KEY_FRONT,
                    new BlockFaceUV(new float[]{f6, f8, f7, f9}, 0));
            builder.addUnculledFace(faceBroker.bakeQuad(
                    fromOf(span.facing, f2, f3, f4, f5),
                    toOf(span.facing, f2, f3, f4, f5),
                    face, sprite, dir, state, null, false
            ));
        }
    }

    private static Vector3f fromOf(Side side, float f2, float f3, float f4, float f5) {
        return switch (side) {
            case UP -> new Vector3f(f2, f3, MIN_Z);
            case DOWN -> new Vector3f(f2, f5, MIN_Z);
            case LEFT -> new Vector3f(f2, f3, MIN_Z);
            case RIGHT -> new Vector3f(f4, f3, MIN_Z);
        };
    }

    private static Vector3f toOf(Side side, float f2, float f3, float f4, float f5) {
        return switch (side) {
            case UP -> new Vector3f(f4, f3, MAX_Z);
            case DOWN -> new Vector3f(f4, f5, MAX_Z);
            case LEFT -> new Vector3f(f2, f5, MAX_Z);
            case RIGHT -> new Vector3f(f4, f5, MAX_Z);
        };
    }

    private static void checkTransition(
            Side side,
            List<Span> spans,
            SpriteContents contents,
            int frame,
            int pixelX,
            int pixelY,
            int cw,
            int ch,
            boolean opaque
    ) {
        boolean flag = isTransparent(contents, frame, pixelX + side.xOffset, pixelY + side.yOffset, cw, ch) && opaque;
        if (flag) {
            Span span = null;
            for (Span s : spans) {
                if (s.facing == side) {
                    int anchor = side.isHorizontal() ? pixelY : pixelX;
                    if (s.anchor == anchor) {
                        span = s;
                        break;
                    }
                }
            }
            int coord = side.isHorizontal() ? pixelX : pixelY;
            if (span == null) {
                int anchor = side.isHorizontal() ? pixelY : pixelX;
                spans.add(new Span(side, coord, anchor));
            } else {
                span.expand(coord);
            }
        }
    }

    private static boolean isTransparent(
            SpriteContents contents, int frame, int x, int y, int cw, int ch
    ) {
        return x >= 0 && y >= 0 && x < cw && y < ch && contents.isTransparent(frame, x, y);
    }

    private enum Side {
        UP(Direction.UP, 0, -1),
        DOWN(Direction.DOWN, 0, 1),
        LEFT(Direction.EAST, -1, 0),
        RIGHT(Direction.WEST, 1, 0);

        private final Direction direction;
        private final int xOffset;
        private final int yOffset;

        Side(Direction direction, int xOffset, int yOffset) {
            this.direction = direction;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        boolean isHorizontal() {
            return this == DOWN || this == UP;
        }
    }

    private static final class Span {
        private final Side facing;
        private int min;
        private int max;
        private final int anchor;

        Span(Side facing, int minMax, int anchor) {
            this.facing = facing;
            this.min = minMax;
            this.max = minMax;
            this.anchor = anchor;
        }

        void expand(int pos) {
            if (pos < this.min) {
                this.min = pos;
            } else if (pos > this.max) {
                this.max = pos;
            }
        }
    }
}
