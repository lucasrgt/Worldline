package worldline.smoke.b173groundfloraconformance;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import worldline.b173server.B173GroundFloraPhysicalScenarioFactory;
import worldline.b173server.B173GroundFloraSupportLossScenarioFactory;
import worldline.b173server.B173ServerCollisionTestRuntimeProvider;
import worldline.b173server.B173ServerLifecycleTestRuntimeProvider;
import worldline.b173server.B173ServerLightTestRuntimeProvider;
import worldline.b173server.B173ServerStateDomainTestRuntimeProvider;
import worldline.testkit.BlockCollisionFamilyCycle;
import worldline.testkit.BlockLightFamilyCycle;
import worldline.testkit.BlockStateDomainFamilyCycle;
import worldline.testkit.BlockSupportLossFamilyCycle;

/** Qualifies the small ground-flora conformance subsystem through four public families. */
public final class B173GroundFloraConformanceSmoke {
    private static final String PHYSICAL_FAMILY = "ground-flora-envelope";
    private static final String SUPPORT_FAMILY = "ground-flora-support";
    private static final String SERVER_PROPERTY = "worldline.b173.lifecycle.serverJar";
    private static final String STATE =
            "4c5474b8302148f24c66eb117dee1e34723bee34560a1a7640ae7913f117d57f";
    private static final String COLLISION =
            "1ed214c5a9814940e9159faecdd4fb46c203549f06821e2673f82e2e6917e386";
    private static final String LIGHT =
            "c33ecd57cb22064f008e31485c66596b339a6ccd17183a5dfe1d647987baa31a";
    private static final String SUPPORT =
            "7d5cfbdca87c219229e5f394ba53e41eeafbf68789cbc5fc90c115336334beac";

    private B173GroundFloraConformanceSmoke() {
    }

    public static void main(String[] arguments) throws Exception {
        BlockStateDomainFamilyCycle.run(scoped(arguments, "state-domain"), PHYSICAL_FAMILY,
                B173GroundFloraPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerStateDomainTestRuntimeProvider(),
                B173GroundFloraPhysicalScenarioFactory.stateDomains());
        BlockCollisionFamilyCycle.run(scoped(arguments, "collision-shape"), PHYSICAL_FAMILY,
                B173GroundFloraPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerCollisionTestRuntimeProvider(),
                B173GroundFloraPhysicalScenarioFactory.collisions());
        BlockLightFamilyCycle.run(scoped(arguments, "light-behavior"), PHYSICAL_FAMILY,
                B173GroundFloraPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerLightTestRuntimeProvider(),
                B173GroundFloraPhysicalScenarioFactory.lights());
        BlockSupportLossFamilyCycle.run(scoped(arguments, "support-loss"), SUPPORT_FAMILY,
                B173GroundFloraSupportLossScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerLifecycleTestRuntimeProvider(),
                B173GroundFloraSupportLossScenarioFactory.rows());
        String signal = "family=ground-flora-conformance,subjects=4,claims=19,"
                + "state-subjects=3,collision=4,light=4,tick=4,neighbor=4,tick-window=240,"
                + "reload=FRESH_LOGINx15,state=" + STATE + ",collision-signature=" + COLLISION
                + ",light-signature=" + LIGHT + ",support=" + SUPPORT;
        String trace = "v1|server=official-b1.7.3|seed=17320110707|family="
                + "ground-flora-conformance|subjects=37,38,31,32|claims=19|layers="
                + "state-domain,collision-shape,light-behavior,tick-policy,neighbor-response"
                + "|state=" + STATE + "|collision=" + COLLISION + "|light=" + LIGHT
                + "|support=" + SUPPORT + "|oracle=four-public-family-signatures";
        System.out.println("WORLDLINE_B173_GROUND_FLORA_SET=" + signal);
        System.out.println("WORLDLINE_B173_GROUND_FLORA_TRACE=" + trace);
        System.out.println("WORLDLINE_B173_GROUND_FLORA_SIGNATURE=" + sha(trace));
    }

    private static String[] scoped(String[] arguments, String layer) {
        if (arguments.length != 4) {
            throw new IllegalArgumentException(
                    "usage: ground flora conformance server.jar workspace port seed");
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
