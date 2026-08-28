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
            "bbd71065fea417376213f2789efb8d4a1454dc12516e4e15f34293d26e7fcafd";
    private static final String COLLISION =
            "86e624e3de6aae05f18e4abbbe6495888b832add499c926193e14fe5b7b0c6d8";
    private static final String LIGHT =
            "3897805c57dd4919cb25b8c46d17cdeff2fdaf180e21a43e986241307b6651f4";

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
