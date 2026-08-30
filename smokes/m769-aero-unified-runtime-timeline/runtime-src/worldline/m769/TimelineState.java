package worldline.m769;

import aero.modellib.test.WorldlineM769Rehydrator;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;
import worldline.profiling.ClientProfilerRuntime;

/** Deterministic restored-world lifecycle and camera route for M769. */
public final class TimelineState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m769.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m769.prepare");
    private static final int TICKS = Integer.getInteger("worldline.m769.ticks", 3600);
    private static final int DRAIN_FRAMES = 20;
    private static final int LOAD_TIMEOUT_TICKS = 1200;
    private static final int BASE_Y = 63;
    private static int stage;
    private static int warmup;
    private static int stable;
    private static int retained;

    private TimelineState() {}

    public static boolean drive(Minecraft game) {
        if (!ENABLED) return false;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            TimelineAeroMetrics.initialize();
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM769] start prepare=" + PREPARE);
            game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
            return false;
        }
        if (game.world == null || game.player == null || stage >= 4) return false;
        if (stage == 1) warm(game);
        if (stage == 2) retain(game);
        return false;
    }

    public static int phase() {
        if (retained < 1200) return 1;
        if (retained < 2400) return 2;
        return 3;
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
            WorldlineM769Rehydrator.rehydrate(world);
            machines = machineCount(world);
            System.out.println("[WorldlineM769] restored synthetic machines=" + machines);
        }
        if (machines < 576 && warmup < LOAD_TIMEOUT_TICKS) return;
        require(machines == 576, "M769 fixture machine count drift: " + machines);
        world.entities.retainAll(java.util.Collections.singleton(game.player));
        world.globalEntities.clear();
        place(game.player, 0);
        int backlog = ((TimelineRendererStats) game.worldRenderer).worldlineCompileBacklog();
        stable = backlog == 0 ? stable + 1 : 0;
        if (stable < DRAIN_FRAMES) return;
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM769] template-ready machines=576 backlog=" + backlog);
            stage = 4;
            game.scheduleStop();
            return;
        }
        TimelineJfrCapture.start();
        ClientProfilerRuntime.startCapture();
        stage = 2;
        System.out.println("[WorldlineM769] retained-start machines=576");
    }

    private static void retain(Minecraft game) {
        place(game.player, retained);
        retained++;
        if (retained < TICKS) return;
        ClientProfilerRuntime.finish("retained-complete");
        TimelineJfrCapture.finish();
        System.out.println("[WorldlineM769] retained-complete ticks=" + retained);
        stage = 4;
        game.scheduleStop();
    }

    private static void place(ClientPlayerEntity player, int tick) {
        double y = BASE_Y + 2.0D;
        float yaw = 45.0F;
        float pitch = 4.0F;
        if (tick >= 1200 && tick < 2400) {
            int route = tick - 1200;
            yaw = 45.0F + route * 9.0F;
            pitch = (float) (Math.sin(route * 0.08D) * 24.0D);
            y += Math.max(0.0D, Math.sin(route * 0.16D)) * 1.2D;
        }
        player.velocityX = 0.0D;
        player.velocityY = 0.0D;
        player.velocityZ = 0.0D;
        player.setPositionAndAngles(8.5D, y, 8.5D, yaw, pitch);
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
