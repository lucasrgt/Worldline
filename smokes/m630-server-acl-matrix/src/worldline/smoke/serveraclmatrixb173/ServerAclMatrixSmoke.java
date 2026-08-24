package worldline.smoke.serveraclmatrixb173;
import static worldline.b173server.B173FixtureSupport.awaitPlayers;
import static worldline.b173server.B173FixtureSupport.sha;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173ServerAclAccess;
import worldline.b173server.B173WireClient;
import worldline.testkit.ServerAclFixture;

/** Proves player, operator, deoperator, kick, ban, and pardon ACL boundaries. */
public final class ServerAclMatrixSmoke {
  private static final String ACTOR = "AclActor630";
  private static final String KICKED = "AclKick630";
  private static final String BANNED = "AclBan630";
  private static final long REGULAR_TARGET = 200000L;
  private static final long OPERATOR_TARGET = 300000L;
  private static final long REVOKED_TARGET = 400000L;
  private ServerAclMatrixSmoke() { }

  public static void main(String[] a) throws Exception {
    if (a.length != 4) throw new IllegalArgumentException(
        "usage: ServerAclMatrixSmoke server.jar workspace port seed");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(
        jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = client(port, ACTOR, timeout);
    B173WireClient kicked = null, kickAgain = null, banned = null, pardonAgain = null;
    try {
      server.boot();
      B173PlayerSeed.write(workspace, ACTOR, 4.5D, 60D, 4.5D);
      B173PlayerSeed.write(workspace, KICKED, 4.5D, 60D, 4.5D);
      B173PlayerSeed.write(workspace, BANNED, 4.5D, 60D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      awaitPlayers(server, 1);

      actor.sendChat("/time set " + REGULAR_TARGET);
      String regularRecord = server.awaitPlayerCommand(
          ACTOR, "time set " + REGULAR_TARGET, false);
      long regularTime = saveTime(server);

      server.operator(ACTOR);
      actor.sendChat("/time set " + OPERATOR_TARGET);
      String operatorRecord = server.awaitPlayerCommand(
          ACTOR, "time set " + OPERATOR_TARGET, true);
      long operatorTime = saveTime(server);

      server.deoperator(ACTOR);
      actor.sendChat("/time set " + REVOKED_TARGET);
      String revokedRecord = server.awaitPlayerCommand(
          ACTOR, "time set " + REVOKED_TARGET, false);
      long revokedTime = saveTime(server);

      kicked = client(port, KICKED, timeout);
      kicked.connect();
      kicked.synchronizePose();
      awaitPlayers(server, 2);
      server.kick(KICKED);
      String kickDisconnect = B173ServerAclAccess.awaitDisconnect(kicked);
      kicked.close();
      awaitPlayers(server, 1);
      kickAgain = client(port, KICKED, timeout);
      kickAgain.connect();
      kickAgain.synchronizePose();
      awaitPlayers(server, 2);
      kickAgain.close();
      awaitPlayers(server, 1);

      banned = client(port, BANNED, timeout);
      banned.connect();
      banned.synchronizePose();
      awaitPlayers(server, 2);
      server.ban(BANNED);
      String banDisconnect = B173ServerAclAccess.awaitDisconnect(banned);
      banned.close();
      awaitPlayers(server, 1);
      String banRejection = B173ServerAclAccess.loginRejection(
          "127.0.0.1", port, BANNED, timeout);
      server.pardon(BANNED);
      pardonAgain = client(port, BANNED, timeout);
      pardonAgain.connect();
      pardonAgain.synchronizePose();
      awaitPlayers(server, 2);
      pardonAgain.close();
      awaitPlayers(server, 1);

      ServerAclFixture.Evidence evidence = ServerAclFixture.observe(
          regularRecord, regularTime, REGULAR_TARGET,
          operatorRecord, operatorTime, OPERATOR_TARGET,
          revokedRecord, revokedTime, REVOKED_TARGET,
          kickDisconnect, true, banDisconnect, banRejection, true);
      require(evidence.regularDenied() && evidence.operatorAllowed()
              && evidence.deoperatorDenied() && evidence.kickReconnect()
              && evidence.banRejected() && evidence.pardonReconnect(),
          "server ACL fixture evidence drifted");
      String signal = "regular=time-denied,op=time-allowed,deop=time-denied,"
          + "kick=disconnect+reconnect,ban=disconnect+relogin-denied,"
          + "pardon=relogin-allowed,identities=3,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=preseeded-acl-actor+kick-target+ban-target"
          + "|cause=player-time+console-op-deop-kick-ban-pardon"
          + "|wire=server-log-tried-or-issued+packet255-disconnect+login-rejection"
          + "|oracle=role-command-and-session-acl-matrix|" + signal;
      System.out.println("WORLDLINE_M630_ACL=" + signal);
      System.out.println("WORLDLINE_M630_TRACE=" + trace);
      System.out.println("WORLDLINE_M630_SIGNATURE=" + sha(trace));
    } finally {
      close(pardonAgain);
      close(banned);
      close(kickAgain);
      close(kicked);
      actor.close();
      server.close();
    }
  }

  private static B173WireClient client(int port, String username, Duration timeout) {
    return new B173WireClient("127.0.0.1", port, username, timeout);
  }
  private static long saveTime(B173DedicatedServer server) {
    server.save();
    return server.state().worldTime();
  }
  private static void close(B173WireClient client) {
    if (client != null) client.close();
  }
  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }
}
