package worldline.smoke.noteinstrumentsb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import worldline.api.*;
import worldline.b173server.*;

/** Plays official note block 25 on stone, planks, and sand and captures Packet54 instrument ids. */
public final class NoteInstrumentsSmoke {
  private NoteInstrumentsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: NoteInstrumentsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition stone, sandSupport, wood, sand, nStone, nWood, nSand;
    RemoteNoteEvent pStone, pWood, pSand;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 5, 12, 25}, new int[] {32, 1, 1, 3}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "note instrument inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      stone = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (water(
          initial.blockAt(local(stone.x(), cx), stone.y() + 1, local(stone.z(), cz)).legacyId())) {
        stone = place(actor, stone, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded note instrument fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        stone = place(actor, stone, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      sandSupport = place(actor, stone, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      wood = place(actor, sandSupport, BlockFace.EAST, 5);
      actor.selectHeldSlot(2);
      sand = place(actor, sandSupport, BlockFace.UP, 12);
      actor.selectHeldSlot(3);
      nStone = place(actor, stone, BlockFace.UP, 25);
      nWood = place(actor, wood, BlockFace.UP, 25);
      nSand = place(actor, sand, BlockFace.UP, 25);
      actor.selectHeldSlot(4);
      pStone = play(actor, nStone);
      pWood = play(actor, nWood);
      pSand = play(actor, nSand);
      Set<Integer> instruments =
          new LinkedHashSet<Integer>(Arrays.asList(Integer.valueOf(pStone.instrument()),
              Integer.valueOf(pWood.instrument()), Integer.valueOf(pSand.instrument())));
      require(instruments.size() >= 3
              && actor.sustainTicks(5)
                  .blockAt(nStone.x(), nStone.y(), nStone.z())
                  .equals(new BlockState(25, 0)),
          "official note instruments absent: " + pStone.instrument() + "," + pWood.instrument()
              + "," + pSand.instrument());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(nStone.x(), cx), nStone.y(), local(nStone.z(), cz))
                  .equals(new BlockState(25, 0))
              && after.blockAt(local(nWood.x(), cx), nWood.y(), local(nWood.z(), cz))
                  .equals(new BlockState(25, 0))
              && after.blockAt(local(nSand.x(), cx), nSand.y(), local(nSand.z(), cz))
                  .equals(new BlockState(25, 0)),
          "persisted note instrument drift");
      String evidence = "column=" + column + ",bases=" + cell(stone, 1, 0) + "+" + cell(wood, 5, 0)
          + "+" + cell(sand, 12, 0) + ",notes=" + cell(nStone, 25, 0) + "+" + cell(nWood, 25, 0)
          + "+" + cell(nSand, 25, 0) + ",play=" + wire(pStone) + "+" + wire(pWood) + "+"
          + wire(pSand) + ",instruments=" + pStone.instrument() + "," + pWood.instrument() + ","
          + pSand.instrument() + ",persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+planks5+sand12+noteblock25x3|cause=packet15-item25-place+empty-hand-packet14-play|wire=packet54-instrument"
          + pStone.instrument() + "+" + pWood.instrument() + "+" + pSand.instrument()
          + "|oracle=official-note-instruments+fresh-login-block25|" + evidence;
      System.out.println("WORLDLINE_M313_INSTRUMENTS=" + evidence);
      System.out.println("WORLDLINE_M313_TRACE=" + trace);
      System.out.println("WORLDLINE_M313_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static RemoteNoteEvent play(B173WireClient a, BlockPosition note) throws Exception {
    a.beginBreak(note);
    RemoteNoteEvent click = B173NoteAccess.await(a);
    require(click.packetId() == 54 && click.position().equals(note) && click.pitch() == 0
            && a.awaitBlock(note, new BlockState(25, 0))
                .blockAt(note.x(), note.y(), note.z())
                .equals(new BlockState(25, 0)),
        "official note play absent: packet=" + click.packetId() + ",pos=" + click.position()
            + ",instrument=" + click.instrument() + ",pitch=" + click.pitch());
    return click;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic note instrument foundation");
  }
  private static String cell(BlockPosition p, int id, int meta) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
  }
  private static String wire(RemoteNoteEvent e) {
    return "packet" + e.packetId() + ":" + e.instrument() + ":" + e.pitch();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
