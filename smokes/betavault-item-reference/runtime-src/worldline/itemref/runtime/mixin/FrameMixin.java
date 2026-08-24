package worldline.itemref.runtime.mixin;

import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.itemref.LogicalItemReference;
import worldline.itemref.runtime.ItemRefs;

/** Ends only after the real client inventory owns the canonical reference. */
@Mixin(GameRenderer.class)
public abstract class FrameMixin {
  @Shadow private Minecraft client;
  @Unique private boolean worldline$complete;
  @Unique private long worldline$physicalSince;
  @Unique private int worldline$totalFrames;
  @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
  private void frame(float delta, CallbackInfo callback) {
    if (worldline$complete || client.player == null)
      return;
    worldline$totalFrames++;
    ItemStack stack = client.player.inventory.main[0];
    LogicalItemReference reference = ItemRefs.get(stack);
    if (stack != null && reference == null) {
      if (worldline$physicalSince == 0L)
        worldline$physicalSince = System.nanoTime();
      if (System.nanoTime() - worldline$physicalSince < TimeUnit.SECONDS.toNanos(30L))
        return;
      worldline$complete = true;
      System.out.println("WORLDLINE_ITEMREF_CLIENT=FAIL physical-without-reference");
      client.getNetworkHandler().disconnect();
      client.scheduleStop();
      return;
    }
    if (reference == null)
      return;
    worldline$physicalSince = 0L;
    if (worldline$totalFrames < 300)
      return;
    worldline$complete = true;
    String phase = System.getProperty("worldline.itemref.phase", "");
    System.out.println("WORLDLINE_ITEMREF_CLIENT=PASS phase=" + phase
        + " ref=" + reference.canonical() + " packet104=preserved");
    client.getNetworkHandler().disconnect();
    client.scheduleStop();
  }
}
