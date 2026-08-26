/** Reviewed model profiles for supervised Ox Alpha sessions. */
final class OxAlphaProfile {
    static final String DEFAULT_FALLBACK_MODEL = "opencode/nemotron-3-ultra-free";
    static final int FALLBACK_RESUME_MIN_SECONDS = 7200;
    private static final String PRIMARY_MODEL = "opencode-go/gpt-5.6-luna";
    private static final java.util.Set<String> FALLBACK_MODELS = java.util.Set.of(
            DEFAULT_FALLBACK_MODEL, "opencode/mimo-v2.5-free", "opencode/hy3-free",
            "opencode/nemotron-3.5-lightning-free");

    private OxAlphaProfile() {
    }

    static boolean fallback() {
        return "1".equals(System.getenv("WORLDLINE_OX_ALPHA_FALLBACK"));
    }

    static String model(boolean fallback) {
        return fallback ? fallbackModel(System.getenv("WORLDLINE_OX_ALPHA_FALLBACK_MODEL"))
                : PRIMARY_MODEL;
    }

    static String fallbackModel(String requested) {
        String selected = requested == null || requested.isBlank() ? DEFAULT_FALLBACK_MODEL : requested;
        require(FALLBACK_MODELS.contains(selected), "fallback model is not allowlisted: " + selected);
        return selected;
    }

    static boolean budgetAllowed(boolean fallback, String phase, String session, int seconds) {
        return !fallback || !phase.equals("checkpoint") || session == null
                || seconds >= FALLBACK_RESUME_MIN_SECONDS;
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
        require(fallbackModel(null).equals(DEFAULT_FALLBACK_MODEL),
                "default fallback model changed");
        require(fallbackModel("opencode/mimo-v2.5-free").equals("opencode/mimo-v2.5-free"),
                "allowlisted fallback model was rejected");
        boolean rejected = false;
        try {
            fallbackModel("unreviewed/model");
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        require(rejected, "unreviewed fallback model was accepted");
    }

    static String config(boolean fallback) {
        String variant = fallback ? "" : ",\"variant\":\"max\"";
        return "{\"agent\":{\"ox-alpha\":{\"description\":\"Supervised Worldline milestone worker\","
                + "\"mode\":\"primary\",\"model\":\"" + model(fallback) + "\"" + variant
                + ",\"maxSteps\":200,\"permission\":{\"*\":\"allow\",\"task\":\"deny\","
                + "\"question\":\"deny\",\"external_directory\":\"deny\",\"doom_loop\":\"deny\"}}}}";
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
