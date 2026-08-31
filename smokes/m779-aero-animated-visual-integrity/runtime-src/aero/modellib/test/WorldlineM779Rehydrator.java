package aero.modellib.test;

import aero.modellib.Aero_MeshRenderer;
import aero.modellib.render.Aero_FrustumCull;
import java.util.ArrayList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/** Builds and deterministically poses 40 keyframed, 40 morph, and 40 IK models. */
public final class WorldlineM779Rehydrator {
    private static final int STATIC_BLOCK_ID = 7;

    private WorldlineM779Rehydrator() {}

    public static void rehydrate(World world) {
        clearOutsideFixture(world);
        installStaticEnclosure(world);
        int index = 0;
        for (int floor = 0; floor < 8; floor++) {
            int y = 72 + floor * 4;
            for (int lane = 0; lane < 4; lane++) {
                replace(world, 20 + lane * 6, y, 4, id(index++));
                replace(world, 60, y, 20 + lane * 6, id(index++));
                replace(world, 20 + lane * 6, y, 60, id(index++));
                if (lane < 3) replace(world, 4, y, 22 + lane * 6, id(index++));
            }
        }
    }

    public static int[] counts(World world) {
        int[] counts = new int[3];
        for (Object value : world.blockEntities) {
            BlockEntity block = (BlockEntity) value;
            if (block.isRemoved() || !fixture(block.x, block.y, block.z)) continue;
            if (block instanceof AnimatedMegaModelBlockEntity) counts[0]++;
            else if (block instanceof MorphCrystalBlockEntity) counts[1]++;
            else if (block instanceof TurretIKBlockEntity) counts[2]++;
        }
        return counts;
    }

    public static void pose(World world, int frame) {
        for (Object value : world.blockEntities) {
            BlockEntity block = (BlockEntity) value;
            if (block.isRemoved() || !fixture(block.x, block.y, block.z)) continue;
            float seed = phase(block.x, block.y, block.z);
            float routePhase = frame / (float) AnimatedVisualContract.ROUTE_FRAMES;
            if (block instanceof AnimatedMegaModelBlockEntity) {
                AnimatedMegaModelBlockEntity mega = (AnimatedMegaModelBlockEntity) block;
                mega.animState.setState(AnimatedMegaModelBlockEntity.STATE_SPIN);
                mega.animState.setLoopPhase(routePhase + seed);
            } else if (block instanceof MorphCrystalBlockEntity) {
                MorphCrystalBlockEntity morph = (MorphCrystalBlockEntity) block;
                morph.animState.setState(MorphCrystalBlockEntity.STATE_REST);
                morph.animState.setLoopPhase(routePhase + seed);
                double angle = (routePhase + seed) * Math.PI * 4.0D;
                morph.morphState.set("expanded", 0.5F + 0.5F * (float) Math.sin(angle));
            } else if (block instanceof TurretIKBlockEntity) {
                TurretIKBlockEntity turret = (TurretIKBlockEntity) block;
                turret.animState.setState(TurretIKBlockEntity.STATE_REST);
                turret.animState.setLoopPhase(routePhase + seed);
                turret.orbitTick = frame + (int) (seed * 997.0F);
            }
        }
    }

    public static void prewarm() {
        Aero_MeshRenderer.prewarmModel(AnimatedMegaModelBlockEntityRenderer.MODEL);
        Aero_MeshRenderer.prewarmModel(MorphCrystalBlockEntityRenderer.MODEL);
        Aero_MeshRenderer.prewarmModel(TurretIKBlockEntityRenderer.MODEL);
    }

    public static long visibilitySignature(World world, PlayerEntity player) {
        long sum = 0L, xor = 0L;
        int visible = 0;
        for (Object value : world.blockEntities) {
            BlockEntity block = (BlockEntity) value;
            double radius = radius(block);
            if (radius < 0.0D || !fixture(block.x, block.y, block.z)) continue;
            if (!Aero_FrustumCull.isLikelyVisibleWithRadius(
                    block.x - player.x, block.y - player.y, block.z - player.z, radius)) continue;
            long key = mix(block.x, block.y, block.z, (int) radius);
            sum += key;
            xor ^= Long.rotateLeft(key, (block.x + block.y + block.z) & 63);
            visible++;
        }
        return sum ^ Long.rotateLeft(xor, 17) ^ visible * 0x9E3779B97F4A7C15L;
    }

    private static int id(int index) {
        switch (index % 3) {
            case 0: return AeroTestMod.animatedMegaModelBlock.id;
            case 1: return AeroTestMod.morphCrystalBlock.id;
            default: return AeroTestMod.turretIkBlock.id;
        }
    }

    private static float phase(int x, int y, int z) {
        int hash = x * 73428767 ^ y * 912931 ^ z * 438289;
        return (hash & 0x7fffffff) / (float) Integer.MAX_VALUE;
    }

    private static double radius(BlockEntity block) {
        if (block instanceof AnimatedMegaModelBlockEntity) return 4.0D;
        if (block instanceof MorphCrystalBlockEntity
                || block instanceof TurretIKBlockEntity) return 2.0D;
        return -1.0D;
    }

    private static long mix(int x, int y, int z, int radius) {
        long value = x * 0x9E3779B97F4A7C15L ^ y * 0xC2B2AE3D27D4EB4FL
            ^ z * 0x165667B19E3779F9L ^ radius * 0x85EBCA77C2B2AE63L;
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        return value ^ value >>> 31;
    }

    private static void clearOutsideFixture(World world) {
        for (Object value : new ArrayList<Object>(world.blockEntities)) {
            BlockEntity block = (BlockEntity) value;
            if (!fixture(block.x, block.y, block.z)) world.removeBlockEntity(block.x, block.y, block.z);
        }
    }

    private static void installStaticEnclosure(World world) {
        for (int x = -32; x < 112; x++) {
            for (int z = -32; z < 112; z++) world.setBlockWithoutNotifyingNeighbors(x, 68, z, STATIC_BLOCK_ID);
        }
        for (int y = 69; y < 128; y++) {
            for (int x = -32; x < 112; x++) {
                world.setBlockWithoutNotifyingNeighbors(x, y, -32, STATIC_BLOCK_ID);
                world.setBlockWithoutNotifyingNeighbors(x, y, 111, STATIC_BLOCK_ID);
            }
            for (int z = -31; z < 111; z++) {
                world.setBlockWithoutNotifyingNeighbors(-32, y, z, STATIC_BLOCK_ID);
                world.setBlockWithoutNotifyingNeighbors(111, y, z, STATIC_BLOCK_ID);
            }
        }
    }

    private static boolean fixture(int x, int y, int z) {
        if (y < 72 || y > 100 || (y - 72) % 4 != 0) return false;
        return z == 4 && x >= 20 && x <= 38 && (x - 20) % 6 == 0
            || x == 60 && z >= 20 && z <= 38 && (z - 20) % 6 == 0
            || z == 60 && x >= 20 && x <= 38 && (x - 20) % 6 == 0
            || x == 4 && z >= 22 && z <= 34 && (z - 22) % 6 == 0;
    }

    private static void replace(World world, int x, int y, int z, int id) {
        int actual = world.getBlockId(x, y, z);
        if (actual == id) return;
        if (actual != 0) world.removeBlockEntity(x, y, z);
        world.setBlockWithoutNotifyingNeighbors(x, y, z, id);
    }
}
