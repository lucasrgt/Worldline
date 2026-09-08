package worldline.extension;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

final class ExtensionContractsTest {
    private ExtensionContractsTest() {}

    static void run(ExtensionManifest manifest) throws Exception {
        WorldlineExtensionPlan fluent = plan(manifest, true);
        WorldlineExtensionPlan explicit = plan(manifest, false);
        require(fluent.atlasDocument().equals(explicit.atlasDocument()), "Atlas parity");
        require(fluent.subjects().size() == 2 && fluent.contracts().size() == 3,
                "multiple subjects and contracts");
        for (int index = 0; index < fluent.contracts().size(); index++) {
            ExtensionContract contract = fluent.contracts().get(index);
            Map<String, String> observed = observe(fluent, contract);
            ExtensionEvidence evidence = ExtensionEvidence.capture(manifest, contract,
                    ExtensionMode.CUSTOM_CONTRACT, observed);
            ExtensionContract baseline = explicit.contracts().get(index);
            ExtensionEvidence original = ExtensionEvidence.capture(manifest, baseline,
                    ExtensionMode.CUSTOM_CONTRACT, observe(explicit, baseline));
            require(evidence.canonical().equals(original.canonical()), "evidence parity");
            fluent.oracle(contract.oracleId()).verify(ExtensionMode.CUSTOM_CONTRACT, evidence,
                    contract.expectedSignature(ExtensionMode.CUSTOM_CONTRACT));
            observed.put("count", "wrong");
            ExtensionEvidence wrong = ExtensionEvidence.capture(manifest, contract,
                    ExtensionMode.CUSTOM_CONTRACT, observed);
            boolean rejected = false;
            try {
                fluent.oracle(contract.oracleId()).verify(ExtensionMode.CUSTOM_CONTRACT, wrong,
                        contract.expectedSignature(ExtensionMode.CUSTOM_CONTRACT));
            } catch (AssertionError expected) { rejected = true; }
            require(rejected, "mismatched expectation accepted");
        }
        invalidRegistrations(manifest);
    }

    private static WorldlineExtensionPlan plan(ExtensionManifest manifest, boolean fluent) {
        ExtensionPlanBuilder registry = new ExtensionPlanBuilder(manifest);
        int[] count = { 0 };
        registry.subject(ExtensionSubject.of("example.probe:counter", ExtensionSubjectKind.BLOCK,
                "Counter block"));
        registry.subject(ExtensionSubject.of("example.probe:meter", ExtensionSubjectKind.BLOCK,
                "Meter block"));
        if (fluent) {
            ExtensionContracts.custom("increment", "example.probe:counter")
                    .fixture("empty", context -> count[0] = 0)
                    .action("increment", context -> count[0]++)
                    .expect("count", context -> Integer.toString(count[0]), "1")
                    .expect("ready", context -> "true", "true").registerInto(registry);
            ExtensionContracts.custom("increment-twice", "example.probe:counter")
                    .fixture("empty").action("increment")
                    .action("increment-again", context -> count[0]++)
                    .expect("count", "2").expect("ready", "true").registerInto(registry);
            ExtensionContracts.custom("meter-read", "example.probe:meter")
                    .fixture("empty").action("increment")
                    .expect("count", "1").expect("ready", "true").registerInto(registry);
        } else {
            registry.fixture("empty", context -> count[0] = 0);
            registry.action("increment", context -> count[0]++);
            registry.action("increment-again", context -> count[0]++);
            registry.observation("count", context -> Integer.toString(count[0]));
            registry.observation("ready", context -> "true");
            for (String id : Arrays.asList("increment", "increment-twice", "meter-read")) {
                registry.oracle(id, ExtensionOracles.equatable());
                Map<String, String> expected = new LinkedHashMap<String, String>();
                expected.put("count", "increment-twice".equals(id) ? "2" : "1");
                expected.put("ready", "true");
                ExtensionContract.Builder contract = ExtensionContract.builder(id,
                        "meter-read".equals(id) ? "example.probe:meter" : "example.probe:counter")
                        .fixture("empty").action("increment").observation("count")
                        .observation("ready").oracle(id).mode(ExtensionMode.CUSTOM_CONTRACT)
                        .custom(ExtensionEvidence.signature(expected));
                if ("increment-twice".equals(id)) contract.action("increment-again");
                registry.contract(contract.build());
            }
        }
        require(count[0] == 0, "registration executed a provider");
        return new WorldlineExtensionPlan(registry);
    }

    private static Map<String, String> observe(WorldlineExtensionPlan plan,
            ExtensionContract contract) throws Exception {
        plan.fixture(contract.fixtureId()).prepare(null);
        for (String id : contract.actionIds()) plan.action(id).perform(null);
        Map<String, String> observed = new LinkedHashMap<String, String>();
        for (String id : contract.observationIds())
            observed.put(id, plan.observation(id).observe(null));
        return observed;
    }

    private static void invalidRegistrations(ExtensionManifest manifest) {
        rejects(() -> base().expect("count", "1").expect("count", "2"));
        rejects(() -> base().fixture("first").fixture("second"));
        rejects(() -> base().expect("count", (String) null));
        rejects(() -> base().action("step", null));
        ExtensionPlanBuilder invalid = new ExtensionPlanBuilder(manifest);
        rejects(() -> base().fixture("empty", context -> { })
                .action("step", context -> { }).expect("count", context -> "1", "bad=value")
                .registerInto(invalid));
        require(invalid.fixtures().isEmpty(), "invalid contract partially registered providers");
        rejects(() -> base().fixture("empty").expect("count", "1").registerInto(invalid));
        rejects(() -> base().fixture("empty").action("step").registerInto(invalid));
        rejects(() -> base().fixture("empty").action("step").action("step")
                .expect("count", "1").registerInto(invalid));
        ExtensionPlanBuilder missing = new ExtensionPlanBuilder(manifest);
        missing.subject(ExtensionSubject.of("example.probe:counter", ExtensionSubjectKind.BLOCK,
                "Counter block"));
        base().fixture("absent").action("step", context -> { })
                .expect("count", context -> "1", "1").registerInto(missing);
        rejects(() -> new WorldlineExtensionPlan(missing));
        ExtensionPlanBuilder duplicate = new ExtensionPlanBuilder(manifest);
        duplicate.action("step", context -> { });
        rejects(() -> base().fixture("empty").action("step", context -> { })
                .expect("count", "1").registerInto(duplicate));
        ExtensionPlanBuilder oracle = new ExtensionPlanBuilder(manifest);
        oracle.oracle("check", ExtensionOracles.equatable());
        rejects(() -> base().fixture("empty").action("step")
                .expect("count", "1").registerInto(oracle));
    }

    private static ExtensionContracts.Builder base() {
        return ExtensionContracts.custom("check", "example.probe:counter");
    }

    private static void rejects(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException | NullPointerException expected) { return; }
        throw new AssertionError("invalid registration accepted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
