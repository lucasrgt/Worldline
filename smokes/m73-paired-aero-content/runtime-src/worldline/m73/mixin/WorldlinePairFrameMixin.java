package worldline.m73.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m73.WorldlinePairMod;
import worldline.m73.WorldlinePairSync;
import worldline.m73.probe.WorldlinePairProbe;

/** Opens a common activation boundary, measures a fixed window, then exits normally. */
@Mixin(GameRenderer.class)
public abstract class WorldlinePairFrameMixin {
  @Shadow private Minecraft client;
  @Unique private boolean sent, ready, complete;
  @Inject(method = "onFrameUpdate(F)V", at = @At("TAIL"))
  private void frame(float delta, CallbackInfo callback) {
    if (complete)
      return;
    WorldlinePairSync.apply(client.world);
    if (!sent && !WorldlinePairProbe.warm())
      return;
    client.currentScreen = null;
    client.paused = false;
    client.skipGameRender = false;
    client.options.hideHud = true;
    client.options.bobView = false;
    if (!sent) {
      MessagePacket packet = new MessagePacket(WorldlinePairMod.ACTIVATE);
      packet.ints = new int[] {WorldlinePairProbe.nonce()};
      client.getNetworkHandler().sendPacket(packet);
      sent = true;
      WorldlinePairProbe.trigger();
      return;
    }
    if (!ready) {
      if (!WorldlinePairSync.tracked(client.world, client.player.x, client.player.y,
              client.player.z, client.player.yaw, client.player.pitch))
        return;
      MessagePacket packet = new MessagePacket(WorldlinePairMod.READY);
      packet.ints = WorldlinePairSync.plan();
      client.getNetworkHandler().sendPacket(packet);
      ready = true;
      System.out.println("[WorldlinePairContent] plan-ready");
      return;
    }
    if (!WorldlinePairProbe.window())
      return;
    WorldlinePairProbe.verifyFixture(WorldlinePairSync.received(), WorldlinePairSync.applied());
    complete = true;
    System.out.println("[WorldlinePairContent] complete mode=" + WorldlinePairProbe.mode()
        + " frames=" + WorldlinePairProbe.frames() + " windowMs="
        + WorldlinePairProbe.windowMillis() + " rendered=" + WorldlinePairProbe.rendered());
    client.getNetworkHandler().disconnect();
    client.scheduleStop();
  }
}
