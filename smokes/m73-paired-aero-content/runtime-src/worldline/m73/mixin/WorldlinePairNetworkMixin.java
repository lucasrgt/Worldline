package worldline.m73.mixin;

import net.minecraft.client.network.ClientNetworkHandler;
import net.minecraft.network.packet.login.LoginHelloPacket;
import net.minecraft.network.packet.play.PlayerMovePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m73.probe.WorldlinePairProbe;

/** Exact login/play markers inherited from the qualified multiplayer seam. */
@Mixin(ClientNetworkHandler.class)
public abstract class WorldlinePairNetworkMixin {
    @Inject(method = "onHello", at = @At("TAIL")) private void hello(LoginHelloPacket p, CallbackInfo c) { WorldlinePairProbe.hello(); }
    @Inject(method = "onPlayerMove", at = @At("TAIL")) private void play(PlayerMovePacket p, CallbackInfo c) { WorldlinePairProbe.play(); }
}
