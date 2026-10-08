package org.academy.desktop.launch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 桌面 Shader Graph 编辑器入口。
 *
 * 26.2 中借助 {@code net.neoforged.fml.startup.Entrypoint/FatalErrorReporting} 启动 NeoForge 客户端；
 * 1.21.1 (C-stun) 下编辑器是独立桌面应用, 直接委托给 {@code GraphEditorMainKt} 即可。
 */
public final class EditorEntrypoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditorEntrypoint.class);
    private static final String DEFAULT_MAIN =
            System.getProperty("academy.desktop.main", "org.academy.desktop.grapheditor.GraphEditorMainKt");

    private EditorEntrypoint() {
    }

    public static void main(String[] args) {
        try {
            Class<?> mainClass = Class.forName(DEFAULT_MAIN);
            mainClass.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (Throwable t) {
            LOGGER.error("Fatal error launching desktop editor", t);
            System.exit(1);
        }
    }
}
