package worldline.smoke.b173specialcollisionphysical;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import worldline.b173server.B173ServerCollisionTestRuntimeProvider;
import worldline.b173server.B173ServerLightTestRuntimeProvider;
import worldline.b173server.B173ServerStateDomainTestRuntimeProvider;
import worldline.b173server.B173SpecialCollisionPhysicalScenarioFactory;
import worldline.testkit.BlockCollisionFamilyCycle;
import worldline.testkit.BlockLightFamilyCycle;
import worldline.testkit.BlockStateDomainFamilyCycle;

/** Qualifies one special-collision physical envelope across three public TestKit dimensions. */
public final class B173SpecialCollisionPhysicalEnvelopeSmoke {
    private static final String FAMILY = "special-collision-envelope";
    private static final String SERVER_PROPERTY = "worldline.b173.lifecycle.serverJar";
    private static final String STATE =
            "827ce95011f52e6fbe8ef776b4320729f78d447eb9af30bbd19565359f26f8d9";
    private static final String COLLISION =
            "ee69dd568f1be75709409186d37769c8e95743f88654d8c8e2e45af97a61f3c3";
    private static final String LIGHT =
            "ed8a42ebf9f0c9201027a5dc1ba4cbbde64d75140f5ad4b0a4cd38b74ce305d8";

    private B173SpecialCollisionPhysicalEnvelopeSmoke() {
    }

    public static void main(String[] arguments) throws Exception {
        BlockStateDomainFamilyCycle.run(scoped(arguments, "state-domain"), FAMILY,
                B173SpecialCollisionPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerStateDomainTestRuntimeProvider(),
                B173SpecialCollisionPhysicalScenarioFactory.stateDomains());
        BlockCollisionFamilyCycle.run(scoped(arguments, "collision-shape"), FAMILY,
                B173SpecialCollisionPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerCollisionTestRuntimeProvider(),
                B173SpecialCollisionPhysicalScenarioFactory.collisions());
        BlockLightFamilyCycle.run(scoped(arguments, "light-behavior"), FAMILY,
                B173SpecialCollisionPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerLightTestRuntimeProvider(),
                B173SpecialCollisionPhysicalScenarioFactory.lights());
        String signal = "family=special-collision-physical-envelope,subjects=4,claims=12,layers=3"
                + ",reload=FRESH_LOGINx12,state=" + STATE + ",collision=" + COLLISION
                + ",light=" + LIGHT;
        String trace = "v1|server=official-b1.7.3|seed=17320110707|family="
                + "special-collision-physical-envelope|subjects=4|claims=12|layers="
                + "state-domain,collision-shape,light-behavior|state=" + STATE
                + "|collision=" + COLLISION + "|light=" + LIGHT
                + "|oracle=three-public-family-signatures";
        System.out.println("WORLDLINE_B173_STATIC_PHYSICAL_SET=" + signal);
        System.out.println("WORLDLINE_B173_STATIC_PHYSICAL_TRACE=" + trace);
        System.out.println("WORLDLINE_B173_STATIC_PHYSICAL_SIGNATURE=" + sha(trace));
    }

    private static String[] scoped(String[] arguments, String layer) {
        if (arguments.length != 4) {
            throw new IllegalArgumentException(
                    "usage: special collision physical family server.jar workspace port seed");
        }
        String[] result = arguments.clone();
        result[1] = Paths.get(arguments[1]).resolve(layer).toString();
        return result;
    }

    private static String sha(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : bytes) {
            result.append(String.format("%02x", item & 255));
        }
        return result.toString();
    }
}
