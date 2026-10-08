package org.academy.internal.common.ability.teleport.chunk;

import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import org.academy.AcademyCraft;

/**
 * Holds the ticket type used to keep 区块跃迁 map views loaded.
 *
 * <p>The ticket loads chunks to FULL and lets them be read, but does not make them tick entities and —
 * unlike {@code TicketType.FORCED} — is never written to the level's {@code chunks.dat}, so opening
 * the map does not permanently mark the world explored.
 */
public final class ChunkLeapTickets {
    private static final String NAME = AcademyCraft.MOD_ID + ":chunk_leap_view";

    public static final TicketType<Unit> MAP_VIEW =
            TicketType.create(NAME, (a, b) -> 0);

    private ChunkLeapTickets() {
    }
}
