package worldline.m772;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/** Owns the fixed twelve-chunk mutation fixture and camera placement. */
final class AutosaveScene {
    private static final int TARGETS = 12;
    private static final int SENTINEL_Y = 120;

    private AutosaveScene() {}

    static void loadTargets(World world) {
        for (int index = 0; index < TARGETS; index++) {
            world.getChunk(index % 4, index / 4);
        }
    }

    static int setSentinels(World world, int block) {
        int changed = 0;
        for (int index = 0; index < TARGETS; index++) {
            if (world.setBlockWithoutNotifyingNeighbors(
                    x(index), SENTINEL_Y, z(index), block)) {
                changed++;
            }
        }
        return changed;
    }

    static int sentinels(World world, int block) {
        int count = 0;
        for (int index = 0; index < TARGETS; index++) {
            if (world.getBlockId(x(index), SENTINEL_Y, z(index)) == block) {
                count++;
            }
        }
        return count;
    }

    static int targetDirty(World world) {
        int count = 0;
        for (int index = 0; index < TARGETS; index++) {
            Chunk chunk = world.getChunk(index % 4, index / 4);
            if (chunk.dirty) {
                count++;
            }
        }
        return count;
    }

    static void place(ClientPlayerEntity player) {
        player.velocityX = 0.0D;
        player.velocityY = 0.0D;
        player.velocityZ = 0.0D;
        player.setPositionAndAngles(31.5D, 124.0D, 23.5D, 35.0F, 70.0F);
    }

    static void prepareDisplay(Minecraft game) {
        game.currentScreen = null;
        game.paused = false;
        game.skipGameRender = false;
        if (game.options != null) {
            game.options.hideHud = true;
            game.options.bobView = false;
            game.options.viewDistance = 0;
            game.options.fpsLimit = 0;
        }
    }

    private static int x(int index) {
        return (index % 4) * 16 + 15;
    }

    private static int z(int index) {
        return (index / 4) * 16 + 15;
    }
}
