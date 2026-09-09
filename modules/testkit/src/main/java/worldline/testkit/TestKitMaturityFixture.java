package worldline.testkit;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import worldline.testapi.ExternalConsumerUse;

/** Reusable release gate; dates and public artifact provenance cannot be replaced with repeated local runs. */
public final class TestKitMaturityFixture {
    private TestKitMaturityFixture() { }
    public static TestKitMaturityEvidence assess(List<ExternalConsumerUse> observations, LocalDate asOf,
            int minimumSpanDays) {
        require(observations != null && !observations.isEmpty() && asOf != null && minimumSpanDays >= 1,
                "missing maturity policy or observations");
        Map<String, ExternalConsumerUse> canonical = new TreeMap<>();
        Map<String, LocalDate> first = new TreeMap<>(), last = new TreeMap<>();
        Set<String> proofIds = new HashSet<>();
        for (ExternalConsumerUse use : observations) {
            require(use != null && !use.date().isAfter(asOf), "future or absent consumer use");
            require(proofIds.add(use.proof()) && canonical.put(use.canonical(), use) == null,
                    "reused consumer proof");
            if (!use.publicArtifactsOnly()) continue;
            String repo = use.repository(); LocalDate date = use.date();
            if (!first.containsKey(repo) || date.isBefore(first.get(repo))) first.put(repo, date);
            if (!last.containsKey(repo) || date.isAfter(last.get(repo))) last.put(repo, date);
        }
        require(first.size() >= 2, "two actual public-artifact consumers are required for a maturity baseline");
        List<String> sustained = new ArrayList<>();
        for (String repo : first.keySet()) {
            if (ChronoUnit.DAYS.between(first.get(repo), last.get(repo)) >= minimumSpanDays) sustained.add(repo);
        }
        StringBuilder out = new StringBuilder("v1|testkit-external-maturity|as-of=").append(asOf)
                .append("|minimum-span-days=").append(minimumSpanDays).append('\n');
        for (String row : canonical.keySet()) out.append(row).append('\n');
        out.append("public-consumers=").append(first.size()).append("\nsustained-consumers=")
                .append(sustained.size()).append("\nready-for-1.0=").append(sustained.size() >= 2).append('\n');
        return new TestKitMaturityEvidence(out.toString(), sustained, first.size());
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
