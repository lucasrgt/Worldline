package worldline.m794.mixin;
import aero.modellib.test.AeroTestModClient;
import aero.modellib.test.AnimatedMegaModelBlockEntity;
import net.modificationstation.stationapi.api.client.event.block.entity.BlockEntityRendererRegisterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m794.ProductionModelRenderer;
import worldline.m794.ProductionState;
/** Registers the controlled consumer once through the existing StationAPI event. */
@Mixin(value = AeroTestModClient.class, remap = false)
abstract class ProductionModelMixin {
  @Inject(method = "registerBlockEntityRenderers", at = @At("TAIL"))
  private static void worldlineModel(BlockEntityRendererRegisterEvent event, CallbackInfo callback) {
    if (ProductionState.ENABLED)
      event.renderers.put(AnimatedMegaModelBlockEntity.class, new ProductionModelRenderer());
  }
}
