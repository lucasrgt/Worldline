package worldline.m783;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

/** Stable display and machine-census boundary for the restored fixture. */
final class VisualFixture {
    private VisualFixture() {}

    static int machineCount(World world) {
        int count = 0;
        for (Object value : world.blockEntities) {
            BlockEntity block = (BlockEntity) value;
            if (!block.isRemoved() && block.y >= 64 && block.y <= 124
                    && (block.y - 64) % 4 == 0
                    && clustered(block.x) && clustered(block.z)) count++;
        }
        return count;
    }

    static void prepareDisplay(Minecraft game) {
        game.currentScreen = null;
        game.paused = false;
        game.skipGameRender = false;
        if (game.options == null) return;
        game.options.hideHud = true;
        game.options.bobView = false;
        game.options.viewDistance = 1;
        game.options.fpsLimit = 0;
    }

    private static boolean clustered(int value) {
        return (value >= 1 && value <= 3) || (value >= 11 && value <= 13);
    }
}
