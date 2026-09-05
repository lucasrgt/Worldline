package worldline.m792;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import worldline.extension.ExtensionCapabilities;
import worldline.extension.ExtensionMode;
import worldline.extension.WorldlineExtensionDiscovery;
import worldline.extension.WorldlineExtensionPlan;

/** Public-only discovery probe for Aero's project-owned SDK extension. */
public final class ExtensionProbe {
    private ExtensionProbe() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 1, "project root argument");
        Path project = Paths.get(arguments[0]);
        ExtensionCapabilities capabilities = ExtensionCapabilities.of(
            ExtensionCapabilities.TESTKIT_V1, ExtensionCapabilities.ATLAS_V1,
            ExtensionCapabilities.CUSTOM_CONTRACT_V1);
        List<WorldlineExtensionPlan> plans = WorldlineExtensionDiscovery.discover(
            project, ExtensionProbe.class.getClassLoader(), capabilities);
        require(plans.size() == 1, "extension count");
        WorldlineExtensionPlan plan = plans.get(0);
        require("ultra-journey".equals(plan.manifest().id()), "extension id");
        require(plan.subjects().size() == 1 && plan.contracts().size() == 1,
            "extension registrations");
        require(plan.adapters().size() == 1 && plan.atlasIndexPages().size() == 3,
            "extension projections");
        require(plan.contracts().get(0).modes().contains(ExtensionMode.CUSTOM_CONTRACT),
            "custom contract mode");
        require(plan.manifest().provides().contains("aero.render-occlusion.v1"),
            "occlusion capability");
        System.out.println("WORLDLINE_M792_EXTENSION=PASS extensions=1,subjects=1,contracts=1,"
            + "adapters=1,atlas-pages=3,mode=custom-contract");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
