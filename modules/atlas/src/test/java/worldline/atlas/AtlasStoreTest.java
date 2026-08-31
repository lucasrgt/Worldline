package worldline.atlas;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
        require(AtlasSubsystems.DOMAINS.length == 6, "taxonomy domains");
        require("actors".equals(AtlasSubsystems.domain("tile-entities")),
                "tile entity taxonomy");
        require(first.kind(AtlasKind.COVERAGE_UNIT).size() == 175, "coverage units");
        require(first.kind(AtlasKind.BOUNDARY).size() >= 24, "boundary baseline");
        require(first.kind(AtlasKind.SCENARIO).size() == WorldlineBehavior.all().size() + 1,
                "behavior scenarios");
        require(first.kind(AtlasKind.CLAIM).size() == 1320, "functional census denominator");
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
        AtlasRecord completed = first.get("atlas.claim.block-054.tick-policy");
        require(AtlasStatus.VERIFIED.equals(completed.status()), "final census claim verified");
        AtlasRecord pigSpawn = first.get("atlas.claim.entity-090.spawn-materialization");
        require(AtlasStatus.VERIFIED.equals(pigSpawn.status())
                && pigSpawn.refs().contains("atlas.subsystem.entities")
                && pigSpawn.control().contains("automation=PUBLIC_TESTKIT"),
                "public entity lifecycle proof was not imported honestly");
        require(first.get("atlas.claim.entity-050.damage-death").control()
                .contains("layer=SINGULAR;applicability=APPLICABLE;automation=PUBLIC_TESTKIT"),
                "singular entity lifecycle route was not indexed");
        require(first.get("atlas.claim.entity-051.drop-matrix").control()
                .contains("layer=ARCHETYPE;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-094.spawn-materialization").control()
                .contains("layer=UNIVERSAL;applicability=APPLICABLE;automation=PUBLIC_TESTKIT"),
                "bounded entity archetype routes were not indexed");
        require(first.get("atlas.claim.entity-010.spawn-materialization").control()
                .contains("layer=UNIVERSAL;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-041.spawn-materialization").refs()
                .contains("atlas.experiment.m154-boat-spawn"),
                "Packet23 materialization routes were not indexed");
        require(first.get("atlas.claim.entity-009.spawn-materialization").control()
                .contains("layer=UNIVERSAL;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-009.interaction-state").control()
                .contains("layer=SINGULAR;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-009.environment-response").refs()
                .contains("atlas.experiment.m582-painting-support-break-set"),
                "painting subsystem routes were not indexed");
        require(first.get("atlas.claim.entity-055.spawn-materialization").control()
                .contains("layer=UNIVERSAL;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-055.movement-policy").control()
                .contains("layer=SINGULAR;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-055.interaction-state").refs()
                .contains("atlas.experiment.m412-slime-split-set")
                && first.get("atlas.claim.entity-055.drop-matrix").refs()
                .contains("atlas.experiment.m423-slimeball-set"),
                "slime subsystem routes were not indexed");
        require(first.get("atlas.claim.entity-040.movement-policy").control()
                .contains("layer=SINGULAR;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-041.movement-policy").refs()
                .contains("atlas.experiment.m504-m508-sw-entity-dynamics")
                && first.get("atlas.claim.entity-055.movement-policy").refs()
                .contains("atlas.experiment.m504-m508-sw-entity-dynamics")
                && first.get("atlas.claim.entity-056.movement-policy").control()
                .contains("layer=SINGULAR;applicability=APPLICABLE;automation=PUBLIC_TESTKIT"),
                "complete controlled entity dynamics matrix was not indexed");
        require(first.get("atlas.claim.entity-091.spawn-materialization").control()
                .contains("layer=UNIVERSAL;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-091.save-reload").refs()
                .contains("atlas.experiment.m506-sw-sheep-sheared-persistence")
                && first.get("atlas.claim.entity-091.damage-death").refs()
                .contains("atlas.experiment.m444-remaining-mob-drops-rest")
                && first.get("atlas.claim.entity-091.interaction-state").refs()
                .contains("atlas.experiment.m406-sheep-dye-set"),
                "complete sheep lifecycle routes were not indexed");
        require(first.get("atlas.claim.entity-093.spawn-materialization").control()
                .contains("layer=UNIVERSAL;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-093.spawn-materialization").refs()
                .contains("atlas.experiment.m407-chicken-egg-set")
                && AtlasStatus.UNKNOWN.equals(
                        first.get("atlas.claim.entity-093.tick-lifecycle").status()),
                "honest chicken egg-family boundary was not indexed");
        require(first.get("atlas.claim.entity-095.spawn-materialization").control()
                .contains("layer=UNIVERSAL;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-095.spawn-materialization").refs()
                .contains("atlas.experiment.m420-wolf-tame-set")
                && first.get("atlas.claim.entity-095.interaction-state").control()
                .contains("layer=SINGULAR;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-095.interaction-state").refs()
                .contains("atlas.experiment.m583-wolf-sit-set"),
                "complete wolf owner-state subsystem was not indexed");
        require(first.get("atlas.claim.entity-021.spawn-materialization").control()
                .contains("layer=UNIVERSAL;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-021.tick-lifecycle").control()
                .contains("layer=SINGULAR;applicability=APPLICABLE;automation=PUBLIC_TESTKIT")
                && first.get("atlas.claim.entity-021.tick-lifecycle").refs()
                .contains("atlas.experiment.m597-falling-sand-entity-set"),
                "complete falling sand lifecycle was not indexed");
        require(AtlasStatus.UNKNOWN.equals(first.get("atlas.claim.entity-092.drop-matrix").status()),
                "implicit entity census gap was not materialized");
        require(!AtlasIndex.search(first, "chunk", 20).isEmpty(), "semantic chunk index");
        String context = AtlasContextQuery.json(first, "chunk",
                AtlasContext.build(first, "chunk", 20, 1));
        require(context.contains("WORLDLINE-ATLAS-CONTEXT/1")
                && context.contains("certainty") && context.contains("GRAPH_DEPTH_1")
                && context.contains("\"domains\"") && context.contains("subsystem-chunks"),
                "agent context json");
        requireFacet(first, "domain-world");
        requireFacet(first, "layer-universal");
        requireFacet(first, "surface-public-testkit");
        require(AtlasIndex.search(first, "surface-public-testkit", 1000).size() == 1000,
                "public TestKit facet should reach the bounded query limit");
        AtlasTaxonomy.validate(first);
        String taxonomy = AtlasQuery.taxonomy(first);
        require(taxonomy.contains("WORLDLINE-ATLAS-TAXONOMY/1")
                && taxonomy.contains("domain=world")
                && taxonomy.contains("subsystem=tile-entities"), "taxonomy index");
        String tags = AtlasQuery.tags(first);
        require(tags.contains("tag=category-claim")
                && tags.contains("tag=surface-public-testkit\trecords=1100")
                && !tags.contains("tag=surface-internal-api")
                && tags.contains("tag=surface-smoke-only\trecords=8"), "tag index");
        try {
            String documentation = new String(Files.readAllBytes(Paths.get("docs", "ATLAS.md")),
                    StandardCharsets.UTF_8);
            require(documentation.contains(AtlasTaxonomy.markdown()), "taxonomy docs drift");
        } catch (java.io.IOException error) {
            throw new AssertionError(error);
        }
        require(AtlasGaps.list(first).stream().noneMatch(
                record -> record.id().startsWith("atlas.claim.block-")),
                "completed block Census regressed");
        require(AtlasGaps.list(first).stream().anyMatch(
                record -> record.id().startsWith("atlas.claim.entity-")),
                "entity Census gaps were hidden");
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

    private static void requireFacet(AtlasStore store, String facet) {
        java.util.List<AtlasHit> hits = AtlasIndex.search(store, facet, 1000);
        require(!hits.isEmpty(), facet + " index");
        for (AtlasHit hit : hits) {
            require(AtlasTaxonomy.tags(store, hit.record()).contains(facet),
                    facet + " leaked another facet");
        }
    }
}
