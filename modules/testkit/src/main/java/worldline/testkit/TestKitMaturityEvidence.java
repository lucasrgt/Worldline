package worldline.testkit;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/** An assessment can pass verification while honestly leaving 1.0 promotion blocked. */
public final class TestKitMaturityEvidence {
    private final String canonical;
    private final List<String> sustained;
    private final int publicConsumers;
    TestKitMaturityEvidence(String canonical, List<String> sustained, int publicConsumers) {
        this.canonical = canonical; this.sustained = Collections.unmodifiableList(new ArrayList<>(sustained));
        this.publicConsumers = publicConsumers;
    }
    public boolean readyForOne() { return sustained.size() >= 2; }
    public int publicConsumers() { return publicConsumers; }
    public List<String> sustainedConsumers() { return sustained; }
    public String canonical() { return canonical; }
    public void requireReadyForOne() {
        if (!readyForOne()) throw new IllegalStateException("TestKit 1.0 needs sustained public use by two mods");
    }
    @Override public boolean equals(Object other) {
        return other instanceof TestKitMaturityEvidence && canonical.equals(((TestKitMaturityEvidence) other).canonical);
    }
    @Override public int hashCode() { return canonical.hashCode(); }
}
