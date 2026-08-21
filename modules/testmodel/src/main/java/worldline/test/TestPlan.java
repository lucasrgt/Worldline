package worldline.test;

/** Immutable root returned after a spec is collected. */
public final class TestPlan {
    private final String specName;
    private final SuiteDefinition root;

    TestPlan(String specName, SuiteDefinition root) {
        this.specName = specName; this.root = root; root.freeze();
    }
    public String specName() { return specName; }
    public SuiteDefinition root() { return root; }
}
