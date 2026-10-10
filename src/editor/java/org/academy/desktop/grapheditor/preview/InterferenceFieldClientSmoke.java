package org.academy.desktop.grapheditor.preview;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.academy.api.client.render.post.DirectionalFieldRenderer;
import org.academy.api.client.render.post.WorldSurfaceMasks;
import org.academy.api.common.vfx.DirectionalArea;
import org.academy.api.server.ability.AbilitySystemServer;
import org.academy.api.server.ability.DarkmatterGraphEffects;
import org.academy.internal.client.render.vfx.InterferenceFieldClient;
import org.academy.internal.common.ability.Skills;
import org.academy.internal.common.ability.darkmatter.skills.lv3.DarkmatterRadiation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/** Reproducible GPU and gameplay checks in the existing isolated VFX test world. */
@EventBusSubscriber(modid = "academy", value = Dist.CLIENT)
public final class InterferenceFieldClientSmoke {
    private static boolean opened, configured;
    private static volatile boolean ready;
    private static volatile Throwable serverFailure;
    private static int ticks, total;
    private static float yaw = -90, pitch = 12;
    private static long framesBefore;
    private static java.util.concurrent.CompletableFuture<Void> reloading;
    private static long lastSyntheticTick;
    private static boolean exiting, finished;
    private static net.minecraft.world.entity.animal.Cow lifecycleActor;
    private static final DirectionalArea AREA = new DirectionalArea(new Vec3(0, 83, 178), new Vec3(0, 0, 1),
            new DirectionalArea.Cone(16, Math.cos(Math.toRadians(25))),
            new DirectionalArea.Cone(12, Math.cos(Math.toRadians(52))));

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("academy.interferenceFieldSmoke") || finished) return;
        var mc = Minecraft.getInstance();
        if (serverFailure != null) throw new IllegalStateException("Field server check failed", serverFailure);
        if (++total > 3000) throw new IllegalStateException("Field capture timed out");
        if (mc.level == null) {
            if (exiting) {
                InterferenceFieldClient.tick();
                require(InterferenceFieldClient.activeCount()==0&&!WorldSurfaceMasks.active(),
                        "Leaving the world clears an active field and its textures");
                finished=true;
                System.out.println("[interference-field-smoke] PASSED"); mc.stop(); return;
            }
            if (mc.screen instanceof net.neoforged.neoforge.client.gui.LoadingErrorScreen warning) {
                for (var widget : warning.children()) if (widget instanceof net.minecraft.client.gui.components.Button button
                        && button.getMessage().getString().equals("Proceed to main menu")) {
                    button.onPress();
                    break;
                }
            }
            if (!opened && mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen) {
                opened = true; mc.createWorldOpenFlows().openWorld("railgun", mc::stop);
            }
            return;
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.player.setYRot(yaw); mc.player.yRotO = yaw;
        mc.player.setXRot(pitch); mc.player.xRotO = pitch;
        if (!configured) {
            configured = true;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            server(p -> {
                for (int x=-24;x<=24;x++) for(int z=164;z<=210;z++) {
                    p.level().setBlock(new BlockPos(x,80,z), ((x+z)&1)==0?Blocks.SMOOTH_STONE.defaultBlockState():Blocks.DEEPSLATE.defaultBlockState(),3);
                    for(int y=81;y<=88;y++) p.level().setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
                }
                for (var old : p.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, p.getBoundingBox().inflate(100))) old.discard();
                for (var old : p.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, p.getBoundingBox().inflate(100))) old.discard();
                for (var old : p.level().getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class, p.getBoundingBox().inflate(100))) old.discard();
                p.setGameMode(GameType.CREATIVE); p.getInventory().clearContent();
                p.getAbilities().flying = true; p.onUpdateAbilities();
                SmokePort.tp(p,-13,84,187,yaw,pitch);
                command(p,"time set noon"); command(p,"weather clear");
                for(int y=81;y<=84;y++) for(int z=186;z<=190;z++) p.level().setBlock(new BlockPos(-3,y,z),Blocks.GLASS.defaultBlockState(),3);
                for(int x=2;x<=6;x++) for(int z=185;z<=189;z++) p.level().setBlock(new BlockPos(x,81,z),
                        x==2||x==6||z==185||z==189?Blocks.STONE_BRICKS.defaultBlockState():Blocks.WATER.defaultBlockState(),3);
                for(int y=81;y<=86;y++) p.level().setBlock(new BlockPos(0,y,195),Blocks.STONE_BRICKS.defaultBlockState(),3);
                for(var position:java.util.List.of(new Vec3(0,81,188),new Vec3(8,81,187),new Vec3(-6,81,190))) {
                    var cow=new net.minecraft.world.entity.animal.Cow(net.minecraft.world.entity.EntityType.COW,p.level());
                    cow.setPos(position);cow.setNoAi(true);cow.setNoGravity(true);p.level().addFreshEntity(cow);
                }
                var item=new net.minecraft.world.entity.item.ItemEntity(p.level(),2,82,187,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
                item.setNoGravity(true);item.setDeltaMovement(Vec3.ZERO);item.setUnlimitedLifetime();p.level().addFreshEntity(item);
                learn(p,Skills.DARKMATTER_GENERATION.get(),Skills.DARKMATTER_SHAPING.get(),Skills.DARKMATTER_PHASE_TUNING.get(),Skills.DARKMATTER_RADIATION.get());
                ready=true;
            });
        }
        if (!ready) return;
        ticks++;
        if(ticks==25) { capture("outside_before"); framesBefore=DirectionalFieldRenderer.renderedFrames(); }
        if(ticks>=35&&ticks<=155&&ticks%5==0) server(p -> DarkmatterGraphEffects.interference(p,AREA));
        if(ticks==55) {
            require(InterferenceFieldClient.activeCount()==1,"One authoritative field is maintained");
            require(DirectionalFieldRenderer.renderedFrames()>framesBefore,"Surface and air passes rendered");
            capture("outside_side");
        }
        if(ticks==65) { yaw=0;pitch=8;server(p -> SmokePort.tp(p,0,81.4,178.5,yaw,pitch)); }
        if(ticks==80) capture("inside");
        if(ticks==90) { yaw=180;pitch=15;server(p -> SmokePort.tp(p,0,85,203,yaw,pitch)); }
        if(ticks==105) capture("outside_front");
        if(ticks==110) { yaw=-90;pitch=12;server(p -> {SmokePort.tp(p,-13,84,187,yaw,pitch);command(p,"time set midnight");}); }
        if(ticks==125) capture("outside_night");
        if(ticks==130) server(p -> {
            command(p,"time set noon");
            for(int x=-8;x<=8;x++) for(int z=178;z<=196;z++) p.level().setBlock(new BlockPos(x,88,z),Blocks.STONE.defaultBlockState(),3);
        });
        if(ticks==145) capture("indoor");
        if(ticks==160) server(DarkmatterGraphEffects::stopInterference);
        if(ticks==180) {
            require(InterferenceFieldClient.activeCount()==0,"Explicit stop clears range rendering");
            require(InterferenceFieldClient.activeCount()==0&&!WorldSurfaceMasks.active(),
                    "Stopping releases scene mask textures"); capture("stopped");
        }
        if(ticks==185) server(p -> DarkmatterGraphEffects.interference(p,AREA));
        if(ticks==205) require(InterferenceFieldClient.activeCount()==0,"Missing updates expire the field");
        if(ticks==210) {
            yaw=0;pitch=0;
            server(p -> {
                SmokePort.tp(p,0,81.4,178,yaw,pitch);
                var manager=AbilitySystemServer.getSystem(p).getDarkmatterResourceManager();
                manager.debugSetPools(p,100,0,0,0);manager.setAlphaPoints(p,150);
                require(DarkmatterRadiation.Server.beginChannel(p),"Real interference channel starts");
            });
        }
        if(ticks==220) {require(InterferenceFieldClient.activeCount()==1,"Actual skill synchronizes its two sectors");capture("actual_skill");}
        if(ticks==225) server(DarkmatterRadiation.Server::endChannel);
        if(ticks==245) {
            require(InterferenceFieldClient.activeCount()==0,"Actual channel stop clears the field");
            yaw=-90; pitch=12;
            server(p -> {
                SmokePort.tp(p,-13,84,187,yaw,pitch);
                for(int x=-8;x<=8;x++) for(int z=178;z<=196;z++) p.level().setBlock(new BlockPos(x,88,z),Blocks.AIR.defaultBlockState(),3);
            });
        }
        if(ticks>=305&&ticks<=355&&ticks%5==0) server(p -> DarkmatterGraphEffects.interference(p,AREA));
        if(ticks==360) {
            reportPerformance(); server(DarkmatterGraphEffects::stopInterference);
        }
        if(ticks>=370&&ticks<=385&&ticks%5==0) for(int i=0;i<18;i++) synthetic(1000000+i, AREA);
        if(ticks==380) {
            require(InterferenceFieldClient.activeCount()==18,"Overlapping fields survive multiple uniform batches");
            capture("overlapping_18");
        }
        if(ticks==400) {
            require(InterferenceFieldClient.activeCount()==0,"Untracked casters also expire safely");
            yaw=0; pitch=35; server(p -> SmokePort.tp(p,0,96,174,yaw,pitch));
        }
        if(ticks>=405&&ticks<=435&&ticks%5==0) synthetic(1000000,
                new DirectionalArea(AREA.origin(),new Vec3(0,0.6,1),AREA.first(),AREA.second()));
        if(ticks==420) capture("pitched_overhead");
        if(ticks==440) server(p -> {
            lifecycleActor=p.level().getEntitiesOfClass(net.minecraft.world.entity.animal.Cow.class,p.getBoundingBox().inflate(100)).getFirst();
            DarkmatterGraphEffects.interference(lifecycleActor,AREA);
        });
        if(ticks==445) server(p -> lifecycleActor.discard());
        if(ticks==449) require(InterferenceFieldClient.activeCount()==0,"Removing a tracked caster clears its field");
        if(ticks==450) {
            require(InterferenceFieldClient.activeCount()==0,"Pitched range expires");
            reloading=mc.reloadResourcePacks();
        }
        if(ticks>=460&&ticks<=485&&ticks%5==0) synthetic(1000000,AREA);
        if(ticks==480) {
            require(reloading.isDone()&&!reloading.isCompletedExceptionally(),"Resource reload completes");
            require(InterferenceFieldClient.activeCount()==1&&WorldSurfaceMasks.ready(),"Masks rebuild after resource reload");
            capture("after_reload");
        }
        if(ticks>=505 && mc.level.getGameTime()-lastSyntheticTick>14) {
            require(InterferenceFieldClient.activeCount()==0,"No stale field after reload and expiry");
            synthetic(1000000,AREA); exiting=true;
            mc.disconnect(new net.minecraft.client.gui.screens.TitleScreen(),false);
        }
    }

    private static void synthetic(int id, DirectionalArea area) {
        lastSyntheticTick=Minecraft.getInstance().level.getGameTime();
        InterferenceFieldClient.accept(new org.academy.internal.common.network.DarkmatterVisualPacket(
                Minecraft.getInstance().level.dimension().location(),
                org.academy.internal.common.network.DarkmatterVisualPacket.Kind.INTERFERENCE,
                id,true,area.origin(),1,2,(float)area.radius(),0,false,1,area));
    }

    @SubscribeEvent public static void beforeFrame(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre event) {
        // C-stun: 1.21.1 无 GPU 时间戳查询 API, 性能采样降级为帧计数。
        if (!Boolean.getBoolean("academy.interferenceFieldSmoke")) return;
    }

    @SubscribeEvent public static void afterFrame(net.neoforged.neoforge.client.event.RenderFrameEvent.Post event) {
        // C-stun: no-op (GPU timestamp queries removed)。
    }

    private static void reportPerformance() {
        var target = Minecraft.getInstance().getMainRenderTarget();
        String report = String.format(java.util.Locale.ROOT,
                "Resolution %dx%d; GPU frame timing sampling is unavailable on this loader (1.21.1 C-stun)%n",
                target.width, target.height);
        System.out.print("[interference-field-smoke] " + report);
        try { Files.writeString(Path.of(System.getProperty("academy.interferenceFieldOutput")).resolve("performance.txt"), report); }
        catch (java.io.IOException e) { throw new RuntimeException(e); }
    }
    private static void capture(String name) {
        var output = Path.of(System.getProperty("academy.interferenceFieldOutput")).resolve(name + ".png");
        try (com.mojang.blaze3d.platform.NativeImage image = net.minecraft.client.Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())) {
            Files.createDirectories(output.getParent()); image.writeToFile(output);
        } catch (Exception exception) { throw new IllegalStateException(exception); }
    }
    private static void command(ServerPlayer player, String command) {
        var server = player.level().getServer();
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
    }

    private static void learn(ServerPlayer player, org.academy.api.common.ability.Skill... skills) {
        var system = AbilitySystemServer.getSystem(player);
        var id = player.getUUID();
        system.setPlayerAbilityCategory(id, skills[0].getCategory());
        system.setPlayerLevel(id, 5);
        for (var skill : skills) {
            system.addPlayerSkill(player, skill.getKeyString());
            if (!skill.isEnabled(player)) skill.toggle(player);
            system.setPlayerSkillProficiency(id, skill, 0);
        }
        var data = system.getPlayerData(id);
        data.setAcademyMaxCp(500); data.getCpData().setMaxCP(500); data.getCpData().setAvailableCP(500);
        for (var kind : java.util.List.of(org.academy.api.common.ability.SyncTypes.CP_DATA,
                org.academy.api.common.ability.SyncTypes.SKILL_DATA, org.academy.api.common.ability.SyncTypes.ABILITY_CATEGORY)) {
            system.schedulePlayerSync(id, kind);
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
        System.out.println("[interference-field-smoke] " + message);
    }
    private static void server(Consumer<ServerPlayer> task) {
        var mc = Minecraft.getInstance(); var id = mc.player.getUUID();
        mc.getSingleplayerServer().execute(() -> {
            try { task.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(id)); }
            catch (Throwable failure) { serverFailure = failure; }
        });
    }
    }
