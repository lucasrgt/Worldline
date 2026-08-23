package aero.modellib.test.mixin;

import aero.modellib.test.worldline.WorldlineMultiplayerProbe;
import net.minecraft.client.InteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MultiplayerInteractionManager;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.util.Session;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Auto-connects one real StationAPI/Aero client and stops after remote frames. */
@Mixin(Minecraft.class)
public abstract class WorldlineMultiplayerLoginMixin {
  @Shadow public World world;
  @Shadow public ClientPlayerEntity player;
  @Shadow public InteractionManager interactionManager;
  @Shadow public Session session;
  @Shadow public abstract void setScreen(net.minecraft.client.gui.screen.Screen screen);
  @Shadow public abstract void scheduleStop();
  @Unique
  private static final boolean WORLDLINE_ENABLED =
      Boolean.getBoolean("worldline.multiplayer.enabled");
  @Unique
  private static final String WORLDLINE_HOST =
      System.getProperty("worldline.multiplayer.host", "127.0.0.1");
  @Unique
  private static final int WORLDLINE_PORT = Integer.getInteger("worldline.multiplayer.port", 25565);
  @Unique
  private static final String WORLDLINE_USERNAME =
      System.getProperty("worldline.multiplayer.username", "AeroPeer68");
  @Unique private boolean worldlineConnected;

  @Inject(method = "tick()V", at = @At("HEAD"))
  private void worldlineMultiplayer(CallbackInfo callback) {
    if (!WORLDLINE_ENABLED || worldlineConnected)
      return;
    Minecraft game = (Minecraft) (Object) this;
    if (world == null) {
      worldlineConnected = true;
      session.username = WORLDLINE_USERNAME;
      System.out.println("[WorldlineMultiplayer] connect username=" + WORLDLINE_USERNAME);
      setScreen(new ConnectScreen(game, WORLDLINE_HOST, WORLDLINE_PORT));
    }
  }
}
