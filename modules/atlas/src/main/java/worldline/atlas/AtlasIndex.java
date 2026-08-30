package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Deterministic lexical-semantic index over canonical records and graph references. */
public final class AtlasIndex {
    private AtlasIndex() {}

    public static List<AtlasHit> search(AtlasStore store, String query, int limit) {
        if (store == null || query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("query");
        }
        if (limit < 1 || limit > 1000) throw new IllegalArgumentException("limit");
        Set<String> terms = terms(query);
        List<AtlasHit> hits = new ArrayList<AtlasHit>();
        for (AtlasRecord record : store.records()) {
            int score = score(store, record, query, terms);
            if (score > 0) hits.add(new AtlasHit(record, score, "MATCH"));
        }
        Collections.sort(hits);
        if (hits.size() > limit) hits = new ArrayList<AtlasHit>(hits.subList(0, limit));
        return Collections.unmodifiableList(hits);
    }

    static Set<String> terms(String query) {
        Set<String> result = new LinkedHashSet<String>();
        for (String token : query.toLowerCase(Locale.US).split("[^a-z0-9]+")) {
            if (token.isEmpty()) continue;
            result.add(token); result.add(singular(token)); addSynonyms(result, token);
        }
        return result;
    }

    private static int score(AtlasStore store, AtlasRecord record, String query,
            Set<String> terms) {
        String phrase = query.toLowerCase(Locale.US).trim();
        String facet = phrase.startsWith("tag:") ? phrase.substring(4) : phrase;
        String id = record.id().toLowerCase(Locale.US);
        String subject = record.subject().toLowerCase(Locale.US);
        List<String> tags = AtlasTaxonomy.tags(store, record);
        if (facetQuery(facet) && !tags.contains(facet)) return 0;
        int score = 0;
        if (id.equals(phrase) || AtlasKind.token(record.id()).equals(phrase)) score += 200;
        if (id.contains(phrase)) score += 90;
        if (subject.contains(phrase)) score += 75;
        if (tags.contains(facet)) score += 160;
        for (String term : terms) {
            if (term.isEmpty()) continue;
            if (id.contains(term)) score += 28;
            if (subject.contains(term)) score += 22;
            if (contains(tags, term)) score += 18;
            if (contains(record.refs(), term)) score += 10;
            if (contains(record.evidence(), term)) score += 6;
        }
        if (score > 0 && AtlasStatus.VERIFIED.equals(record.status())) score += 5;
        return score;
    }

    private static boolean facetQuery(String query) {
        for (String prefix : new String[] {"domain-", "subsystem-", "category-", "status-",
                "certainty-", "artifact-", "layer-"}) {
            if (query.startsWith(prefix) && query.length() > prefix.length()) return true;
        }
        return false;
    }

    private static boolean contains(List<String> values, String term) {
        for (String value : values) {
            if (value.toLowerCase(Locale.US).contains(term)) return true;
        }
        return false;
    }

    private static String singular(String token) {
        if (token.endsWith("ies") && token.length() > 3) {
            return token.substring(0, token.length() - 3) + "y";
        }
        if (token.endsWith("s") && token.length() > 3) return token.substring(0, token.length() - 1);
        return token;
    }

    private static void addSynonyms(Set<String> terms, String token) {
        if ("chunk".equals(token) || "chunks".equals(token)) {
            Collections.addAll(terms, "region", "terrain", "worldgen", "save");
        } else if ("light".equals(token) || "lighting".equals(token)) {
            Collections.addAll(terms, "skylight", "opacity", "emission");
        } else if ("tick".equals(token) || "ticks".equals(token)) {
            Collections.addAll(terms, "scheduler", "random-tick", "clock");
        } else if ("block".equals(token) || "blocks".equals(token)) {
            Collections.addAll(terms, "state", "placement", "break");
        } else if ("save".equals(token) || "persistence".equals(token)) {
            Collections.addAll(terms, "reload", "region", "chunk");
        }
    }
}
