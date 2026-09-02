package worldline.testapi;

import java.util.Objects;

/** Equatable evidence for the native built-environment material matrix. */
public final class BuiltEnvironmentMaterialsEvidence {
    private final BuiltEnvironmentMaterialsObservation observation;

    public BuiltEnvironmentMaterialsEvidence(BuiltEnvironmentMaterialsObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }

    public BuiltEnvironmentMaterialsObservation observation() { return observation; }

    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.built-environment-materials-evidence.v1\n");
        value.append("subjects=b1.7.3:block/001+017+020+030+043+044+053+065+067+085+086+088+089+091\n");
        value.append("claims=47|states=6+shapes=7+light=8+ticks=13+neighbors=13\n");
        value.append("states=").append(observation.states()).append('\n');
        value.append("shapes=").append(observation.shapes()).append('\n');
        value.append("light=").append(observation.light()).append('\n');
        value.append("ticks=").append(observation.ticks()).append('\n');
        return value.append("neighbors=").append(observation.neighbors()).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof BuiltEnvironmentMaterialsEvidence
                && observation.equals(((BuiltEnvironmentMaterialsEvidence) other).observation);
    }

    @Override public int hashCode() { return observation.hashCode(); }
}
