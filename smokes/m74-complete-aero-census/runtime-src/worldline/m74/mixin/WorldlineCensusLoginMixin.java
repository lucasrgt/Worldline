package worldline.m74.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.util.Session;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Auto-connects only to the local census fixture. */
@Mixin(Minecraft.class)
public abstract class WorldlineCensusLoginMixin {
  @Shadow public World world;
  @Shadow public Session session;
  @Unique private boolean connected;
  @Shadow public abstract void setScreen(net.minecraft.client.gui.screen.Screen screen);
  @Inject(method = "tick()V", at = @At("HEAD"))
  private void connect(CallbackInfo callback) {
    if (!Boolean.getBoolean("worldline.census.enabled") || connected || world != null)
      return;
    int port = Integer.getInteger("worldline.census.port", 0);
    String name = System.getProperty("worldline.census.username", "");
    if (port < 1 || port > 65535 || !name.matches("[A-Za-z0-9_]{1,16}"))
      throw new IllegalStateException("invalid M74 localhost boundary");
    connected = true;
    session.username = name;
    System.out.println("[WorldlineCensus] connect username=" + name);
    setScreen(new ConnectScreen((Minecraft) (Object) this, "127.0.0.1", port));
  }
}
