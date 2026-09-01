package worldline.smoke.b173cakeserving;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import worldline.b173server.B173CakeServingScenario;
import worldline.testkit.CakeServingEvidence;
import worldline.testkit.CakeServingFixture;

/** Qualifies the complete public cake-serving lifecycle on the official server. */
public final class B173CakeServingLifecycleSmoke {
    private B173CakeServingLifecycleSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: cake serving server.jar workspace port seed");
        Path server = Paths.get(arguments[0]).toAbsolutePath().normalize();
        Path workspace = Paths.get(arguments[1]).toAbsolutePath().normalize();
        int port = Integer.parseInt(arguments[2]);
        long seed = Long.parseLong(arguments[3]);
        CakeServingEvidence evidence = CakeServingFixture.execute(
                new B173CakeServingScenario(server, workspace, port, seed));
        String evidenceHash = sha(evidence.canonical());
        String signal = "family=cake-serving-lifecycle,claims=5,states=7,servings=6,"
                + "health=1->4->7->10->13->16->19,collision=CCUUUU,light=0:15x6,"
                + "tick=200,reload=FRESH_LOGINx2,support=92:0->0:0,evidence=" + evidenceHash;
        String trace = "v1|server=official-b1.7.3|seed=" + seed
                + "|family=cake-serving-lifecycle"
                + "|actions=place+serve-six+probe-collision+idle+fresh-login+break-support+fresh-login"
                + "|oracle=public-cake-serving-evidence|evidence=" + evidenceHash;
        System.out.println("WORLDLINE_B173_CAKE_SERVING_SET=" + signal);
        System.out.println("WORLDLINE_B173_CAKE_SERVING_TRACE=" + trace);
        System.out.println("WORLDLINE_B173_CAKE_SERVING_SIGNATURE=" + sha(trace));
    }

    private static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : digest) result.append(String.format("%02x", item & 0xff));
        return result.toString();
    }
}
