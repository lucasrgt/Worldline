package worldline.smoke.notepitchladderb173;

import static worldline.b173server.B173FixtureSupport.awaitPlayers;
import static worldline.b173server.B173FixtureSupport.local;
import static worldline.b173server.B173FixtureSupport.place;
import static worldline.b173server.B173FixtureSupport.sha;
import static worldline.b173server.B173FixtureSupport.water;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteNoteEvent;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173NoteAccess;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.testkit.NotePitchFixture;

/**
 * Tunes an official note block through the full twenty-five-pitch ladder plus
 * one confirmation click past the wrap using empty-hand block activation, then
 * proves the retained pitch across a save-stop-restart reload.
 */
public final class NotePitchLadderSmoke {
  private static final String USER = "NotePitch645";
  private NotePitchLadderSmoke() {
  }

  public static void main(String[] a) throws Exception {
    if (a.length != 4) throw new IllegalArgumentException(
        "usage: NotePitchLadderSmoke server.jar workspace port seed");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    Duration timeout = Duration.ofSeconds(120);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173DedicatedServer reloaded = null;
    B173WireClient actor = new B173WireClient("127.0.0.1", port, USER, timeout), reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, USER, 4.5D, 60D, 4.5D,
          new int[] {0, 1}, new int[] {1, 25}, new int[] {64, 1}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "note ladder inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(0, 0).chunkAt(0, 0);
      BlockPosition top = foundation(initial);
      actor.selectHeldSlot(0);
      int column = 0;
      while (water(initial.blockAt(local(top.x(), 0), top.y() + 1, local(top.z(), 0)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded note ladder fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
      }
      actor.selectHeldSlot(1);
      BlockPosition note = place(actor, top, BlockFace.UP, 25);
      actor.selectHeldSlot(2);
      List<RemoteNoteEvent> ladder = new ArrayList<RemoteNoteEvent>();
      for (int click = 0; click <= NotePitchFixture.PITCHES; click++) {
        actor.activateBlock(note, BlockFace.UP);
        ladder.add(B173NoteAccess.await(actor));
      }
      require(actor.awaitBlock(note, new BlockState(25, 0))
                  .blockAt(note.x(), note.y(), note.z())
                  .equals(new BlockState(25, 0)),
          "tuned note block drifted during the ladder");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      server.close();
      reloaded = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
      reloaded.boot();
      reader = new B173WireClient("127.0.0.1", port, USER, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(0, 0).chunkAt(0, 0);
      require(after.blockAt(local(note.x(), 0), note.y(), local(note.z(), 0))
                  .equals(new BlockState(25, 0)),
          "reloaded note block drifted");
      reader.selectHeldSlot(2);
      reader.activateBlock(note, BlockFace.UP);
      NotePitchFixture.Evidence evidence =
          NotePitchFixture.cycle(ladder, B173NoteAccess.await(reader));
      require(evidence.instrument() == 1 && evidence.retainedPitch() == 2,
          "official stone-supported ladder evidence drifted: instrument "
              + evidence.instrument() + ", retained " + evidence.retainedPitch());
      String signal = "seed=" + seed + ",clicks=" + evidence.clicks()
          + ",ladder=pitches1-24-wrap0,instrument=" + evidence.instrument()
          + ",retained=" + evidence.retainedPitch()
          + ",persisted=true,replicas=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone-column+noteblock25-on-stone"
          + "|cause=packet15-item25-place+empty-hand-packet15-activation-x26"
          + "|wire=packet54-pitch-1..24+packet54-wrap-pitch-0+packet54-retained-pitch-2"
          + "|oracle=save-stop-restart-fresh-login-tileentity|" + signal;
      System.out.println("WORLDLINE_M645_NOTE=" + signal);
      System.out.println("WORLDLINE_M645_TRACE=" + trace);
      System.out.println("WORLDLINE_M645_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null) reader.close();
      if (reloaded != null) reloaded.close();
      server.close();
    }
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(x, y, z);
    throw new IllegalStateException("no deterministic note ladder foundation");
  }
  private static void require(boolean v, String m) {
    if (!v) throw new IllegalStateException(m);
  }
}
