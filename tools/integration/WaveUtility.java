import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Measures delivered capability and census value independently of milestone count. */
final class WaveUtility {
    final int scopedCandidates;
    final int qualifiedPackages;
    final int capabilities;
    final int atoms;
    final int coverageClaims;

    private WaveUtility(int scopedCandidates, int qualifiedPackages, int capabilities,
            int atoms, int coverageClaims) {
        this.scopedCandidates = scopedCandidates;
        this.qualifiedPackages = qualifiedPackages;
        this.capabilities = capabilities;
        this.atoms = atoms;
        this.coverageClaims = coverageClaims;
    }

    static WaveUtility of(Path root, List<WaveCensus.Row> rows) throws Exception {
        int scoped = 0, qualified = 0;
        Set<String> capabilities = new HashSet<>();
        Set<String> atoms = new HashSet<>();
        Set<String> claims = new HashSet<>();
        for (WaveCensus.Row row : rows) {
            Path path = root.resolve("coordination/swarm/objectives")
                    .resolve(row.id() + ".properties");
            if (!Files.isRegularFile(path)) continue;
            scoped++;
            if (!row.qualified() || !row.integrated()) continue;
            MilestoneObjective objective = MilestoneObjective.loadReviewed(root, row.id());
            require(capabilities.add(objective.capability()),
                    "qualified wave split one capability across milestones");
            for (String atom : objective.atoms()) {
                require(atoms.add(atom), "qualified wave duplicated behavior atom: " + atom);
            }
            for (String claim : objective.claims()) {
                require(claims.add(claim), "qualified wave duplicated census claim: " + claim);
            }
            qualified++;
        }
        return new WaveUtility(scoped, qualified, capabilities.size(), atoms.size(), claims.size());
    }

    static WaveUtility previous(Path root, WaveCensus.Snapshot snapshot) throws Exception {
        return of(root, snapshot == null ? List.of() : snapshot.rows());
    }

    boolean improves(WaveUtility prior) {
        return coverageClaims > prior.coverageClaims;
    }

    boolean substantial() {
        return qualifiedPackages == 0
                || atoms >= qualifiedPackages * 3 && coverageClaims >= qualifiedPackages * 3;
    }

    String json() {
        return "{\"scoped_candidates\":" + scopedCandidates
                + ",\"qualified_packages\":" + qualifiedPackages
                + ",\"capabilities\":" + capabilities
                + ",\"behavior_atoms\":" + atoms
                + ",\"coverage_claims\":" + coverageClaims
                + ",\"atoms_per_package\":" + ratio(atoms, qualifiedPackages)
                + ",\"claims_per_package\":" + ratio(coverageClaims, qualifiedPackages) + "}";
    }

    String report(WaveUtility prior) {
        return "\n  \"utility\":" + json()
                + ",\n  \"utility_delta\":{\"coverage_claims\":"
                + (coverageClaims - prior.coverageClaims)
                + ",\"behavior_atoms\":" + (atoms - prior.atoms) + "},";
    }

    String summary() {
        return ", coverage-claims=" + coverageClaims;
    }

    private static String ratio(int value, int count) {
        return count == 0 ? "0" : WaveReportFormat.decimal((double) value / count);
    }

    static void selfTest() {
        WaveUtility empty = new WaveUtility(0, 0, 0, 0, 0);
        require(empty.substantial() && empty.json().contains("\"coverage_claims\":0"),
                "empty utility baseline drifted");
        require(!new WaveUtility(1, 1, 1, 1, 3).substantial(),
                "micromilestone utility was accepted");
        require(new WaveUtility(1, 1, 1, 3, 3).substantial(),
                "substantial capability package was rejected");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
