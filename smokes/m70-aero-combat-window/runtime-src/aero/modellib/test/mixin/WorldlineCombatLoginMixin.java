package aero.modellib.test.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Auto-connects the real Aero observer to the isolated localhost fixture. */
@Mixin(Minecraft.class)
public abstract class WorldlineCombatLoginMixin {
    @Shadow public net.minecraft.world.World world;
    @Shadow public net.minecraft.client.util.Session session;
    @Shadow public abstract void setScreen(net.minecraft.client.gui.screen.Screen screen);
    @Unique private boolean worldlineConnected;
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void worldlineConnect(CallbackInfo callback) {
        if (!Boolean.getBoolean("worldline.combat.enabled") || worldlineConnected || world != null) return;
        String host = System.getProperty("worldline.combat.host", "127.0.0.1");
        int port = Integer.getInteger("worldline.combat.port", 25565);
        String username = System.getProperty("worldline.combat.username", "AeroObserver70");
        if (!"127.0.0.1".equals(host) || port < 1 || port > 65535 || !username.matches("[A-Za-z0-9_]{1,16}"))
            throw new IllegalStateException("invalid combat connect boundary");
        worldlineConnected = true; session.username = username;
        System.out.println("[WorldlineCombat] connect username=" + username);
        setScreen(new ConnectScreen((Minecraft) (Object) this, host, port));
    }
}
