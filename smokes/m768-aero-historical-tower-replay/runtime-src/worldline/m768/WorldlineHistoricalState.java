package worldline.m768;

import aero.modellib.WorldlineHistoricalCensus;
import aero.modellib.test.WorldlineMegaRehydrator;
import java.util.ArrayList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.InteractionManager;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;

/** Deterministic lifecycle and camera route for one historical tower arm. */
public final class WorldlineHistoricalState {
    public static final boolean ENABLED = Boolean.getBoolean("worldline.m768.enabled");
    private static final boolean PREPARE = Boolean.getBoolean("worldline.m768.prepare");
    private static final String ARM = System.getProperty("worldline.m768.arm", "solid-aero-save");
    private static final int TICKS = Integer.getInteger("worldline.m768.ticks", 12400);
    private static final int DRAIN_FRAMES = 20;
    private static final int LOAD_TIMEOUT_TICKS = 1200;
    private static final int MEGA_BASE_Y = 63;
    private static int stage, warmup, stable, retained, baseY;

    private WorldlineHistoricalState() {}

    public static boolean drive(Minecraft game) {
        if (!ENABLED) return false;
        prepareDisplay(game);
        if (stage == 0 && game.world == null) {
            stage = 1;
            game.interactionManager = new SingleplayerInteractionManager(game);
            System.out.println("[WorldlineM768] start arm=" + ARM + " prepare=" + PREPARE);
            game.startGame("WorldlineAero", "Worldline Aero", 17320110707L);
            return false;
        }
        if (game.world == null || game.player == null || stage >= 4) return false;
        if (stage == 1) warm(game);
        if (stage == 2) retain(game);
        return false;
    }

    public static int phase() {
        if (retained < 2400) return 1;
        if (retained < 4800) return 2;
        return 3;
    }

    private static void warm(Minecraft game) {
        World world = game.world;
        if (warmup++ == 0) for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++) world.getChunk(x, z);
        boolean noAero = ARM.equals("solid-no-aero");
        int machines = machineCount(world);
        if (!PREPARE && !noAero && warmup == 2 && machines == 0) {
            WorldlineMegaRehydrator.rehydrate(world);
            machines = machineCount(world);
            System.out.println("[WorldlineM768] restored synthetic machines=" + machines);
        }
        int required = noAero ? 0 : 576;
        if (machines < required && warmup < LOAD_TIMEOUT_TICKS) return;
        require(machines >= required, "central MEGA fixture did not reach " + required
                + " block entities; found " + machines);
        if (baseY == 0) {
            baseY = MEGA_BASE_Y;
            applyControl(world);
            world.entities.retainAll(java.util.Collections.singleton(game.player));
            world.globalEntities.clear();
        }
        place(game.player, 0);
        int backlog = ((WorldlineRendererStats) game.worldRenderer).worldlineCompileBacklog();
        if (warmup % 100 == 0) System.out.println("[WorldlineM768] warmup ticks=" + warmup
                + " blockEntities=" + world.blockEntities.size() + " machines="
                + machineCount(world) + " backlog=" + backlog);
        stable = backlog == 0 ? stable + 1 : 0;
        if (stable < DRAIN_FRAMES) return;
        int expected = ARM.equals("solid-no-aero") ? 0 : 576;
        int actual = machineCount(world);
        require(actual == expected, "arm machine count drift; expected=" + expected
                + " actual=" + actual);
        if (PREPARE) {
            world.saveWithLoadingDisplay(true, null);
            System.out.println("[WorldlineM768] template-ready machines=" + expected
                    + " baseY=" + baseY + " backlog=" + backlog);
            stage = 4; game.scheduleStop(); return;
        }
        WorldlineHistoricalCensus.arm(ARM);
        stage = 2;
        System.out.println("[WorldlineM768] retained-start arm=" + ARM + " machines=" + expected
                + " terrain=" + (ARM.equals("sparse-aero") ? "sparse" : "solid"));
    }

    private static void retain(Minecraft game) {
        if (!WorldlineHistoricalCensus.active()) { place(game.player, 0); return; }
        place(game.player, retained);
        retained++;
        if (retained < TICKS) return;
        WorldlineHistoricalCensus.seal();
        System.out.println("[WorldlineM768] retained-complete arm=" + ARM + " ticks=" + retained);
        stage = 4; game.scheduleStop();
    }

    private static void place(ClientPlayerEntity player, int tick) {
        double y = baseY + 2.0D; float yaw = 45.0F, pitch = 4.0F;
        if (tick >= 2400 && tick < 4800) {
            int route = tick - 2400;
            yaw = 45.0F + route * 9.0F;
            pitch = (float) (Math.sin(route * 0.08D) * 24.0D);
            y += Math.max(0.0D, Math.sin(route * 0.16D)) * 1.2D;
        }
        player.velocityX = player.velocityY = player.velocityZ = 0.0D;
        player.setPositionAndAngles(8.5D, y, 8.5D, yaw, pitch);
    }

    private static void applyControl(World world) {
        if (ARM.equals("solid-no-aero")) {
            int[][] origins = {{1, 1}, {1, 11}, {11, 1}, {11, 11}};
            for (int floor = 0; floor < 16; floor++) for (int[] origin : origins)
                for (int sx = 0; sx < 3; sx++) for (int sz = 0; sz < 3; sz++) {
                    int x = origin[0] + sx, y = baseY + floor * 4 + 1, z = origin[1] + sz;
                    world.removeBlockEntity(x, y, z);
                    world.setBlockWithoutNotifyingNeighbors(x, y, z, 0);
                }
            ArrayList removed = new ArrayList();
            for (Object value : new ArrayList(world.blockEntities)) {
                BlockEntity be = (BlockEntity) value;
                if (isMachinePosition(be)) removed.add(be);
            }
            world.blockEntities.removeAll(removed);
        } else if (ARM.equals("sparse-aero")) {
            for (int floor = 0; floor <= 16; floor++) sparseFloor(world, baseY + floor * 4);
        }
    }

    private static void sparseFloor(World world, int y) {
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            boolean cluster = ((x >= 1 && x <= 3) || (x >= 11 && x <= 13))
                    && ((z >= 1 && z <= 3) || (z >= 11 && z <= 13));
            boolean cross = x == 7 || x == 8 || z == 7 || z == 8;
            if (!cluster && !cross) world.setBlockWithoutNotifyingNeighbors(x, y, z, 0);
        }
    }

    private static int machineCount(World world) {
        int count = 0;
        for (Object value : world.blockEntities) {
            BlockEntity be = (BlockEntity) value;
            if (isMachinePosition(be) && !be.isRemoved()) count++;
        }
        return count;
    }

    private static boolean isMachinePosition(BlockEntity be) {
        if (be.y < MEGA_BASE_Y + 1 || be.y > MEGA_BASE_Y + 61
                || (be.y - MEGA_BASE_Y - 1) % 4 != 0)
            return false;
        boolean x = (be.x >= 1 && be.x <= 3) || (be.x >= 11 && be.x <= 13);
        boolean z = (be.z >= 1 && be.z <= 3) || (be.z >= 11 && be.z <= 13);
        return x && z;
    }

    private static void prepareDisplay(Minecraft game) {
        game.currentScreen = null; game.paused = false; game.skipGameRender = false;
        if (game.options != null) {
            game.options.hideHud = true; game.options.bobView = false; game.options.viewDistance = 3;
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
