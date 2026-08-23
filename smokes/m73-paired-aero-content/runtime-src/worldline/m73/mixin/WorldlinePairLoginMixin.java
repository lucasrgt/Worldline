package worldline.m73.mixin;

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

/** Auto-connects only to the local paired fixture. */
@Mixin(Minecraft.class)
public abstract class WorldlinePairLoginMixin {
  @Shadow public World world;
  @Shadow public Session session;
  @Unique private boolean connected;
  @Shadow public abstract void setScreen(net.minecraft.client.gui.screen.Screen screen);
  @Inject(method = "tick()V", at = @At("HEAD"))
  private void connect(CallbackInfo callback) {
    if (!Boolean.getBoolean("worldline.pair.enabled") || connected || world != null)
      return;
    int port = Integer.getInteger("worldline.pair.port", 0);
    String name = System.getProperty("worldline.pair.username", "");
    if (port < 1 || port > 65535 || !name.matches("[A-Za-z0-9_]{1,16}"))
      throw new IllegalStateException("invalid M73 localhost boundary");
    connected = true;
    session.username = name;
    System.out.println("[WorldlinePairContent] connect username=" + name);
    setScreen(new ConnectScreen((Minecraft) (Object) this, "127.0.0.1", port));
  }
}
