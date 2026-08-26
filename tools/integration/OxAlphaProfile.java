/** Reviewed model profiles for supervised Ox Alpha sessions. */
final class OxAlphaProfile {
    static final String FALLBACK_MODEL = "opencode/nemotron-3-ultra-free";
    static final int FALLBACK_RESUME_MIN_SECONDS = 7200;
    private static final String PRIMARY_MODEL = "opencode-go/gpt-5.6-luna";

    private OxAlphaProfile() {
    }

    static boolean fallback() {
        return "1".equals(System.getenv("WORLDLINE_OX_ALPHA_FALLBACK"));
    }

    static String model(boolean fallback) {
        return fallback ? FALLBACK_MODEL : PRIMARY_MODEL;
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
