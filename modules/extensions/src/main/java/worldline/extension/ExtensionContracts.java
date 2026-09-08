package worldline.extension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** Explicit registration shortcuts for custom contracts with equatable observations. */
public final class ExtensionContracts {
    private ExtensionContracts() {}

    public static Builder custom(String id, String subjectId) {
        return new Builder(id, subjectId);
    }

    /**
     * Collects registrations without executing providers. IDs remain extension-wide.
     * The equatable oracle uses the contract ID in the registry's oracle namespace.
     * Use ID-only overloads to reference shared providers registered elsewhere.
     */
    public static final class Builder {
        private final String id;
        private final ExtensionContract.Builder contract;
        private final Map<String, String> expected = new LinkedHashMap<String, String>();
        private final List<Consumer<WorldlineExtensionRegistry>> registrations =
                new ArrayList<Consumer<WorldlineExtensionRegistry>>();
        private boolean hasFixture;

        private Builder(String id, String subjectId) {
            this.id = id;
            contract = ExtensionContract.builder(id, subjectId)
                    .mode(ExtensionMode.CUSTOM_CONTRACT).oracle(id);
        }

        public Builder fixture(String fixtureId) {
            if (hasFixture) throw new IllegalArgumentException("fixture already selected");
            contract.fixture(fixtureId);
            hasFixture = true;
            return this;
        }

        public Builder fixture(String fixtureId, ExtensionFixture fixture) {
            Objects.requireNonNull(fixture, "fixture");
            fixture(fixtureId);
            registrations.add(registry -> registry.fixture(fixtureId, fixture));
            return this;
        }

        public Builder action(String actionId) {
            contract.action(actionId);
            return this;
        }

        public Builder action(String actionId, ExtensionAction action) {
            Objects.requireNonNull(action, "action");
            action(actionId);
            registrations.add(registry -> registry.action(actionId, action));
            return this;
        }

        public Builder expect(String observationId, String value) {
            Objects.requireNonNull(value, "expected value");
            if (expected.containsKey(observationId))
                throw new IllegalArgumentException("duplicate observation " + observationId);
            contract.observation(observationId);
            expected.put(observationId, value);
            return this;
        }

        public Builder expect(String observationId, ExtensionObservation observation, String value) {
            Objects.requireNonNull(observation, "observation");
            expect(observationId, value);
            registrations.add(registry -> registry.observation(observationId, observation));
            return this;
        }

        /**
         * Validates the contract before contributing providers through the supplied registry.
         * Registry or discovery failures must abort extension loading, as with direct registration.
         */
        public void registerInto(WorldlineExtensionRegistry registry) {
            Objects.requireNonNull(registry, "registry");
            ExtensionContract value = contract.custom(ExtensionEvidence.signature(expected)).build();
            for (Consumer<WorldlineExtensionRegistry> registration : registrations)
                registration.accept(registry);
            registry.oracle(id, ExtensionOracles.equatable());
            registry.contract(value);
        }
    }
}
