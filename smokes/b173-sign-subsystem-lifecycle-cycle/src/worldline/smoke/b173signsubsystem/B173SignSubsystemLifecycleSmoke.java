package worldline.smoke.b173signsubsystem;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import worldline.b173server.B173SignSubsystemScenario;
import worldline.testkit.SignSubsystemEvidence;
import worldline.testkit.SignSubsystemFixture;

/** Qualifies both native sign variants through one public official-server subsystem. */
public final class B173SignSubsystemLifecycleSmoke {
    private B173SignSubsystemLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: sign subsystem server.jar workspace port seed");
        Path server = Paths.get(arguments[0]).toAbsolutePath().normalize();
        Path workspace = Paths.get(arguments[1]).toAbsolutePath().normalize();
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        SignSubsystemEvidence evidence = SignSubsystemFixture.execute(
                new B173SignSubsystemScenario(server, workspace, port, seed));
        String evidenceHash = sha(evidence.canonical());
        String signal = "family=sign-subsystem-lifecycle,claims=13,standing-domain=0..15,"
                + "placed=63:4+68:5,inventory=20->19,break=63:0->0:0,drop=323x1:0,"
                + "collision=UU,light=0:15x2,tick=240,reload=FRESH_LOGINx2,"
                + "support=63:4+68:5->0:0+0:0,evidence=" + evidenceHash;
        String trace = "v1|server=official-b1.7.3|seed=" + seed
                + "|family=sign-subsystem-lifecycle"
                + "|actions=place-domain+break-drop+place-pair+packet130+fresh-login"
                + "+collision+light+idle+break-supports+fresh-login"
                + "|oracle=public-sign-subsystem-evidence|evidence=" + evidenceHash;
        System.out.println("WORLDLINE_B173_SIGN_SUBSYSTEM_SET=" + signal);
        System.out.println("WORLDLINE_B173_SIGN_SUBSYSTEM_TRACE=" + trace);
        System.out.println("WORLDLINE_B173_SIGN_SUBSYSTEM_SIGNATURE=" + sha(trace));
    }

    private static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : digest) result.append(String.format("%02x", item & 0xff));
        return result.toString();
    }
}
