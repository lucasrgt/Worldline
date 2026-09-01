package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/** Public TestKit boundary for the qualified Beta 1.7.3 fluid lifecycle families. */
final class FluidSemantics {
    private FluidSemantics() { }

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("fluid", "FLUID_FLOWING_LIFECYCLE_TESTKIT",
                        "worldline/testkit/FlowingFluidLifecycleFixture", "method", "execute",
                        "(Lworldline/testkit/FlowingFluidLifecycleScenario;)"
                                + "Lworldline/testkit/FlowingFluidLifecycleEvidence;",
                        "FLUID", "FLUID", "FLUID",
                        "b173-flowing-fluid-lifecycle-cycle", "", 9998),
                SemanticMapping.of("fluid", "FLUID_FROZEN_MATTER_TESTKIT",
                        "worldline/testkit/FluidFrozenMatterFixture", "method", "execute",
                        "(Lworldline/testkit/FluidFrozenMatterScenario;)"
                                + "Lworldline/testkit/FluidFrozenMatterEvidence;",
                        "FLUID", "FLUID", "FLUID",
                        "b173-fluid-frozen-matter-lifecycle-conformance-cycle", "", 9998),
                SemanticMapping.of("fluid", "FLUID_SOURCE_DYNAMICS_TESTKIT",
                        "worldline/testkit/FluidDynamicsFixture", "method", "execute",
                        "(Lworldline/testkit/FluidDynamicsScenario;"
                                + "Lworldline/api/BlockLifecycleDriver;)"
                                + "Lworldline/testkit/FluidDynamicsEvidence;",
                        "FLUID", "FLUID", "FLUID",
                        "b173-source-fluid-dynamics-cycle", "", 9998),
                SemanticMapping.of("fluid", "FLUID_SOURCE_PHYSICAL_ENVELOPE_TESTKIT",
                        "worldline/testkit/BlockStateDomainFamilyCycle", "method", "run",
                        "([Ljava/lang/String;Ljava/lang/String;JLjava/lang/String;"
                                + "Lworldline/test/TestRuntimeProvider;Ljava/util/List;)V",
                        "FLUID", "FLUID", "FLUID",
                        "b173-source-fluid-physical-envelope-cycle", "", 9998)));
    }
}
