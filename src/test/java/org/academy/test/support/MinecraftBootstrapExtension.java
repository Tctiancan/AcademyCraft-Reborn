package org.academy.test.support;

import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.LoadingModList;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Global JUnit extension registered via {@code META-INF/services/...} so that it is
 * applied to every test class before its tests run. It invokes
 * {@link Bootstrap#bootStrap()} exactly once so classes depending on the game's
 * registries (BuiltInRegistries, SoundEvents, Blocks, Items, RenderType, ItemStack
 * codecs, ...) can be exercised in a plain unit-test JVM.
 *
 * <p>Best-effort: a bootstrap failure is reported to stderr but never propagated, so
 * it cannot break tests that do not need the registries.
 */
public class MinecraftBootstrapExtension implements BeforeAllCallback {
    private static final AtomicBoolean BOOTSTRAPPED = new AtomicBoolean();

    @Override
    public void beforeAll(ExtensionContext context) {
        if (BOOTSTRAPPED.compareAndSet(false, true)) {
            try {
                initializeFeatureFlagsEnvironment();
                Bootstrap.bootStrap();
            } catch (Throwable t) {
                System.err.println("[MinecraftBootstrapExtension] Bootstrap.bootStrap() failed:");
                dumpChain(t, 0, false);
            }
        }
    }

    /**
     * In a plain unit-test JVM NeoForge's FML early-loading never runs, so
     * {@link LoadingModList#get()} is {@code null}. {@link net.minecraft.world.flag.FeatureFlags}
     * however calls {@code FeatureFlagLoader.loadModdedFlags} during its class initialisation,
     * which dereferences {@code LoadingModList.get().getModFiles()} and throws a
     * {@link NullPointerException}. That failure cascades into every registry-heavy class
     * (Blocks, Items, RenderType, ...) and breaks rendering tests before they run.
     *
     * <p>Seeder a {@link LoadingModList} with no mods (which, per its {@code of} factory, also
     * publishes itself as the singleton) so the feature-flag loop simply iterates over an empty
     * list and vanilla registries initialise normally.
     *
     * <p>The game version also has to be pinned: in an IDE/test runtime
     * {@code SharedConstants.CHECK_DATA_FIXER_SCHEMA} is {@code true}, so {@code EntityType}'s
     * class initialisation resolves choice types through {@code DataFixers}, which demands a
     * non-null current version. {@link DetectedVersion#BUILT_IN} carries the exact 1.21.1
     * constants (data version 3955, correct pack/protocol versions).
     */
    private static void initializeFeatureFlagsEnvironment() {
        try {
            SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        } catch (IllegalStateException alreadySet) {
            // A previous bootstrap (or the game itself) already chose a version; keep it.
        }
        if (LoadingModList.get() == null) {
            LoadingModList.of(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyMap());
        }
    }

    private static void dumpChain(Throwable t, int depth, boolean suppressed) {
        if (t == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        sb.append(suppressed ? "Suppressed: " : "Caused by: ");
        sb.append(t.getClass().getName());
        String msg = t.getMessage();
        if (msg != null) {
            sb.append(" [").append(msg).append("]");
        }
        System.err.println(sb);
        StackTraceElement[] frames = t.getStackTrace();
        int limit = Math.min(frames.length, 15);
        for (int i = 0; i < limit; i++) {
            StringBuilder s2 = new StringBuilder();
            for (int j = 0; j <= depth; j++) {
                s2.append("  ");
            }
            s2.append("    at ").append(frames[i]);
            System.err.println(s2);
        }
        dumpChain(t.getCause(), depth + 1, false);
        for (Throwable su : t.getSuppressed()) {
            dumpChain(su, depth + 1, true);
        }
    }
}
