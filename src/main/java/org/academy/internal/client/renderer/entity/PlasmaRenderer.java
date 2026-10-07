package org.academy.internal.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.academy.internal.common.world.entity.skill.Plasma;

/**
 * Entity renderer intentionally stays empty; graph-authored VFX assets own the visual.
 */
public final class PlasmaRenderer extends EntityRenderer<Plasma> {
    public PlasmaRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Plasma entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}
