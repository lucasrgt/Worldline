/** Read-only explanation of the next smoke cache decision. */
record SmokeCacheExplanation(String smoke, String fingerprint, boolean cacheEnabled,
        boolean localProof, boolean trackedPin, String decision, String reason, String currentTreeMode) {
    String json() {
        return "{\n  \"schema\":\"worldline.smoke-cache-explanation.v1\",\n"
                + "  \"smoke\":\"" + escape(smoke) + "\",\n"
                + "  \"fingerprint\":\"" + fingerprint + "\",\n"
                + "  \"cache_enabled\":" + cacheEnabled + ",\n"
                + "  \"local_proof\":" + localProof + ",\n"
                + "  \"tracked_pin\":" + trackedPin + ",\n"
                + "  \"decision\":\"" + decision + "\",\n"
                + "  \"reason\":\"" + escape(reason) + "\",\n"
                + "  \"current_tree_mode\":" + nullable(currentTreeMode) + "\n}\n";
    }

    private static String nullable(String value) {
        return value == null ? "null" : "\"" + escape(value) + "\"";
    }
    private static String escape(String value) { return SmokeInputGraphResult.escape(value); }
}
