package worldline.smoke.b173commoncubephysical;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import worldline.b173server.B173CommonCubePhysicalScenarioFactory;
import worldline.b173server.B173ServerCollisionTestRuntimeProvider;
import worldline.b173server.B173ServerLightTestRuntimeProvider;
import worldline.b173server.B173ServerStateDomainTestRuntimeProvider;
import worldline.testkit.BlockCollisionFamilyCycle;
import worldline.testkit.BlockLightFamilyCycle;
import worldline.testkit.BlockStateDomainFamilyCycle;

/** Qualifies one common-cube physical envelope across three public TestKit dimensions. */
public final class B173CommonCubePhysicalEnvelopeSmoke {
    private static final String FAMILY = "common-cube-envelope";
    private static final String SERVER_PROPERTY = "worldline.b173.lifecycle.serverJar";
    private static final String STATE =
            "c4de617bddd74f0bc494b4dc7943200070ad7d03394660e4334b6e092de82a7b";
    private static final String COLLISION =
            "0cf7880f196131cb38b705aa1d2b75a7901844d49487d24f60fce78108c54157";
    private static final String LIGHT =
            "68697b917ee58cf227a7967d3c951b6a86a623bc6f27af818bb25e5544deaff7";

    private B173CommonCubePhysicalEnvelopeSmoke() {
    }

    public static void main(String[] arguments) throws Exception {
        BlockStateDomainFamilyCycle.run(scoped(arguments, "state-domain"), FAMILY,
                B173CommonCubePhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerStateDomainTestRuntimeProvider(),
                B173CommonCubePhysicalScenarioFactory.stateDomains());
        BlockCollisionFamilyCycle.run(scoped(arguments, "collision-shape"), FAMILY,
                B173CommonCubePhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerCollisionTestRuntimeProvider(),
                B173CommonCubePhysicalScenarioFactory.collisions());
        BlockLightFamilyCycle.run(scoped(arguments, "light-behavior"), FAMILY,
                B173CommonCubePhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerLightTestRuntimeProvider(),
                B173CommonCubePhysicalScenarioFactory.lights());
        String signal = "family=common-cube-physical-envelope,subjects=5,claims=15,layers=3"
                + ",reload=FRESH_LOGINx15,state=" + STATE + ",collision=" + COLLISION
                + ",light=" + LIGHT;
        String trace = "v1|server=official-b1.7.3|seed=17320110707|family="
                + "common-cube-physical-envelope|subjects=5|claims=15|layers="
                + "state-domain,collision-shape,light-behavior|state=" + STATE
                + "|collision=" + COLLISION + "|light=" + LIGHT
                + "|oracle=three-public-family-signatures";
        System.out.println("WORLDLINE_B173_COMMON_CUBE_PHYSICAL_SET=" + signal);
        System.out.println("WORLDLINE_B173_COMMON_CUBE_PHYSICAL_TRACE=" + trace);
        System.out.println("WORLDLINE_B173_COMMON_CUBE_PHYSICAL_SIGNATURE=" + sha(trace));
    }

    private static String[] scoped(String[] arguments, String layer) {
        if (arguments.length != 4) {
            throw new IllegalArgumentException(
                    "usage: common cube physical family server.jar workspace port seed");
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
