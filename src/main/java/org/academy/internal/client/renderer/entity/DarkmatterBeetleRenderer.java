package org.academy.internal.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.academy.api.client.resources.R;
import org.academy.internal.client.model.DarkmatterCreatureModel;
import org.academy.internal.common.world.entity.ability.DarkmatterBeetle;

/**
 * Compatibility-named renderer for the new composable creature model.
 */
public final class DarkmatterBeetleRenderer extends MobRenderer<DarkmatterBeetle, DarkmatterCreatureModel> {
    public DarkmatterBeetleRenderer(EntityRendererProvider.Context context) {
        super(context, new DarkmatterCreatureModel(
                DarkmatterCreatureModel.createBodyLayer().bakeRoot()), 0.45f);
    }

    @Override
    public ResourceLocation getTextureLocation(DarkmatterBeetle entity) {
        return R.textures.darkmatter_beetle;
    }
}
