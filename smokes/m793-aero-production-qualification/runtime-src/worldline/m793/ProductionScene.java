package worldline.m793;
import net.minecraft.entity.player.ClientPlayerEntity;
/** Twelve production cases; three exact captures per case and arm. */
final class ProductionScene {
    static final int CASES = 12, CHECKPOINTS = CASES * 3, HOLD_FRAMES = 180, WARM_FRAMES = 60;
    private ProductionScene() {}
    static void place(ClientPlayerEntity player, int scene) {
        int frame = Math.max(0, ProductionState.localFrame());
        double x = 16.5D, y = 76D;
        float yaw = 0F, pitch = -20F;
        if (scene == 11) {
            x = frame < 60 ? 15.5D + frame / 30D : frame < 120 ? 48.5D : 16.5D;
            y = frame < 120 ? 76D : 90D;
            yaw = frame < 120 ? 90F : 180F;
            pitch = frame < 120 ? -20F : 20F;
        }
        player.velocityX = player.velocityY = player.velocityZ = 0D;
        player.setPositionAndAngles(x, y, 16.5D, yaw, pitch);
    }
}
