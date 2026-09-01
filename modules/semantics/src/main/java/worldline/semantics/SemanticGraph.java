package worldline.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import worldline.api.SemanticMapping;

/**
 * Fail-closed static role graph. Tokens are normalized uppercase category names;
 * unknown read/write/dep tokens fail construction.
 */
public final class SemanticGraph {
    private final List<String> tokens;
    private final List<String> edges;

    private SemanticGraph(List<String> tokens, List<String> edges) {
        this.tokens = tokens;
        this.edges = edges;
    }

    public static SemanticGraph of(SemanticCatalog catalog) {
        if (catalog == null) throw new NullPointerException("catalog");
        Set<String> allowed = allowedTokens();
        List<String> edges = new ArrayList<String>();
        for (SemanticMapping mapping : catalog.mappings()) {
            add(edges, allowed, mapping.role(), "reads", mapping.reads());
            add(edges, allowed, mapping.role(), "writes", mapping.writes());
            add(edges, allowed, mapping.role(), "deps", mapping.deps());
        }
        return new SemanticGraph(Collections.unmodifiableList(new ArrayList<String>(allowed)),
                Collections.unmodifiableList(edges));
    }

    public List<String> tokens() { return tokens; }
    public List<String> edges() { return edges; }

    public List<String> writers(String token) { return named(token, "writes"); }
    public List<String> readers(String token) { return named(token, "reads"); }

    public String render() {
        StringBuilder text = new StringBuilder();
        text.append("tokens=").append(tokens.size()).append('\n');
        text.append("edges=").append(edges.size()).append('\n');
        text.append("complete=true\n");
        for (String edge : edges) text.append(edge).append('\n');
        return text.toString();
    }

    private List<String> named(String token, String kind) {
        if (token == null || !tokens.contains(token)) {
            throw new IllegalArgumentException("unknown token " + token);
        }
        List<String> roles = new ArrayList<String>();
        String suffix = " " + kind + " " + token;
        for (String edge : edges) {
            if (edge.endsWith(suffix)) roles.add(edge.substring(0, edge.indexOf(' ')));
        }
        return Collections.unmodifiableList(roles);
    }

    private static void add(List<String> edges, Set<String> allowed, String role, String kind,
            List<String> tokens) {
        for (String token : tokens) {
            if (!allowed.contains(token)) throw new IllegalArgumentException("unknown token " + token);
            edges.add(role + " " + kind + " " + token);
        }
    }

    private static Set<String> allowedTokens() {
        Set<String> tokens = new LinkedHashSet<String>();
        for (String category : SemanticRoles.categories()) {
            tokens.add(category.toUpperCase(Locale.US).replace('-', '_'));
        }
        return tokens;
    }
}
