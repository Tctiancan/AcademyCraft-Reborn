package org.academy.internal.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.academy.internal.common.world.entity.skill.RailgunRay;

/** The resolved ray is visualized by the railgun_shot VFX graph. */
public final class RailgunRayRenderer extends EntityRenderer<RailgunRay> {
    public RailgunRayRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(RailgunRay entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}
