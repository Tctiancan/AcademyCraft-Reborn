package org.academy;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.academy.internal.client.data.AcademyCraftClientData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod(AcademyCraft.MOD_ID)
public final class AcademyCraft {
    public static final String MOD_ID = "academy";
    public static final String MOD_NAME = "AcademyCraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ExecutorService executorService = Executors.newFixedThreadPool(2, runnable -> {
        var thread = new Thread(runnable, "academy-async");
        thread.setDaemon(true);
        return thread;
    });
    private static final StackWalker STACK_WALKER =
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    public static boolean DEBUG_UI = false;

    public AcademyCraft(IEventBus modEventBus) {
        AcademyCraftRegister.register(modEventBus);
        modEventBus.addListener(AcademyCraftClientData::dataSetup);
    }

    public static ResourceLocation custom(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ResourceLocation vanilla(String name) {
        return ResourceLocation.withDefaultNamespace(name);
    }

    public static ResourceLocation academy(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static Logger getLogger() {
        return LoggerFactory.getLogger("academy/" + STACK_WALKER.getCallerClass().getSimpleName());
    }
}
