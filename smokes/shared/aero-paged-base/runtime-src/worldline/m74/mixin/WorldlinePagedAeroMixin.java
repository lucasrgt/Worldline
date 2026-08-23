package worldline.m74.mixin;

import aero.modellib.Aero_BECellRenderer;import aero.modellib.model.Aero_MeshModel;import aero.modellib.render.Aero_RenderOptions;import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;import worldline.m74.client.WorldlinePagedTimer;

/** Times the real enqueue and populated/empty flush boundaries. */
@Mixin(Aero_BECellRenderer.class)
public abstract class WorldlinePagedAeroMixin {
    @Inject(method="queueAtRest",at=@At("HEAD"),remap=false)private static void qb(Aero_MeshModel m,String t,BlockEntity be,double x,double y,double z,float r,float b,Aero_RenderOptions o,CallbackInfo ci){WorldlinePagedTimer.queueBegin();}
    @Inject(method="queueAtRest",at=@At("RETURN"),remap=false)private static void qe(Aero_MeshModel m,String t,BlockEntity be,double x,double y,double z,float r,float b,Aero_RenderOptions o,CallbackInfo ci){WorldlinePagedTimer.queueEnd();}
    @Inject(method="flush(DDD)V",at=@At("HEAD"),remap=false)private static void fb(double x,double y,double z,CallbackInfo ci){WorldlinePagedTimer.flushBegin();}
    @Inject(method="flush(DDD)V",at=@At("RETURN"),remap=false)private static void fe(double x,double y,double z,CallbackInfo ci){WorldlinePagedTimer.flushEnd();}
}
