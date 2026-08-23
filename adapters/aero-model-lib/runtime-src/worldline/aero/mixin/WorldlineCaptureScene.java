package worldline.aero.mixin;

import java.util.Collections;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;
import worldline.aero.WorldlineFrameOracle;

/** Scene setup and measurement kept independent from the capture state machine. */
final class WorldlineCaptureScene {
    private WorldlineCaptureScene() { }

    static void prepareDisplay(Minecraft game, boolean displayReady) {
        game.currentScreen = null;
        game.paused = false;
        game.skipGameRender = !displayReady;
        game.options.hideHud = true;
        game.options.bobView = false;
        if (WorldlineCaptureSettings.VIEW_DISTANCE >= 0) {
            game.options.viewDistance = Math.min(3, WorldlineCaptureSettings.VIEW_DISTANCE);
        }
    }

    static void placePlayer(ClientPlayerEntity player, int y, int ticks) {
        boolean look = "look".equals(WorldlineCaptureSettings.PATH);
        int step = "moving".equals(WorldlineCaptureSettings.PATH) ? Math.min(ticks, 60) : 0;
        double x = 8.5D + step * 0.25D;
        double z = 8.5D + step * 0.125D;
        double placedY = y + (look && ticks % 10 < 5 ? 1.2D : 0.0D);
        float yaw = 45.0F + (look ? ticks * 12.0F : step * 2.0F);
        player.setPositionAndAngles(x, placedY, z, yaw, look ? 20.0F : 0.0F);
        WorldlineFrameOracle.pose(WorldlineCaptureSettings.PATH,
                WorldlineCaptureSettings.VIEW_DISTANCE, x, placedY, z, yaw);
    }

    static void stabilize(Minecraft game, World world, ClientPlayerEntity player) {
        world.setTime(6000L);
        world.getProperties().setRaining(false);
        world.getProperties().setThundering(false);
        world.setRainGradient(0.0F);
        world.entities.retainAll(Collections.singleton(player));
        world.globalEntities.clear();
        game.raining = false;
    }

    static int entityBlocks(World world) {
        int count = 0;
        for (int x = -16; x < 32; x++) {
            for (int z = -16; z < 32; z++) {
                for (int y = 0; y < 128; y++) {
                    int id = world.getBlockId(x, y, z);
                    if (id > 0 && Block.BLOCKS[id] instanceof BlockWithEntity) count++;
                }
            }
        }
        return count;
    }
}
