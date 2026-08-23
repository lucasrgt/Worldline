/** Extracts one language object from tokei's JSON without crossing into later languages. */
final class TokeiJson {
    private TokeiJson() { }

    static String language(String json, String language) {
        String marker = "\"" + language + "\":";
        int markerAt = json.indexOf(marker);
        if (markerAt < 0) throw new IllegalStateException("tokei JSON did not contain " + language);
        int start = json.indexOf('{', markerAt + marker.length());
        if (start < 0) throw new IllegalStateException("invalid tokei JSON language object");
        int depth = 0;
        boolean quoted = false;
        boolean escaped = false;
        for (int index = start; index < json.length(); index++) {
            char current = json.charAt(index);
            if (quoted) {
                if (escaped) escaped = false;
                else if (current == '\\') escaped = true;
                else if (current == '"') quoted = false;
            } else if (current == '"') quoted = true;
            else if (current == '{') depth++;
            else if (current == '}' && --depth == 0) return json.substring(start + 1, index);
        }
        throw new IllegalStateException("unterminated tokei JSON language object");
    }
}
