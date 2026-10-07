package org.academy.internal.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.academy.internal.common.world.entity.skill.ArcEffect;

public class ArcEffectRenderer extends EntityRenderer<ArcEffect> {
    public ArcEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ArcEffect entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}
