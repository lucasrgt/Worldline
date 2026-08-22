package worldline.aero.mixin;

import worldline.aero.WorldlineChunkGeometry;
import net.minecraft.client.render.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Feeds chunk rebuild vertices into the M15 geometry oracle. */
@Mixin(Tessellator.class)
public abstract class WorldlineTessellatorProbeMixin {
    @Shadow private double u, v, xOffset, yOffset, zOffset;
    @Shadow private int color, normal;
    @Shadow private boolean hasColor, hasTexture, hasNormals;

    @Inject(method = "vertex(DDD)V", at = @At("HEAD"))
    private void worldlineVertex(double x, double y, double z, CallbackInfo callback) {
        int flags = (hasColor ? 1 : 0) | (hasTexture ? 2 : 0) | (hasNormals ? 4 : 0);
        WorldlineChunkGeometry.vertex(x + xOffset, y + yOffset, z + zOffset,
                u, v, color, normal, flags);
    }
}
