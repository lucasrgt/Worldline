package worldline.m794.mixin;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.world.DistanceChunkSorter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Controlled oracle tie order; preserves all non-equal native distance comparisons. */
@Mixin(DistanceChunkSorter.class)
abstract class ProductionSortMixin {
  @Inject(
      method =
          "compare(Lnet/minecraft/client/render/chunk/ChunkBuilder;Lnet/minecraft/client/render/chunk/ChunkBuilder;)I",
      at = @At("RETURN"), cancellable = true)
  private void
  worldlineCoordinateTie(ChunkBuilder first, ChunkBuilder second, CallbackInfoReturnable<Integer> callback) {
    if (callback.getReturnValueI() != 0)
      return;
    int order = Integer.compare(first.x, second.x);
    if (order == 0)
      order = Integer.compare(first.y, second.y);
    if (order == 0)
      order = Integer.compare(first.z, second.z);
    callback.setReturnValue(order);
  }
}
