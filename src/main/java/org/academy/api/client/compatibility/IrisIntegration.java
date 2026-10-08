package org.academy.api.client.compatibility;

import net.irisshaders.iris.api.v0.IrisApi;
import net.neoforged.fml.ModList;

public final class IrisIntegration {
    private static boolean hasIris;

    private IrisIntegration() {
    }

    public static void init() {
        var modList = ModList.get();
        if (modList != null) {
            hasIris = modList.isLoaded("iris");
        }
    }

    public static boolean hasIris() {
        return hasIris;
    }

    public static boolean isShaderPackInUse() {
        return hasIris() && IrisApi.getInstance().isShaderPackInUse();
    }
}
