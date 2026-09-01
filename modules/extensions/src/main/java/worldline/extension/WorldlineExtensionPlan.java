package worldline.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Fully validated, immutable result of loading one public extension entrypoint. */
public final class WorldlineExtensionPlan {
    private final ExtensionManifest manifest;
    private final Map<String, ExtensionSubject> subjects;
    private final Map<String, ExtensionFixture> fixtures;
    private final Map<String, ExtensionAction> actions;
    private final Map<String, ExtensionObservation> observations;
    private final Map<String, ExtensionOracle> oracles;
    private final List<ExtensionContract> contracts;
    private final List<ExtensionRuntimeAdapter> adapters;
    private final List<ExtensionAtlasPage> atlasPages;

    WorldlineExtensionPlan(ExtensionPlanBuilder builder) {
        manifest = builder.manifest();
        subjects = frozen(builder.subjects()); fixtures = frozen(builder.fixtures());
        actions = frozen(builder.actions()); observations = frozen(builder.observations());
        oracles = frozen(builder.oracles());
        contracts = Collections.unmodifiableList(new ArrayList<ExtensionContract>(builder.contracts()));
        adapters = Collections.unmodifiableList(new ArrayList<ExtensionRuntimeAdapter>(builder.adapters()));
        atlasPages = Collections.unmodifiableList(new ArrayList<ExtensionAtlasPage>(builder.atlasPages()));
        validate();
    }

    public ExtensionManifest manifest() { return manifest; }
    public List<ExtensionSubject> subjects() {
        return Collections.unmodifiableList(new ArrayList<ExtensionSubject>(subjects.values()));
    }
    public List<ExtensionContract> contracts() { return contracts; }
    public List<ExtensionRuntimeAdapter> adapters() { return adapters; }
    public List<ExtensionAtlasPage> atlasPages() { return atlasPages; }
    public List<ExtensionAtlasPage> atlasIndexPages() {
        return ExtensionAtlasDocument.pages(this);
    }
    public ExtensionFixture fixture(String id) { return required(fixtures, id, "fixture"); }
    public ExtensionAction action(String id) { return required(actions, id, "action"); }
    public ExtensionObservation observation(String id) {
        return required(observations, id, "observation");
    }
    public ExtensionOracle oracle(String id) { return required(oracles, id, "oracle"); }
    public String atlasDocument() { return ExtensionAtlasDocument.render(this); }

    private void validate() {
        if (subjects.isEmpty()) throw new IllegalArgumentException("extension has no subjects");
        if (contracts.isEmpty()) throw new IllegalArgumentException("extension has no contracts");
        String prefix = manifest.id() + ":";
        for (ExtensionSubject subject : subjects.values()) if (!subject.id().startsWith(prefix))
            throw new IllegalArgumentException("subject outside extension namespace " + subject.id());
        Map<String, ExtensionContract> ids = new LinkedHashMap<String, ExtensionContract>();
        for (ExtensionContract contract : contracts) {
            if (!subjects.containsKey(contract.subjectId()))
                throw new IllegalArgumentException("unknown contract subject " + contract.subjectId());
            required(fixtures, contract.fixtureId(), "fixture");
            required(oracles, contract.oracleId(), "oracle");
            for (String id : contract.actionIds()) required(actions, id, "action");
            for (String id : contract.observationIds()) required(observations, id, "observation");
            if (ids.put(contract.id(), contract) != null)
                throw new IllegalArgumentException("duplicate contract " + contract.id());
        }
        String provenance = "extension:" + manifest.id() + "@" + manifest.version();
        for (ExtensionAtlasPage page : atlasPages) {
            if (!provenance.equals(page.provenance()))
                throw new IllegalArgumentException("atlas page provenance " + page.id());
            String[] parts = page.id().split("\\.", 3);
            String pagePrefix = parts.length < 2 ? "" : "atlas." + parts[1] + "."
                    + manifest.id() + ".";
            if (!page.id().startsWith(pagePrefix)) {
                throw new IllegalArgumentException("atlas page outside extension namespace "
                        + page.id());
            }
        }
    }

    private static <T> Map<String, T> frozen(Map<String, T> values) {
        return Collections.unmodifiableMap(new LinkedHashMap<String, T>(values));
    }
    private static <T> T required(Map<String, T> values, String id, String label) {
        T value = values.get(id);
        if (value == null) throw new IllegalArgumentException("unknown " + label + " " + id);
        return value;
    }
}
