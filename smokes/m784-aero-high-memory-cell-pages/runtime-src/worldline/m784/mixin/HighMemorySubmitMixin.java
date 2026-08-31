package worldline.m784.mixin;

import net.minecraft.client.render.Culler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m784.HighMemoryState;

@Mixin(value = WorldRenderer.class, priority = 1200)
public abstract class HighMemorySubmitMixin {
    @Inject(method = "renderEntities(Lnet/minecraft/util/math/Vec3d;"
        + "Lnet/minecraft/client/render/Culler;F)V", at = @At("TAIL"))
    private void worldlineSubmitFixture(Vec3d camera, Culler culler,
            float tickDelta, CallbackInfo callback) {
        HighMemoryState.submitFixture(camera.x, camera.y, camera.z);
    }
}
