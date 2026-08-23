package worldline.smoke.noterestinstrumentsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import worldline.api.*;
import worldline.b173server.*;

/** Plays official note block 25 on glass and gold and captures Packet54 ids outside M313. */
public final class NoteRestInstrumentsSmoke {
  private NoteRestInstrumentsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: NoteRestInstrumentsSmoke server.jar workspace port seed username chunkX chunkZ");
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
    BlockPosition stone, glass, gold, nGlass, nGold;
    RemoteNoteEvent pGlass, pGold;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 20, 41, 25}, new int[] {32, 1, 1, 2}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4, "note rest instrument inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      stone = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (water(
          initial.blockAt(local(stone.x(), cx), stone.y() + 1, local(stone.z(), cz)).legacyId())) {
        stone = place(actor, stone, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded note rest instrument fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        stone = place(actor, stone, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      glass = place(actor, stone, BlockFace.EAST, 20);
      actor.selectHeldSlot(2);
      gold = place(actor, glass, BlockFace.EAST, 41);
      actor.selectHeldSlot(3);
      nGlass = place(actor, glass, BlockFace.UP, 25);
      nGold = place(actor, gold, BlockFace.UP, 25);
      actor.selectHeldSlot(4);
      pGlass = play(actor, nGlass);
      pGold = play(actor, nGold);
      Set<Integer> instruments = new LinkedHashSet<Integer>(
          Arrays.asList(Integer.valueOf(pGlass.instrument()), Integer.valueOf(pGold.instrument())));
      require(instruments.size() >= 2 && instruments.contains(Integer.valueOf(3))
              && !instruments.contains(Integer.valueOf(1))
              && !instruments.contains(Integer.valueOf(4))
              && !instruments.contains(Integer.valueOf(2))
              && actor.sustainTicks(5)
                  .blockAt(nGlass.x(), nGlass.y(), nGlass.z())
                  .equals(new BlockState(25, 0)),
          "official note rest instruments absent: " + pGlass.instrument() + ","
              + pGold.instrument());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(nGlass.x(), cx), nGlass.y(), local(nGlass.z(), cz))
                  .equals(new BlockState(25, 0))
              && after.blockAt(local(nGold.x(), cx), nGold.y(), local(nGold.z(), cz))
                  .equals(new BlockState(25, 0)),
          "persisted note rest instrument drift");
      String evidence = "column=" + column + ",bases=" + cell(glass, 20, 0) + "+"
          + cell(gold, 41, 0) + ",notes=" + cell(nGlass, 25, 0) + "+" + cell(nGold, 25, 0)
          + ",play=" + wire(pGlass) + "+" + wire(pGold) + ",instruments=" + pGlass.instrument()
          + "," + pGold.instrument() + ",persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+glass20+goldblock41+noteblock25x2|cause=packet15-item25-place+empty-hand-packet14-play|wire=packet54-instrument"
          + pGlass.instrument() + "+" + pGold.instrument()
          + "|oracle=official-note-rest-instruments+fresh-login-block25|" + evidence;
      System.out.println("WORLDLINE_M355_INSTRUMENTS=" + evidence);
      System.out.println("WORLDLINE_M355_TRACE=" + trace);
      System.out.println("WORLDLINE_M355_SIGNATURE=" + sha(trace));
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
        "official note rest play absent: packet=" + click.packetId() + ",pos=" + click.position()
            + ",instrument=" + click.instrument() + ",pitch=" + click.pitch());
    return click;
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic note rest instrument foundation");
  }
  private static String cell(BlockPosition p, int id, int meta) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
  }
  private static String wire(RemoteNoteEvent e) {
    return "packet" + e.packetId() + ":" + e.instrument() + ":" + e.pitch();
  }
  private static boolean water(int id) {
    return id == 8 || id == 9;
  }
  private static int local(int v, int c) {
    return v - c * 16;
  }
  private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
    long e = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < e) {
      if (s.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  private static String sha(String s) throws Exception {
    byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    StringBuilder v = new StringBuilder();
    for (byte x : b)
      v.append(String.format("%02x", x & 255));
    return v.toString();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
