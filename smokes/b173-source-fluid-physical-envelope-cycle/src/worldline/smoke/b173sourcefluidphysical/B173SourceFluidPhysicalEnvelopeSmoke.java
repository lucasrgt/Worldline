package worldline.smoke.b173sourcefluidphysical;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import worldline.b173server.B173ServerCollisionTestRuntimeProvider;
import worldline.b173server.B173ServerLightTestRuntimeProvider;
import worldline.b173server.B173ServerStateDomainTestRuntimeProvider;
import worldline.b173server.B173SourceFluidPhysicalScenarioFactory;
import worldline.testkit.BlockCollisionFamilyCycle;
import worldline.testkit.BlockLightFamilyCycle;
import worldline.testkit.BlockStateDomainFamilyCycle;

/** Qualifies source water and lava across the public physical TestKit families. */
public final class B173SourceFluidPhysicalEnvelopeSmoke {
    private static final String FAMILY = "source-fluid-envelope";
    private static final String SERVER_PROPERTY = "worldline.b173.lifecycle.serverJar";
    private static final String STATE =
            "08a39f4392d26e7a98085d1752edbd345eb3c44437148113275bbb9ba4646a46";
    private static final String COLLISION =
            "5bfdc6f75f2127693a48c4abac6de2bb01e6f5f0319a72bb50483f92b80c836a";
    private static final String LIGHT =
            "44259d22f8e2b8f911728d94dae3b3f96309ab502a597086aea3590292aaef76";

    private B173SourceFluidPhysicalEnvelopeSmoke() { }

    public static void main(String[] arguments) throws Exception {
        BlockStateDomainFamilyCycle.run(scoped(arguments, "state-domain"), FAMILY,
                B173SourceFluidPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerStateDomainTestRuntimeProvider(),
                B173SourceFluidPhysicalScenarioFactory.stateDomains());
        BlockCollisionFamilyCycle.run(scoped(arguments, "collision-shape"), FAMILY,
                B173SourceFluidPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerCollisionTestRuntimeProvider(),
                B173SourceFluidPhysicalScenarioFactory.collisions());
        BlockLightFamilyCycle.run(scoped(arguments, "light-behavior"), FAMILY,
                B173SourceFluidPhysicalScenarioFactory.SEED, SERVER_PROPERTY,
                new B173ServerLightTestRuntimeProvider(),
                B173SourceFluidPhysicalScenarioFactory.lights());
        String signal = "family=source-fluid-physical-envelope,subjects=2,claims=6,layers=3"
                + ",reload=FRESH_LOGINx6,state=" + STATE + ",collision=" + COLLISION
                + ",light=" + LIGHT;
        String trace = "v1|server=official-b1.7.3|seed=17320110707|family="
                + "source-fluid-physical-envelope|subjects=2|claims=6|layers="
                + "state-domain,collision-shape,light-behavior|state=" + STATE
                + "|collision=" + COLLISION + "|light=" + LIGHT
                + "|oracle=three-public-family-signatures";
        System.out.println("WORLDLINE_B173_SOURCE_FLUID_PHYSICAL_SET=" + signal);
        System.out.println("WORLDLINE_B173_SOURCE_FLUID_PHYSICAL_TRACE=" + trace);
        System.out.println("WORLDLINE_B173_SOURCE_FLUID_PHYSICAL_SIGNATURE=" + sha(trace));
    }

    private static String[] scoped(String[] arguments, String layer) {
        if (arguments.length != 4) throw new IllegalArgumentException(
                "usage: source fluid physical family server.jar workspace port seed");
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
