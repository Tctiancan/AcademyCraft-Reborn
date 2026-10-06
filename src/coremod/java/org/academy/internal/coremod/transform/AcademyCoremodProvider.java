package org.academy.internal.coremod.transform;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Bytecode-only processors loaded before the game module layer is created. */
public final class AcademyCoremodProvider implements ITransformationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcademyCoremodProvider.class);

    @Override
    public String name() {
        return "academy_coremod";
    }

    @Override
    public void initialize(IEnvironment environment) {
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {
    }

    @Override
    public List<? extends ITransformer<?>> transformers() {
        return List.of(
                new ClassTransformer("academy:temporal_boundary", Set.of(
                        "net.minecraft.server.level.ServerLevel",
                        "net.minecraft.client.multiplayer.ClientLevel",
                        "net.minecraft.client.KeyboardHandler",
                        "net.minecraft.client.MouseHandler",
                        "org.academy.api.client.input.InputSystem")) {
                    @Override
                    protected void apply(ClassNode input) {
                        TemporalBoundaryTransformer.apply(input);
                    }
                },
                new ClassTransformer("academy:world_weaver_config", Set.of(
                        "net.minecraft.world.level.levelgen.feature.EndSpikeFeature",
                        "net.minecraft.world.level.levelgen.feature.EndSpikeFeature$EndSpike",
                        "net.minecraft.world.level.levelgen.feature.EndPodiumFeature")) {
                    @Override
                    protected void apply(ClassNode input) {
                        WorldWeaverConfigTransformer.apply(input);
                    }
                },
                new ClassTransformer("academy:health_read_inliner", Set.of(
                        "net.minecraft.world.entity.LivingEntity")) {
                    @Override
                    protected void apply(ClassNode input) {
                        HealthReadInliner.apply(input);
                        VectorHealthReadTransformer.apply(input);
                    }
                });
    }

    private abstract static class ClassTransformer implements ITransformer<ClassNode> {
        private final String[] labels;
        private final Set<String> classNames;

        ClassTransformer(String label, Set<String> classNames) {
            Objects.requireNonNull(label);
            this.labels = new String[]{label};
            this.classNames = classNames;
        }

        protected abstract void apply(ClassNode input);

        @Override
        public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
            try {
                apply(input);
            } catch (Exception e) {
                LOGGER.error("Failed to apply {} transform to {}", labels[0], input.name, e);
                throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
            }
            return input;
        }

        @Override
        public TransformerVoteResult castVote(ITransformerVotingContext context) {
            return TransformerVoteResult.YES;
        }

        @Override
        public TargetType<ClassNode> getTargetType() {
            return TargetType.CLASS;
        }

        @Override
        public Set<ITransformer.Target<ClassNode>> targets() {
            return classNames.stream().map(ITransformer.Target::targetClass).collect(Collectors.toSet());
        }
    }
}
