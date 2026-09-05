package worldline.m792.mixin;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Culler;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m792.OcclusionState;

/** Freezes Beta's HashSet-derived submission order without changing membership. */
@Mixin(value = WorldRenderer.class, priority = 1200)
public abstract class OcclusionOrderMixin {
    @Shadow public List<BlockEntity> globalBlockEntities;
    private static final Comparator<BlockEntity> WORLDLINE_ORDER = new Comparator<BlockEntity>() {
        public int compare(BlockEntity left, BlockEntity right) {
            int result = Integer.compare(left.y, right.y);
            if (result == 0) result = Integer.compare(left.z, right.z);
            if (result == 0) result = Integer.compare(left.x, right.x);
            return result;
        }
    };

    @Inject(method = "renderEntities(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/client/render/Culler;F)V",
        at = @At("HEAD"))
    private void worldlineFreezeSubmissionOrder(Vec3d camera, Culler culler,
            float tickDelta, CallbackInfo callback) {
        if (OcclusionState.retaining()) Collections.sort(globalBlockEntities, WORLDLINE_ORDER);
    }
}
