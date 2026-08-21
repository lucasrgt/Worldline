package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import worldline.api.SemanticMapping;
import worldline.semantics.SemanticCatalog;

/** Derived neighborhood over catalog edges, record refs, and hypothesis links. */
public final class AtlasGraph {
    private AtlasGraph() {}

    public static String render(AtlasStore store, String id) {
        AtlasRecord record = store.get(id);
        Set<String> edges = new LinkedHashSet<String>();
        addRefs(edges, record);
        addIncoming(edges, store, id);
        if (AtlasKind.ROLE.equals(record.kind())) addCatalog(edges, record);
        if (AtlasKind.BOUNDARY.equals(record.kind())) addBoundaryRoles(edges, store, record);
        StringBuilder text = new StringBuilder();
        text.append("id=").append(id).append('\n');
        text.append("edges=").append(edges.size()).append('\n');
        for (String edge : edges) text.append(edge).append('\n');
        return text.toString();
    }

    private static void addRefs(Set<String> edges, AtlasRecord record) {
        for (String ref : record.refs()) edges.add(record.id() + " REFS " + ref);
        if (!record.control().isEmpty() && AtlasKind.HYPOTHESIS.equals(record.kind())) {
            for (String ref : record.refs()) {
                edges.add(record.id() + " " + record.control() + " " + ref);
            }
        }
    }

    private static void addIncoming(Set<String> edges, AtlasStore store, String id) {
        for (AtlasRecord other : store.records()) {
            if (other.refs().contains(id)) edges.add(other.id() + " REFS " + id);
        }
    }

    private static void addCatalog(Set<String> edges, AtlasRecord record) {
        SemanticMapping mapping = SemanticCatalog.standard().role(AtlasKind.token(record.id()));
        relate(edges, record.id(), "READS", mapping.reads());
        relate(edges, record.id(), "WRITES", mapping.writes());
        relate(edges, record.id(), "DEPENDS_ON", mapping.deps());
    }

    private static void addBoundaryRoles(Set<String> edges, AtlasStore store, AtlasRecord boundary) {
        String token = AtlasKind.token(boundary.id());
        for (AtlasRecord role : store.kind(AtlasKind.ROLE)) {
            SemanticMapping mapping = SemanticCatalog.standard().role(AtlasKind.token(role.id()));
            if (mapping.reads().contains(token) || mapping.writes().contains(token)
                    || mapping.deps().contains(token)) {
                edges.add(role.id() + " DEPENDS_ON " + boundary.id());
            }
        }
    }

    private static void relate(Set<String> edges, String id, String relation, List<String> tokens) {
        for (String token : tokens) {
            edges.add(id + " " + relation + " atlas.boundary." + token);
        }
    }

    static List<String> neighbors(AtlasStore store, String id) {
        String rendered = render(store, id);
        List<String> lines = new ArrayList<String>();
        Collections.addAll(lines, rendered.split("\n"));
        return lines;
    }
}
