package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteObjectSpawn;

/** Packet7 coal-263 fuel and oracle Packet23 minecart type observation. */
public final class B173FurnaceCartPush {
  private static final int[] CART_TYPES = {10, 11, 12};

  private B173FurnaceCartPush() {}

  public static RemoteObjectSpawn awaitMinecart(B173WireClient client) {
    B173PlayInbound inbound = client.channel().inbound();
    Thread pulse = inbound.pulse();
    long deadline = System.nanoTime() + inbound.timeoutNanos();
    try {
      int count = 0;
      while (count < 8192 && System.nanoTime() < deadline) {
        RemoteObjectSpawn cart = takeMinecart(inbound);
        if (cart != null)
          return cart;
        inbound.pumpOne();
        cart = takeMinecart(inbound);
        if (cart != null)
          return cart;
        count++;
      }
      throw new IllegalStateException("minecart Packet23 type 10/11/12 absent");
    } catch (IOException error) {
      throw new IllegalStateException("minecart Packet23 type 10/11/12 absent", error);
    } finally {
      pulse.interrupt();
    }
  }

  public static void useCoal(B173WireClient client, int entity) {
    try {
      B173PlayChannel channel = client.channel();
      B173PlayInbound inbound = channel.inbound();
      int local = client.state().entityId();
      int slot = findCoal(inbound.inventory());
      if (entity < 1 || entity == local)
        throw new IllegalArgumentException("invalid furnace-cart entity");
      if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
        throw new IllegalStateException("furnace-cart fuel requires synchronized play");
      if (slot < 0)
        throw new IllegalStateException("coal 263 absent from hotbar");
      synchronized (channel.output) {
        channel.output.writeByte(16);
        channel.output.writeShort(slot);
        channel.output.flush();
        channel.output.writeByte(7);
        channel.output.writeInt(local);
        channel.output.writeInt(entity);
        channel.output.writeByte(0);
        channel.output.flush();
      }
    } catch (IOException error) {
      throw new IllegalStateException("furnace-cart coal Packet7 failed", error);
    }
  }

  public static boolean coalPresent(B173WireClient client) {
    return findCoal(client.channel().inbound().inventory()) >= 0;
  }

  public static B173EntityVelocity takeVelocity(B173WireClient client, int entity) {
    return client.channel().inbound().velocities().take(entity);
  }

  private static RemoteObjectSpawn takeMinecart(B173PlayInbound inbound) {
    int index = 0;
    while (index < CART_TYPES.length) {
      RemoteObjectSpawn cart = inbound.objects().take(CART_TYPES[index]);
      if (cart != null)
        return cart;
      index++;
    }
    return null;
  }

  private static int findCoal(RemoteInventoryView view) {
    int slot = 0;
    while (slot <= 8) {
      if (!view.slot(36 + slot).empty() && view.slot(36 + slot).item().legacyId() == 263)
        return slot;
      slot++;
    }
    return -1;
  }
}
