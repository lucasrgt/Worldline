package worldline.itemref.runtime.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.util.Session;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Auto-connects only under the explicit item-reference runtime profile. */
@Mixin(Minecraft.class)
public abstract class LoginMixin {
    @Shadow public World world; @Shadow public Session session;
    @Shadow public abstract void setScreen(net.minecraft.client.gui.screen.Screen screen);
    @Unique private boolean worldline$connected;
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void connect(CallbackInfo callback) {
        if (!Boolean.getBoolean("worldline.itemref.client") || worldline$connected || world != null) return;
        int port = Integer.getInteger("worldline.itemref.port", 25565);
        String username = System.getProperty("worldline.itemref.username", "VaultCell");
        if (port < 1 || port > 65535 || !username.matches("[A-Za-z0-9_]{1,16}")) {
            throw new IllegalStateException("item-reference client boundary");
        }
        worldline$connected = true; session.username = username;
        setScreen(new ConnectScreen((Minecraft) (Object) this, "127.0.0.1", port));
    }
}
