package worldline.testkit;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import worldline.extension.ExtensionAction;
import worldline.extension.ExtensionContext;
import worldline.extension.ExtensionContract;
import worldline.extension.ExtensionEvidence;
import worldline.extension.ExtensionMode;
import worldline.extension.WorldlineExtensionPlan;
import worldline.test.TestContext;
import worldline.test.Worldline;
import worldline.test.WorldlineSpec;

/** Adapts public extension contracts into ordinary TestKit specs. */
public final class ExtensionTestSpecs {
    private ExtensionTestSpecs() {}

    public static List<WorldlineSpec> create(WorldlineExtensionPlan plan, ExtensionMode mode) {
        if (plan == null || mode == null) throw new NullPointerException();
        List<WorldlineSpec> specs = new ArrayList<WorldlineSpec>();
        for (ExtensionContract contract : plan.contracts()) if (contract.modes().contains(mode))
            specs.add(new ContractSpec(plan, contract, mode));
        if (specs.isEmpty()) throw new IllegalArgumentException("extension has no " + mode.token()
                + " contracts");
        return specs;
    }

    private static final class ContractSpec extends WorldlineSpec {
        private final WorldlineExtensionPlan plan;
        private final ExtensionContract contract;
        private final ExtensionMode mode;
        ContractSpec(WorldlineExtensionPlan plan, ExtensionContract contract, ExtensionMode mode) {
            this.plan = plan; this.contract = contract; this.mode = mode;
        }
        @Override protected void define() {
            Worldline.test(plan.manifest().id() + " / " + contract.id() + " / " + mode.token(),
                    context -> execute(plan, contract, mode, context)).tag("extension")
                    .tag(plan.manifest().id()).tag(mode.token());
        }
    }

    private static void execute(WorldlineExtensionPlan plan, ExtensionContract contract,
            ExtensionMode mode, TestContext test) throws Exception {
        ExtensionContext context = new Context(test);
        plan.fixture(contract.fixtureId()).prepare(context);
        for (String id : contract.actionIds()) {
            ExtensionAction action = plan.action(id);
            test.step("extension-action:" + id, ignored -> action.perform(context));
        }
        Map<String, String> observations = new LinkedHashMap<String, String>();
        for (String id : contract.observationIds())
            observations.put(id, plan.observation(id).observe(context));
        ExtensionEvidence evidence = ExtensionEvidence.capture(plan.manifest(), contract, mode,
                observations);
        String stem = "extension-" + plan.manifest().id() + "-" + contract.id()
                + "-" + mode.token();
        test.attach(stem + ".evidence.properties",
                evidence.canonical().getBytes(StandardCharsets.UTF_8));
        test.attach(stem + ".atlas.properties",
                plan.atlasDocument().getBytes(StandardCharsets.UTF_8));
        if (mode == ExtensionMode.DIFFERENTIAL) test.attach(stem + ".diff.properties",
                differential(contract, evidence).getBytes(StandardCharsets.UTF_8));
        plan.oracle(contract.oracleId()).verify(mode, evidence, contract.expectedSignature(mode));
    }

    private static String differential(ExtensionContract contract, ExtensionEvidence evidence) {
        String baseline = contract.expectedSignature(ExtensionMode.DIFFERENTIAL);
        return "WORLDLINE-EXTENSION-DIFF/1\ncontract=" + contract.id() + "\nbehavior="
                + contract.vanillaBehavior() + "\nbaseline.signature=" + baseline
                + "\nobserved.signature=" + evidence.signature() + "\nchanged="
                + !evidence.signature().equals(baseline) + "\n";
    }

    private static final class Context implements ExtensionContext {
        private final TestContext delegate;
        Context(TestContext delegate) { this.delegate = delegate; }
        @Override public long seed() { return delegate.seed(); }
        @Override public int attempt() { return delegate.attempt(); }
        @Override public worldline.api.AutomatedMinecraftRuntime runtime() { return delegate.runtime(); }
        @Override public worldline.api.GameWorld world() { return delegate.runtime().world(); }
        @Override public worldline.api.GamePlayer player() { return delegate.player(); }
        @Override public worldline.api.GameUi ui() { return delegate.ui(); }
        @Override public void tick() { delegate.tick(); }
        @Override public void tick(int count) { delegate.tick(count); }
        @Override public void attach(String name, byte[] bytes) { delegate.attach(name, bytes); }
        @Override public void skip(String reason) { delegate.skip(reason); }
    }
}
