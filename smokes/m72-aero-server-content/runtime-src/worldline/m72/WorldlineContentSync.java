package worldline.m72;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

/** Buffers the server message until its preceding remote block change is applied. */
public final class WorldlineContentSync {
  private static boolean received, applied;
  private static int x, y, z, nonce;
  private WorldlineContentSync() {
  }
  public static synchronized void receive(World world, int px, int py, int pz, int value) {
    if (py < 0 || py > 127 || value <= 0)
      throw new IllegalStateException("invalid M72 message values");
    if (received && (x != px || y != py || z != pz || nonce != value))
      throw new IllegalStateException("conflicting M72 content message");
    if (received)
      return;
    x = px;
    y = py;
    z = pz;
    nonce = value;
    received = true;
    System.out.println(
        "[WorldlineContent] message x=" + x + " y=" + y + " z=" + z + " nonce=" + nonce);
    apply(world);
  }
  public static synchronized void apply(World world) {
    if (!received || applied || world == null)
      return;
    if (world.getBlockId(x, y, z) != WorldlineContentMod.block.id)
      return;
    BlockEntity current = world.getBlockEntity(x, y, z);
    if (current != null && !(current instanceof WorldlineContentBlockEntity))
      throw new IllegalStateException("M72 remote BlockEntity type drifted");
    WorldlineContentBlockEntity entity =
        current == null ? new WorldlineContentBlockEntity() : (WorldlineContentBlockEntity) current;
    if (entity.nonce() != 0 && entity.nonce() != nonce)
      throw new IllegalStateException("conflicting M72 content nonce");
    entity.setNonce(nonce);
    if (current == null)
      world.setBlockEntity(x, y, z, entity);
    applied = true;
    System.out.println("[WorldlineContent] applied identifier=" + WorldlineContentMod.ID
        + ":server_probe raw=" + WorldlineContentMod.block.id + " x=" + x + " y=" + y + " z=" + z
        + " nonce=" + nonce);
  }
}
