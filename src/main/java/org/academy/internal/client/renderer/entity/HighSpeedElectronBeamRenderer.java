package org.academy.internal.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.academy.internal.common.world.entity.skill.HighSpeedElectronBeam;

public class HighSpeedElectronBeamRenderer extends EntityRenderer<HighSpeedElectronBeam> {
    public HighSpeedElectronBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HighSpeedElectronBeam entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}
