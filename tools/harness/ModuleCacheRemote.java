import java.nio.file.Path;
import java.util.Map;

/** Opt-in configuration boundary for normal ModuleBuild CAS hydration. */
final class ModuleCacheRemote {
    private ModuleCacheRemote() { }

    static boolean hydrate(Path root, Path moduleCache, String module,
            String inputSha256, Path target) throws Exception {
        return hydrate(root, moduleCache, module, inputSha256, target, System.getenv());
    }

    static boolean hydrate(Path root, Path moduleCache, String module,
            String inputSha256, Path target, Map<String, String> environment) throws Exception {
        String mode = environment.get("WORLDLINE_MODULE_CAS_MODE");
        if (!enabled(mode)) return false;
        if ("shared-directory".equals(mode)) {
            ModuleCacheSharedDirectory backend = ModuleCacheSharedDirectory.connect(
                    Path.of(required(environment, "WORLDLINE_MODULE_CAS_DIRECTORY")),
                    Path.of(required(environment, "WORLDLINE_MODULE_CAS_INDEX")),
                    Path.of(required(environment, "WORLDLINE_MODULE_CAS_INDEX_TRUST")),
                    required(environment, "WORLDLINE_MODULE_CAS_PUBLIC_KEY"),
                    required(environment, "WORLDLINE_MODULE_CAS_CHANNEL"),
                    moduleCache.getParent().resolve("module-downloads"));
            boolean hydrated = backend.hydrate(module, inputSha256, target);
            if (hydrated) System.out.println("  hydrated module " + module + " from shared-directory CAS");
            return hydrated;
        }
        if ("s3".equals(mode)) {
            ModuleCacheS3 backend = ModuleCacheS3.connect(ModuleCacheS3Config.from(environment),
                    Path.of(required(environment, "WORLDLINE_MODULE_CAS_INDEX")),
                    Path.of(required(environment, "WORLDLINE_MODULE_CAS_INDEX_TRUST")),
                    required(environment, "WORLDLINE_MODULE_CAS_PUBLIC_KEY"),
                    required(environment, "WORLDLINE_MODULE_CAS_CHANNEL"),
                    moduleCache.getParent().resolve("module-s3-downloads"));
            boolean hydrated = backend.hydrate(module, inputSha256, target);
            if (hydrated) System.out.println("  hydrated module " + module + " from S3-compatible CAS");
            return hydrated;
        }
        require("shadow".equals(mode), "unsupported WORLDLINE_MODULE_CAS_MODE");
        ModuleCacheHydrator hydrator = ModuleCacheHydrator.connect(root,
                Path.of(required(environment, "WORLDLINE_CAS_BINARY")),
                required(environment, "WORLDLINE_MODULE_CAS_ADDRESS"),
                Path.of(required(environment, "WORLDLINE_MODULE_CAS_PERMITS")),
                Path.of(required(environment, "WORLDLINE_MODULE_CAS_INDEX")),
                Path.of(required(environment, "WORLDLINE_MODULE_CAS_INDEX_TRUST")),
                required(environment, "WORLDLINE_MODULE_CAS_PUBLIC_KEY"),
                required(environment, "WORLDLINE_MODULE_CAS_CHANNEL"),
                moduleCache.getParent().resolve("module-cas-objects"));
        boolean hydrated = hydrator.hydrate(module, inputSha256, target);
        if (hydrated) System.out.println("  hydrated module " + module + " from shadow CAS");
        return hydrated;
    }

    static void selfTest() throws Exception {
        require(!enabled(null) && !enabled("") && !enabled("off") && enabled("shadow")
                && enabled("shared-directory") && enabled("s3"),
                "module CAS mode classifier drifted");
        ModuleCacheSharedDirectory.selfTest(); ModuleCacheS3.selfTest();
        System.out.println("  module cache remote mode self-test: passed");
    }

    private static boolean enabled(String mode) {
        return mode != null && !mode.isBlank() && !"off".equals(mode);
    }
    private static String required(Map<String, String> environment, String name) {
        String value = environment.get(name);
        require(value != null && !value.isBlank(), name + " is required in the selected module CAS mode");
        return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
