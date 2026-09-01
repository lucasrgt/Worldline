package worldline.smoke.b173inertsolidphysical;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import worldline.b173server.B173InertSolidPhysicalScenarioFactory;
import worldline.b173server.B173ServerCollisionTestRuntimeProvider;
import worldline.b173server.B173ServerLightTestRuntimeProvider;
import worldline.b173server.B173ServerStateDomainTestRuntimeProvider;
import worldline.testkit.BlockCollisionFamilyCycle;
import worldline.testkit.BlockLightFamilyCycle;
import worldline.testkit.BlockStateDomainFamilyCycle;

/** Qualifies one inert-solid physical envelope across three public TestKit dimensions. */
public final class B173InertSolidPhysicalEnvelopeSmoke {
    private static final String FAMILY = "inert-solid-envelope";
    private static final String SERVER_PROPERTY = "worldline.b173.lifecycle.serverJar";
    private static final String STATE =
            "59bbacb0e907dd06fcf3f8b51ffe204f6b8daf5a29627d4de292b3d4d413932f";
    private static final String COLLISION =
            "d922a045a0b4e376716b9cf6f5c0897c72b751b3c24a12d206c77de3b8659c4d";
    private static final String LIGHT =
            "eb712a7a3fc12f3a84d0d1d4a106175bed3b49a0e3d36c0ba114834436852c36";

    private B173InertSolidPhysicalEnvelopeSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockStateDomainFamilyCycle.run(scoped(arguments, "state-domain"), FAMILY,
                B173InertSolidPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerStateDomainTestRuntimeProvider(),
                B173InertSolidPhysicalScenarioFactory.stateDomains());
        BlockCollisionFamilyCycle.run(scoped(arguments, "collision-shape"), FAMILY,
                B173InertSolidPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerCollisionTestRuntimeProvider(),
                B173InertSolidPhysicalScenarioFactory.collisions());
        BlockLightFamilyCycle.run(scoped(arguments, "light-behavior"), FAMILY,
                B173InertSolidPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerLightTestRuntimeProvider(),
                B173InertSolidPhysicalScenarioFactory.lights());
        String signal = "family=inert-solid-physical-envelope,subjects=17,claims=51,layers=3"
                + ",reload=FRESH_LOGINx51,state=" + STATE + ",collision=" + COLLISION
                + ",light=" + LIGHT;
        String trace = "v1|server=official-b1.7.3|seed=17320110707|family="
                + "inert-solid-physical-envelope|subjects=17|claims=51|layers="
                + "state-domain,collision-shape,light-behavior|state=" + STATE
                + "|collision=" + COLLISION + "|light=" + LIGHT
                + "|oracle=three-public-family-signatures";
        System.out.println("WORLDLINE_B173_STATIC_PHYSICAL_SET=" + signal);
        System.out.println("WORLDLINE_B173_STATIC_PHYSICAL_TRACE=" + trace);
        System.out.println("WORLDLINE_B173_STATIC_PHYSICAL_SIGNATURE=" + sha(trace));
    }

    private static String[] scoped(String[] arguments, String layer) {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: inert solid physical family server.jar workspace port seed");
        String[] result = arguments.clone();
        result[1] = Paths.get(arguments[1]).resolve(layer).toString();
        return result;
    }

    private static String sha(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : bytes) result.append(String.format("%02x", item & 255));
        return result.toString();
    }
}
