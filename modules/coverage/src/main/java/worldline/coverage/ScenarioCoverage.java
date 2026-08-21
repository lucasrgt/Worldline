package worldline.coverage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import worldline.minimization.Scenario;
import worldline.semantics.SemanticFields;
import worldline.semantics.SemanticRoles;
import worldline.semantics.SemanticSteps;
import worldline.trace.CanonicalStateDocument;

/**
 * Dynamic coverage of one public-grammar scenario against the closed
 * semantic catalog: which control categories its steps engage and which
 * catalog roles its observed trace fields carry.
 */
public final class ScenarioCoverage {
    private final List<String> categories;
    private final Map<String, Integer> stepCounts;
    private final List<String> roles;
    private final int totalCategories;

    private ScenarioCoverage(List<String> categories, Map<String, Integer> stepCounts,
            List<String> roles, int totalCategories) {
        this.categories = categories; this.stepCounts = stepCounts;
        this.roles = roles; this.totalCategories = totalCategories;
    }

    public static ScenarioCoverage of(Scenario scenario, CanonicalStateDocument trace) {
        if (scenario == null) throw new NullPointerException("scenario");
        Set<String> touched = new LinkedHashSet<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (int index = 0; index < scenario.size(); index++) {
            String category = SemanticSteps.category(scenario.step(index));
            require(!category.isEmpty(), "unclassified step " + scenario.step(index));
            touched.add(category);
            Integer previous = counts.get(category);
            counts.put(category, previous == null ? 1 : previous + 1);
        }
        Set<String> roles = new TreeSet<>();
        if (trace != null) {
            for (String field : trace.fields()) {
                String role = SemanticFields.role(field);
                if (!role.isEmpty()) roles.add(role);
            }
        }
        List<String> ordered = new ArrayList<>();
        for (String category : SemanticRoles.categories()) {
            if (touched.contains(category)) ordered.add(category);
        }
        require(ordered.size() == touched.size(), "unknown category encountered");
        return new ScenarioCoverage(ordered, counts,
                new ArrayList<>(roles), SemanticRoles.categories().size());
    }

    /** Touched categories in canonical catalog order. */
    public List<String> categories() { return categories; }

    /** Steps per touched category; keys follow {@link #categories()}. */
    public Map<String, Integer> stepCounts() { return stepCounts; }

    /** Catalog role aliases carried by observed trace fields, sorted. */
    public List<String> roles() { return roles; }

    public int totalCategories() { return totalCategories; }

    /** Floor percentage of the catalog's control categories that were engaged. */
    public int percentCategories() { return categories.size() * 100 / totalCategories; }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
