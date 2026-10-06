package org.academy.internal.common.skilldata;

import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;

public class CommonSkillData extends SkillData {
    public static final ResourceLocation ID = AcademyCraft.academy("common");

    public CommonSkillData() {
    }

    @Override
    public ResourceLocation getType() {
        return ID;
    }
}
