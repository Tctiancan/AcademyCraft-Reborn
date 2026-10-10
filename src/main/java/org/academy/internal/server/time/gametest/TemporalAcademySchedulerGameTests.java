package org.academy.internal.server.time.gametest;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import org.academy.AcademyCraft;
import org.academy.api.common.ability.program.*;
import org.academy.api.server.time.*;
import org.academy.internal.common.ability.program.ProgramSessionScheduler;
import org.academy.internal.common.ability.program.ServerProgramScheduler;
import org.academy.internal.common.ability.program.compile.CompiledProgram;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/** End-to-end validation for owner-local Academy scheduler time. */
@EventBusSubscriber(modid = AcademyCraft.MOD_ID)
public final class TemporalAcademySchedulerGameTests {
    private static final ResourceLocation CATEGORY =
            AcademyCraft.academy("temporal_scheduler_test");
    private static final ResourceLocation ENTRY_NODE =
            AcademyCraft.academy("temporal_scheduler_test_entry");
    private static final ProgramNodeType<Unit> ENTRY_TYPE = entryType();
    private static final CompiledProgram IMMEDIATE_PROGRAM = immediateProgram();

    private TemporalAcademySchedulerGameTests() {
    }

    @SubscribeEvent
    private static void registerTests(RegisterGameTestsEvent event) {
        event.register(TemporalAcademySchedulerGameTests.class);
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void time_academy_scheduler_owner_pause(GameTestHelper helper) {
        runOwnerPauseTest(helper);
    }

    private static void runOwnerPauseTest(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        var pausedOwner = helper.spawn(EntityType.ZOMBIE, 1, 2, 1);
        var normalOwner = helper.spawn(EntityType.ZOMBIE, 3, 2, 1);
        var pausedKey = new ServerProgramScheduler.SessionKey(
                pausedOwner.getUUID(),
                CATEGORY,
                UUID.randomUUID()
        );
        var normalKey = new ServerProgramScheduler.SessionKey(
                normalOwner.getUUID(),
                CATEGORY,
                UUID.randomUUID()
        );
        var pausedTermination =
                new AtomicReference<ProgramSessionScheduler.TerminationKind>();
        var normalTermination =
                new AtomicReference<ProgramSessionScheduler.TerminationKind>();
        var lease = TemporalApi.get(server).acquireField(TemporalField.pause(
                TemporalScope.entities(Set.of(pausedOwner.getUUID())),
                Set.of(TemporalChannel.ACADEMY_SCHEDULER),
                TemporalPauseSource.ACADEMY_PAUSE
        ));

        try {
            helper.assertTrue(
                    start(server, pausedKey, pausedTermination),
                    "Paused owner program session did not start"
            );
            helper.assertTrue(
                    start(server, normalKey, normalTermination),
                    "Normal owner program session did not start"
            );
            helper.runAfterDelay(2L, () -> validateOwnerIsolation(
                    helper,
                    pausedOwner,
                    normalOwner,
                    pausedKey,
                    normalKey,
                    pausedTermination,
                    normalTermination,
                    lease
            ));
        } catch (RuntimeException | Error throwable) {
            cleanup(
                    server,
                    pausedOwner,
                    normalOwner,
                    pausedKey,
                    normalKey,
                    lease
            );
            throw throwable;
        }
    }

    private static void validateOwnerIsolation(
            GameTestHelper helper,
            Entity pausedOwner,
            Entity normalOwner,
            ServerProgramScheduler.SessionKey pausedKey,
            ServerProgramScheduler.SessionKey normalKey,
            AtomicReference<ProgramSessionScheduler.TerminationKind> pausedTermination,
            AtomicReference<ProgramSessionScheduler.TerminationKind> normalTermination,
            TemporalFieldLease lease
    ) {
        var server = helper.getLevel().getServer();
        try {
            helper.assertTrue(
                    pausedTermination.get() == null,
                    "Entity-local pause did not stop its Academy program session"
            );
            helper.assertValueEqual(
                    normalTermination.get(),
                    ProgramSessionScheduler.TerminationKind.COMPLETED,
                    "Entity-local pause leaked into another owner's program session"
            );
            lease.close();
            helper.runAfterDelay(2L, () -> {
                try {
                    helper.assertValueEqual(
                            pausedTermination.get(),
                            ProgramSessionScheduler.TerminationKind.COMPLETED,
                            "Academy program session did not resume after the field closed"
                    );
                    helper.succeed();
                } finally {
                    cleanup(
                            server,
                            pausedOwner,
                            normalOwner,
                            pausedKey,
                            normalKey,
                            lease
                    );
                }
            });
        } catch (RuntimeException | Error throwable) {
            cleanup(
                    server,
                    pausedOwner,
                    normalOwner,
                    pausedKey,
                    normalKey,
                    lease
            );
            throw throwable;
        }
    }

    private static boolean start(
            MinecraftServer server,
            ServerProgramScheduler.SessionKey key,
            AtomicReference<ProgramSessionScheduler.TerminationKind> termination
    ) {
        return ServerProgramScheduler.start(
                server,
                key,
                IMMEDIATE_PROGRAM,
                ignored -> null,
                null,
                2,
                20L,
                (unused830, result) -> termination.set(result.kind())
        );
    }

    private static void cleanup(
            MinecraftServer server,
            Entity pausedOwner,
            Entity normalOwner,
            ServerProgramScheduler.SessionKey pausedKey,
            ServerProgramScheduler.SessionKey normalKey,
            TemporalFieldLease lease
    ) {
        if (lease.isActive()) lease.close();
        ServerProgramScheduler.cancel(server, pausedKey);
        ServerProgramScheduler.cancel(server, normalKey);
        pausedOwner.discard();
        normalOwner.discard();
    }

    private static CompiledProgram immediateProgram() {
        var graphNode = new ProgramGraph.Node(
                0,
                ENTRY_NODE,
                1,
                new JsonObject()
        );
        var compiledNode = new CompiledProgram.CompiledNode(
                0,
                ENTRY_NODE,
                ENTRY_TYPE,
                Unit.INSTANCE,
                ProgramNodeRole.ENTRY,
                ProgramNodeSchema.EMPTY
        );
        return new CompiledProgram(
                new ProgramGraph(List.of(graphNode), List.of()),
                0,
                Map.of(0, compiledNode),
                List.of(),
                Map.of(),
                Map.of()
        );
    }

    private static ProgramNodeType<Unit> entryType() {
        return new ProgramNodeType<>() {
            @Override
            public Codec<Unit> configurationCodec() {
                return MapCodec.unit(Unit.INSTANCE).codec();
            }

            @Override
            public int schemaVersion() {
                return 1;
            }

            @Override
            public ProgramNodeSchema schema(Unit configuration) {
                return ProgramNodeSchema.EMPTY;
            }

            @Override
            public ProgramNodeRole role() {
                return ProgramNodeRole.ENTRY;
            }

            @Override
            public ProgramNodePurity purity() {
                return ProgramNodePurity.STATE;
            }

            @Override
            public ProgramNodeScope scope() {
                return ProgramNodeScope.COMMON;
            }
        };
    }

    private enum Unit {
        INSTANCE
    }
}
