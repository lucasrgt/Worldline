package worldline.m72.mixin;

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

/** Auto-connects only to the local M72 server fixture. */
@Mixin(Minecraft.class)
public abstract class WorldlineContentLoginMixin {
    @Shadow public World world; @Shadow public Session session;
    @Shadow public abstract void setScreen(net.minecraft.client.gui.screen.Screen screen);
    @Unique private boolean worldlineConnected;
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void connect(CallbackInfo callback) {
        if (!Boolean.getBoolean("worldline.content.enabled") || worldlineConnected || world != null) return;
        int port = Integer.getInteger("worldline.content.port", 25565);
        String username = System.getProperty("worldline.content.username", "AeroContent72");
        if (port < 1 || port > 65535 || !username.matches("[A-Za-z0-9_]{1,16}"))
            throw new IllegalStateException("invalid M72 localhost boundary");
        worldlineConnected = true; session.username = username;
        System.out.println("[WorldlineContent] connect username=" + username);
        setScreen(new ConnectScreen((Minecraft) (Object) this, "127.0.0.1", port));
    }
}
