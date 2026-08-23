package worldline.smoke.furnacesmeltinterruptb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.RemoteFurnaceSmelt;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Places three idle furnaces 61:2, completes one cobble smelt, and interrupts input and fuel. */
public final class FurnaceSmeltInterruptSmoke {
  private FurnaceSmeltInterruptSmoke() {}

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 7)
      throw new IllegalArgumentException(
          "usage: FurnaceSmeltInterruptSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String user = arguments[4];
    int cx = Integer.parseInt(arguments[5]), cz = Integer.parseInt(arguments[6]);
    FurnaceSmeltInterruptSupport.require(seed == 17320110707L && user.equals("FurnInt591") && user.length() <= 16,
        "furnace-smelt-interrupt identity drift");
    Duration timeout = Duration.ofSeconds(180);
    RemoteItemStack cobble = new RemoteItemStack(4, 1, 0), coal = new RemoteItemStack(263, 1, 0),
                    stone = new RemoteItemStack(1, 1, 0);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5, 6, 7},
          new int[] {1, 61, 4, 4, 4, 263, 263, 263}, new int[] {32, 3, 1, 1, 1, 1, 1, 1},
          new int[] {0, 0, 0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inventory = actor.awaitInventory();
      FurnaceSmeltInterruptSupport.require(inventory.occupiedSlots() == 8 && inventory.slot(38).item().equals(cobble)
              && inventory.slot(39).item().equals(cobble) && inventory.slot(40).item().equals(cobble)
              && inventory.slot(41).item().equals(coal) && inventory.slot(42).item().equals(coal)
              && inventory.slot(43).item().equals(coal)
              && !inventory.slot(38).item().equals(new RemoteItemStack(15, 1, 0)),
          "furnace-smelt-interrupt inventory drift");
      FurnaceSmeltInterruptSupport.Raised raised =
          FurnaceSmeltInterruptSupport.raise(actor, actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz), cx, cz);
      actor.selectHeldSlot(1);
      BlockPosition controlFurnace = FurnaceSmeltInterruptSupport.furnace(actor, raised.support);
      BlockPosition inputFurnace = FurnaceSmeltInterruptSupport.furnace(actor, raised.east);
      BlockPosition fuelFurnace = FurnaceSmeltInterruptSupport.furnace(actor, raised.west);
      RemoteFurnaceSmelt control = FurnaceSmeltInterruptClicks.control(actor, controlFurnace, 38, 41);
      FurnaceSmeltInterruptClicks.input(actor, inputFurnace, 39, 42);
      FurnaceSmeltInterruptClicks.fuel(actor, fuelFurnace, 40, 43);
      FurnaceSmeltInterruptSupport.require(control.output().equals(stone), "control stone output drifted");
      actor.close();
      FurnaceSmeltInterruptSupport.awaitPlayers(server, 0);
      String evidence = "column=" + raised.column
          + ",support=" + FurnaceSmeltInterruptSupport.cell(raised.support, 1, 0)
          + ",control=" + FurnaceSmeltInterruptSupport.cell(controlFurnace, 61, 2)
          + ",input=" + FurnaceSmeltInterruptSupport.cell(inputFurnace, 61, 2)
          + ",fuel=" + FurnaceSmeltInterruptSupport.cell(fuelFurnace, 61, 2)
          + ",recipe=4->1,interrupt=input+fuel,mid=40,wait=220,control-out="
          + FurnaceSmeltInterruptSupport.item(stone) + ",input-out=empty,fuel-out=empty,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed + "|fixture=raised-stone+3xfurnace61:2+cobble4+coal263"
          + "|cause=packet15-item61+packet102-load-4+263+take-input-or-fuel"
          + "|wire=packet100-type2-Furnace-39+packet103-slot2-empty+packet105-cook-reset"
          + "|oracle=idle-61:2+interrupt-no-stone-not-m60-m221-m296-m338|" + evidence;
      System.out.println("WORLDLINE_M591_INTERRUPT=" + evidence);
      System.out.println("WORLDLINE_M591_TRACE=" + trace);
      System.out.println("WORLDLINE_M591_SIGNATURE=" + FurnaceSmeltInterruptSupport.sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
}
