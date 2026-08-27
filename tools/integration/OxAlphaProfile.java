/** Reviewed model profiles for supervised Ox Alpha sessions. */
final class OxAlphaProfile {
    static final String DEFAULT_MODEL = "opencode-go/glm-5.3-flash";
    static final String DEFAULT_FALLBACK_MODEL = "opencode-go/deepseek-v4-flash";
    static final int FALLBACK_RESUME_MIN_SECONDS = 7200;
    private static final java.util.Set<String> PRIMARY_MODELS = java.util.Set.of(
            DEFAULT_MODEL, DEFAULT_FALLBACK_MODEL, "opencode-go/deepseek-v4-pro");
    private static final java.util.Set<String> FALLBACK_MODELS = java.util.Set.of(
            DEFAULT_MODEL, DEFAULT_FALLBACK_MODEL);

    private OxAlphaProfile() {
    }

    static boolean fallback() {
        return "1".equals(System.getenv("WORLDLINE_OX_ALPHA_FALLBACK"));
    }

    static String model(boolean fallback) {
        String primary = reviewedModel(System.getenv("WORLDLINE_OX_ALPHA_MODEL"),
                DEFAULT_MODEL, PRIMARY_MODELS);
        if (!fallback) {
            return primary;
        }
        String selected = reviewedModel(System.getenv("WORLDLINE_OX_ALPHA_FALLBACK_MODEL"),
                DEFAULT_FALLBACK_MODEL, FALLBACK_MODELS);
        require(!selected.equals(primary), "fallback model must differ from the primary model");
        return selected;
    }

    static String reviewedModel(String requested, String defaultModel, java.util.Set<String> allowlist) {
        String selected = requested == null || requested.isBlank() ? defaultModel : requested;
        require(allowlist.contains(selected), "Ox Alpha model is not allowlisted: " + selected);
        return selected;
    }

    static boolean allowedModel(String model) {
        return PRIMARY_MODELS.contains(model);
    }

    static boolean allowedFallbackModel(String model) {
        return FALLBACK_MODELS.contains(model);
    }

    static boolean budgetAllowed(boolean fallback, String phase, String session, int seconds) {
        return !fallback || !phase.equals("checkpoint") || session == null
                || seconds == FALLBACK_RESUME_MIN_SECONDS;
    }

    static void selfTest() {
        require(config(false).contains("\"task\":\"deny\""),
                "nested task permission is not denied");
        require(config(false).contains("\"question\":\"deny\""),
                "worker question is not denied");
        require(!budgetAllowed(true, "checkpoint", "session", 3600),
                "short fallback resume accepted");
        require(budgetAllowed(true, "checkpoint", "session", 7200),
                "extended fallback resume rejected");
        require(!budgetAllowed(true, "checkpoint", "session", 7201),
                "unbounded fallback resume accepted");
        require(reviewedModel(null, DEFAULT_MODEL, PRIMARY_MODELS).equals(DEFAULT_MODEL),
                "default Ox Alpha model changed");
        require(reviewedModel(null, DEFAULT_FALLBACK_MODEL, FALLBACK_MODELS).equals(DEFAULT_FALLBACK_MODEL),
                "default fallback model changed");
        require(reviewedModel("opencode-go/deepseek-v4-pro", DEFAULT_MODEL, PRIMARY_MODELS)
                .equals("opencode-go/deepseek-v4-pro"), "reviewed pro model was rejected");
        boolean rejected = false;
        try {
            reviewedModel("opencode/mimo-v2.5-free", DEFAULT_MODEL, PRIMARY_MODELS);
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        require(rejected, "retired OpenCode model was accepted");
        rejected = false;
        try {
            reviewedModel("opencode-go/deepseek-v4-pro", DEFAULT_FALLBACK_MODEL, FALLBACK_MODELS);
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        require(rejected, "pro model was accepted as free fallback");
    }

    static String config(boolean fallback) {
        return "{\"agent\":{\"ox-alpha\":{\"description\":\"Supervised Worldline milestone worker\","
                + "\"mode\":\"primary\",\"model\":\"" + model(fallback) + "\""
                + ",\"maxSteps\":200,\"permission\":{\"*\":\"allow\",\"task\":\"deny\","
                + "\"question\":\"deny\",\"external_directory\":\"deny\",\"doom_loop\":\"deny\"}}}}";
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
