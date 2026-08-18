package worldline.m72.mixin;

import net.minecraft.client.network.ClientNetworkHandler;
import net.minecraft.network.packet.login.LoginHelloPacket;
import net.minecraft.network.packet.play.PlayerMovePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m72.probe.WorldlineContentProbe;

/** Records exact remote-login and play-readiness handlers. */
@Mixin(ClientNetworkHandler.class)
public abstract class WorldlineContentNetworkMixin {
    @Inject(method = "onHello", at = @At("TAIL")) private void hello(LoginHelloPacket p, CallbackInfo c) { WorldlineContentProbe.hello(); }
    @Inject(method = "onPlayerMove", at = @At("TAIL")) private void play(PlayerMovePacket p, CallbackInfo c) { WorldlineContentProbe.play(); }
}
