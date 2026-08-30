package worldline.atlas;

import java.nio.file.Paths;
import worldline.semantics.SemanticCatalog;
import worldline.semantics.SemanticRoles;
import worldline.api.WorldlineBehavior;

public final class AtlasStoreTest {
    private AtlasStoreTest() {}

    public static void main(String[] arguments) {
        AtlasStore first = AtlasStore.standard(Paths.get("."));
        AtlasStore second = AtlasStore.standard(Paths.get("."));
        require(first.kind(AtlasKind.ROLE).size() == SemanticRoles.roleCount(), "role count");
        require(first.kind(AtlasKind.ROLE).size() >= 196, "catalog baseline");
        require(first.kind(AtlasKind.INVARIANT).size() == 6, "invariants");
        require(first.kind(AtlasKind.SUBSYSTEM).size() == 25, "subsystems");
        require(first.kind(AtlasKind.COVERAGE_UNIT).size() == 175, "coverage units");
        require(first.kind(AtlasKind.BOUNDARY).size() >= 24, "boundary baseline");
        require(first.kind(AtlasKind.SCENARIO).size() == WorldlineBehavior.all().size() + 1,
                "behavior scenarios");
        require(first.kind(AtlasKind.CLAIM).size() == 1056, "functional census denominator");
        require(first.kind(AtlasKind.EXPERIMENT).size() >= 90, "experiments");
        require(first.kind(AtlasKind.HYPOTHESIS).size() >= 40, "hypotheses");
        require(first.kind(AtlasKind.FIELD).size() >= 20, "trace fields");
        require(first.kind(AtlasKind.MAPPING_SET).size() == 2, "mapping sets");
        require(first.kind(AtlasKind.API).size() == 2, "modding APIs");
        require(AtlasStatus.REJECTED.equals(
                first.get("atlas.hypothesis.aero-fixed-two-rebuild").status()), "M15 rejected");
        require(AtlasStatus.UNKNOWN.equals(
                first.get("atlas.hypothesis.aero-historical-spike").status()), "spike non-claim");
        require(AtlasStatus.REJECTED.equals(first.get("atlas.hypothesis.the-end").status()),
                "out of version");
        require("ENTITY_POS_Y".equals(first.get("atlas.field.y").subject()), "trace field y");
        String graph = AtlasGraph.render(first, "atlas.role.CLIENT_TICK_ROOT");
        require(graph.contains("READS atlas.boundary.INPUT")
                && graph.contains("DEPENDS_ON atlas.boundary.CLOCK"), "catalog graph");
        require(AtlasDelta.since(first, "M70").contains("atlas.experiment.m80-natural-membership-rebuild"),
                "changed since M70");
        require(first.canonical().startsWith(AtlasSchema.STORE), "export header");
        AtlasRecord tick = first.get("atlas.role.CLIENT_TICK_ROOT");
        require(AtlasStatus.STRONG.equals(tick.status()), "tick status");
        require(tick.subject().contains("runTick"), "tick subject");
        AtlasRecord invariant = first.get("atlas.invariant.item-conservation");
        require(AtlasStatus.VERIFIED.equals(invariant.status()), "item conservation");
        AtlasRecord experiment = first.get("atlas.experiment.m80-natural-membership-rebuild");
        require(AtlasStatus.OBSERVATIONAL.equals(experiment.status()), "m80 status");
        require(AtlasSchema.WORLDLINE.equals(experiment.artifact()), "m80 composed artifact");
        require(experiment.evidence().contains(
                "expected.signature=3df82b51703daacc031e1f745f86fc7af6678d2da74901eb6c00183915e8a77a"),
                "m80 signature");
        require(first.get("atlas.experiment.symbols-map.controlled-client-tick").evidence().size() == 1,
                "symbols.map hash");
        require(AtlasSchema.SERVER.equals(first.get("atlas.experiment.m469-void-death-set")
                .artifact()), "m469 server artifact");
        require(first.sha256().equals(second.sha256()), "store hash drifted");
        require(first.get(WorldlineBehavior.FLUID_FLOW.atlasId()).refs().contains(
                "atlas.experiment.b173-source-fluid-dynamics-cycle"), "behavior proof ref");
        AtlasRecord stoneRegistry = first.get("atlas.claim.block-001.registry-presence");
        require(AtlasStatus.VERIFIED.equals(stoneRegistry.status()), "wildcard census expansion");
        require(AtlasCertainty.VERIFIED.equals(AtlasCertainty.of(stoneRegistry)),
                "verified certainty");
        AtlasRecord unknown = first.get("atlas.claim.block-054.tick-policy");
        require(AtlasStatus.UNKNOWN.equals(unknown.status()), "implicit census unknown");
        require(!AtlasIndex.search(first, "chunk", 20).isEmpty(), "semantic chunk index");
        String context = AtlasContextQuery.json("chunk", AtlasContext.build(first, "chunk", 20, 1));
        require(context.contains("WORLDLINE-ATLAS-CONTEXT/1")
                && context.contains("certainty") && context.contains("GRAPH_DEPTH_1"),
                "agent context json");
        require(AtlasGaps.list(first).contains(unknown), "unknown Census claim gap");
        try { AtlasSynchronization.validateAll(Paths.get(".")); }
        catch (java.io.IOException error) { throw new AssertionError(error); }
        require(first.get("atlas.role.CLIENT_TICK_ROOT").canonical()
                .equals(AtlasRecord.parse(tick.canonical()).canonical()), "record round-trip");
        require(SemanticCatalog.standard().role("CLIENT_TICK_ROOT").known(), "catalog known");
        require(AtlasStatus.UNKNOWN.equals(first.get(
                "atlas.ecosystem-claim.mapping-completeness-ranking").status()),
                "community completeness claim stays unknown");
        System.out.println("AtlasStoreTest passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
