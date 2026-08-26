/** Reviewed model profiles for supervised Ox Alpha sessions. */
final class OxAlphaProfile {
    static final String FALLBACK_MODEL = "opencode/nemotron-3-ultra-free";
    private static final String PRIMARY_MODEL = "opencode-go/gpt-5.6-luna";

    private OxAlphaProfile() {
    }

    static boolean fallback() {
        return "1".equals(System.getenv("WORLDLINE_OX_ALPHA_FALLBACK"));
    }

    static String model(boolean fallback) {
        return fallback ? FALLBACK_MODEL : PRIMARY_MODEL;
    }

    static String config(boolean fallback) {
        String variant = fallback ? "" : ",\"variant\":\"max\"";
        return "{\"agent\":{\"ox-alpha\":{\"description\":\"Supervised Worldline milestone worker\","
                + "\"mode\":\"primary\",\"model\":\"" + model(fallback) + "\"" + variant
                + ",\"maxSteps\":200,\"permission\":{\"*\":\"allow\",\"task\":\"deny\","
                + "\"question\":\"deny\",\"external_directory\":\"deny\",\"doom_loop\":\"deny\"}}}}";
    }
}
