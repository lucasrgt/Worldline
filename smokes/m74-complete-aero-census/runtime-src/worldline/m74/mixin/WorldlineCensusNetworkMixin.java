package worldline.m74.mixin;

import net.minecraft.client.network.ClientNetworkHandler;
import net.minecraft.network.packet.login.LoginHelloPacket;
import net.minecraft.network.packet.play.PlayerMovePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldline.m74.WorldlineCensusProbe;

/** Exact login/play readiness markers. */
@Mixin(ClientNetworkHandler.class)
public abstract class WorldlineCensusNetworkMixin {
    @Inject(method = "onHello", at = @At("TAIL")) private void hello(LoginHelloPacket p, CallbackInfo c) { WorldlineCensusProbe.hello(); }
    @Inject(method = "onPlayerMove", at = @At("TAIL")) private void play(PlayerMovePacket p, CallbackInfo c) { WorldlineCensusProbe.play(); }
}
