package worldline.m771;

import aero.modellib.test.WorldlineM771Rehydrator;
import java.util.Collections;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;
import worldline.profiling.ClientProfilerRuntime;

/** Identical restored-world lifecycle shared by both negative-control labels. */
public final class HitchState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m771.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m771.prepare");
    private static final String ARM = System.getProperty("worldline.m771.arm", "prepare");
    private static final int TICKS = Integer.getInteger("worldline.m771.ticks", 1200);
    private static final long MINIMUM_MILLIS = Long.getLong("worldline.m771.minimumMillis", 60000L);
    private static final long FRAME_SEAL_MARGIN_MILLIS = 1000L;
    private static final int DRAIN_FRAMES = 20;
    private static final int LOAD_TIMEOUT_TICKS = 1200;
    private static final int BASE_Y = 63;
    private static int stage;
    private static int warmup;
    private static int stable;
    private static int retained;
    private static long retainedStarted;

    private HitchState() {}

    public static void drive(Minecraft game) {
        if (!ENABLED) return;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM771] start prepare=" + PREPARE + " arm=" + ARM);
            game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
            return;
        }
        if (game.world == null || game.player == null || stage >= 4) return;
        if (stage == 1) warm(game);
        if (stage == 2) retain(game);
    }

    private static void warm(Minecraft game) {
        World world = game.world;
        if (warmup++ == 0) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) world.getChunk(x, z);
            }
        }
        int machines = machineCount(world);
        if (!PREPARE && warmup == 2 && machines == 0) {
            WorldlineM771Rehydrator.rehydrate(world);
            machines = machineCount(world);
            System.out.println("[WorldlineM771] restored synthetic machines=" + machines);
        }
        if (machines < 576 && warmup < LOAD_TIMEOUT_TICKS) return;
        require(machines == 576, "M771 fixture machine count drift: " + machines);
        world.entities.retainAll(Collections.singleton(game.player));
        world.globalEntities.clear();
        place(game.player, 0);
        int backlog = ((HitchRendererStats) game.worldRenderer).worldlineCompileBacklog();
        stable = backlog == 0 ? stable + 1 : 0;
        if (stable < DRAIN_FRAMES) return;
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM771] template-ready machines=576 backlog=" + backlog);
            stage = 4;
            game.scheduleStop();
            return;
        }
        ClientProfilerRuntime.startCapture();
        retainedStarted = System.nanoTime();
        stage = 2;
        System.out.println("[WorldlineM771] retained-start arm=" + ARM + " machines=576");
    }

    private static void retain(Minecraft game) {
        place(game.player, retained);
        retained++;
        long elapsed = (System.nanoTime() - retainedStarted) / 1_000_000L;
        if (retained < TICKS || elapsed < MINIMUM_MILLIS + FRAME_SEAL_MARGIN_MILLIS) return;
        ClientProfilerRuntime.finish("retained-complete");
        System.out.println("[WorldlineM771] retained-complete arm=" + ARM
                + " ticks=" + retained + " millis=" + elapsed);
        stage = 4;
        game.scheduleStop();
    }

    private static void place(ClientPlayerEntity player, int tick) {
        int half = Math.max(1, TICKS / 2);
        int route = tick % half;
        float yaw = tick < half ? 45.0F : 45.0F + route * 7.0F;
        float pitch = tick < half ? 4.0F : (float) (Math.sin(route * 0.08D) * 20.0D);
        player.velocityX = 0.0D;
        player.velocityY = 0.0D;
        player.velocityZ = 0.0D;
        player.setPositionAndAngles(8.5D, BASE_Y + 2.0D, 8.5D, yaw, pitch);
    }

    private static int machineCount(World world) {
        int count = 0;
        for (Object value : world.blockEntities) {
            BlockEntity blockEntity = (BlockEntity) value;
            if (isMachinePosition(blockEntity) && !blockEntity.isRemoved()) count++;
        }
        return count;
    }

    private static boolean isMachinePosition(BlockEntity blockEntity) {
        if (blockEntity.y < BASE_Y + 1 || blockEntity.y > BASE_Y + 61
                || (blockEntity.y - BASE_Y - 1) % 4 != 0) return false;
        boolean x = (blockEntity.x >= 1 && blockEntity.x <= 3)
                || (blockEntity.x >= 11 && blockEntity.x <= 13);
        boolean z = (blockEntity.z >= 1 && blockEntity.z <= 3)
                || (blockEntity.z >= 11 && blockEntity.z <= 13);
        return x && z;
    }

    private static void prepareDisplay(Minecraft game) {
        game.currentScreen = null;
        game.paused = false;
        game.skipGameRender = false;
        if (game.options != null) {
            game.options.hideHud = true;
            game.options.bobView = false;
            game.options.viewDistance = 3;
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
