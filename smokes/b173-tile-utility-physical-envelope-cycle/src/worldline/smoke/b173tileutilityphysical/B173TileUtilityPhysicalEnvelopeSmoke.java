package worldline.smoke.b173tileutilityphysical;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import worldline.b173server.B173ServerCollisionTestRuntimeProvider;
import worldline.b173server.B173ServerLightTestRuntimeProvider;
import worldline.b173server.B173ServerStateDomainTestRuntimeProvider;
import worldline.b173server.B173TileUtilityPhysicalScenarioFactory;
import worldline.testkit.BlockCollisionFamilyCycle;
import worldline.testkit.BlockLightFamilyCycle;
import worldline.testkit.BlockStateDomainFamilyCycle;

/** Qualifies six tile-backed utilities across the public physical TestKit families. */
public final class B173TileUtilityPhysicalEnvelopeSmoke {
    private static final String FAMILY = "tile-utility-envelope";
    private static final String SERVER_PROPERTY = "worldline.b173.lifecycle.serverJar";
    private static final String STATE =
            "0000000000000000000000000000000000000000000000000000000000000000";
    private static final String COLLISION =
            "0000000000000000000000000000000000000000000000000000000000000000";
    private static final String LIGHT =
            "0000000000000000000000000000000000000000000000000000000000000000";

    private B173TileUtilityPhysicalEnvelopeSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockStateDomainFamilyCycle.run(scoped(arguments, "state-domain"), FAMILY,
                B173TileUtilityPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerStateDomainTestRuntimeProvider(),
                B173TileUtilityPhysicalScenarioFactory.stateDomains());
        BlockCollisionFamilyCycle.run(scoped(arguments, "collision-shape"), FAMILY,
                B173TileUtilityPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerCollisionTestRuntimeProvider(),
                B173TileUtilityPhysicalScenarioFactory.collisions());
        BlockLightFamilyCycle.run(scoped(arguments, "light-behavior"), FAMILY,
                B173TileUtilityPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerLightTestRuntimeProvider(),
                B173TileUtilityPhysicalScenarioFactory.lights());
        String signal = "family=tile-utility-physical-envelope,subjects=6,claims=17,layers=3"
                + ",reload=FRESH_LOGINx17,state=" + STATE + ",collision=" + COLLISION
                + ",light=" + LIGHT;
        String trace = "v1|server=official-b1.7.3|seed=17320110707|family="
                + "tile-utility-physical-envelope|subjects=6|claims=17|layers="
                + "state-domain,collision-shape,light-behavior|state=" + STATE
                + "|collision=" + COLLISION + "|light=" + LIGHT
                + "|oracle=three-public-family-signatures";
        System.out.println("WORLDLINE_B173_TILE_UTILITY_PHYSICAL_SET=" + signal);
        System.out.println("WORLDLINE_B173_TILE_UTILITY_PHYSICAL_TRACE=" + trace);
        System.out.println("WORLDLINE_B173_TILE_UTILITY_PHYSICAL_SIGNATURE=" + sha(trace));
    }

    private static String[] scoped(String[] arguments, String layer) {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: tile utility physical family server.jar workspace port seed");
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
