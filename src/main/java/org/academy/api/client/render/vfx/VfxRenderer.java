package org.academy.api.client.render.vfx;

import java.util.List;

/**
 * @deprecated author an output block and shader for
 * {@link org.academy.api.client.render.vfxgraph.render.VfxGraphRenderer}
 */
@Deprecated(since = "0.0.4")
public interface VfxRenderer<T extends VfxRenderData> {
    void init();

    void render(VfxRenderContext ctx, List<? extends T> data);

    default void close() {
    }
}
