package worldline.smoke.jukeboxejectsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places two official jukeboxes 84, inserts discs 2256 and 2257, then Packet14-breaks both to Packet21 ejects. */
public final class JukeboxEjectSetSmoke {
  private static final RemoteItemStack GOLD = new RemoteItemStack(2256, 1, 0),
                                       GREEN = new RemoteItemStack(2257, 1, 0),
                                       AXE = new RemoteItemStack(286, 1, 0);
  private static final BlockState AIR = new BlockState(0, 0);
  private JukeboxEjectSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: JukeboxEjectSetSmoke server.jar workspace port seed username chunkX chunkZ");
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
    BlockPosition top, east, gold, green;
    int column;
    RemoteNoteEvent goldPlay, greenPlay;
    RemoteDroppedItem goldDrop, greenDrop;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 84, 2256, 2257, 286}, new int[] {32, 2, 1, 1, 1},
          new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5
              && actor.inventory().slot(40).item().equals(AXE),
          "jukebox-eject-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded jukebox-eject-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      east = place(actor, top, BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      gold = place(actor, top, BlockFace.UP, 84);
      green = place(actor, east, BlockFace.UP, 84);
      goldPlay = insert(actor, gold, 2, 2256);
      greenPlay = insert(actor, green, 3, 2257);
      require(goldPlay.pitch() == 2256 && greenPlay.pitch() == 2257,
          "official jukebox-eject-set insert absent");
      goldDrop = eject(actor, gold, 2256, GOLD);
      greenDrop = eject(actor, green, 2257, GREEN);
      require(goldDrop.item().equals(GOLD) && greenDrop.item().equals(GREEN)
              && goldDrop.entityId() != greenDrop.entityId(),
          "official Packet21 disc ejects absent");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(gold.x(), cx), gold.y(), local(gold.z(), cz)).equals(AIR)
              && after.blockAt(local(green.x(), cx), green.y(), local(green.z(), cz)).equals(AIR),
          "persisted jukebox-eject-set air drift");
      String evidence = "column=" + column + ",support=" + cell(top, 1, 0) + "+" + cell(east, 1, 0)
          + ",jukebox=" + cell(gold, 84, 1) + "->0:0+" + cell(green, 84, 1)
          + "->0:0,disc=2256->empty+2257->empty,play=" + wire(goldPlay) + "+" + wire(greenPlay)
          + ",eject=packet21:2256+packet21:2257,persisted=air,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+jukebox84x2|cause=packet15-item84-place+packet15-disc2256+packet15-disc2257+packet14-goldaxe286|wire=packet61-instrument1005-pitch2256+packet61-instrument1005-pitch2257+packet21-2256+packet21-2257|oracle=official-jukebox-eject-set+fresh-login-air|"
          + evidence;
      System.out.println("WORLDLINE_M398_EJECTS=" + evidence);
      System.out.println("WORLDLINE_M398_TRACE=" + trace);
      System.out.println("WORLDLINE_M398_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static RemoteNoteEvent insert(B173WireClient a, BlockPosition box, int slot, int disc)
      throws Exception {
    a.selectHeldSlot(slot);
    require(!a.inventory().slot(36 + slot).empty()
            && a.inventory().slot(36 + slot).item().equals(new RemoteItemStack(disc, 1, 0)),
        "disc seed drift " + disc);
    a.useHeldItemOnBlock(box, BlockFace.UP);
    RemoteNoteEvent click = worldline.test.WorldlineSmokeAwait.awaitEntity(a,
        ()
            -> B173NoteAccess.poll(a),
        value
        -> value != null && a.inventory().slot(36 + slot).empty(),
        "record insertion event", 40);
    BlockState live =
        worldline.test.WorldlineSmokeAwait.awaitBlock(a, box, new BlockState(84, 1), 40)
            .blockAt(box.x(), box.y(), box.z());
    boolean played = click.packetId() == 61 && click.instrument() == 1005 && click.pitch() == disc
        && click.position().equals(box);
    require(live.equals(new BlockState(84, 1)) && played && a.inventory().slot(36 + slot).empty(),
        "official record insert absent: disc=" + disc + ",live=" + live + ",click=" + click);
    return click;
  }
  private static RemoteDroppedItem eject(
      B173WireClient a, BlockPosition box, int disc, RemoteItemStack expected) throws Exception {
    require(worldline.test.WorldlineSmokeAwait.observe(a, 1)
                .blockAt(box.x(), box.y(), box.z())
                .equals(new BlockState(84, 1)),
        "playing jukebox drift before eject " + disc);
    a.selectHeldSlot(4);
    a.beginBreak(box);
    worldline.test.WorldlineSmokeAwait.observe(a, 12);
    a.finishBreak(box);
    a.awaitBlock(box, AIR);
    RemoteDroppedItem drop = a.peekDroppedItem(expected);
    if (drop == null)
      drop = a.awaitDroppedItem(expected);
    require(drop.item().equals(expected) && drop.item().legacyId() == disc
            && drop.item().count() == 1
            && worldline.test.WorldlineSmokeAwait.observe(a, 1)
                .blockAt(box.x(), box.y(), box.z())
                .equals(AIR),
        "Packet21 eject " + disc + " absent");
    return drop;
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
    throw new IllegalStateException("no deterministic jukebox-eject-set foundation");
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
