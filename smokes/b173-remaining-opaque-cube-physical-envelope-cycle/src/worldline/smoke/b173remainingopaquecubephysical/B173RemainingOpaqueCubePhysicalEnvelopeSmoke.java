package worldline.smoke.b173remainingopaquecubephysical;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import worldline.b173server.B173RemainingOpaqueCubePhysicalScenarioFactory;
import worldline.b173server.B173ServerCollisionTestRuntimeProvider;
import worldline.b173server.B173ServerLightTestRuntimeProvider;
import worldline.b173server.B173ServerStateDomainTestRuntimeProvider;
import worldline.testkit.BlockCollisionFamilyCycle;
import worldline.testkit.BlockLightFamilyCycle;
import worldline.testkit.BlockStateDomainFamilyCycle;

/** Qualifies remaining opaque-cube physical envelopes across three public TestKit dimensions. */
public final class B173RemainingOpaqueCubePhysicalEnvelopeSmoke {
    private static final String FAMILY = "remaining-opaque-cube-envelope";
    private static final String SERVER_PROPERTY = "worldline.b173.lifecycle.serverJar";
    private static final String STATE =
            "fa2e04cf670b72003243e3ba325d4a0645226113d785c8562225ccac120788d2";
    private static final String COLLISION =
            "a201a281cfcffc63a733d91e79efc2de78b034e702f115cb6b390895cf2b1c8a";
    private static final String LIGHT =
            "2d3aa97160e24e2035804088276b0c0d47fefd425806be2a6c5a5f9784ffaa9e";

    private B173RemainingOpaqueCubePhysicalEnvelopeSmoke() {
    }

    public static void main(String[] arguments) throws Exception {
        BlockStateDomainFamilyCycle.run(scoped(arguments, "state-domain"), FAMILY,
                B173RemainingOpaqueCubePhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerStateDomainTestRuntimeProvider(),
                B173RemainingOpaqueCubePhysicalScenarioFactory.stateDomains());
        BlockCollisionFamilyCycle.run(scoped(arguments, "collision-shape"), FAMILY,
                B173RemainingOpaqueCubePhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerCollisionTestRuntimeProvider(),
                B173RemainingOpaqueCubePhysicalScenarioFactory.collisions());
        BlockLightFamilyCycle.run(scoped(arguments, "light-behavior"), FAMILY,
                B173RemainingOpaqueCubePhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerLightTestRuntimeProvider(),
                B173RemainingOpaqueCubePhysicalScenarioFactory.lights());
        String signal = "family=remaining-opaque-cube-physical-envelope,subjects=4,claims=12,layers=3"
                + ",reload=FRESH_LOGINx12,state=" + STATE + ",collision=" + COLLISION
                + ",light=" + LIGHT;
        String trace = "v1|server=official-b1.7.3|seed=17320110707|family="
                + "remaining-opaque-cube-physical-envelope|subjects=4|claims=12|layers="
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
                    "usage: remaining opaque cube family server.jar workspace port seed");
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
