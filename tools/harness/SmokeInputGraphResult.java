import java.util.List;

/** Immutable, secret-free read model of one smoke input identity. */
record SmokeInputGraphResult(String smoke, String rootSha256, List<Node> nodes) {
    SmokeInputGraphResult { nodes = List.copyOf(nodes); }

    String json() {
        StringBuilder value = new StringBuilder("{\n  \"schema\":\"worldline.smoke-input-graph.v1\",\n")
                .append("  \"mode\":\"qualification\",\n  \"smoke\":\"")
                .append(escape(smoke)).append("\",\n  \"root_sha256\":\"")
                .append(rootSha256).append("\",\n  \"nodes\":[\n");
        for (int index = 0; index < nodes.size(); index++) {
            Node node = nodes.get(index);
            value.append("    {\"index\":").append(node.index()).append(",\"kind\":\"")
                    .append(escape(node.kind())).append("\",\"key\":\"")
                    .append(escape(node.key())).append("\",\"sha256\":\"")
                    .append(node.sha256()).append("\"}");
            if (index + 1 < nodes.size()) value.append(',');
            value.append('\n');
        }
        return value.append("  ]\n}\n").toString();
    }

    static String escape(String input) {
        StringBuilder value = new StringBuilder();
        for (int index = 0; index < input.length(); index++) {
            char character = input.charAt(index);
            switch (character) {
                case '\\' -> value.append("\\\\");
                case '"' -> value.append("\\\"");
                case '\n' -> value.append("\\n");
                case '\r' -> value.append("\\r");
                case '\t' -> value.append("\\t");
                default -> {
                    if (character < 0x20) value.append(String.format("\\u%04x", (int) character));
                    else value.append(character);
                }
            }
        }
        return value.toString();
    }

    record Node(int index, String kind, String key, String sha256) { }
}
