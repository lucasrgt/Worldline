package worldline.smoke.serveradmissionb173;

import static worldline.b173server.B173FixtureSupport.sha;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import worldline.b173server.B173AdmissionServer;
import worldline.b173server.B173ServerAclAccess;
import worldline.b173server.B173WireClient;
import worldline.testkit.ServerEntryPolicyFixture;

/** Freezes whitelist and one-slot capacity admission from official handshakes. */
public final class ServerAdmissionSmoke {
    private static final String LISTED = "Listed656";
    private static final String UNLISTED = "Unlisted656";
    private static final String OVERFLOW = "Overflow656";
    private ServerAdmissionSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 4) {
            throw new IllegalArgumentException(
                    "usage: ServerAdmissionSmoke server.jar workspace port seed");
        }
        Path jar = Paths.get(arguments[0]);
        Path workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        Duration timeout = Duration.ofSeconds(90);
        B173AdmissionServer restricted = new B173AdmissionServer(
                jar, workspace, port, seed, timeout, 1, true,
                Arrays.asList(LISTED, OVERFLOW));
        B173AdmissionServer open = null;
        B173WireClient listed = client(port, LISTED, timeout);
        B173WireClient unlisted = null;
        try {
            restricted.boot();
            String whitelistRejection = B173ServerAclAccess.loginRejection(
                    "127.0.0.1", port, UNLISTED, timeout);
            restricted.awaitPlayers(0);

            listed.connect();
            listed.synchronizePose();
            restricted.awaitPlayers(1);
            boolean listedAccepted = restricted.players().contains(LISTED);
            String capacityRejection = B173ServerAclAccess.loginRejection(
                    "127.0.0.1", port, OVERFLOW, timeout);
            restricted.awaitPlayers(1);
            listed.close();
            restricted.awaitPlayers(0);
            restricted.close();

            open = new B173AdmissionServer(jar, workspace, port, seed, timeout,
                    1, false, Collections.emptyList());
            open.boot();
            unlisted = client(port, UNLISTED, timeout);
            unlisted.connect();
            unlisted.synchronizePose();
            open.awaitPlayers(1);
            boolean openAccepted = open.players().contains(UNLISTED);

            ServerEntryPolicyFixture.Evidence evidence =
                    ServerEntryPolicyFixture.observe(1, 3, whitelistRejection,
                            listedAccepted, capacityRejection, openAccepted);
            String signal = "whitelist=unlisted-rejected+listed-accepted"
                    + ",capacity=listed-overflow-rejected"
                    + ",disabled=unlisted-accepted,max=1,identities=3,disconnect=clean";
            String trace = "v1|server=official-b1.7.3|seed=" + seed
                    + "|fixture=whitelist-on+one-slot+two-listed-identities"
                    + "|cause=unlisted-login+listed-login+listed-overflow"
                    + "+restart-whitelist-off+same-unlisted-login"
                    + "|wire=protocol14-login+packet255-rejections+player-census"
                    + "|oracle=membership-and-capacity-entry-policy|" + signal;
            require(evidence.unlistedRejected() && evidence.listedAccepted()
                            && evidence.overflowRejected() && evidence.openAccepted(),
                    "server entry policy evidence drifted");
            unlisted.close();
            open.awaitPlayers(0);
            System.out.println("WORLDLINE_M656_ADMISSION=" + signal);
            System.out.println("WORLDLINE_M656_TRACE=" + trace);
            System.out.println("WORLDLINE_M656_SIGNATURE=" + sha(trace));
        } finally {
            if (unlisted != null) {
                unlisted.close();
            }
            listed.close();
            if (open != null) {
                open.close();
            }
            restricted.close();
        }
    }

    private static B173WireClient client(int port, String username, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, username, timeout);
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
