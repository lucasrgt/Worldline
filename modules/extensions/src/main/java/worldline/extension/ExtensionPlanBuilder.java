package worldline.extension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Mutable registration phase hidden behind the public registry interface. */
final class ExtensionPlanBuilder implements WorldlineExtensionRegistry {
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9-]{0,62}");
    private final ExtensionManifest manifest;
    private final Map<String, ExtensionSubject> subjects = new LinkedHashMap<String, ExtensionSubject>();
    private final Map<String, ExtensionFixture> fixtures = new LinkedHashMap<String, ExtensionFixture>();
    private final Map<String, ExtensionAction> actions = new LinkedHashMap<String, ExtensionAction>();
    private final Map<String, ExtensionObservation> observations =
            new LinkedHashMap<String, ExtensionObservation>();
    private final Map<String, ExtensionOracle> oracles = new LinkedHashMap<String, ExtensionOracle>();
    private final List<ExtensionContract> contracts = new ArrayList<ExtensionContract>();
    private final List<ExtensionRuntimeAdapter> adapters = new ArrayList<ExtensionRuntimeAdapter>();
    private final List<ExtensionAtlasPage> atlasPages = new ArrayList<ExtensionAtlasPage>();

    ExtensionPlanBuilder(ExtensionManifest manifest) { this.manifest = manifest; }

    @Override public void subject(ExtensionSubject value) {
        if (value == null || subjects.put(value.id(), value) != null)
            throw new IllegalArgumentException("duplicate extension subject");
    }
    @Override public void fixture(String id, ExtensionFixture value) { put(fixtures, id, value, "fixture"); }
    @Override public void action(String id, ExtensionAction value) { put(actions, id, value, "action"); }
    @Override public void observation(String id, ExtensionObservation value) {
        put(observations, id, value, "observation");
    }
    @Override public void oracle(String id, ExtensionOracle value) { put(oracles, id, value, "oracle"); }
    @Override public void contract(ExtensionContract value) {
        if (value == null) throw new NullPointerException("contract"); contracts.add(value);
    }
    @Override public void adapter(ExtensionRuntimeAdapter value) {
        if (value == null) throw new NullPointerException("adapter");
        for (ExtensionRuntimeAdapter item : adapters) if (item.loaderId().equals(value.loaderId()))
            throw new IllegalArgumentException("duplicate loader adapter " + value.loaderId());
        adapters.add(value);
    }
    @Override public void atlas(ExtensionAtlasPage value) {
        if (value == null) throw new NullPointerException("atlas page");
        for (ExtensionAtlasPage item : atlasPages) if (item.id().equals(value.id()))
            throw new IllegalArgumentException("duplicate atlas page " + value.id());
        atlasPages.add(value);
    }

    ExtensionManifest manifest() { return manifest; }
    Map<String, ExtensionSubject> subjects() { return subjects; }
    Map<String, ExtensionFixture> fixtures() { return fixtures; }
    Map<String, ExtensionAction> actions() { return actions; }
    Map<String, ExtensionObservation> observations() { return observations; }
    Map<String, ExtensionOracle> oracles() { return oracles; }
    List<ExtensionContract> contracts() { return contracts; }
    List<ExtensionRuntimeAdapter> adapters() { return adapters; }
    List<ExtensionAtlasPage> atlasPages() { return atlasPages; }

    private static <T> void put(Map<String, T> values, String id, T value, String label) {
        if (id == null || !TOKEN.matcher(id).matches()) throw new IllegalArgumentException(label + " id");
        if (value == null) throw new NullPointerException(label);
        if (values.put(id, value) != null) throw new IllegalArgumentException("duplicate " + label + " " + id);
    }
}
