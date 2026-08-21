package worldline.atlas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Provenance-aware loader, API, namespace, and mapping-set knowledge. */
final class AtlasEcosystemImport {
    private static final String COMMUNITY = "community:calmilamsy:2026-08-21";

    private AtlasEcosystemImport() {}

    static List<AtlasRecord> load() {
        List<AtlasRecord> records = new ArrayList<AtlasRecord>();
        records.add(record("loader.fabric", AtlasKind.LOADER, AtlasStatus.STRONG,
                "Fabric Loader used by Babric and supported by Ornithe",
                "https://babric.github.io/develop/",
                "https://ornithemc.net/", "atlas.subsystem.mod-ecosystem"));
        records.add(record("api.stationapi", AtlasKind.API, AtlasStatus.STRONG,
                "General modding API for Fabric Loader on legacy Minecraft",
                "https://github.com/ModificationStation/StationAPI",
                "license:MIT", "atlas.subsystem.stationapi", "atlas.loader.fabric"));
        records.add(record("api.retroapi", AtlasKind.API, AtlasStatus.STRONG,
                "Cross-version legacy registration API with optional StationAPI integration",
                "https://github.com/matthewperiut/RetroAPI",
                "atlas.subsystem.mod-ecosystem", "atlas.loader.fabric"));
        records.add(record("mapping-set.biny", AtlasKind.MAPPING_SET, AtlasStatus.STRONG,
                "BINY mappings and BINY-Ornithe port for Beta 1.7.3",
                "https://github.com/calmilamsy/biny-mappings", "license:CC0-1.0",
                "coordinate:net.glasslauncher:biny-ornithe:b1.7.3+build.VERSION:mergedv2",
                "atlas.subsystem.mappings", "atlas.namespace.intermediary"));
        records.add(record("mapping-set.nostalgia", AtlasKind.MAPPING_SET,
                AtlasStatus.EXPERIMENTAL, "Nostalgia client and server mapping artifacts",
                "https://mvn.devos.one/releases/me/alphamode/nostalgia/",
                "https://github.com/Lenni0451/SourceGen",
                "license:UNKNOWN", "atlas.subsystem.mappings"));
        records.add(record("namespace.intermediary", AtlasKind.NAMESPACE, AtlasStatus.STRONG,
                "Stable intermediary namespace used between official and named mappings",
                "https://babric.github.io/develop/", "https://ornithemc.net/develop/",
                "atlas.subsystem.mappings"));
        records.add(record("namespace.feather", AtlasKind.NAMESPACE, AtlasStatus.STRONG,
                "Ornithe named mappings paired with Calamus intermediary mappings",
                "https://ornithemc.net/develop/", "atlas.subsystem.mappings"));
        records.add(claim("babric-mainstream", AtlasStatus.OBSERVATIONAL,
                "Babric is the current mainstream Beta 1.7.3 loader stack"));
        records.add(claim("babric-eventual-sunset", AtlasStatus.UNKNOWN,
                "StationAPI may eventually move from Babric to Ornithe"));
        records.add(claim("stationapi-ornithe-current-clash", AtlasStatus.OBSERVATIONAL,
                "Recent StationAPI versions reportedly clash with current Ornithe tooling"));
        records.add(claim("mapping-completeness-ranking", AtlasStatus.UNKNOWN,
                "BINY is reported fairly complete and Nostalgia most complete"));
        return Collections.unmodifiableList(records);
    }

    private static AtlasRecord claim(String id, String status, String subject) {
        return record("ecosystem-claim." + id, AtlasKind.ECOSYSTEM_CLAIM, status, subject,
                COMMUNITY, "atlas.subsystem.mod-ecosystem");
    }

    private static AtlasRecord record(String suffix, String kind, String status, String subject,
            String... tokens) {
        List<String> evidence = new ArrayList<String>();
        List<String> refs = new ArrayList<String>();
        for (String token : Arrays.asList(tokens)) {
            if (token.startsWith("atlas.")) refs.add(token); else evidence.add(token);
        }
        return AtlasRecord.of("atlas." + suffix, kind, status, AtlasSchema.WORLDLINE,
                AtlasSchema.SCOPE, subject, "", 0, evidence, refs);
    }
}
