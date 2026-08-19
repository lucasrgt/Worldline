import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

/** Fails closed when release metadata or the public/legal boundary drifts. */
public final class ReleaseCheck {
    private final Path root = Paths.get("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        if (arguments.length != 0) {
            System.err.println("usage: java tools/harness/ReleaseCheck.java");
            System.exit(2);
        }
        try { new ReleaseCheck().execute(); }
        catch (Exception error) {
            System.err.println("release check failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Properties release = load("release/worldline.properties");
        Properties harness = load("harness.properties");
        Properties artifact = load("artifacts/minecraft-b1.7.3-client.properties");
        Properties serverArtifact = load("artifacts/minecraft-b1.7.3-server.properties");
        Properties toolchain = load("toolchains/retromcp.properties");
        Properties server = load("smokes/deterministic-world-tick/smoke.properties");
        Properties client = load("smokes/controlled-client-tick/smoke.properties");
        Properties m3 = load("smokes/m3-domain-api/smoke.properties");
        Properties m4 = load("smokes/m4-durable-snapshot/smoke.properties");
        Properties m5 = load("smokes/m5-reproduction-bundle/smoke.properties");
        Properties m6 = load("smokes/m6-trace-explorer/smoke.properties");
        Properties m7 = load("smokes/m7-mod-loading/smoke.properties");
        Properties m8 = load("smokes/m8-mod-version-diff/smoke.properties");
        Properties m9 = load("smokes/m9-scenario-minimization/smoke.properties");
        Properties m10 = load("smokes/m10-native-render/smoke.properties");
        Properties m11 = load("smokes/m11-aero-attribution/smoke.properties");
        Properties m12 = load("smokes/m12-aero-reproduction/smoke.properties");
        Properties m13 = load("smokes/m13-aero-differential/smoke.properties");
        Properties m14 = load("smokes/m14-chunk-backlog/smoke.properties");
        Properties m15 = load("smokes/m15-chunk-contract/smoke.properties");
        Properties m16 = load("smokes/m16-adaptive-chunks/smoke.properties");
        Properties m17 = load("smokes/m17-scheduler-hardening/smoke.properties");
        Properties m17Profile = load("adapters/aero-model-lib/opt-in/worldline-adaptive.properties");
        Properties m18 = load("smokes/m18-save-attribution/smoke.properties");
        Properties m19 = load("smokes/m19-forced-autosave/smoke.properties");
        Properties m20 = load("smokes/m20-server-bootstrap/smoke.properties");
        Properties m21 = load("smokes/m21-server-control/smoke.properties");
        Properties m22 = load("smokes/m22-multiplayer-wire/smoke.properties");
        Properties m23 = load("smokes/m23-player-persistence/smoke.properties");
        Properties m24 = load("smokes/m24-play-pose/smoke.properties");
        Properties m25 = load("smokes/m25-player-movement/smoke.properties"); Properties m26 = load("smokes/m26-native-multiplayer/smoke.properties");
        Properties m27 = load("smokes/m27-multiplayer-chat/smoke.properties"), m28 = load("smokes/m28-remote-chunk/smoke.properties"); Properties m29 = load("smokes/m29-remote-chunk-snapshot/smoke.properties"), m30 = load("smokes/m30-remote-world-cache/smoke.properties"), m31 = load("smokes/m31-incremental-world/smoke.properties"), m32 = load("smokes/m32-remote-terrain-render/smoke.properties"), m33 = load("smokes/m33-chunk-traversal/smoke.properties"), m34 = load("smokes/m34-pose-correction/smoke.properties"), m35 = load("smokes/m35-movement-outcome/smoke.properties"), m36 = load("smokes/m36-route-recovery/smoke.properties"), m37 = load("smokes/m37-route-policy/smoke.properties"), m38 = load("smokes/m38-explicit-fallback/smoke.properties"), m39 = load("smokes/m39-route-observation/smoke.properties"), m40 = load("smokes/m40-observer-control/smoke.properties"), m41 = load("smokes/m41-route-termination/smoke.properties"), m42 = load("smokes/m42-route-correlation/smoke.properties"), m43 = load("smokes/m43-correlated-batch/smoke.properties"), m44 = load("smokes/m44-batch-observation/smoke.properties"), m45 = load("smokes/m45-event-batch-stop/smoke.properties"), m46 = load("smokes/m46-batch-terminal/smoke.properties"), m47 = load("smokes/m47-batch-counts/smoke.properties"), m48 = load("smokes/m48-inventory-observation/smoke.properties"), m49 = load("smokes/m49-held-item-peer/smoke.properties"), m50 = load("smokes/m50-drop-held-item/smoke.properties"), m51 = load("smokes/m51-dropped-item-spawn/smoke.properties"), m52 = load("smokes/m52-item-collection/smoke.properties"), m53 = load("smokes/m53-held-block-placement/smoke.properties"), m54 = load("smokes/m54-chest-window/smoke.properties"), m55 = load("smokes/m55-accepted-personal-transaction/smoke.properties");
	        Properties m56 = load("smokes/m56-rejected-transaction-recovery/smoke.properties"), m57 = load("smokes/m57-personal-crafting/smoke.properties"), m58 = load("smokes/m58-window-lifecycle/smoke.properties"), m59 = load("smokes/m59-chest-transfer/smoke.properties"), m60 = load("smokes/m60-furnace-smelt/smoke.properties"), m61 = load("smokes/m61-furnace-output/smoke.properties"), m62 = load("smokes/m62-workbench-window/smoke.properties"), m63 = load("smokes/m63-workbench-prepare/smoke.properties"), m64 = load("smokes/m64-workbench-output/smoke.properties"), m65 = load("smokes/m65-peer-armor/smoke.properties"), m66 = load("smokes/m66-player-combat/smoke.properties"), m67 = load("smokes/m67-chest-retrieval/smoke.properties"), m68 = load("smokes/m68-aero-multiplayer-login/smoke.properties"), m69 = load("smokes/m69-peer-swing/smoke.properties"), m70 = load("smokes/m70-aero-combat-window/smoke.properties"), m71 = load("smokes/m71-paired-aero-window/smoke.properties"), m72 = load("smokes/m72-aero-server-content/smoke.properties"), m73 = load("smokes/m73-paired-aero-content/smoke.properties"), m74 = load("smokes/m74-complete-aero-census/smoke.properties"), m75 = load("smokes/m75-aero-density-ladder/smoke.properties"), m76 = load("smokes/m76-renderer-decomposition/smoke.properties"), m77 = load("smokes/m77-direct-stage-timing/smoke.properties"), m78 = load("smokes/m78-paged-stage-timing/smoke.properties"), m79 = load("smokes/m79-cold-page-rebuild/smoke.properties"), m80 = load("smokes/m80-natural-membership-rebuild/smoke.properties"), m81 = load("smokes/m81-natural-multipage-rebuild/smoke.properties"), m82 = load("smokes/m82-natural-wave-ladder/smoke.properties"), m83 = load("smokes/m83-page-topology-contrast/smoke.properties"), m84 = load("smokes/m84-four-page-topology-contrast/smoke.properties"), m85 = load("smokes/m85-natural-membership-recovery/smoke.properties"), m86 = load("smokes/m86-repeated-membership-recovery/smoke.properties"), m87 = load("smokes/m87-two-cell-membership-recovery/smoke.properties"), m88 = load("smokes/m88-reverse-two-cell-membership-recovery/smoke.properties"), m89 = load("smokes/m89-sibling-cell-membership-recovery/smoke.properties"), m90 = load("smokes/m90-larger-page-sibling-recovery/smoke.properties"), m91 = load("smokes/m91-larger-page-depletion-recovery/smoke.properties"), m92 = load("smokes/m92-third-member-depletion-recovery/smoke.properties"), m93 = load("smokes/m93-full-page-depletion-recovery/smoke.properties"), m94 = load("smokes/m94-default-ttl-page-recovery/smoke.properties"), m95 = load("smokes/m95-page-capacity-thrash/smoke.properties"), m96 = load("smokes/m96-page-capacity-two-thrash/smoke.properties"), m97 = load("smokes/m97-page-capacity-one-thrash/smoke.properties"), m98 = load("smokes/m98-zero-capacity-floor/smoke.properties"), m99 = load("smokes/m99-rebuild-budget-fallback/smoke.properties"), m100 = load("smokes/m100-rebuild-budget-one-fallback/smoke.properties"), m101 = load("smokes/m101-rebuild-budget-zero-direct/smoke.properties"), m102 = load("smokes/m102-unlimited-rebuild-sentinel/smoke.properties"), m103 = load("smokes/m103-pages-disabled-immediate-direct/smoke.properties"); Properties lab = load("smokes/lab-cycle/smoke.properties"); Properties gui = load("smokes/gui-tree/smoke.properties");
        match(release, "id", "worldline");
	        match(release, "version", "1.91.0"); match(release, "milestone", "m103-pages-disabled-immediate-direct");
        match(release, "status", "go");
        match(release, "scope", "local-research");
        match(release, "canonical.command", "java tools/harness/Verify.java --smoke");
        same(release, "java.release", harness, "java.release");
        same(release, "client.sha256", artifact, "expected.sha256");
        same(release, "server.sha256", serverArtifact, "expected.sha256");
        same(release, "retromcp.revision", toolchain, "revision");
        same(release, "server.signature", server, "expected.signature");
        same(release, "client.signature", client, "expected.signature");
        same(release, "client.state.signature", client, "expected.state.signature");
        same(release, "m2.signature", client, "expected.state.signature");
        same(release, "m3.signature", m3, "expected.signature");
        same(release, "m4.signature", m4, "expected.snapshot.sha256");
        same(release, "m5.signature", m5, "expected.bundle.sha256");
        same(release, "m6.signature", m6, "expected.divergence.sha256");
        same(release, "m7.signature", m7, "expected.signature");
        same(release, "m8.signature", m8, "expected.signature");
        same(release, "m9.signature", m9, "expected.signature");
        same(release, "m10.signature", m10, "expected.frame.sha256");
        same(release, "m10.aero.status", m10, "aero.status");
        same(release, "m11.signature", m11, "expected.signature");
        same(release, "m11.aero.revision", m11, "aero.revision");
        same(release, "m11.aero.version", m11, "aero.version");
        same(release, "m12.signature", m12, "expected.signature");
        same(release, "m12.aero.revision", m12, "aero.revision");
        same(release, "m12.seed", m12, "seed");
        same(release, "m13.signature", m13, "expected.signature");
        same(release, "m13.aero.revision", m13, "aero.revision");
        same(release, "m13.seed", m13, "seed");
        same(release, "m14.signature", m14, "expected.signature");
        same(release, "m14.aero.revision", m14, "aero.revision");
        same(release, "m14.seed", m14, "seed");
        same(release, "m15.signature", m15, "expected.signature");
        same(release, "m15.aero.revision", m15, "aero.revision");
        same(release, "m15.seed", m15, "seed");
        same(release, "m16.signature", m16, "expected.signature");
        same(release, "m16.aero.revision", m16, "aero.revision");
        same(release, "m16.seed", m16, "seed");
        same(release, "m17.signature", m17, "expected.signature");
        same(release, "m17.aero.revision", m17, "aero.revision");
        same(release, "m17.seed", m17, "seed");
        match(m17Profile, "default.enabled", "false");
        match(m17Profile, "shipping.status", "lab-only-no-go");
        same(release, "m18.signature", m18, "expected.signature");
        same(release, "m18.aero.revision", m18, "aero.revision");
        same(release, "m18.seed", m18, "seed");
        same(release, "m19.signature", m19, "expected.signature");
        same(release, "m19.aero.revision", m19, "aero.revision");
        same(release, "m19.seed", m19, "seed");
        same(release, "m20.signature", m20, "expected.signature");
        same(release, "server.sha256", m20, "server.jar.sha256");
        same(release, "m21.signature", m21, "expected.signature");
        same(release, "server.sha256", m21, "server.jar.sha256");
        same(release, "m22.signature", m22, "expected.signature");
        same(release, "server.sha256", m22, "server.jar.sha256");
        same(release, "m23.signature", m23, "expected.signature");
        same(release, "server.sha256", m23, "server.jar.sha256");
        same(release, "m24.signature", m24, "expected.signature");
        same(release, "server.sha256", m24, "server.jar.sha256");
        same(release, "m25.signature", m25, "expected.signature");
        same(release, "server.sha256", m25, "server.jar.sha256");
        same(release, "m26.signature", m26, "expected.signature");
        same(release, "server.sha256", m26, "server.jar.sha256");
        same(release, "client.sha256", m26, "client.jar.sha256");
        same(release, "m27.signature", m27, "expected.signature");
        same(release, "server.sha256", m27, "server.jar.sha256");
        same(release, "m28.signature", m28, "expected.signature");
        same(release, "server.sha256", m28, "server.jar.sha256");
        same(release, "m29.signature", m29, "expected.signature");
        same(release, "server.sha256", m29, "server.jar.sha256");
        same(release, "m30.signature", m30, "expected.signature"); same(release, "server.sha256", m30, "server.jar.sha256");
        same(release, "m31.signature", m31, "expected.signature"); same(release, "server.sha256", m31, "server.jar.sha256");
        same(release, "m32.signature", m32, "expected.signature"); same(release, "server.sha256", m32, "server.jar.sha256"); same(release, "client.sha256", m32, "client.jar.sha256");
        same(release, "m33.signature", m33, "expected.signature"); same(release, "server.sha256", m33, "server.jar.sha256"); same(release, "client.sha256", m33, "client.jar.sha256");
        same(release, "m34.signature", m34, "expected.signature"); same(release, "server.sha256", m34, "server.jar.sha256"); same(release, "m35.signature", m35, "expected.signature"); same(release, "server.sha256", m35, "server.jar.sha256"); same(release, "m36.signature", m36, "expected.signature"); same(release, "server.sha256", m36, "server.jar.sha256"); same(release, "m37.signature", m37, "expected.signature"); same(release, "server.sha256", m37, "server.jar.sha256"); same(release, "m38.signature", m38, "expected.signature"); same(release, "server.sha256", m38, "server.jar.sha256"); same(release, "m39.signature", m39, "expected.signature"); same(release, "server.sha256", m39, "server.jar.sha256"); same(release, "m40.signature", m40, "expected.signature"); same(release, "server.sha256", m40, "server.jar.sha256"); same(release, "m41.signature", m41, "expected.signature"); same(release, "server.sha256", m41, "server.jar.sha256"); same(release, "m42.signature", m42, "expected.signature"); same(release, "server.sha256", m42, "server.jar.sha256"); same(release, "m43.signature", m43, "expected.signature"); same(release, "server.sha256", m43, "server.jar.sha256"); same(release, "m44.signature", m44, "expected.signature"); same(release, "server.sha256", m44, "server.jar.sha256"); same(release, "m45.signature", m45, "expected.signature"); same(release, "server.sha256", m45, "server.jar.sha256"); same(release, "m46.signature", m46, "expected.signature"); same(release, "server.sha256", m46, "server.jar.sha256"); same(release, "m47.signature", m47, "expected.signature"); same(release, "server.sha256", m47, "server.jar.sha256"); same(release, "m48.signature", m48, "expected.signature"); same(release, "server.sha256", m48, "server.jar.sha256"); same(release, "m49.signature", m49, "expected.signature"); same(release, "server.sha256", m49, "server.jar.sha256"); same(release, "m50.signature", m50, "expected.signature"); same(release, "server.sha256", m50, "server.jar.sha256"); same(release, "m51.signature", m51, "expected.signature"); same(release, "server.sha256", m51, "server.jar.sha256"); same(release, "m52.signature", m52, "expected.signature"); same(release, "server.sha256", m52, "server.jar.sha256"); same(release, "m53.signature", m53, "expected.signature"); same(release, "server.sha256", m53, "server.jar.sha256"); same(release, "m54.signature", m54, "expected.signature"); same(release, "server.sha256", m54, "server.jar.sha256"); same(release, "m55.signature", m55, "expected.signature"); same(release, "server.sha256", m55, "server.jar.sha256");
	        same(release, "m56.signature", m56, "expected.signature"); same(release, "server.sha256", m56, "server.jar.sha256"); same(release, "m57.signature", m57, "expected.signature"); same(release, "server.sha256", m57, "server.jar.sha256"); same(release, "m58.signature", m58, "expected.signature"); same(release, "server.sha256", m58, "server.jar.sha256"); same(release, "m59.signature", m59, "expected.signature"); same(release, "server.sha256", m59, "server.jar.sha256"); same(release, "m60.signature", m60, "expected.signature"); same(release, "server.sha256", m60, "server.jar.sha256"); same(release, "m61.signature", m61, "expected.signature"); same(release, "server.sha256", m61, "server.jar.sha256"); same(release, "m62.signature", m62, "expected.signature"); same(release, "server.sha256", m62, "server.jar.sha256"); same(release, "m63.signature", m63, "expected.signature"); same(release, "server.sha256", m63, "server.jar.sha256"); same(release, "m64.signature", m64, "expected.signature"); same(release, "server.sha256", m64, "server.jar.sha256"); same(release, "m65.signature", m65, "expected.signature"); same(release, "server.sha256", m65, "server.jar.sha256"); same(release, "m66.signature", m66, "expected.signature"); same(release, "server.sha256", m66, "server.jar.sha256"); same(release, "m67.signature", m67, "expected.signature"); same(release, "server.sha256", m67, "server.jar.sha256"); same(release, "m68.signature", m68, "expected.signature"); same(release, "server.sha256", m68, "server.jar.sha256"); same(release, "m68.aero.revision", m68, "aero.revision"); same(release, "m69.signature", m69, "expected.signature"); same(release, "server.sha256", m69, "server.jar.sha256"); same(release, "m70.signature", m70, "expected.signature"); same(release, "server.sha256", m70, "server.jar.sha256"); same(release, "m70.aero.revision", m70, "aero.revision"); same(release, "m71.signature", m71, "expected.signature"); same(release, "server.sha256", m71, "server.jar.sha256"); same(release, "m71.aero.revision", m71, "aero.revision"); same(release, "m72.signature", m72, "expected.signature"); same(release, "m72.aero.revision", m72, "aero.revision"); same(release, "m73.signature", m73, "expected.signature"); same(release, "m73.aero.revision", m73, "aero.revision"); same(release, "m74.signature", m74, "expected.signature"); same(release, "m74.aero.revision", m74, "aero.revision"); same(release, "m75.signature", m75, "expected.signature"); same(release, "m75.aero.revision", m75, "aero.revision"); same(release, "m76.signature", m76, "expected.signature"); same(release, "m76.aero.revision", m76, "aero.revision"); same(release, "m77.signature", m77, "expected.signature"); same(release, "m77.aero.revision", m77, "aero.revision"); same(release, "m78.signature", m78, "expected.signature"); same(release, "m78.aero.revision", m78, "aero.revision"); same(release, "m79.signature", m79, "expected.signature"); same(release, "m79.aero.revision", m79, "aero.revision"); same(release, "m80.signature", m80, "expected.signature"); same(release, "m80.aero.revision", m80, "aero.revision"); same(release, "m81.signature", m81, "expected.signature"); same(release, "m81.aero.revision", m81, "aero.revision"); same(release, "m82.signature", m82, "expected.signature"); same(release, "m82.aero.revision", m82, "aero.revision"); same(release, "m83.signature", m83, "expected.signature"); same(release, "m83.aero.revision", m83, "aero.revision"); same(release, "m84.signature", m84, "expected.signature"); same(release, "m84.aero.revision", m84, "aero.revision"); same(release, "m85.signature", m85, "expected.signature"); same(release, "m85.aero.revision", m85, "aero.revision"); same(release, "m86.signature", m86, "expected.signature"); same(release, "m86.aero.revision", m86, "aero.revision"); same(release, "m87.signature", m87, "expected.signature"); same(release, "m87.aero.revision", m87, "aero.revision"); same(release, "m88.signature", m88, "expected.signature"); same(release, "m88.aero.revision", m88, "aero.revision"); same(release, "m89.signature", m89, "expected.signature"); same(release, "m89.aero.revision", m89, "aero.revision"); same(release, "m90.signature", m90, "expected.signature"); same(release, "m90.aero.revision", m90, "aero.revision"); same(release, "m91.signature", m91, "expected.signature"); same(release, "m91.aero.revision", m91, "aero.revision"); same(release, "m92.signature", m92, "expected.signature"); same(release, "m92.aero.revision", m92, "aero.revision"); same(release, "m93.signature", m93, "expected.signature"); same(release, "m93.aero.revision", m93, "aero.revision"); same(release, "m94.signature", m94, "expected.signature"); same(release, "m94.aero.revision", m94, "aero.revision"); same(release, "m95.signature", m95, "expected.signature"); same(release, "m95.aero.revision", m95, "aero.revision"); same(release, "m96.signature", m96, "expected.signature"); same(release, "m96.aero.revision", m96, "aero.revision"); same(release, "m97.signature", m97, "expected.signature"); same(release, "m97.aero.revision", m97, "aero.revision"); same(release, "m98.signature", m98, "expected.signature"); same(release, "m98.aero.revision", m98, "aero.revision"); same(release, "m99.signature", m99, "expected.signature"); same(release, "m99.aero.revision", m99, "aero.revision"); same(release, "m100.signature", m100, "expected.signature"); same(release, "m100.aero.revision", m100, "aero.revision"); same(release, "m101.signature", m101, "expected.signature"); same(release, "m101.aero.revision", m101, "aero.revision"); same(release, "m102.signature", m102, "expected.signature"); same(release, "m102.aero.revision", m102, "aero.revision"); same(release, "m103.signature", m103, "expected.signature"); same(release, "m103.aero.revision", m103, "aero.revision"); same(release, "lab.signature", lab, "expected.signature");
        same(release, "gui.signature", gui, "expected.signature");
        same(release, "invariants.signature", client, "expected.state.signature");
        match(release, "semantics.signature",
                "b4d1f4fdf968f785cc5c94b2400d5f4ad4966f8f7b042d0fd2372d24e9dadf88");
        requireText("modules/api/src/main/java/worldline/api/WorldlineVersion.java",
                "public static final String VERSION = \"" + value(release, "version") + "\";");
        requireText("docs/SEMANTICS_CYCLE.md", value(release, "semantics.signature"));
        for (String file : Arrays.asList("README.md", "CHANGELOG.md", "AGENTS.md",
                "docs/VISION.md", "docs/ROADMAP.md", "docs/ARCHITECTURE.md",
                "optimizations/TEMPLATE.properties",
                "docs/FIRST_CYCLE.md", "docs/M2_RUNTIME.md", "docs/M2_CYCLE.md",
                "docs/M3_API.md", "docs/M3_CYCLE.md",
                "docs/M4_SNAPSHOT.md", "docs/M4_CYCLE.md", "smokes/controlled-client-tick/MAP.md",
                "docs/M5_BUNDLE.md", "docs/M5_CYCLE.md", "smokes/m3-domain-api/MAP.md",
                "docs/M6_TRACE.md", "docs/M6_CYCLE.md", "smokes/m4-durable-snapshot/MAP.md",
                "smokes/m5-reproduction-bundle/MAP.md", "smokes/m6-trace-explorer/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M7_MODS.md", "docs/M7_CYCLE.md",
                "smokes/m7-mod-loading/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M8_RESULTS.md", "docs/M8_CYCLE.md",
                "smokes/m8-mod-version-diff/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M9_MINIMIZATION.md", "docs/M9_CYCLE.md",
                "smokes/m9-scenario-minimization/MAP.md", "docs/GUI_TREE.md", "docs/GUI_CYCLE.md",
                "smokes/gui-tree/MAP.md", "docs/INVARIANTS.md", "docs/INVARIANTS_CYCLE.md",
                "docs/SEMANTICS.md", "docs/SEMANTICS_CYCLE.md",
                "docs/OPTIMIZATION_SDK.md",
                "optimizations/catalog/README.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M10_RENDER.md", "docs/M10_CYCLE.md",
                "smokes/m10-native-render/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M11_ATTRIBUTION.md", "docs/M11_CYCLE.md",
                "smokes/m11-aero-attribution/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M12_CAPTURE.md", "docs/M12_CYCLE.md",
                "smokes/m12-aero-reproduction/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M13_DIFFERENTIAL.md", "docs/M13_CYCLE.md",
                "smokes/m13-aero-differential/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M14_CHUNK_BACKLOG.md", "docs/M14_CYCLE.md",
                "smokes/m14-chunk-backlog/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M15_CHUNK_CONTRACT.md", "docs/M15_CYCLE.md",
                "smokes/m15-chunk-contract/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M16_ADAPTIVE_CHUNKS.md", "docs/M16_CYCLE.md",
                "smokes/m16-adaptive-chunks/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M17_SCHEDULER_HARDENING.md", "docs/M17_CYCLE.md",
                "smokes/m17-scheduler-hardening/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M18_SAVE_ATTRIBUTION.md", "docs/M18_CYCLE.md",
                "smokes/m18-save-attribution/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M19_FORCED_AUTOSAVE.md", "docs/M19_CYCLE.md",
                "smokes/m19-forced-autosave/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("artifacts/minecraft-b1.7.3-server.properties",
                "docs/M20_SERVER_BOOTSTRAP.md", "docs/M20_CYCLE.md",
                "smokes/m20-server-bootstrap/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M21_SERVER_CONTROL.md", "docs/M21_CYCLE.md",
                "smokes/m21-server-control/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M22_MULTIPLAYER_WIRE.md", "docs/M22_CYCLE.md",
                "smokes/m22-multiplayer-wire/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M23_PLAYER_PERSISTENCE.md", "docs/M23_CYCLE.md",
                "smokes/m23-player-persistence/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M24_PLAY_POSE.md", "docs/M24_CYCLE.md",
                "smokes/m24-play-pose/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M25_PLAYER_MOVEMENT.md", "docs/M25_CYCLE.md",
                "smokes/m25-player-movement/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M26_NATIVE_MULTIPLAYER.md", "docs/M26_CYCLE.md",
                "smokes/m26-native-multiplayer/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M27_MULTIPLAYER_CHAT.md", "docs/M27_CYCLE.md",
                "smokes/m27-multiplayer-chat/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M28_REMOTE_CHUNK.md", "docs/M28_CYCLE.md",
                "smokes/m28-remote-chunk/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M29_REMOTE_CHUNK_SNAPSHOT.md", "docs/M29_CYCLE.md",
                "smokes/m29-remote-chunk-snapshot/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M30_REMOTE_WORLD_CACHE.md", "docs/M30_CYCLE.md", "smokes/m30-remote-world-cache/MAP.md"))
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        for (String file : Arrays.asList("docs/M31_INCREMENTAL_WORLD.md", "docs/M31_CYCLE.md", "smokes/m31-incremental-world/MAP.md")) if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
	        for (String file : Arrays.asList("docs/M32_REMOTE_TERRAIN_RENDER.md", "docs/M32_CYCLE.md", "smokes/m32-remote-terrain-render/MAP.md", "docs/M55_ACCEPTED_PERSONAL_TRANSACTION.md", "docs/M55_CYCLE.md", "smokes/m55-accepted-personal-transaction/MAP.md", "docs/M56_REJECTED_TRANSACTION_RECOVERY.md", "docs/M56_CYCLE.md", "smokes/m56-rejected-transaction-recovery/MAP.md", "docs/M57_PERSONAL_CRAFTING.md", "docs/M57_CYCLE.md", "smokes/m57-personal-crafting/MAP.md", "docs/M58_WINDOW_LIFECYCLE.md", "docs/M58_CYCLE.md", "smokes/m58-window-lifecycle/MAP.md", "docs/M59_CHEST_TRANSFER.md", "docs/M59_CYCLE.md", "smokes/m59-chest-transfer/MAP.md", "docs/M60_FURNACE_SMELT.md", "docs/M60_CYCLE.md", "smokes/m60-furnace-smelt/MAP.md", "docs/M61_FURNACE_OUTPUT.md", "docs/M61_CYCLE.md", "smokes/m61-furnace-output/MAP.md", "docs/M62_WORKBENCH_WINDOW.md", "docs/M62_CYCLE.md", "smokes/m62-workbench-window/MAP.md", "docs/M63_WORKBENCH_PREPARATION.md", "docs/M63_CYCLE.md", "smokes/m63-workbench-prepare/MAP.md", "docs/M64_WORKBENCH_OUTPUT.md", "docs/M64_CYCLE.md", "smokes/m64-workbench-output/MAP.md", "docs/M65_PEER_ARMOR.md", "docs/M65_CYCLE.md", "smokes/m65-peer-armor/MAP.md", "docs/M66_PLAYER_COMBAT.md", "docs/M66_CYCLE.md", "smokes/m66-player-combat/MAP.md", "docs/M67_CHEST_RETRIEVAL.md", "docs/M67_CYCLE.md", "smokes/m67-chest-retrieval/MAP.md", "docs/M68_AERO_MULTIPLAYER_LOGIN.md", "docs/M68_CYCLE.md", "smokes/m68-aero-multiplayer-login/MAP.md", "docs/M69_PEER_SWING.md", "docs/M69_CYCLE.md", "smokes/m69-peer-swing/MAP.md", "docs/M70_AERO_COMBAT_WINDOW.md", "docs/M70_CYCLE.md", "smokes/m70-aero-combat-window/MAP.md", "docs/M71_PAIRED_AERO_WINDOW.md", "docs/M71_CYCLE.md", "smokes/m71-paired-aero-window/MAP.md", "docs/M72_AERO_SERVER_CONTENT.md", "docs/M72_CYCLE.md", "smokes/m72-aero-server-content/MAP.md", "docs/M73_PAIRED_AERO_CONTENT.md", "docs/M73_CYCLE.md", "smokes/m73-paired-aero-content/MAP.md", "docs/M74_COMPLETE_AERO_CENSUS.md", "docs/M74_CYCLE.md", "smokes/m74-complete-aero-census/MAP.md", "docs/M75_AERO_DENSITY_LADDER.md", "docs/M75_CYCLE.md", "smokes/m75-aero-density-ladder/MAP.md", "docs/M76_RENDERER_DECOMPOSITION.md", "docs/M76_CYCLE.md", "smokes/m76-renderer-decomposition/MAP.md", "docs/M77_DIRECT_STAGE_TIMING.md", "docs/M77_CYCLE.md", "smokes/m77-direct-stage-timing/MAP.md", "docs/M78_PAGED_STAGE_TIMING.md", "docs/M78_CYCLE.md", "smokes/m78-paged-stage-timing/MAP.md", "docs/M79_COLD_PAGE_REBUILD.md", "docs/M79_CYCLE.md", "smokes/m79-cold-page-rebuild/MAP.md", "docs/M80_NATURAL_MEMBERSHIP_REBUILD.md", "docs/M80_CYCLE.md", "smokes/m80-natural-membership-rebuild/MAP.md", "docs/M81_NATURAL_MULTIPAGE_REBUILD.md", "docs/M81_CYCLE.md", "smokes/m81-natural-multipage-rebuild/MAP.md", "docs/M82_NATURAL_WAVE_LADDER.md", "docs/M82_CYCLE.md", "smokes/m82-natural-wave-ladder/MAP.md", "docs/M83_PAGE_TOPOLOGY_CONTRAST.md", "docs/M83_CYCLE.md", "smokes/m83-page-topology-contrast/MAP.md", "docs/M84_FOUR_PAGE_TOPOLOGY_CONTRAST.md", "docs/M84_CYCLE.md", "smokes/m84-four-page-topology-contrast/MAP.md", "docs/M85_NATURAL_MEMBERSHIP_RECOVERY.md", "docs/M85_CYCLE.md", "smokes/m85-natural-membership-recovery/MAP.md", "docs/M86_REPEATED_MEMBERSHIP_RECOVERY.md", "docs/M86_CYCLE.md", "smokes/m86-repeated-membership-recovery/MAP.md", "docs/M87_TWO_CELL_MEMBERSHIP_RECOVERY.md", "docs/M87_CYCLE.md", "smokes/m87-two-cell-membership-recovery/MAP.md", "docs/M88_REVERSE_TWO_CELL_RECOVERY.md", "docs/M88_CYCLE.md", "smokes/m88-reverse-two-cell-membership-recovery/MAP.md", "docs/M89_SIBLING_CELL_RECOVERY.md", "docs/M89_CYCLE.md", "smokes/m89-sibling-cell-membership-recovery/MAP.md", "docs/M90_LARGER_PAGE_SIBLING_RECOVERY.md", "docs/M90_CYCLE.md", "smokes/m90-larger-page-sibling-recovery/MAP.md", "docs/M91_LARGER_PAGE_DEPLETION_RECOVERY.md", "docs/M91_CYCLE.md", "smokes/m91-larger-page-depletion-recovery/MAP.md", "docs/M92_THIRD_MEMBER_DEPLETION_RECOVERY.md", "docs/M92_CYCLE.md", "smokes/m92-third-member-depletion-recovery/MAP.md", "docs/M93_FULL_PAGE_DEPLETION_RECOVERY.md", "docs/M93_CYCLE.md", "smokes/m93-full-page-depletion-recovery/MAP.md", "docs/M94_DEFAULT_TTL_PAGE_RECOVERY.md", "docs/M94_CYCLE.md", "smokes/m94-default-ttl-page-recovery/MAP.md", "docs/M95_PAGE_CAPACITY_THRASH.md", "docs/M95_CYCLE.md", "smokes/m95-page-capacity-thrash/MAP.md", "docs/M96_PAGE_CAPACITY_TWO_THRASH.md", "docs/M96_CYCLE.md", "smokes/m96-page-capacity-two-thrash/MAP.md", "docs/M97_PAGE_CAPACITY_ONE_THRASH.md", "docs/M97_CYCLE.md", "smokes/m97-page-capacity-one-thrash/MAP.md", "docs/M98_ZERO_CAPACITY_FLOOR.md", "docs/M98_CYCLE.md", "smokes/m98-zero-capacity-floor/MAP.md", "docs/M99_REBUILD_BUDGET_FALLBACK.md", "docs/M99_CYCLE.md", "smokes/m99-rebuild-budget-fallback/MAP.md", "docs/M100_REBUILD_BUDGET_ONE_FALLBACK.md", "docs/M100_CYCLE.md", "smokes/m100-rebuild-budget-one-fallback/MAP.md", "docs/M101_REBUILD_BUDGET_ZERO_DIRECT.md", "docs/M101_CYCLE.md", "smokes/m101-rebuild-budget-zero-direct/MAP.md", "docs/M102_UNLIMITED_REBUILD_SENTINEL.md", "docs/M102_CYCLE.md", "smokes/m102-unlimited-rebuild-sentinel/MAP.md", "docs/M103_PAGES_DISABLED_IMMEDIATE_DIRECT.md", "docs/M103_CYCLE.md", "smokes/m103-pages-disabled-immediate-direct/MAP.md")) if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        for (String file : Arrays.asList("docs/M33_CHUNK_TRAVERSAL.md", "docs/M33_CYCLE.md", "smokes/m33-chunk-traversal/MAP.md", "docs/M34_POSE_CORRECTION.md", "docs/M34_CYCLE.md", "smokes/m34-pose-correction/MAP.md", "docs/M35_MOVEMENT_OUTCOME.md", "docs/M35_CYCLE.md", "smokes/m35-movement-outcome/MAP.md", "docs/M36_ROUTE_RECOVERY.md", "docs/M36_CYCLE.md", "smokes/m36-route-recovery/MAP.md", "docs/M37_ROUTE_POLICY.md", "docs/M37_CYCLE.md", "smokes/m37-route-policy/MAP.md", "docs/M38_EXPLICIT_FALLBACK.md", "docs/M38_CYCLE.md", "smokes/m38-explicit-fallback/MAP.md", "docs/M39_ROUTE_OBSERVATION.md", "docs/M39_CYCLE.md", "smokes/m39-route-observation/MAP.md", "docs/M40_OBSERVER_CONTROL.md", "docs/M40_CYCLE.md", "smokes/m40-observer-control/MAP.md", "docs/M41_ROUTE_TERMINATION.md", "docs/M41_CYCLE.md", "smokes/m41-route-termination/MAP.md", "docs/M42_ROUTE_CORRELATION.md", "docs/M42_CYCLE.md", "smokes/m42-route-correlation/MAP.md", "docs/M43_CORRELATED_BATCH.md", "docs/M43_CYCLE.md", "smokes/m43-correlated-batch/MAP.md", "docs/M44_BATCH_OBSERVATION.md", "docs/M44_CYCLE.md", "smokes/m44-batch-observation/MAP.md", "docs/M45_EVENT_BATCH_STOP.md", "docs/M45_CYCLE.md", "smokes/m45-event-batch-stop/MAP.md", "docs/M46_BATCH_TERMINAL.md", "docs/M46_CYCLE.md", "smokes/m46-batch-terminal/MAP.md", "docs/M47_BATCH_COUNTS.md", "docs/M47_CYCLE.md", "smokes/m47-batch-counts/MAP.md", "docs/M48_INVENTORY_OBSERVATION.md", "docs/M48_CYCLE.md", "smokes/m48-inventory-observation/MAP.md", "docs/M49_HELD_ITEM_PEER.md", "docs/M49_CYCLE.md", "smokes/m49-held-item-peer/MAP.md", "docs/M50_DROP_HELD_ITEM.md", "docs/M50_CYCLE.md", "smokes/m50-drop-held-item/MAP.md", "docs/M51_DROPPED_ITEM_SPAWN.md", "docs/M51_CYCLE.md", "smokes/m51-dropped-item-spawn/MAP.md", "docs/M52_ITEM_COLLECTION.md", "docs/M52_CYCLE.md", "smokes/m52-item-collection/MAP.md", "docs/M53_HELD_BLOCK_PLACEMENT.md", "docs/M53_CYCLE.md", "smokes/m53-held-block-placement/MAP.md", "docs/M54_CHEST_WINDOW.md", "docs/M54_CYCLE.md", "smokes/m54-chest-window/MAP.md")) if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        verifyPublicTree();
	        System.out.println("  release: Worldline v1.91.0 M103 pages-disabled-immediate-direct GO"); System.out.println("  public artifact boundary: verified");
    }

    private void verifyPublicTree() throws IOException {
        Set<String> excluded = new HashSet<>(Arrays.asList(".git", ".worldline", "local"));
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                Path relative = root.relativize(path);
                if (relative.getNameCount() > 0 && excluded.contains(relative.getName(0).toString())) return;
                String name = path.getFileName().toString().toLowerCase();
                String normalized = relative.toString().replace('\\', '/').toLowerCase();
                if (name.endsWith(".jar") || name.endsWith(".class")
                        || normalized.contains("minecraft/src/")
                        || normalized.contains("minecraft/bin/")) {
                    throw new IllegalStateException("prohibited public artifact: " + relative);
                }
            });
        }
    }

    private Properties load(String relative) throws IOException {
        Properties result = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(root.resolve(relative), StandardCharsets.UTF_8)) {
            result.load(reader);
        }
        return result;
    }

    private void same(Properties left, String leftKey, Properties right, String rightKey) {
        match(left, leftKey, value(right, rightKey));
    }

    private void match(Properties source, String key, String expected) {
        String actual = value(source, key);
        if (!actual.equals(expected)) {
            throw new IllegalStateException(key + " is " + actual + "; expected " + expected);
        }
    }

    private String value(Properties source, String key) {
        String result = source.getProperty(key);
        if (result == null || result.trim().isEmpty()) throw new IllegalStateException("missing " + key);
        return result.trim();
    }

    private void requireText(String relative, String expected) throws IOException {
        String value = new String(Files.readAllBytes(root.resolve(relative)), StandardCharsets.UTF_8);
        if (!value.contains(expected)) throw new IllegalStateException(relative + " does not declare release version");
    }
}
