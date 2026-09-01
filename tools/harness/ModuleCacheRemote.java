import java.nio.file.Path;
import java.util.Map;

/** Opt-in configuration boundary for normal ModuleBuild CAS hydration. */
final class ModuleCacheRemote {
    private ModuleCacheRemote() { }

    static boolean hydrate(Path root, Path moduleCache, String module,
            String inputSha256, Path target) throws Exception {
        Map<String, String> environment = System.getenv();
        String mode = environment.get("WORLDLINE_MODULE_CAS_MODE");
        if (!enabled(mode)) return false;
        require("shadow".equals(mode), "unsupported WORLDLINE_MODULE_CAS_MODE");
        ModuleCacheHydrator hydrator = ModuleCacheHydrator.connect(root,
                Path.of(required(environment, "WORLDLINE_CAS_BINARY")),
                required(environment, "WORLDLINE_MODULE_CAS_ADDRESS"),
                Path.of(required(environment, "WORLDLINE_MODULE_CAS_PERMITS")),
                Path.of(required(environment, "WORLDLINE_MODULE_CAS_INDEX")),
                required(environment, "WORLDLINE_MODULE_CAS_INDEX_SHA256"),
                moduleCache.getParent().resolve("module-cas-objects"));
        boolean hydrated = hydrator.hydrate(module, inputSha256, target);
        if (hydrated) System.out.println("  hydrated module " + module + " from shadow CAS");
        return hydrated;
    }

    static void selfTest() {
        require(!enabled(null) && !enabled("") && !enabled("off") && enabled("shadow"),
                "module CAS mode classifier drifted");
        System.out.println("  module cache remote mode self-test: passed");
    }

    private static boolean enabled(String mode) {
        return mode != null && !mode.isBlank() && !"off".equals(mode);
    }
    private static String required(Map<String, String> environment, String name) {
        String value = environment.get(name);
        require(value != null && !value.isBlank(), name + " is required in module CAS shadow mode");
        return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
